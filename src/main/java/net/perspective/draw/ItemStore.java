/**
 * ItemStore.java
 * 
 * Created on 26 Aug 2026 08:35:57
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.perspective.draw.geom.DrawItem;

/**
 *
 * @author claude
 */

/**
 * Thread-safe store for canvas items.
 *
 * <p>Authoritative state ({@code items}, {@code itemsById}, {@code removed}) is guarded by
 * {@code lock} and is never exposed. Readers — principally the EDT paint path — see only the
 * immutable {@link Snapshot} published to the volatile {@code snapshot} field, and therefore
 * never take a lock. This matters: the EDT must not block on a mutation performed by the sync
 * thread, and a lock shared between the two would risk deadlock if any write path ever calls
 * back onto the EDT.</p>
 *
 * <h2>Threading contract</h2>
 * <ul>
 *   <li>All mutators may be called from any thread.</li>
 *   <li>{@link #snapshot()} is lock-free and may be called from any thread, including the EDT.</li>
 *   <li>A reader must take <em>one</em> snapshot per pass and use it for the whole pass. Calling
 *       {@code snapshot()} repeatedly within a single paint, hit-test or group operation yields a
 *       torn view.</li>
 * </ul>
 *
 * <h2>Identity</h2>
 * <p>{@link ItemId}s are minted by this class. {@link DrawItem} does not carry one and the
 * serialized format does not persist one — identity is a store concern, not part of the object
 * model. An id names a <em>slot</em> in the document, not an instance: {@link #replaceItem} keeps
 * the slot's existing id and rebinds it to the new instance, so an id stays valid when the sync
 * layer supplies a deserialized replacement. Selection, multi-selection and group membership are
 * therefore safe to hold as ids.</p>
 *
 * <p>Because ids are minted per session, they are meaningless to other peers and must not be sent
 * on the wire or written to a file. The sync layer continues to address items by index; resolve
 * ids to indices at that boundary only, via {@link #indexOf}, from a single snapshot.</p>
 *
 * <h2>Item mutability contract</h2>
 * <p>The snapshot freezes <em>which</em> items exist and in what order. It does not freeze the
 * items themselves. In-place mutation of a published {@link DrawItem} is permitted <strong>only
 * from the EDT</strong>, because the paint pass is also an EDT task and the two cannot interleave.
 * The sync thread must never mutate an item in place; it supplies freshly deserialized
 * replacement instances via {@link #replaceItem}. If painting is ever moved off the EDT, or the
 * sync thread ever begins applying field-level updates to existing items, this contract breaks
 * and items must become effectively immutable.</p>
 *
 * <p>Note that background serialization reads items off the EDT and is therefore <em>not</em>
 * covered by that contract — see {@link #saveState()}.</p>
 */

@Singleton
public class ItemStore {

    /** Creates a new instance of <code>ItemStore</code> */
    @Inject
    public ItemStore() {
    }

    /**
     * An immutable point-in-time view of the store.
     *
     * @param order    item ids in z-order
     * @param byId     item lookup
     * @param revision monotonically increasing publication counter
     */
    public record Snapshot(List<ItemId> order, Map<ItemId, DrawItem> byId, long revision) {

        /**
         * @param i index into z-order
         * @return the item at that index
         */
        public DrawItem at(int i) {
            return byId.get(order.get(i));
        }

        /**
         * @param id item id
         * @return the item, or null if not present
         */
        public DrawItem get(ItemId id) {
            // Null-safe for the same reason as indexOf: the map is immutable and get(null) throws.
            return id == null ? null : byId.get(id);
        }

        /**
         * @param id item id
         * @return index in z-order, or -1 if not present
         */
        public int indexOf(ItemId id) {
            /*
             * Null-safe: the order list is immutable, and ImmutableCollections.indexOf throws
             * on a null argument. A null id means "nothing selected", which is -1, not a fault.
             */
            return id == null ? -1 : order.indexOf(id);
        }

        /** @return number of items */
        public int size() {
            return order.size();
        }

        /** @return true if there are no items */
        public boolean isEmpty() {
            return order.isEmpty();
        }
    }

