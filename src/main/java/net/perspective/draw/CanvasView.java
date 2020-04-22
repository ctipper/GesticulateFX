/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
 * 
 */
package net.perspective.draw;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;
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
    private final List<DrawItem> list;
    private ObservableList<DrawItem> drawings;
    private final List<ImageItem> images;
    private DrawItem newitem;
    private final Set<Integer> selectionIndex;
    private Group drawingAnchors;
    private Node drawMarquee;
    private boolean isDrawing;
    private boolean isMarquee;

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());

    /**
     * Creates a new instance of <code>CanvasView</code>
     */
    @Inject
    public CanvasView() {
        this.list = new ArrayList<>();
        this.images = new ArrayList<>();
        this.newitem = null;
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
                if (change.wasPermutated()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        // permutate
                        nodes.set(change.getPermutation(i), drawings.get(i).draw());
                        logger.trace("node " + change.getPermutation(i) + " updated from " + i);
                    }
                } else if (change.wasUpdated()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        // update item
                        nodes.set(i, drawings.get(i).draw());
                        logger.trace("node " + i + " updated");
                    }
                } else {
                    if (change.wasRemoved()) {
                        int i = 0;
                        List<Node> deleted = new ArrayList<>();
                        for (DrawItem removal : change.getRemoved()) {
                            // remove item
                            deleted.add(nodes.get(change.getFrom() + i));
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
                            nodes.add(change.getFrom() + i, additem.draw());
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
     * @param item
     */
    public void appendItemToCanvas(DrawItem item) {
        drawings.add(item);
    }

    /**
     * Update the canvas item at given index
     * 
     * @param index
     * @param item
     */
    public void updateCanvasItem(int index, DrawItem item) {
        drawings.set(index, item);
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
     * Insert or update new item
     * 
     * @param item
     */
    public void setNewItem(DrawItem item) {
        if (newitem == null) {
            this.appendItemToCanvas(item);
        } else {
            this.updateCanvasItem(drawings.size() - 1, item);
        }
        newitem = item;
    }

    /**
     * Reset new item to null
     */
    public void resetNewItem() {
        newitem = null;
    }

    /**
     * Get the new item
     * 
     * @return a DrawItem
     */
    public DrawItem getNewItem() {
        return newitem;
    }

    /**
     * Update the properties of the selected item or select properties
     * of the selected item if dropper is enabled
     */
    public void updateSelectedItem() {
        if (this.getSelected() != -1) {
            if (controller.getDropperDisabled()) {
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
                } else if (item instanceof ArrowLine) {
                    if (drawarea.getArrow() == ArrowType.NONE) {
                        item = ((ArrowLine) item).getLine();
                        item.updateProperties(drawarea);
                    } else {
                        item.updateProperties(drawarea);
                    }
                }

                this.updateCanvasItem(this.getSelected(), item);
            } else { // dropper enabled
                DrawItem item = drawings.get(this.getSelected());
                if (item instanceof Figure) {
                    this.figureDropper((Figure) item);
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
        if (item instanceof ArrowLine) {
            styleId = dropper.getStyleSelector((BasicStroke) item.getStroke(), ((ArrowLine) item).getArrowType());
        } else {
            styleId = dropper.getStyleSelector((BasicStroke) item.getStroke(), ArrowType.NONE);
        }
        logger.trace("strokeId " + strokeId + " styleId " + styleId);
        /**
         * get colours
         */
        Color color = item.getColor();
        Color fillcolor = item.getFillColor();
        logger.trace("color: " + controller.toRGBCode(color) + " fillcolor: " + controller.toRGBCode(fillcolor));
        /**
         * arrow type
         */
        if (item instanceof ArrowLine) {
            drawarea.setArrow(((ArrowLine) item).getArrowType());
        } else {
            drawarea.setArrow(ArrowType.NONE);
        }
        controller.setStrokeCombo(strokeId);
        controller.setStyleCombo(styleId);
        controller.setColor(color);
        controller.setFillColor(fillcolor);
    }

    /**
     * Send DrawItem backwards in drawing list
     */
    public void sendBackwards() {
        if (this.getSelected() != -1) {
            DrawItem item = drawings.get(this.getSelected());
            if (this.getSelected() > 0) {
                int selection = this.getSelected();
                if (selection < 2) {
                    drawings.add(0, item);
                } else {
                    drawings.add(selection - 1, item);
                }
                drawings.remove(selection + 1);
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
                drawings.add(0, item);
                drawings.remove(selection + 1);
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
                if (selection >= (drawings.size() - 2)) {
                    drawings.add(item);
                } else {
                    drawings.add(selection + 2, item);
                }
                drawings.remove(selection);
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
                drawings.add(item);
                drawings.remove(selection);
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
            if (item instanceof Grouped) {
                boolean added = false;
                for (DrawItem shape : ((Grouped) item).getShapes()) {
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
     * Get the list of draw items
     * 
     * @return
     */
    public List<DrawItem> getDrawings() {
        return list;
    }

    /**
     * Get the list of image items
     * 
     * @return
     */
    public List<ImageItem> getImageItems() {
        return images;
    }

    /**
     * Get the number of images
     * 
     * @return
     */
    public int getImageItemsSize() {
        return images.size();
    }

    /**
     * Add image to drawing and return image count
     * 
     * @param imageItem
     * @return
     */
    public int setImageItem(ImageItem imageItem) {
        images.add(imageItem);
        return images.size() - 1;
    }

    /**
     * Set image at index
     * 
     * @param i image index
     * @param imageItem
     */
    public void setImageItem(int i, ImageItem imageItem) {
        images.set(i, imageItem);
    }

    /**
     * Add image to drawing
     * 
     * @param imageItem
     */
    public void addImageItem(ImageItem imageItem) {
        images.add(imageItem);
    }

    /**
     * Get image item at index
     * 
     * @param index
     * @return
     */
    public ImageItem getImageItem(int index) {
        return images.get(index);
    }

    /**
     * Replace image at index
     * 
     * @param index
     * @param image
     */
    public void replaceImage(int index, Image image) {
        ImageItem item = images.get(index);
        item.setImage(image);
        images.set(index, item);
    }

    /**
     * Return the first selected item
     * 
     * @param selection
     */
    public void setSelected(int selection) {
        if (selection == -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
            selectionIndex.clear();
            drawingAnchors.getChildren().clear();
        } else {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
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
     * @param selection 
     */
    public void moveSelection(int selection) {
        if (!drawingAnchors.getChildren().isEmpty()) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(drawingAnchors);
            drawingAnchors.getChildren().clear();
        }
        if (selection != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            drawingAnchors = getAnchors();
            nodes.add(drawingAnchors);
        }
    }

    /**
     * Define the drawing anchors
     * 
     * @return  anchor group
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
            Integer[] a = selectionIndex.toArray(new Integer[selectionIndex.size()]);
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
            Integer[] a = selectionIndex.toArray(new Integer[selectionIndex.size()]);
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
     * @return
     */
    public Set<Integer> getMultiSelection() {
        return selectionIndex;
    }

    /**
     * Are multiple items selected
     * 
     * @return
     */
    public boolean isMultiSelected() {
        return selectionIndex.size() > 1;
    }

    /**
     * Select the DrawItems within given rectangular bounds
     * 
     * @param item 
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
     * Set drawing mode
     * 
     * @param isDrawing
     */
    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    /**
     * Is drawing mode
     * 
     * @return
     */
    public boolean isDrawing() {
        return isDrawing;
    }

    /**
     * Activate the marquee selection
     * 
     * @param isMarquee 
     */
    public void setMarquee(boolean isMarquee) {
        this.isMarquee = isMarquee;
        if (isMarquee()) {
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
     * @return 
     */
    public boolean isMarquee() {
        return isMarquee;
    }

    /**
     * Helper method used by export routines
     * 
     * @return 
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
