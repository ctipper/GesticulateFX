/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
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

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.G2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.perspective.draw.ItemStore.Snapshot;

/**
 * 
 * @author ctipper
 */

@Singleton
public class CanvasView {

    private final DrawingArea drawarea;
    private final ApplicationController controller;
    private final TextController textController;
    @Inject ItemStore store;
    @Inject FxSnapshotBinder binder;
    @Inject Dropper dropper;
    @Inject G2 g2;
    private final List<ImageItem> images;
    private final Set<ItemId> selectionIds;
    /** The scene node currently rendering each slot. FX thread only. */
    private final Map<ItemId, Node> itemNodes;
    private Optional<DrawItem> newitem;
    private final Timeline caretTimeline;
    private Group drawingAnchors;
    private Node drawMarquee;
    private Node drawGuides;
    private Group highlight;
    private Group itemPivot;
    private boolean isDrawing;
    private boolean isEditing;
    private boolean isMapping;
    private boolean isMarquee;
    private boolean hasGuides;

    /**
     * Node id prefix of the grid layer, set by {@link G2#drawGridLayout}. Matched as a prefix
     * because {@code redrawGrid} identifies its own nodes the same way.
     */
    private static final String GRID_ID = "grid";

    /** Node id of the guides layer, so {@link #itemBase} can recognise it. */
    static final String GUIDES_ID = "guides";

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());

    /**
     * Creates a new instance of <code>CanvasView</code>
     */
    @Inject
    public CanvasView(DrawingArea drawarea, ApplicationController controller, TextController textController) {
        this.drawarea = drawarea;
        this.controller = controller;
        this.textController = textController;
        this.images = new ArrayList<>();
        this.selectionIds = new LinkedHashSet<>();
        this.itemNodes = new HashMap<>();
        newitem = Optional.empty();
        this.drawingAnchors = new Group();
        this.itemPivot = new Group();
        /**
         * provide cursor animation
         */
        this.caretTimeline = new Timeline();
        this.caretTimeline.setCycleCount(Timeline.INDEFINITE);
        this.caretTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, e -> highlight.getChildren().forEach(n -> ((Path) n).setStroke(Color.TRANSPARENT))),
                new KeyFrame(Duration.seconds(.5), e -> highlight.getChildren().forEach(n -> ((Path) n).setStroke(Color.BLACK))),
                new KeyFrame(Duration.seconds(1)));
    }

    /**
     * Clear the drawings
     */
    public void clearView() {
        this.deleteContents();
        this.setSelected(-1);
    }

    /**
     * Is the item store empty
     * 
     * @return is empty
     */
    public boolean isEmpty() {
        return store.isEmpty();
    }

    /**
     * Remove contents of drawing list
     */
    private void deleteContents() {
        store.clear();
        images.clear();
    }

    /**
     * Mirror the item store onto the canvas
     *
     * <p>Replaces the former {@code ObservableList} listener. The store publishes from whichever
     * thread wrote, so {@link FxSnapshotBinder} coalesces publications onto the FX thread and
     * reports what actually changed by {@link ItemId}; this class only has to maintain the node
     * for each slot.</p>
     */
    public void setDrawingListener() {
        binder.setItemListener(new FxSnapshotBinder.ItemListener() {

            @Override
            public void added(ItemId id, DrawItem item, int index) {
                Node node = item.draw();
                itemNodes.put(id, node);
                drawarea.getCanvas().getChildren().add(itemBase() + index, node);
                logger.trace("node added at {}", index);
            }

            @Override
            public void rebound(ItemId id, DrawItem item) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                Node previous = itemNodes.get(id);
                int at = previous == null ? -1 : nodes.indexOf(previous);
                if (at == -1) {
                    return;                          // never rendered; the restack will place it
                }
                Node node = item.draw();
                nodes.set(at, node);                 // in place: no index arithmetic
                itemNodes.put(id, node);
                logger.trace("node {} updated", at);
            }

            @Override
            public void removed(ItemId id) {
                Node node = itemNodes.remove(id);
                if (node != null) {
                    drawarea.getCanvas().getChildren().remove(node);
                    logger.trace("node removed");
                }
            }

            @Override
            public void reordered() {
                restack();
            }
        });
        binder.attach();
    }

    /**
     * Index of the first item node among the canvas children
     *
     * <p>The canvas holds leading chrome — the grid and the guides, each inserted at index 0 by
     * {@link DrawingArea#redrawGrid} and {@link #setGuides} — then the item nodes, then whatever
     * has been appended: the anchors, the caret highlight, the rotation pivot, and the map
     * controls. Only the leading chrome shifts the item region, and appending never does.</p>
     *
     * <p>Counted from the scene graph rather than from {@code isGridVisible()} and
     * {@code hasGuides()}. Those flags are set in one place and the nodes added in another, so a
     * moment where they disagree would silently offset every subsequent index by one, corrupting
     * the wrong node rather than failing.</p>
     *
     * @return the index at which item nodes begin
     */
    private int itemBase() {
        ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
        int base = 0;
        while (base < nodes.size() && isChrome(nodes.get(base))) {
            base++;
        }
        return base;
    }

    /**
     * @param node a canvas child
     * @return true if it is leading chrome rather than an item
     */
    private static boolean isChrome(Node node) {
        String id = node.getId();
        return id != null && (id.startsWith(GRID_ID) || id.equals(GUIDES_ID));
    }

    /**
     * Restack the item nodes to match the store's z-order
     *
     * <p>Checks before mutating: an add or a remove already leaves the region in order, and only a
     * genuine reposition needs the list rebuilt. The nodes are reused, so this restacks rather than
     * redrawing.</p>
     */
    private void restack() {
        ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
        List<Node> ordered = new ArrayList<>(binder.getOrder().size());
        for (ItemId id : binder.getOrder()) {
            Node node = itemNodes.get(id);
            if (node != null) {
                ordered.add(node);
            }
        }
        int base = itemBase();
        boolean inOrder = base + ordered.size() <= nodes.size();
        for (int i = 0; inOrder && i < ordered.size(); i++) {
            inOrder = nodes.get(base + i) == ordered.get(i);
        }
        if (inOrder) {
            return;
        }
        nodes.removeAll(ordered);
        nodes.addAll(itemBase(), ordered);            // recomputed: removal cannot shift the chrome
    }

    /**
     * Add item to canvas
     *
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @return the id of the slot the item was placed in
     */
    public ItemId appendItemToCanvas(DrawItem item) {
        return store.append(item);
    }

    /**
     * Add several items to the top of the canvas as one publication.
     *
     * <p>Appending in a loop instead rebuilds the snapshot per item, which is O(n²) across the
     * batch. Unlike {@link #loadItemsToCanvas} this adds to the document rather than replacing it,
     * so ids held elsewhere stay valid and the selection is left alone.</p>
     *
     * @param items the {@link net.perspective.draw.geom.DrawItem}s in z-order
     */
    public void appendItemsToCanvas(List<DrawItem> items) {
        store.insertAll(store.size(), items);
    }

    /**
     * Replace the document with the contents of a file.
     *
     * <p>Images are installed before the items, and this ordering is required rather than tidy: a
     * {@link net.perspective.draw.geom.Picture} renders by resolving its image index against that
     * list, so an item published ahead of its bitmap has nothing to draw.</p>
     *
     * <p>The selection is dropped because {@link ItemStore#load} mints fresh ids, leaving every id
     * held from before the load — the selection above all — pointing at nothing. Cleared here so a
     * caller cannot forget.</p>
     *
     * <p>One publication for the whole document. Appending item by item instead rebuilds the
     * snapshot per item, which is O(n²) over a load, and asks the binder to reconcile n times.</p>
     *
     * @param items the {@link net.perspective.draw.geom.DrawItem}s in z-order
     * @param pictures the {@link net.perspective.draw.ImageItem}s in saved order
     */
    public void loadItemsToCanvas(List<DrawItem> items, List<ImageItem> pictures) {
        this.setSelected(-1);
        images.clear();
        images.addAll(pictures);
        store.load(items);
    }

    /**
     * Update the canvas item at given index
     *
     * @param selection item index
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void updateCanvasItem(int selection, DrawItem item) {
        if (selection != -1) {
            store.replaceItem(selection, item);      // keeps the slot id, so the node is rebound
        }
    }

    /**
     * Update the item bound to a slot
     *
     * @param id the slot id
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void updateCanvasItem(ItemId id, DrawItem item) {
        if (id != null) {
            store.replaceItem(id, item);
        }
    }

    /**
     * Delete the selected item
     */
    public void deleteSelectedItem() {
        ItemId id = this.getSelectedId();
        if (id != null) {
            setSelected(-1);                         // drops the anchors before the item goes
            store.removeById(id);
        }
    }

    /**
     * Use the textController's rich text markup
     * 
     * @param isRichText formatted text active
     */
    public void enableRichText(boolean isRichText) {
        textController.enableRichText(isRichText);
    }

    /**
     * Insert or update new item
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void setNewItem(DrawItem item) {
        if (newitem.isEmpty()) {
            this.appendItemToCanvas(item);
        } else {
            this.updateCanvasItem(store.size() - 1, item);
        }
        newitem = Optional.of(item);
    }

    /**
     * Reset new item to null
     */
    public void resetNewItem() {
        newitem = Optional.empty();
    }

    /**
     * Get the new item
     * 
     * @return the {@link net.perspective.draw.geom.DrawItem}
     */
    public Optional<DrawItem> getNewItem() {
        return newitem;
    }

    /**
     * Update the properties of the selected item or select properties
     * of the selected item if dropper is enabled
     */
    public void updateSelectedItem() {
        if (this.getSelected() != -1 && controller.getDropperDisabled()) {
            /**
             * Update item properties
             */
            DrawItem item = store.lookupId(this.getSelected());
            if (item == null) {
                return;                              // stale selection
            }

            if (!this.isMultiSelected() && !drawarea.isMultiSelectEnabled()) {
                item.updateProperties(drawarea);
            }

            switch (item) {
                case Figure figure when !(figure instanceof ArrowLine) -> {
                    FigureType type = figure.getType();
                    if (drawarea.getArrow() != ArrowType.NONE) {
                        if (type.equals(FigureType.SKETCH) || type.equals(FigureType.LINE)) {
                            item = new ArrowLine(figure);
                            item.updateProperties(drawarea);
                        }
                    }
                }
                case ArrowLine arrowLine -> {
                    if (drawarea.getArrow() == ArrowType.NONE) {
                        item = arrowLine.getLine();
                        item.updateProperties(drawarea);
                    } else {
                        item.updateProperties(drawarea);
                    }
                }
                default -> {
                }
            }

            this.updateCanvasItem(this.getSelected(), item);
        } else if (this.getSelected() != -1 && !controller.getDropperDisabled()) { // dropper enabled
            /**
             * Select item properties and update UI
             */
            DrawItem item = store.lookupId(this.getSelected());
            if (item == null) {
                return;                              // stale selection
            }
            switch (item) {
                case Figure figure -> this.figureDropper(figure);
                case Text text -> this.textDropper(text);
                default -> {
                }
            }
        }
    }

    private void figureDropper(Figure item) {
        String styleId;
        /**
         * get stroke
         */
        int strokeId = dropper.getStrokeIdBinary((BasicStroke) item.getStroke());
        if (item instanceof ArrowLine arrowLine) {
            styleId = dropper.getStyleSelector((BasicStroke) item.getStroke(), arrowLine.getArrowType());
        } else {
            styleId = dropper.getStyleSelector((BasicStroke) item.getStroke(), ArrowType.NONE);
        }
        logger.trace("strokeId: {} styleId: {}", strokeId, styleId);
        /**
         * get colours
         */
        Color color = item.getColor();
        Color fillcolor = item.getFillColor();
        logger.trace("color: {} fillcolor: {}", controller.toRGBCode(color), controller.toRGBCode(fillcolor));
        /**
         * arrow type
         */
        if (item instanceof ArrowLine arrowLine) {
            drawarea.setArrow(arrowLine.getArrowType());
        } else {
            drawarea.setArrow(ArrowType.NONE);
        }
        /**
         * set properties
         */
        controller.setStrokeCombo(strokeId);
        controller.setStyleCombo(styleId);
        controller.setColor(color);
        controller.setFillColor(fillcolor);
    }

    private void textDropper(Text item) {
        /**
         * get font properties
         */
        String fontFamily = item.getFont();
        int fontSize = item.getSize();
        int fontStyle = item.getStyle();
        logger.trace("font: {} size: {} style: {}", fontFamily, fontSize, fontStyle);
        /**
         * get text colour
         */
        Color color = item.getColor();
        logger.trace("color: {}", controller.toRGBCode(color));
        /**
         * set properties
         */
        controller.setColor(color);
        controller.setFontFamily(fontFamily);
        controller.setFontSize(fontSize);
        drawarea.updateFontStyle(fontStyle);
    }

