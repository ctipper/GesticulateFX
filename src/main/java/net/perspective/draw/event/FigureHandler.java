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
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class FigureHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;

    private final PointFactory pointFactory;

    public FigureHandler() {
        this.pointFactory = new FigurePointFactory();
    }

    public void upEvent() {
        view.setDrawing(false);
        view.addItemToCanvas(view.getNewItem());
        view.setNewItem(null);
    }

    public void downEvent() {
    }

    public void dragEvent() {
        java.util.List<CanvasPoint> points;
        FigureType type;

        type = drawarea.getFigureType();
        Figure item = new Figure(type);
        item.setStroke(drawarea.getStroke());
        item.setColor(drawarea.getColor());
        points = pointFactory.createPoints(type, 
                drawarea.getStartX(), drawarea.getStartY(), drawarea.getTempX(), drawarea.getTempY());
        item.setPoints(points);
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }
}
