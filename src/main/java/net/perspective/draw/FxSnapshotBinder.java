/**
 * FxSnapshotBinder.java
 * 
 * Created on 26 Aug 2026 09:02:57
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.perspective.draw.ItemStore.Snapshot;
import net.perspective.draw.geom.DrawItem;

/**
 *
 * @author claude
 */

/**
 * Keeps an {@link ObservableList} in step with an {@link ItemStore}.
 *
 * <h2>Why reconcile rather than replay</h2>
 * <p>An {@code ObservableList} may only be mutated on the FX Application Thread, while the store
 * publishes from whichever thread performed the write. Notifications therefore have to be posted,
 * and once they are posted a burst of publications must coalesce into a single pass or the FX
 * thread drowns. A coalesced pass cannot replay individual deltas — it has to be able to go from
 * whatever the list currently holds to whatever the newest snapshot holds. Hence reconciliation.</p>
 *
 * <h2>Why not setAll</h2>
 * <p>{@code list.setAll(snapshot items)} is one line and correct, and is the right answer if the
 * list only backs a {@code ListView} or similar. It is the wrong answer if each item maps to a
 * {@code Node} in a scene graph: it fires a single change that replaces every element, so every
 * node is discarded and rebuilt on every publication — including publications that changed one
 * item. This class diffs by {@link ItemId} instead and emits only the changes that occurred.</p>
 *
 * <h2>Identity</h2>
 * <p>Diffing works because ids are stable slot identities: {@code replaceItem} rebinds a slot's
 * existing id and {@code move} repositions without changing it. A replaced item is detected as a
 * <em>rebind</em> — same id, different instance — and refreshes just that entry, which is what
 * lets an inbound sync replacement update one node rather than the whole graph.</p>
 *
 * <h2>Threading</h2>
 * <p>{@link #attach()} and {@link #detach()} are called from the FX thread. Everything else is
 * internal. The store notifies from inside its lock, so the listener does nothing but schedule.</p>
 */

@Singleton
public class FxSnapshotBinder {

    private final ItemStore store;

    /** Z-order, mirrored for rendering. FX thread only. */
    private final ObservableList<ItemId> order = FXCollections.observableArrayList();

    /** Which instance each id is currently bound to, for rebind detection. FX thread only. */
    private final Map<ItemId, DrawItem> bound = new HashMap<>();

    /** Set when a pass is queued but has not yet started. */
    private final AtomicBoolean scheduled = new AtomicBoolean();

    /** Revision last applied to {@code order}. FX thread only. */
    private long appliedRevision = -1L;

    /** Notified per changed entry, for scene-graph maintenance. May be null. */
    private ItemListener itemListener;

    /**
     * Notified about individual entries as the list is reconciled, so a scene graph can be kept in
     * step without diffing the {@link ObservableList} a second time.
     */
    public interface ItemListener {

        /**
         * A new slot appeared.
         *
         * @param id    the slot id
         * @param item  the item
         * @param index its position in z-order
         */
        void added(ItemId id, DrawItem item, int index);

        /**
         * A slot's item was replaced — same id, different instance. The node for this id should be
         * rebuilt or refreshed; it should not be recreated from scratch if refresh is cheaper.
         *
         * @param id   the slot id
         * @param item the new item
         */
        void rebound(ItemId id, DrawItem item);

        /**
         * A slot was removed. Any node cached for this id should be discarded.
         *
         * @param id the slot id
         */
        void removed(ItemId id);

        /** Z-order changed; nodes should be restacked to match. */
        void reordered();
    }

    /**
     * @param store the store to mirror
     */
    @Inject
    public FxSnapshotBinder(ItemStore store) {
        this.store = store;
    }

    /**
     * The mirrored z-order. Read on the FX thread; do not mutate.
     *
     * @return the observable id list
     */
    public ObservableList<ItemId> getOrder() {
        return order;
    }

    /**
     * @param id the slot id
     * @return the item currently bound to that id, or null
     */
    public DrawItem get(ItemId id) {
        return bound.get(id);
    }

    /**
     * @param itemListener notified per changed entry, or null
     */
    public void setItemListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    /**
     * Begin mirroring. Call on the FX thread.
     */
    public void attach() {
        store.setListener(this::onPublished);
        schedule();                                  // pick up whatever is already there
    }

    /**
     * Stop mirroring. Call on the FX thread.
     */
    public void detach() {
        store.setListener(null);
    }

    /**
     * Called from the store, inside its lock, on whatever thread wrote. Does nothing but schedule.
     *
     * @param revision the revision published
     */
    private void onPublished(long revision) {
        schedule();
    }

    private void schedule() {
        if (scheduled.compareAndSet(false, true)) {
            Platform.runLater(this::reconcile);
        }
    }

    /**
     * Bring the mirrored list up to the newest snapshot. FX thread only.
     */
    private void reconcile() {
        /*
         * Clear the flag BEFORE reading the snapshot. A publication arriving during reconciliation
         * then schedules a further pass, rather than being swallowed because the flag was still
         * set while we worked.
         */
        scheduled.set(false);

        /*
         * Drain before reading the snapshot — see ItemStore.drainDirty(). An id taken here is
         * guaranteed to be at least as new in the snapshot read next; a write landing in the gap
         * is not drained, and its own publication has already rescheduled this pass.
         */
        Set<ItemId> dirty = store.drainDirty();

        Snapshot snap = store.snapshot();            // always the newest, never a queued one
        if (snap.revision() == appliedRevision && dirty.isEmpty()) {
            return;                                  // coalesced away
        }

        List<ItemId> target = snap.order();
        boolean structural = false;

        // 1. Removals, as one change event.
        Set<ItemId> live = new HashSet<>(target);
        List<ItemId> gone = new ArrayList<>();
        for (ItemId id : order) {
            if (!live.contains(id)) {
                gone.add(id);
            }
        }
        if (!gone.isEmpty()) {
            order.removeAll(gone);
            for (ItemId id : gone) {
                bound.remove(id);
                if (itemListener != null) {
                    itemListener.removed(id);
                }
            }
            structural = true;
        }

        // 2. Insertions and reordering, walking to match target position by position.
        for (int i = 0; i < target.size(); i++) {
            ItemId id = target.get(i);
            if (i < order.size() && order.get(i).equals(id)) {
                continue;
            }
            int at = order.indexOf(id);
            if (at == -1) {
                order.add(i, id);
                DrawItem item = snap.get(id);
                bound.put(id, item);
                if (itemListener != null) {
                    itemListener.added(id, item, i);
                }
            } else {
                order.remove(at);                    // moved
                order.add(i, id);
            }
            structural = true;
        }

        /*
         * 3. Refreshes.
         *
         * Two ways a slot's content can change without its position changing. The sync layer
         * supplies a replacement instance, which shows up as a different identity. The EDT edits
         * the published item in place and republishes it, which shows up as nothing at all —
         * dragging out a new figure does exactly this, mutating one instance from mouse-down to
         * mouse-up — so that case is driven by the store's dirty set rather than by comparison.
         */
        for (ItemId id : target) {
            DrawItem current = snap.get(id);
            if (current == null) {
                continue;
            }
            DrawItem previous = bound.get(id);
            if (previous == null) {
                bound.put(id, current);              // just added above; already rendered
                continue;
            }
            if (previous != current || dirty.contains(id)) {
                bound.put(id, current);
                if (itemListener != null) {
                    itemListener.rebound(id, current);
                }
            }
        }

        if (structural && itemListener != null) {
            itemListener.reordered();
        }

        appliedRevision = snap.revision();
    }

}