// ---------------------------------------------------------------------------
// Z-order
// ---------------------------------------------------------------------------

    /**
     * Send DrawItem backwards in drawing list
     *
     * <p>{@link ItemStore#move} repositions the slot without minting a new id, so the item stays
     * selected across the move and no re-selection is needed here. The same holds for the three
     * methods below.</p>
     */
    public void sendBackwards() {
        int selection = this.getSelected();
        if (selection <= 0) {
            return;                                  // nothing selected, or already at back
        }
        /*
         * The original special-cased selection < 2 to insert at 0. That branch was redundant:
         * for selection == 1, selection - 1 is already 0.
         */
        store.move(selection, selection - 1);
    }

    /**
     * Send DrawItem to back of drawing list
     */
    public void sendToBack() {
        int selection = this.getSelected();
        if (selection <= 0) {
            return;
        }
        store.move(selection, 0);
    }

    /**
     * Bring DrawItem forwards in drawing list
     */
    public void bringForwards() {
        int selection = this.getSelected();
        if (selection == -1 || selection >= store.size() - 1) {
            return;                                  // nothing selected, or already at front
        }
        /*
         * The original special-cased selection >= size - 2 to append. Also redundant: removing
         * selection and then inserting at selection + 1 lands the item at the end either way.
         */
        store.move(selection, selection + 1);
    }

    /**
     * Bring DrawItem to front of drawing list
     */
    public void bringToFront() {
        int selection = this.getSelected();
        int last = store.size() - 1;
        if (selection == -1 || selection == last) {
            return;
        }
        store.move(selection, last);
    }

