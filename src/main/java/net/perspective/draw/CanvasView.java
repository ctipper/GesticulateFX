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
import javax.inject.Singleton;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
@Singleton
public class CanvasView {

    @Inject private DrawingArea drawarea;
    private java.util.List<Figure> list;
    private ObservableList<Figure> drawings;
    private Figure olditem, newitem;
    private int selection;
    private boolean isDrawing;

    private static final Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());
    
    /**
     * Creates a new instance of <code>DocView</code>
     */
    public CanvasView() {
        newitem = null;
        olditem = null;
        list = new ArrayList<>();
        selection = -1;
        isDrawing = false;
    }

    public void clearView() {
        this.deleteContents();
        this.setDrawingListener();
        this.setSelected(-1);
    }
    
    public void deleteContents() {
        list = new ArrayList<>();
    }
    
    public void setDrawingListener() {
        drawings = FXCollections.observableList(list);
        drawings.addListener(new ListChangeListener<Figure>() {

            @Override
            public void onChanged(Change<? extends Figure> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            // permutate
                        }
                    } else if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            // update item
                            drawarea.getCanvas().getChildren().set(i, drawings.get(i).draw());
                        }
                    } else {
                        int i = 0;
                        for (Figure remitem : c.getRemoved()) {
                            // removed
                            drawarea.getCanvas().getChildren().remove(c.getFrom()+i);
                            i++;
                        }
                        for (Figure additem : c.getAddedSubList()) {
                            // added
                            drawarea.getCanvas().getChildren().add(additem.draw());
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
        selection = s;
    }

    public int getSelected() {
        return selection;
    }

    public void setNewItem(Figure s) {
        if (s != null) {
            if (newitem == null) {
                this.addItemToCanvas(s);
            } else {
                this.updateCanvasItem(drawings.size() - 1, s);
            }
        }
        newitem = s;
    }

    public Figure getNewItem() {
        return newitem;
    }

    public void setPreviousItem(Figure s) {
        olditem = s;
    }

    public Figure getPreviousItem() {
        return olditem;
    }
}