    /**
     * A point-in-time capture of the document for serialization.
     *
     * @param items    the items in z-order
     * @param revision the store revision this was taken at
     */
    public record SaveState(List<DrawItem> items, long revision) {
    }

    private static final Logger logger = LoggerFactory.getLogger(ItemStore.class.getName());

    private final ReentrantLock lock = new ReentrantLock();

    /** Z-ordered item ids. Guarded by {@code lock}. */
    private final List<ItemId> items = new ArrayList<>();

    /** Item lookup. Guarded by {@code lock}. */
    private final Map<ItemId, DrawItem> itemsById = new HashMap<>();

    /** Tombstones awaiting commit by {@link #removeItems()}. Guarded by {@code lock}. */
    private final Set<ItemId> removed = new LinkedHashSet<>();

    /**
     * Slots written since the last {@link #drainDirty()}. Guarded by {@code lock}.
     *
     * <p>Exists because an in-place edit is undetectable by a reader. The EDT is permitted to
     * mutate a published item and republish the same instance, which no comparison — {@code ==}
     * or {@code equals} — can distinguish from no change at all. So the writer records it.</p>
     *
     * <p>Accumulates rather than being cleared per publication: a reader that coalesces several
     * publications into one pass must still learn about every slot touched across them.</p>
     */
    private final Set<ItemId> dirty = new LinkedHashSet<>();

    private long revision;
    private int batchDepth;

    /** The published view. Volatile: written under lock, read without. */
    private volatile Snapshot snapshot = new Snapshot(List.of(), Map.of(), 0L);

    /**
     * Notified after each publication.
     *
     * <p>Called while the store lock is held, so an implementation must not block, must not call
     * back into the store, and must not do work — it should post to a UI thread and return. The
     * revision identifies the publication; the listener is expected to read
     * {@link #snapshot()} itself when it runs, so that coalesced notifications converge on the
     * latest state rather than replaying intermediate ones.</p>
     */
    @FunctionalInterface
    public interface SnapshotListener {

        /**
         * @param revision the revision just published
         */
        void published(long revision);
    }

    /** Notified after each publication. May be null. */
    private volatile SnapshotListener listener;

    /**
     * Set the listener notified after each publication.
     *
     * <p>This is the only notification the store emits — see {@link FxSnapshotBinder}, which
     * registers here and marshals onto the FX thread. Nothing renders if it is left unset.</p>
     *
     * @param listener the listener, or null to disable
     */
    public void setListener(SnapshotListener listener) {
        this.listener = listener;
    }

    /**
     * Lock-free. Safe to call from the EDT.
     *
     * @return the current published view
     */
    public Snapshot snapshot() {
        return snapshot;
    }

    /**
     * Take the set of slots written since the last call, and clear it.
     *
     * <p>Identifies items that changed without changing instance — an EDT edit applied in place
     * and republished. A reader cannot detect those itself, so the store records them here.</p>
     *
     * <p><strong>Drain before reading {@link #snapshot()}, never after.</strong> Draining first
     * means every id returned is guaranteed to be at least as new in that snapshot; a write
     * landing in the gap is simply not drained, and the publication it performed has already
     * scheduled the reader again, so it is picked up on the next pass. Draining second can strand
     * a write: its id is taken while its state is missing from the older snapshot already read,
     * and nothing schedules another pass.</p>
     *
     * <p>Single-consumer: whoever drains takes sole responsibility for applying the result.</p>
     *
     * @return the dirty slot ids, in write order
     */
    public Set<ItemId> drainDirty() {
        lock.lock();
        try {
            if (dirty.isEmpty()) {
                return Set.of();
            }
            Set<ItemId> out = new LinkedHashSet<>(dirty);
            dirty.clear();
            return out;
        } finally {
            lock.unlock();
        }
    }

    // -----------------------------------------------------------------------
    // Read accessors
    //
    // Each of these takes exactly one snapshot internally, so a single call is
    // always self-consistent. For a multi-step pass — hit-testing, grouping,
    // painting, anything that walks the z-order — call snapshot() once and work
    // against that instead, or successive calls here may see different states.
    // -----------------------------------------------------------------------