// ---------------------------------------------------------------------------
// Grouping
// ---------------------------------------------------------------------------

    /**
     * Group selected DrawItems
     *
     * <p>Members are added to the {@link Grouped} in z-order rather than the order they were
     * selected in, so the group renders identically to the loose items it replaces. The group
     * takes over the bottom-most member's slot, keeping that id and its position in the z-order;
     * the other members are removed. Both mutations publish as one snapshot, so no reader observes
     * the group and its members coexisting.</p>
     */
    public void groupSelection() {
        Snapshot snap = store.snapshot();            // one snapshot for the whole operation
        List<ItemId> members = getSelectionInZOrder(snap);
        if (members.isEmpty()) {
            return;                                  // nothing selected, or the selection went stale
        }

        ItemId bottom = members.get(0);
        Grouped groupedItem = new Grouped();
        List<ItemId> removals = new ArrayList<>(members.size() - 1);
        for (ItemId id : members) {
            groupedItem.addDrawItem(snap.get(id));   // non-null: getSelectionInZOrder dropped the rest
            if (!id.equals(bottom)) {
                removals.add(id);                    // the bottom slot is replaced, not removed
            }
        }

        int bottomIndex = snap.indexOf(bottom);
        this.setSelected(-1);                        // also clears selectionIds
        store.batch(() -> {
            store.replaceItem(bottomIndex, groupedItem);
            store.removeAllById(removals);           // all above bottomIndex, which therefore holds
        });
    }

    /**
     * Explode selected DrawItem group
     *
     * <p>The first member takes over the group's slot, keeping its id and z-position; the rest are
     * inserted immediately above it, in order. Both mutations publish as one snapshot.</p>
     */
    public void ungroupSelection() {
        Snapshot snap = store.snapshot();
        List<ItemId> members = getSelectionInZOrder(snap);
        if (members.isEmpty()) {
            return;
        }
        ItemId groupId = members.get(0);
        if (!(snap.get(groupId) instanceof Grouped grouped)) {
            return;                                  // bottom-most selection is not a group
        }

        List<DrawItem> shapes = grouped.getDrawItems();
        int selected = snap.indexOf(groupId);        // non-negative: the id resolved against snap
        this.setSelected(-1);
        if (shapes.isEmpty()) {
            return;                                  // degenerate group; leave the slot as it is
        }
        store.batch(() -> {
            store.replaceItem(selected, shapes.get(0));
            if (shapes.size() > 1) {
                store.insertAll(selected + 1, shapes.subList(1, shapes.size()));
            }
        });
    }

    /**
     * Insert date and time into Text item
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    public void insertDateAndTime(Text item) {
        ZonedDateTime zoned = ZonedDateTime.now();
        String timestamp = zoned.format(DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm"));
        textController.getEditor().insertText(timestamp);
        textController.getEditor().commitText(item);
    }

    /**
     * Get the list of draw items
     *
     * <p>A copy taken from the store's current snapshot, not the live list the old backing
     * {@code ObservableList} exposed: mutating the result no longer affects the document. Each
     * call is O(n), so hold the result rather than calling it per item in a loop.</p>
     *
     * @return list of {@link net.perspective.draw.geom.DrawItem} in z-order
     */
    public List<DrawItem> getDrawings() {
        return store.getDrawItems();
    }

    /**
     * Get the list of image items
     * 
     * @return list of {@link net.perspective.draw.ImageItem}
     */
    public List<ImageItem> getImageItems() {
        return images;
    }

    /**
     * Get the number of images
     * 
     * @return size of image item list
     */
    public int getImageItemsSize() {
        return images.size();
    }

    /**
     * Add image to drawing and return image count
     * 
     * @param imageItem the {@link net.perspective.draw.ImageItem}
     * @return image item index
     */
    public int setImageItem(ImageItem imageItem) {
        images.add(imageItem);
        return images.size() - 1;
    }

    /**
     * Set image at index
     * 
     * @param i image item index
     * @param imageItem the {@link net.perspective.draw.ImageItem}
     */
    public void setImageItem(int i, ImageItem imageItem) {
        images.set(i, imageItem);
    }

    /**
     * Add image to drawing
     * 
     * @param imageItem the {@link net.perspective.draw.ImageItem}
     */
    public void addImageItem(ImageItem imageItem) {
        images.add(imageItem);
    }

    /**
     * Get image item at index
     * 
     * @param index image item index
     * @return {@link net.perspective.draw.ImageItem}
     */
    public ImageItem getImageItem(int index) {
        return images.get(index);
    }

    /**
     * Replace image at index
     * 
     * @param index image item index
     * @param image the {@link net.perspective.draw.ImageItem}
     */
    public void replaceImage(int index, Image image) {
        ImageItem item = images.get(index);
        item.setImage(image);
        images.set(index, item);
    }

