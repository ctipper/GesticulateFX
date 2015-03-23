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
import net.perspective.draw.geom.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
@Singleton
public class CanvasView {

    @Inject private DrawingArea drawarea;
    private final java.util.List<Figure> list;
    private ObservableList<Figure> drawings;
    private Figure newitem;
    private int selection;
    private boolean isDrawing;
    private Node anchors;

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());
    
    /**
     * Creates a new instance of <code>DocView</code>
     */
    public CanvasView() {
        newitem = null;
        list = new ArrayList<>();
        selection = -1;
        isDrawing = false;
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
        drawings.addListener(new ListChangeListener<Figure>() {

            @Override
            public void onChanged(Change<? extends Figure> c) {
                while (c.next()) {
                    ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                    if (c.wasPermutated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            // permutate
                            nodes.set(c.getPermutation(i), drawings.get(i).draw());
                            logger.trace("node " + c.getPermutation(i) + " updated from " + i);
                        }
                    } else if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            // update item
                            nodes.set(i, drawings.get(i).draw());
                            logger.trace("node " + i + " updated");
                        }
                    } else {               
                        if (c.wasRemoved()) {
                            int i = 0;
                            List<Node> deleted = new ArrayList<>();
                            for (Figure remitem : c.getRemoved()) {
                                // remove item
                                deleted.add(nodes.get(c.getFrom() + i));
                                i++;
                                logger.trace("node " + (c.getFrom() + i) + " removed.");
                            }
                            // delete the nodes from the scene graph
                            for (Node delete : deleted) {
                                nodes.remove(delete);
                            }
                        } 
                        if (c.wasAdded()) {
                            int i = 0;
                            for (Figure additem : c.getAddedSubList()) {
                                // add item
                                nodes.add(c.getFrom() + i, additem.draw());
                                i++;
                                logger.trace("node added");
                            }
                        }
                    }
                }
            }
        });
    }
    
    public void addItemToCanvas(Figure f) {
        if (f != null) {
            // to update properties here
            drawings.add(f);
        }
    }
    
    public void appendItemToCanvas(Figure f) {
        drawings.add(f);
    }

    public void updateCanvasItem(int i, Figure f) {
        drawings.set(i, f);
    }
    
    public void updateSelectedItem() {
        if (this.getSelected() != -1) {
            // to update properties here
        }
    }
    
    public void deleteSelectedItem() {
        if (getSelected() != -1) {
            drawings.remove(getSelected());
            setSelected(-1);
        }
    }

    public List<Figure> getDrawings() {
        return list;
    }

    public void setDrawing(boolean d) {
        isDrawing = d;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setSelected(int s) {
        if (selection == -1 && s != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(s).drawAnchors();
            nodes.add(anchors);
        }
        if (selection != -1 && s == -1) {
            if (anchors != null) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                nodes.remove(anchors);
                anchors = null;
            }
        }
        if (selection != s && s != -1) {
            if (anchors != null) {
                ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
                nodes.remove(anchors);
                anchors = null;
            }
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(s).drawAnchors();
            nodes.add(anchors);
        }
        if (s == -1) {
            anchors = null;
        }
        selection = s;
    }

    public void moveSelection(int s) {
        if (anchors != null) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            nodes.remove(anchors);
            anchors = null;
        }        
        if (s != -1) {
            ObservableList<Node> nodes = drawarea.getCanvas().getChildren();
            anchors = drawings.get(s).drawAnchors();
            nodes.add(anchors);
        }   
    }
    
    public int getSelected() {
        return selection;
    }

    public void setNewItem(Figure s) {
        if (newitem == null) {
            this.addItemToCanvas(s);
        } else {
            this.updateCanvasItem(drawings.size() - 1, s);
        }
        newitem = s;
    }
    
    public void resetNewItem() {
        newitem = null;
    }

    public Figure getNewItem() {
        return newitem;
    }
}
