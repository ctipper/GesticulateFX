/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.*;

/**
 *
 * @author ctipper
 */

public class FigureHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    private final FigureFactory figurefactory;

    public FigureHandler() {
        this.figurefactory = new FigureFactoryImpl();
    }

    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem() == null || drawarea.getDrawType() == null) {
            return;
        }
        // add figure to canvas
        Figure item = view.getNewItem();
        item.setStroke(drawarea.getStroke());
        item.setJfxColor(drawarea.getColor());
        item.setJfxFillColor(drawarea.getFillColor());
        item.setEndPoints();
        view.setNewItem(item);
        view.resetNewItem();
    }

    public void downEvent() {
    }

    public void dragEvent() {
        Figure item = figurefactory.createFigure(drawarea.getDrawType());
        item.setStroke(drawarea.getStroke());
        item.setJfxColor(Color.web("lightgray"));
        item.setJfxFillColor(Color.web("white"));
        item.setStart(drawarea.getStartX(), drawarea.getStartY());
        item.setEnd(drawarea.getTempX(), drawarea.getTempY());
        item.setPoints(drawarea.getDrawType());
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }
}
