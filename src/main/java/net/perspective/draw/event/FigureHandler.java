/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class FigureHandler extends Handler {

    private final CanvasView view;
    private final PointFactory pointFactory;

    public FigureHandler(DrawingArea c) {
        super(c);
        this.view = c.getView();
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

        type = canvas.getFigureType();
        Figure item = new Figure(type);
        item.setStroke(canvas.getStroke());
        item.setColor(canvas.getColor());
        points = pointFactory.createPoints(type,
            canvas.getStartX(), canvas.getStartY(), canvas.getTempX(), canvas.getTempY());
        item.setPoints(points);
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }
}
