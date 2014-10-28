/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

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

    public FigureHandler() {
    }

    public void upEvent() {
        view.setDrawing(false);
        // add figure to canvas
        Figure item = view.getNewItem();
        item.setStroke(drawarea.getStroke());
        item.setColor(drawarea.getColor());
        item.setFillColor(drawarea.getFillColor());
        // following enables flexible redraw
        FigureType type = item.getType();
        switch (type) {
        case SQUARE:
            item.setType(FigureType.RECTANGLE);
            break;
        case CIRCLE:
            item.setType(FigureType.ELLIPSE);
            break;
        case TRIANGLE:
            item.setType(FigureType.ISOSCELES);
            break;
        default:
            break;
        }
        item.setEndPoints();
        view.setNewItem(item);
        view.resetNewItem();
    }

    public void downEvent() {
    }

    public void dragEvent() {
        FigureType type;

        type = drawarea.getFigureType();
        Figure item = new Figure(type);
        item.setStroke(drawarea.getStroke());
        item.setColor("lightgray");
        item.setFillColor("white");
        item.setStart(drawarea.getStartX(), drawarea.getStartY());
        item.setEnd(drawarea.getTempX(), drawarea.getTempY());
        item.setPoints();
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }
}
