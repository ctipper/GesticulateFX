/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
 * 
 */
package net.perspective.draw;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.geom.DrawItem;
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
    private final java.util.List<DrawItem> list;
    private ObservableList<DrawItem> drawings;
    private DrawItem newitem;
    private int selection;
    private boolean isDrawing = false;
    private Node anchors;

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());
    
    /**
     * Creates a new instance of <code>CanvasView</code>
     */
    @Inject
    public CanvasView() {
        this.list = new ArrayList<>();
        this.newitem = null;
        this.selection = -1;
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
            this.updateCanvasItem(this.getSelected(), item);
        }
    }
    
    public void deleteSelectedItem() {
        if (this.getSelected() != -1) {
            drawings.remove(this.getSelected());
            setSelected(-1);
        }
    }

    public List<DrawItem> getDrawings() {
        return list;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setSelected(int select) {
        if (selection == -1 && select != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(select).drawAnchors();
            nodes.add(anchors);
        }
        if (selection != -1 && select == -1) {
            if (anchors != null) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                nodes.remove(anchors);
                anchors = null;
            }
        }
        if (selection != select && select != -1) {
            if (anchors != null) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                nodes.remove(anchors);
                anchors = null;
            }
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(select).drawAnchors();
            nodes.add(anchors);
        }
        if (select == -1) {
            anchors = null;
        }
        selection = select;
    }

    public void moveSelection(int select) {
        if (anchors != null) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(anchors);
            anchors = null;
        }        
        if (select != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(select).drawAnchors();
            nodes.add(anchors);
        }   
    }
    
    public int getSelected() {
        return selection;
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
