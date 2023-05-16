/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.G2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

@Singleton
public class CanvasView {

    @Inject private DrawingArea drawarea;
    @Inject private ApplicationController controller;
    @Inject private Dropper dropper;
    @Inject private TextController textController;
    @Inject private G2 g2;
    private final List<DrawItem> list;
    private ObservableList<DrawItem> drawings;
    private final List<ImageItem> images;
    private Optional<DrawItem> newitem;
    private final Set<Integer> selectionIndex;
    private Group drawingAnchors;
    private Node drawMarquee;
    private Node drawGuides;
    private Path highlight;
    private boolean isDrawing;
    private boolean isEditing;
    private boolean isMapping;
    private boolean isMarquee;
    private boolean hasGuides;

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());

    /**
     * Creates a new instance of <code>CanvasView</code>
     */
    @Inject
    public CanvasView() {
        this.list = new ArrayList<>();
        this.images = new ArrayList<>();
        newitem = Optional.empty();
        this.selectionIndex = new LinkedHashSet<>();
        this.drawingAnchors = new Group();
    }

    /**
     * Clear the drawings
     */
    public void clearView() {
        this.deleteContents();
        this.setSelected(-1);
    }

    /**
     * Remove contents of drawing list
     */
    private void deleteContents() {
        drawings.clear();
        images.clear();
    }

    /**
     * Listener operates on drawing list to handle updates
     */
    public void setDrawingListener() {
        drawings = FXCollections.observableList(list);
        drawings.addListener((ListChangeListener.Change<? extends DrawItem> change) -> {
            while (change.next()) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                int g = (drawarea.isGridVisible() ? 1 : 0);
                if (hasGuides()) g += 1;
                if (change.wasPermutated()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        // permutate
                        nodes.set(change.getPermutation(i) + g, drawings.get(i).draw());
                        logger.trace("node " + change.getPermutation(i) + " updated from " + i);
                    }
                } else if (change.wasUpdated()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        // update item
                        nodes.set(i + g, drawings.get(i).draw());
                        logger.trace("node " + i + " updated");
                    }
                } else {
                    if (change.wasRemoved()) {
                        int i = 0;
                        List<Node> deleted = new ArrayList<>();
                        for (DrawItem removal : change.getRemoved()) {
                            // remove item
                            deleted.add(nodes.get(change.getFrom() + i + g));
                            i++;
                            logger.trace("node " + (change.getFrom() + i) + " removed.");
                        }
                        // delete the nodes from the scene graph
                        deleted.stream().forEach((delete) -> {
                            nodes.remove(delete);
                        });
                    }
                    if (change.wasAdded()) {
                        int i = 0;
                        for (DrawItem additem : change.getAddedSubList()) {
                            // add item
                            nodes.add(change.getFrom() + i + g, additem.draw());
                            i++;
                            logger.trace("node added");
                        }
                    }
                }
            }
        });
    }

    /**
     * Add item to canvas
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void appendItemToCanvas(DrawItem item) {
        drawings.add(item);
    }

    /**
     * Update the canvas item at given index
     * 
     * @param selection item index
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void updateCanvasItem(int selection, DrawItem item) {
        if (selection != -1) {
            drawings.set(selection, item);
        }
    }

    /**
     * Delete the selected item
     */
    public void deleteSelectedItem() {
        if (this.getSelected() != -1) {
            drawings.remove(this.getSelected());
            setSelected(-1);
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
            this.updateCanvasItem(drawings.size() - 1, item);
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
            DrawItem item = drawings.get(this.getSelected());
            item.updateProperties(drawarea);

            if ((item instanceof Figure) && !(item instanceof ArrowLine)) {
                FigureType type = ((Figure) item).getType();
                if (drawarea.getArrow() != ArrowType.NONE) {
                    if (type.equals(FigureType.SKETCH) || type.equals(FigureType.LINE)) {
                        item = new ArrowLine((Figure) item);
                        item.updateProperties(drawarea);
                    }
                }
            } else if (item instanceof ArrowLine arrowLine) {
                if (drawarea.getArrow() == ArrowType.NONE) {
                    item = arrowLine.getLine();
                    item.updateProperties(drawarea);
                } else {
                    item.updateProperties(drawarea);
                }
            }

            this.updateCanvasItem(this.getSelected(), item);
        } else if (this.getSelected() != -1 && !controller.getDropperDisabled()) { // dropper enabled
            /**
             * Select item properties and update UI
             */
            DrawItem item = drawings.get(this.getSelected());
            if (item instanceof Figure figure) {
                this.figureDropper(figure);
            } else if (item instanceof Text text) {
                this.textDropper(text);
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

    /**
     * Send DrawItem backwards in drawing list
     */
    public void sendBackwards() {
        if (this.getSelected() != -1) {
            DrawItem item = drawings.get(this.getSelected());
            if (this.getSelected() > 0) {
                int selection = this.getSelected();
                drawings.remove(selection);
                if (selection < 2) {
                    drawings.add(0, item);
                } else {
                    drawings.add(selection - 1, item);
                }
            }
        }
    }

    /**
     * Send DrawItem to back of drawing list
     */
    public void sendToBack() {
        if (this.getSelected() != -1) {
            DrawItem item = drawings.get(this.getSelected());
            if (this.getSelected() != 0) {
                int selection = this.getSelected();
                drawings.remove(selection);
                drawings.add(0, item);
            }
        }
    }

    /**
     * Bring DrawItem forwards in drawing list
     */
    public void bringForwards() {
        if (this.getSelected() != -1) {
            DrawItem item = drawings.get(this.getSelected());
            if (this.getSelected() < (drawings.size() - 1)) {
                int selection = this.getSelected();
                drawings.remove(selection);
                if (selection >= (drawings.size() - 1)) {
                    drawings.add(item);
                } else {
                    drawings.add(selection + 1, item);
                }
            }
        }
    }

    /**
     * Bring DrawItem to front of drawing list
     */
    public void bringToFront() {
        if (this.getSelected() != -1) {
            DrawItem item = drawings.get(this.getSelected());
            if (this.getSelected() != drawings.size() - 1) {
                int selection = this.getSelected();
                drawings.remove(selection);
                drawings.add(item);
            }
        }
    }

    /**
     * Group selected DrawItems
     */
    public void groupSelection() {
        if (this.isMultiSelected()) {
            Grouped groupedItem = new Grouped();
            List<DrawItem> removals = new ArrayList<>();
            List<Integer> selection =  new ArrayList<>();
            Integer selected = this.getBottomSelected();

            // create a Grouped item and queue items for removal
            for (Integer index : this.getMultiSelection()) {
                groupedItem.addShape(drawings.get(index));
                selection.add(index);
            }
            // reverse sort removals
            Collections.reverse(selection);
            for (Integer index : selection) {
                removals.add(drawings.get(index));
            }

            this.setSelected(-1);

            // eliminate shape to replace from list
            removals.remove(drawings.get(selected));

            // replace selected
            drawings.set(selected, groupedItem);

            for (DrawItem item : removals) {
                // delete drawings
                drawings.remove(item);
            }
        }
    }

    /**
     * Explode selected DrawItem group
     */
    public void ungroupSelection() {
        if (this.getSelected() != -1 && !this.isMultiSelected()) {
            int selected = this.getBottomSelected();
            DrawItem item = drawings.get(selected);
            if (item instanceof Grouped grouped) {
                boolean added = false;
                for (DrawItem shape : grouped.getShapes()) {
                    if (!added) {
                        // replace selected
                        drawings.set(selected, shape);
                        added = true;
                    } else {
                        // add drawings
                        drawings.add(selected, shape);
                    }
                    selected++;
                }
                this.setSelected(-1);
            }
        }
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
     * @return list of {@link net.perspective.draw.geom.DrawItem}
     */
    public List<DrawItem> getDrawings() {
        return list;
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

    /**
     * Return the first selected item
     * 
     * @param selection selected index
     */
    public void setSelected(int selection) {
        if (selection == -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
            nodes.remove(highlight);
            selectionIndex.clear();
            drawingAnchors.getChildren().clear();
        } else {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
            nodes.remove(highlight);
            if (isEditing()) {
                highlight = g2.highlightText(drawings.get(selection));
                nodes.add(highlight);
            }            
            if (!drawarea.isMultiSelectEnabled()) {
                selectionIndex.clear();
                drawingAnchors.getChildren().clear();
            }
            selectionIndex.add(selection);
            drawingAnchors = getAnchors();
            nodes.add(drawingAnchors);
        }
    }

    /**
     * Move the selection and update drawing anchors
     * 
     * @param selection selected index
     */
    public void moveSelection(int selection) {
        if (!drawingAnchors.getChildren().isEmpty()) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
            nodes.remove(highlight);
            drawingAnchors.getChildren().clear();
        }
        if (selection != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            if (isEditing()) {
                highlight = g2.highlightText(drawings.get(selection));
                nodes.add(highlight);
            }
            drawingAnchors = getAnchors();
            nodes.add(drawingAnchors);
        }
    }

    /**
     * Define the drawing anchors
     * 
     * @return anchor {@link javafx.scene.Group}
     */
    private Group getAnchors() {
        Group anchorGroup = new Group();
        for (Integer item : selectionIndex) {
            anchorGroup.getChildren().add(drawings.get(item).drawAnchors(drawarea));
        }
        return anchorGroup;
    }

    /**
     * Return the first selected item
     * 
     * @return index
     */
    public int getSelected() {
        int i;
        if (!selectionIndex.isEmpty()) {
            Integer[] a = selectionIndex.toArray(Integer[]::new);
            // find first value
            i = a[0];
        } else {
            i = -1;
        }
        return i;
    }

    /**
     * Return the selection with the lowest index in the 
     * drawing list
     * 
     * @return index
     */
    public int getBottomSelected() {
        int i;
        if (!selectionIndex.isEmpty()) {
            Integer[] a = selectionIndex.toArray(Integer[]::new);
            // find minimum value
            i = a[0];
            for (Integer as : a) {
                if (as < i) {
                    i = as;
                }
            }
        } else {
            i = -1;
        }
        return i;
    }

    /**
     * Get the selection
     * 
     * @return set of selected indices
     */
    public Set<Integer> getMultiSelection() {
        return selectionIndex;
    }

    /**
     * Are multiple items selected
     * 
     * @return multi-select active
     */
    public boolean isMultiSelected() {
        return selectionIndex.size() > 1;
    }

    /**
     * Select the DrawItems within given rectangular bounds
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void selectShapes(DrawItem item) {
        Shape b = item.bounds();
        Rectangle2D boundary = b.getBounds2D();
        for (DrawItem drawing : drawings) {
            Rectangle2D d = drawing.bounds().getBounds2D();
            if (boundary.contains(d)) {
                this.setSelected(drawings.indexOf(drawing));
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
        CanvasPoint topleft, bottomright;

        List<CanvasPoint> points = new ArrayList<>();
        CanvasPoint start = new CanvasPoint();
        CanvasPoint end = new CanvasPoint();

        for (DrawItem shape : list) {
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
