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
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
@Singleton
public class CanvasView {

    @Inject private DrawingArea drawarea;
    private java.util.List<Figure> drawings;
    private ObservableList<Figure> observableList;
    private Figure olditem, newitem;
    private int selection;
    private boolean isDrawing;

    /**
     * Creates a new instance of <code>DocView</code>
     */
    public CanvasView() {
        newitem = null;
        olditem = null;
        drawings = new ArrayList<>();
        selection = -1;
        isDrawing = false;
    }

    public void clearView() {
        this.deleteContents();
        this.setDrawingListener();
        this.setSelected(-1);
    }
    
    public void deleteContents() {
        drawings = new ArrayList<>();
    }
    
    public void setDrawingListener() {
        observableList = FXCollections.observableList(drawings);
        observableList.addListener(new ListChangeListener<Figure>() {

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
                        }
                    } else {
                        for (Figure remitem : c.getRemoved()) {
                            //remitem.remove(Outer.this);
                        }
                        for (Figure additem : c.getAddedSubList()) {
                            //additem.add(Outer.this);
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
//            drawings.add(f);
            observableList.add(f);
        }
    }
    
    public void appendItemToCanvas(Figure f) {
        //drawings.add(f);
        observableList.add(f);
    }

    public void deleteSelectedItem() {
        if (getSelected() != -1) {
            //drawings.remove(getSelected());
            observableList.remove(getSelected());
            setSelected(-1);
        }
    }

    public List<Figure> getDrawings() {
        return drawings;
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
        newitem = s;
        if (s != null) drawarea.getCanvas().getChildren().add(s.sketch());
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