// ---------------------------------------------------------------------------
// Mutation
// ---------------------------------------------------------------------------

    /**
     * Set selection exclusively, clearing any prior multi-selection.
     *
     * @param id the slot id, or null to clear
     */
    public void resetSelectedId(ItemId id) {
        selectionIds.clear();
        setSelectedId(id);
    }

    /**
     * Add a DrawItem to the selection.
     *
     * @param id the slot id, or null to clear
     */
    public void setSelectedId(ItemId id) {
        if (id == null) {
            selectionIds.clear();
            return;
        }
        if (!drawarea.isMultiSelectEnabled()) {
            selectionIds.clear();
        }
        selectionIds.add(id);
    }

    /**
     * Select a DrawItem by index and show the selection.
     *
     * <p>Compatibility shim; prefer {@link #setSelected(ItemId)}.</p>
     *
     * @param selection the index, or -1 to clear
     */
    public void setSelected(int selection) {
        if (selection == -1) {
            setSelected((ItemId) null);
            return;
        }
        List<ItemId> order = store.getItems();
        if (selection < 0 || selection >= order.size()) {
            return;                                  // stale index; leave selection untouched
        }
        setSelected(order.get(selection));
    }

    /**
     * Select a DrawItem and show the selection.
     *
     * <p>Distinct from {@link #setSelectedId}, which only records membership. This also redraws
     * the anchors, the rotation pivot and the caret highlight, so it is what to call when the
     * selection changes outside a mouse gesture — restoring one after a modal interlude, say.
     * Within a gesture the handler's mouse-up already refreshes the overlay.</p>
     *
     * @param id the slot id, or null to clear
     */
    public void setSelected(ItemId id) {
        ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
        if (id == null) {
            nodes.remove(drawingAnchors);
            nodes.remove(highlight);
            nodes.remove(itemPivot);
            selectionIds.clear();
            drawingAnchors.getChildren().clear();
            itemPivot.getChildren().clear();
            return;
        }
        Snapshot snap = store.snapshot();
        DrawItem item = snap.get(id);
        if (item == null) {
            return;                                  // stale id; leave selection untouched
        }
        nodes.remove(drawingAnchors);
        nodes.remove(highlight);
        nodes.remove(itemPivot);
        if (!drawarea.isMultiSelectEnabled()) {
            selectionIds.clear();
            drawingAnchors.getChildren().clear();
        }
        setSelectedId(id);                           // must precede getAnchors(), which reads the selection
        drawingAnchors = getAnchors(snap);
        nodes.add(drawingAnchors);
        if (drawarea.isRotationMode()) {
            itemPivot = g2.drawRotationPivot(item);
            nodes.add(itemPivot);
        }
        setTextHighlight(id);
    }

    /**
     * Remove an item from the selection.
     *
     * @param id the slot id
     * @return true if it was selected
     */
    public boolean deselect(ItemId id) {
        return selectionIds.remove(id);
    }

    /**
     * Drop selected ids whose items no longer exist.
     *
     * <p>Call after a remote delete, or anywhere a stale selection would be visible to the user.
     * Not called automatically: the accessors already skip missing items, so pruning is about
     * keeping {@link #isMultiSelected()} and the size honest rather than about safety.</p>
     *
     * @return the number of ids dropped
     */
    public int pruneSelection() {
        Snapshot snap = store.snapshot();
        int before = selectionIds.size();
        selectionIds.removeIf(id -> snap.get(id) == null);
        return before - selectionIds.size();
    }

    /**
     * Redraw the overlay for whatever is currently selected.
     *
     * <p>The common case by far: an item has been moved, resized or restyled in place and the
     * anchors need to follow it. Replaces {@code moveSelection(getSelected())}, which resolved the
     * selection to an index only to resolve it straight back again — and silently did nothing if
     * the two resolutions disagreed.</p>
     */
    public void refreshSelection() {
        moveSelection(this.getSelectedId());
    }

    /**
     * Move the selection and update drawing anchors
     *
     * <p>Compatibility shim; prefer {@link #moveSelection(ItemId)}.</p>
     *
     * @param selection selected index
     */
    public void moveSelection(int selection) {
        List<ItemId> order = store.getItems();
        moveSelection(selection < 0 || selection >= order.size() ? null : order.get(selection));
    }

    /**
     * Move the selection and update drawing anchors
     *
     * <p>The anchors are dropped first and rebuilt from the current geometry, so this is the
     * refresh to call after an item has been moved or resized in place. A null or stale id leaves
     * the anchors cleared.</p>
     *
     * @param id the slot id, or null
     */
    public void moveSelection(ItemId id) {
        ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
        if (!drawingAnchors.getChildren().isEmpty()) {
            nodes.remove(drawingAnchors);
            nodes.remove(highlight);
            nodes.remove(itemPivot);
            drawingAnchors.getChildren().clear();
        }
        Snapshot snap = store.snapshot();
        DrawItem item = snap.get(id);
        if (item == null) {
            return;                                  // nothing selected, or stale
        }
        drawingAnchors = getAnchors(snap);
        nodes.add(drawingAnchors);
        if (drawarea.isRotationMode()) {
            itemPivot = g2.drawRotationPivot(item);
            nodes.add(itemPivot);
        }
        setTextHighlight(id);
    }

    /**
     * Provide a cursor whilst editing text
     *
     * <p>Compatibility shim; prefer {@link #setTextHighlight(ItemId)}.</p>
     *
     * @param selection index of the current drawitem
     */
    public void setTextHighlight(int selection) {
        List<ItemId> order = store.getItems();
        setTextHighlight(selection < 0 || selection >= order.size() ? null : order.get(selection));
    }

    /**
     * Provide a cursor whilst editing text
     *
     * @param id the slot id of the current drawitem
     */
    public void setTextHighlight(ItemId id) {
        ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
        nodes.remove(highlight);
        if (isEditing()) {
            DrawItem item = store.lookupId(id);
            if (item == null) {
                return;                              // nothing selected, or stale; no caret drawn
            }
            highlight = g2.highlightText(item);
            if (textController.getEditor().getCaretStart() == textController.getEditor().getCaretEnd()) {
                caretTimeline.play();
            } else {
                caretTimeline.stop();
            }
            nodes.add(highlight);
        }
    }

    /**
     * Define the drawing anchors
     *
     * <p>Reads the selection, so any id must already be registered before this is called.
     * Ids whose items have been removed are skipped rather than throwing — a stale member
     * simply contributes no anchor.</p>
     *
     * @param snap the store snapshot to resolve ids against
     * @return anchor {@link javafx.scene.Group}
     */
    private Group getAnchors(Snapshot snap) {
        Group anchorGroup = new Group();
        for (ItemId id : selectionIds) {
            DrawItem item = snap.get(id);
            if (item != null) {
                anchorGroup.getChildren().add(item.drawAnchors(drawarea));
            }
        }
        return anchorGroup;
    }

