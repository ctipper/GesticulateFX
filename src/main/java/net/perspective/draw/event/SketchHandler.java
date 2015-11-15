/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
 * 
 */
package net.perspective.draw.event;

import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureFactoryImpl;

/**
 *
 * @author ctipper
 */

public class SketchHandler implements Handler  {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    private final FigureFactory figurefactory;

    public SketchHandler() {
        this.figurefactory = new FigureFactoryImpl();
    }

    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem() == null || drawarea.getDrawType() == null) {
            return;
        }
        // add figure to canvas
        Figure item = view.getNewItem();
        item.setEndPoints();
        view.setNewItem(item);
        view.resetNewItem();
    }

    public void downEvent() {
        // Create figure
        Figure item = figurefactory.createFigure(drawarea.getDrawType());
        // Initialise sketch
        item.setStroke(drawarea.getStroke());
        item.setJfxColor(Color.web("lightgray"));
        item.setJfxFillColor(Color.web("white"));
        // Initialise sketch
        item.setPoints(drawarea.getDrawType());
        item.addPoint(drawarea.getStartX(), drawarea.getStartY());
        item.setEndPoints();
    	item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    public void dragEvent() {
        // Create Figure
        Figure item = view.getNewItem();
        // continue sketch
        item.addPoint(drawarea.getTempX(), drawarea.getTempY());
        item.setEndPoints();
        item.setPath();
        view.setNewItem(item);
    }
}
