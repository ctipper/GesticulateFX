/*
 * CanvasView.java
 * 
 * Created on Oct 20, 2013 11:14:58 AM
 * 
 */
package net.perspective.draw;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
@Singleton
public class CanvasView {

    private DrawingArea drawarea;
    private java.util.List<Figure> drawings;
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

    public void setDrawArea(DrawingArea c) {
        this.drawarea = c;
    }
    
    public void clearView() {
        this.deleteContents();
        this.setSelected(-1);
    }
    
    public void deleteContents() {
        drawings = new ArrayList<>();
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

    public void deleteSelectedItem() {
        if (getSelected() != -1) {
            drawings.remove(getSelected());
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