// ---------------------------------------------------------------------------
// Access
// ---------------------------------------------------------------------------

    /**
     * The primary selected item.
     *
     * @return the slot id, or null if nothing is selected
     */
    public ItemId getSelectedId() {
        if (selectionIds.isEmpty()) {
            return null;
        }
        return selectionIds.iterator().next();    // insertion order
    }

    /**
     * The primary selected item, as an index.
     *
     * <p>Compatibility shim. Returns -1 when nothing is selected <em>or</em> when the selected
     * item has been removed — callers cannot distinguish, which is the same contract as before.</p>
     *
     * <p>Each call is an O(n) scan of the z-order. That is fine for event handling but not inside
     * a loop: for a pass over many items, take one {@link Snapshot} and use
     * {@code snap.indexOf(id)}, or work in ids and skip the conversion.</p>
     *
     * @return index of the DrawItem, or -1
     */
    public int getSelected() {
        ItemId id = getSelectedId();
        return id == null ? -1 : store.indexOf(id);
    }

    /**
     * Lowest member of the multi-selection in z-order.
     *
     * @return the id, or null if the selection is empty or fully stale
     */
    public ItemId getBottomSelectedId() {
        Snapshot snap = store.snapshot();
        ItemId bottom = null;
        int bottomIndex = Integer.MAX_VALUE;
        for (ItemId id : selectionIds) {
            int index = snap.indexOf(id);
            if (index != -1 && index < bottomIndex) {
                bottomIndex = index;
                bottom = id;
            }
        }
        return bottom;
    }

    /**
     * The selection.
     *
     * <p>Unmodifiable: the original returned the live Set, letting callers mutate selection state
     * behind this class's back. May contain ids whose items have been removed —
     * {@link #getSelectionInZOrder()} or {@code store.lookupAll} filter those.</p>
     *
     * @return the selected slot ids, in insertion order
     */
    public Set<ItemId> getMultiSelection() {
        return Collections.unmodifiableSet(selectionIds);
    }

    /**
     * The selection sorted by z-order, with removed items dropped.
     *
     * <p>Use this wherever stacking matters — grouping in particular, since {@code Grouped} takes
     * members in the order given, and {@link #selectionIds} is in click order.</p>
     *
     * @return the selected slot ids, bottom first
     */
    public List<ItemId> getSelectionInZOrder() {
        return getSelectionInZOrder(store.snapshot());
    }

    /**
     * The selection sorted by z-order, resolved against a snapshot the caller already holds.
     *
     * <p>Walks the z-order and keeps the selected ids, rather than sorting the selection by
     * {@code indexOf}: one pass with a hash lookup per item, and stale ids fall out for free
     * because they are not in {@code snap.order()}.</p>
     *
     * @param snap the store snapshot to resolve ids against
     * @return the selected slot ids, bottom first
     */
    private List<ItemId> getSelectionInZOrder(Snapshot snap) {
        List<ItemId> live = new ArrayList<>(selectionIds.size());
        for (ItemId id : snap.order()) {
            if (selectionIds.contains(id)) {
                live.add(id);
            }
        }
        return live;
    }

    /**
     * The selected items.
     *
     * @return the {@link DrawItem}s, skipping any removed
     */
    public List<DrawItem> getSelectedDrawItems() {
        return store.lookupAll(selectionIds);
    }

    /**
     * @param id the slot id
     * @return true if selected
     */
    public boolean isSelected(ItemId id) {
        return selectionIds.contains(id);
    }

    /**
     * Are multiple items selected.
     *
     * <p>Counts held ids, including any whose items have been removed. Call
     * {@link #pruneSelection()} first if that distinction matters.</p>
     *
     * @return true if more than one DrawItem is selected
     */
    public boolean isMultiSelected() {
        return selectionIds.size() > 1;
    }

    /**
     * @return true if nothing is selected
     */
    public boolean hasNoSelection() {
        return selectionIds.isEmpty();
    }

    /**
     * Select the DrawItems within given rectangular bounds
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void selectShapes(DrawItem item) {
        Shape b = item.bounds();
        Rectangle2D boundary = b.getBounds2D();
        Snapshot snap = store.snapshot();
        for (ItemId id : snap.order()) {
            DrawItem drawing = snap.get(id);
            if (drawing == null) {
                continue;
            }
            Rectangle2D d = drawing.bounds().getBounds2D();
            if (boundary.contains(d)) {
                this.setSelectedId(id);
            }
        }
    }

    /**
     * Initialise the given editing mode defined by KeyboardHandlerType
     * 
     * @param keyHandler the keyboard handler
     */
    public void setEditing(KeyHandlerType keyHandler) {
        switch (keyHandler) {
            case TEXT -> {
                setEditing(true);
                drawarea.setKeyboardHandler(keyHandler);
            }
            case MOVE -> {
                setEditing(false);
                drawarea.setKeyboardHandler(keyHandler);
            }
            case MAP -> {
                setEditing(false);
                drawarea.setKeyboardHandler(keyHandler);
            }
            default -> {
                setEditing(false);
                drawarea.setKeyboardHandler(keyHandler);
            }
        }
    }

    /**
     * Set editing mode
     * 
     * @param isEditing editing is active
     */
    public void setEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    /**
     * Is editing mode
     * 
     * @return is editing
     */
    public boolean isEditing() {
        return isEditing;
    }

    /**
     * cut the text item
     */
    public void cutTextItem() {
        textController.cutSelectedText();
    }

    /**
     * copy the text item
     */
    public void copyTextItem() {
        textController.copySelectedText();
    }

    /**
     * paste the text item
     */
    public void pasteTextItem() {
        textController.pasteSelectedText();
    }


    /**
     * Set mapping mode
     * 
     * @param isMapping
     */
    public void setMapping(boolean isMapping) {
        this.isMapping = isMapping;
    }

    /**
     * Is mapping mode
     * 
     * @return
     */
    public boolean isMapping() {
        return isMapping;
    }

    /**
     * Set drawing mode
     * 
     * @param isDrawing is drawing
     */
    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    /**
     * Is drawing mode
     * 
     * @return is drawing
     */
    public boolean isDrawing() {
        return isDrawing;
    }

    /**
     * Activate the marquee selection
     * 
     * @param isMarquee is loup active
     */
    public void setMarquee(boolean isMarquee) {
        this.isMarquee = isMarquee;
        if (isMarquee) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawMarquee);
            drawMarquee = drawarea.getMarquee().draw();
            nodes.add(drawMarquee);
        } else {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawMarquee);
        }
    }

    /**
     * Marquee is drawing
     * 
     * @return is loup active
     */
    public boolean isMarquee() {
        return isMarquee;
    }

    /**
     * Activate the guides
     * 
     * @param hasGuides marquee is active
     */
    public void setGuides(boolean hasGuides) {
        this.hasGuides = hasGuides;
        if (hasGuides) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawGuides);
            drawGuides = drawarea.getGuides().draw();
            drawGuides.setId(GUIDES_ID);             // leading chrome; see itemBase()
            nodes.add(0, drawGuides);
        } else {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawGuides);
        }
    }

    /**
     * Guides are drawing
     * 
     * @return is drawing
     */
    public boolean hasGuides() {
        return hasGuides;
    }

    /**
     * Helper method used by export routines
     * 
     * @return bounding {@link net.perspective.draw.util.CanvasPoint}
     */
    public CanvasPoint[] getBounds() {
        return getBounds(store.getDrawItems());
    }

    /**
     * Helper method used by export routines
     *
     * <p>Takes the items to measure rather than reading the store, so an exporter can size its
     * output and render its content from one capture. Reading the store twice lets the document
     * change in between, giving a canvas sized for one set of items and drawn from another.</p>
     *
     * @param items the {@link net.perspective.draw.geom.DrawItem}s to measure
     * @return bounding {@link net.perspective.draw.util.CanvasPoint}
     */
    public CanvasPoint[] getBounds(List<DrawItem> items) {
        CanvasPoint topleft, bottomright;

        List<CanvasPoint> points = new ArrayList<>();
        CanvasPoint start = new CanvasPoint();
        CanvasPoint end = new CanvasPoint();

        for (DrawItem shape : items) {
            points.add(shape.getTop()[0]);
            points.add(shape.getBottom()[0]);
            points.add(shape.getUp()[0]);
            points.add(shape.getDown()[0]);
        }
        try {
            topleft = (CanvasPoint) points.get(0).clone();
            bottomright = (CanvasPoint) points.get(1).clone();

            for (CanvasPoint point : points) {
                topleft.x = Math.min(point.x, topleft.x);
                topleft.y = Math.min(point.y, topleft.y);
                bottomright.x = Math.max(point.x, bottomright.x);
                bottomright.y = Math.max(point.y, bottomright.y);
            }

            start = new CanvasPoint(topleft.x, topleft.y);
            end = new CanvasPoint(bottomright.x, bottomright.y);
        } catch (CloneNotSupportedException ex) {
            logger.error(null, ex);
        }
        return new CanvasPoint[] {start, end};
    }

}
