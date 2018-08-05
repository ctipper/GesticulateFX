/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
 * 
 */
package net.perspective.draw;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
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

    @Inject
    private DrawingArea drawarea;
    private final java.util.List<DrawItem> list;
    private ObservableList<DrawItem> drawings;
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
        this.newitem = null;
        this.selectionIndex = new LinkedHashSet<>();
        this.drawingAnchors = new Group();
    }

    public void clearView() {
        this.deleteContents();
        this.setSelected(-1);
    }

    public void deleteContents() {
        drawings.clear();
    }

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

    public void addItemToCanvas(DrawItem item) {
        appendItemToCanvas(item);
    }

    public void appendItemToCanvas(DrawItem item) {
        drawings.add(item);
    }

    public void updateCanvasItem(int index, DrawItem item) {
        drawings.set(index, item);
    }

    public void updateSelectedItem() {
        if (this.getSelected() != -1) {
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
        }
    }

    public void deleteSelectedItem() {
        if (this.getSelected() != -1) {
            drawings.remove(this.getSelected());
            setSelected(-1);
        }
    }

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

    public List<DrawItem> getDrawings() {
        return list;
    }

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

    private Group getAnchors() {
        Group anchorGroup = new Group();
        for (Integer item : selectionIndex) {
            anchorGroup.getChildren().add(drawings.get(item).drawAnchors());
        }
        return anchorGroup;
    }

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

    public Set<Integer> getMultiSelection() {
        return selectionIndex;
    }

    public boolean isMultiSelected() {
        return selectionIndex.size() > 1;
    }

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

    public void setNewItem(DrawItem item) {
        if (newitem == null) {
            this.addItemToCanvas(item);
        } else {
            this.updateCanvasItem(drawings.size() - 1, item);
        }
        newitem = item;
    }

    public void resetNewItem() {
        newitem = null;
    }

    public DrawItem getNewItem() {
        return newitem;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

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
