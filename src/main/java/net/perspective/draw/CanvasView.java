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
import net.perspective.draw.geom.FigureType;

/**
 *
 * @author ctipper
 */
@Singleton
public class CanvasView {

    private java.util.List<Figure> drawings;
    private FigureType figuretype;
    private Figure olditem, newitem;
    private int selection;
    private boolean isDrawing = false;

    /**
     * Creates a new instance of <code>DocView</code>
     */
    public CanvasView() {
        newitem = new Figure();
        olditem = null;
        drawings = new ArrayList<>();
        figuretype = FigureType.SKETCH;
        selection = -1;
    }

    public void initView() {
        this.deleteContents();
        this.setSelected(-1);
        this.setFigureType(FigureType.SKETCH);
    }
    
    public void deleteContents() {
        drawings = new ArrayList<>();
    }
    
    public void addItemToCanvas(Figure f) {
        // to update properties here
        drawings.add(f);
    }
    
    public void appendItemToCanvas(Figure f) {
        drawings.add(f);
    }

    public void deleteSelectedItem() {
        if (getSelected() != -1) {
            drawings.remove(getSelected());
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

    public void setFigureType(FigureType t) {
        this.figuretype = t;
    }

    public FigureType getFigureType() {
        return figuretype;
    }

    public void setNewItem(Figure s) {
        newitem = s;
    }

    public Figure getNewItem() {
        return newitem;
    }

    public void setOldItem(Figure s) {
        olditem = s;
    }

    public Figure getOldItem() {
        return olditem;
    }
}