    /**
     * Z-ordered item ids from the current snapshot.
     *
     * <p>The returned list is immutable and safe to hand out; it is a published copy, not the
     * lock-guarded field.</p>
     *
     * @return the item ids in z-order
     */
    public List<ItemId> getItems() {
        return snapshot.order();
    }

    /**
     * Items in z-order from the current snapshot.
     *
     * @return the items in z-order
     */
    public List<DrawItem> getDrawItems() {
        Snapshot snap = snapshot;
        List<DrawItem> out = new ArrayList<>(snap.size());
        for (ItemId id : snap.order()) {
            DrawItem item = snap.byId().get(id);
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    /** @return the number of items in the current snapshot */
    public int size() {
        return snapshot.size();
    }

    /** @return true if the document is empty */
    public boolean isEmpty() {
        return snapshot.isEmpty();
    }

    /**
     * Resolve an index against the current snapshot.
     *
     * <p>Returns null rather than throwing when the index is out of range: an index can go stale
     * legitimately if the sync thread removes an item between selection and resolution, and that
     * is not an invariant violation. Callers on the EDT must null-check.</p>
     *
     * @param index item index
     * @return the {@link DrawItem}, or null
     */
    public DrawItem lookupId(int index) {
        Snapshot snap = snapshot;
        if (index < 0 || index >= snap.size()) {
            logger.trace("Lookup: Index out of range: {}", index);
            return null;
        }
        return snap.at(index);
    }

    /**
     * Resolve an index against a snapshot order the caller already holds.
     *
     * <p>Use inside a single pass, where {@code theseItems} came from one {@link #getItems()} or
     * {@link Snapshot#order()} call, so the index and the list agree.</p>
     *
     * @param theseItems z-ordered ids
     * @param index      item index
     * @return the {@link DrawItem}, or null
     */
    public DrawItem lookupId(List<ItemId> theseItems, int index) {
        if (index < 0 || index >= theseItems.size()) {
            logger.trace("Lookup: Index out of range: {}", index);
            return null;
        }
        return lookupId(theseItems.get(index));
    }

    /**
     * Resolve an id against the current snapshot.
     *
     * @param id item id
     * @return the {@link DrawItem}, or null
     */
    public DrawItem lookupId(ItemId id) {
        if (id == null) {
            return null;
        }
        return snapshot.byId().get(id);
    }

    /**
     * Resolve a collection of ids to items, skipping any no longer present.
     *
     * <p>Single snapshot, so the result is internally consistent. Use for grouping and
     * multi-selection.</p>
     *
     * @param ids item ids
     * @return the items still present, in the order given
     */
    public List<DrawItem> lookupAll(Collection<ItemId> ids) {
        Snapshot snap = snapshot;
        List<DrawItem> out = new ArrayList<>(ids.size());
        for (ItemId id : ids) {
            DrawItem item = snap.byId().get(id);
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }

    /**
     * Position of an id in the current z-order.
     *
     * <p>Resolve ids to indices at the sync boundary only — an index is valid solely for the
     * snapshot it came from.</p>
     *
     * @param id item id
     * @return the index, or -1 if not present
     */
    public int indexOf(ItemId id) {
        return snapshot.indexOf(id);
    }

        // -----------------------------------------------------------------------
    // Mutators
    // -----------------------------------------------------------------------

    /**
     * Mint the next id. Caller must hold {@code lock}.
     *
     * @return a fresh id
     */
    private ItemId mint() {
        return ItemId.freshId();
    }

    /**
     * Append item to the end of the z-order.
     *
     * <p>Returns the slot id rather than the index it landed at: the index is only true until the
     * next mutation, whereas the id keeps naming this item.</p>
     *
     * @param item the {@link DrawItem}
     * @return the id of the slot the item was placed in
     */
    public ItemId append(DrawItem item) {
        lock.lock();
        try {
            ItemId id = mint();
            itemsById.put(id, item);
            items.add(id);
            logger.debug("Create: Appended item at Index: {}", items.size() - 1);
            publish();
            return id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add item at index.
     *
     * <p>The map entry is written before the list entry so that a concurrent reader never
     * observes an id with no backing item.</p>
     *
     * @param index item index
     * @param item  the {@link DrawItem}
     * @return the id minted for the new slot
     */
    public ItemId addItem(int index, DrawItem item) {
        lock.lock();
        try {
            ItemId id = mint();
            itemsById.put(id, item);
            if (index >= 0 && index < items.size()) {
                items.add(index, id);
                logger.debug("Create: Added item at Index: {}", index);
            } else {
                items.add(id);
                logger.info("Create: Added item.");
            }
            publish();
            return id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replace the item at index, preserving the slot's identity.
     *
     * <p>The existing {@link ItemId} is kept and rebound to the new instance. This is what allows
     * a selection or group membership held as an id to survive the sync layer dropping in a
     * deserialized replacement.</p>
     *
     * <p>Republishes even when the instance is unchanged, so that in-place EDT edits become
     * visible to snapshot readers and bump the revision. Every EDT mutation must come through
     * here — one that does not will be invisible to {@link #saveState()}'s revision check.</p>
     *
     * @param index item index
     * @param item  the {@link DrawItem}
     * @return the slot's id, or null if the index was out of range and the item was appended
     */
    public ItemId replaceItem(int index, DrawItem item) {
        lock.lock();
        try {
            ItemId id;
            if (index >= 0 && index < items.size()) {
                id = items.get(index);          // keep the slot's identity
                itemsById.put(id, item);        // rebind to the new instance
                logger.debug("Update: Replaced item at Index: {}", index);
            } else {
                id = mint();
                itemsById.put(id, item);
                items.add(id);
                logger.info("Update: Replaced item.");
            }
            dirty.add(id);
            publish();
            return id;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replace the item bound to an id.
     *
     * @param id   the slot id
     * @param item the {@link DrawItem}
     * @return true if the id was present and the item was rebound
     */
    public boolean replaceItem(ItemId id, DrawItem item) {
        lock.lock();
        try {
            if (!itemsById.containsKey(id)) {
                logger.trace("Update: Unknown id: {}", id);
                return false;
            }
            itemsById.put(id, item);
            dirty.add(id);
            publish();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Republish a slot whose item was edited in place.
     *
     * <p>The instance is unchanged; this exists so that an EDT edit made directly on a published
     * item still reaches readers. It marks the slot dirty exactly as {@link #replaceItem} does,
     * which is what a reader needs — an unchanged instance is indistinguishable from no change at
     * all by {@code ==} or {@code equals}, so without the dirty mark the edit is filtered out and
     * the node keeps its old geometry. It also bumps the revision, without which the change is
     * invisible to {@link #saveState()}'s check and the document can be thought unmodified when it
     * is not.</p>
     *
     * <p>In-place editing is legal only from the EDT — see the mutability contract on this class.
     * The sync thread must go through {@link #replaceItem} with a fresh instance.</p>
     *
     * @param id the slot id
     * @return true if the id was present
     */
    public boolean touch(ItemId id) {
        lock.lock();
        try {
            if (!itemsById.containsKey(id)) {
                logger.trace("Touch: Unknown id: {}", id);
                return false;
            }
            dirty.add(id);
            publish();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tombstone the item at index. The item remains visible until {@link #removeItems()} commits
     * the deletion; this is the soft-delete path used by undo.
     *
     * @param index item index
     * @return the {@link DrawItem}, or null if index is out of bounds
     */
    public DrawItem deleteItem(int index) {
        lock.lock();
        try {
            if (index < 0 || index >= items.size()) {
                logger.warn("Delete: Index out of bounds: {}", index);
                return null;
            }
            ItemId id = items.get(index);
            removed.add(id);
            logger.debug("Delete: Removed item at Index: {}", index);
            return itemsById.get(id);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Commit all tombstoned deletions.
     *
     * <p>{@code removeAll} performs a single pass rather than one removal per element.</p>
     */
    public void removeItems() {
        lock.lock();
        try {
            if (removed.isEmpty()) {
                return;
            }
            List<ItemId> batch = List.copyOf(removed);
            items.removeAll(batch);
            batch.forEach(itemsById::remove);
            removed.clear();
            publish();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the item at index immediately, bypassing the tombstone.
     *
     * @param index item index
     * @return the removed {@link DrawItem}, or null if out of bounds
     */
    public DrawItem removeAt(int index) {
        lock.lock();
        try {
            if (index < 0 || index >= items.size()) {
                logger.warn("Delete: Index out of bounds: {}", index);
                return null;
            }
            ItemId id = items.remove(index);
            DrawItem gone = itemsById.remove(id);
            removed.remove(id);
            logger.debug("Delete: Removed item at Index: {}", index);
            publish();
            return gone;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the item bound to an id immediately.
     *
     * @param id the slot id
     * @return the removed {@link DrawItem}, or null if the id was not present
     */
    public DrawItem removeById(ItemId id) {
        lock.lock();
        try {
            if (!items.remove(id)) {
                logger.trace("Delete: Unknown id: {}", id);
                return null;
            }
            DrawItem gone = itemsById.remove(id);
            removed.remove(id);
            publish();
            return gone;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Move the item at {@code from} to position {@code to} in the z-order.
     *
     * <p>A single atomic reposition, not a remove followed by an add: the slot keeps its
     * {@link ItemId}, so a selection or group membership held as an id survives the move. Doing
     * this as add-then-remove would mint a new id and silently drop the item out of any group
     * holding the old one.</p>
     *
     * <p>{@code to} is the index the item ends up at in the resulting list, and is clamped to
     * range. Only one snapshot is published, so readers never observe the item duplicated or
     * missing.</p>
     *
     * @param from current index
     * @param to   destination index
     * @return true if the item moved
     */
    public boolean move(int from, int to) {
        lock.lock();
        try {
            if (from < 0 || from >= items.size()) {
                logger.warn("Move: Index out of bounds: {}", from);
                return false;
            }
            int target = Math.max(0, Math.min(to, items.size() - 1));
            if (target == from) {
                return false;
            }
            ItemId id = items.remove(from);
            items.add(target, id);
            logger.debug("Move: {} to {1}", new Object[] {from, target});
            publish();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Move the item bound to an id to position {@code to} in the z-order.
     *
     * @param id the slot id
     * @param to destination index
     * @return true if the item moved
     */
    public boolean moveById(ItemId id, int to) {
        lock.lock();
        try {
            int from = items.indexOf(id);
            if (from == -1) {
                logger.trace("Move: Unknown id: {}", id);
                return false;
            }
            int target = Math.max(0, Math.min(to, items.size() - 1));
            if (target == from) {
                return false;
            }
            items.remove(from);
            items.add(target, id);
            publish();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Insert items at an index, in order, as one publication.
     *
     * @param index  insertion point; out-of-range appends
     * @param toAdd  the items
     * @return the ids minted, in the order given
     */
    public List<ItemId> insertAll(int index, List<DrawItem> toAdd) {
        lock.lock();
        try {
            List<ItemId> ids = new ArrayList<>(toAdd.size());
            batchDepth++;
            try {
                int at = (index >= 0 && index <= items.size()) ? index : items.size();
                for (DrawItem item : toAdd) {
                    ItemId id = mint();
                    itemsById.put(id, item);
                    items.add(at++, id);
                    ids.add(id);
                }
            } finally {
                batchDepth--;
            }
            publish();
            return ids;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove several items by id as one publication.
     *
     * @param ids the slot ids
     * @return the number removed
     */
    public int removeAllById(Collection<ItemId> ids) {
        lock.lock();
        try {
            int count = 0;
            batchDepth++;
            try {
                for (ItemId id : ids) {
                    if (items.remove(id)) {
                        itemsById.remove(id);
                        removed.remove(id);
                        count++;
                    }
                }
            } finally {
                batchDepth--;
            }
            logger.debug("Delete: Removed {} items.", count);
            publish();
            return count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Discard all items. Ids are not reused.
     */
    public void clear() {
        lock.lock();
        try {
            items.clear();
            itemsById.clear();
            removed.clear();
            dirty.clear();                  // the slots are gone; nothing to refresh
            publish();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Apply several mutations as one unit, publishing a single snapshot at the end.
     *
     * <p>Use this for inbound sync deltas and for streaming loads: it amortises the O(n) snapshot
     * rebuild across the whole batch and prevents readers from observing a half-applied delta.
     * Nesting is supported; only the outermost call publishes.</p>
     *
     * <p>The lock is held for the duration, so the work must not block or call back onto the
     * EDT.</p>
     *
     * @param work the mutations to apply
     */
    public void batch(Runnable work) {
        lock.lock();
        try {
            batchDepth++;
            try {
                work.run();
            } finally {
                batchDepth--;
            }
            publish();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Rebuild and publish the snapshot. Caller must hold {@code lock}.
     *
     * <p>The listener is notified with the lock still held and on the writing thread, so it must
     * not block or touch the UI — it schedules and returns. See
     * {@link FxSnapshotBinder#attach}.</p>
     */
    private void publish() {
        if (batchDepth > 0) {
            return;
        }
        revision++;
        snapshot = new Snapshot(List.copyOf(items), Map.copyOf(itemsById), revision);
        SnapshotListener notify = listener;
        if (notify != null) {
            notify.published(revision);
        }
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    /** @return the current publication revision */
    public long revision() {
        return snapshot.revision();
    }

    /**
     * Capture the document together with the revision it was taken at, for a background writer.
     *
     * <p>The capture fixes the set and ordering of items, but not the items themselves: the EDT
     * may mutate one in place while the writer is reading its fields, producing an item that
     * serializes half-old and half-new. Compare {@link #revision()} after the write to detect
     * this:</p>
     *
     * <pre>{@code
     * SaveState state = store.saveState();
     * byte[] bytes = serialize(state.items());
     * if (store.revision() != state.revision()) {
     *     // document changed mid-write; bytes may contain a torn item — re-capture
     * } else {
     *     writeToDisk(bytes);
     * }
     * }</pre>
     *
     * <p>This detects rather than prevents, so bound the retries — a few attempts, then fall back
     * to capturing on the EDT via {@code invokeAndWait} and writing those bytes off-thread. The
     * check is only sound if every EDT mutation republishes, which means going through
     * {@link #replaceItem}; a mutation that does not republish will not move the revision and
     * will not be detected.</p>
     *
     * @return the capture
     */
    public SaveState saveState() {
        Snapshot snap = snapshot;
        List<DrawItem> out = new ArrayList<>(snap.size());
        for (ItemId id : snap.order()) {
            DrawItem item = snap.byId().get(id);
            if (item != null) {
                out.add(item);
            }
        }
        return new SaveState(out, snap.revision());
    }

    /**
     * Replace the entire document.
     *
     * <p>Clear and load happen under one lock hold and publish a single snapshot, so readers never
     * see an empty or half-populated canvas, and the O(n) rebuild happens once rather than once
     * per item. Loading n items through repeated {@link #append(DrawItem)} instead is O(n²) and
     * queues n repaints.</p>
     *
     * <p>Fresh ids are minted for the loaded items, since the serialized format carries none. Any
     * ids held from before the load — in selection state, group membership or undo entries — are
     * therefore stale and must be discarded by the caller.</p>
     *
     * @param loaded the items in z-order
     */
    public void load(List<DrawItem> loaded) {
        lock.lock();
        try {
            batchDepth++;
            try {
                items.clear();
                itemsById.clear();
                removed.clear();
                dirty.clear();              // ids are re-minted below
                for (DrawItem item : loaded) {
                    ItemId id = mint();
                    itemsById.put(id, item);
                    items.add(id);
                }
            } finally {
                batchDepth--;
            }
            logger.debug("Load: Loaded {} items.", items.size());
            publish();
        } finally {
            lock.unlock();
        }
    }

}
