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

    @Inject
    private DrawingArea drawarea;
    @Inject
    private CanvasView view;
    @Inject
    private DrawAreaListener listener;
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
        Figure item = (Figure) view.getNewItem();
        item.setEndPoints();
        item.updateProperties(drawarea);
        view.setNewItem(item);
        view.resetNewItem();
    }

    public void downEvent() {
    }

    public void dragEvent() {
        // Create figure
        Figure item = figurefactory.createFigure(drawarea.getDrawType());
        // Initialise stroke and fill
        item.setStroke(drawarea.getStroke());
        item.setColor(Color.web("lightgray"));
        item.setFillColor(Color.web("white"));
        // Initialise figure
        item.setStart(listener.getStartX(), listener.getStartY());
        item.setEnd(listener.getTempX(), listener.getTempY());
        item.setPoints(drawarea.getDrawType());
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }
}
