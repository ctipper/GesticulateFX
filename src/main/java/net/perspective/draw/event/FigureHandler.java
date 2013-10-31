/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

import net.perspective.draw.DocModel;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.*;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class FigureHandler extends HandlerAdapter {

    private final DocModel model;
    private final PointFactory pointFactory;

    public FigureHandler(DrawingCanvas c) {
        super(c);
        this.model = c.getView();
        this.pointFactory = new FigurePointFactory();
    }

    @Override
    public void upEvent() {
        model.setDrawing(false);
        model.addDrawItemToCanvas(model.getNewItem());
    }

    @Override
    public void downEvent() {
    }

    @Override
    public void dragEvent() {
        java.util.List<CanvasPoint> points;
        FigureType type;

        type = model.getFigureType();
        Figure item = new Figure(type);
        item.setStroke(6.0);
        item.setColor("#4860E0");
        points = pointFactory.createPoints(type, 
            c.getStartX(), c.getStartY(), c.getTempX(), c.getTempY());
        item.setPoints(points);
        item.setPath();
        model.setNewItem(item);
        model.setDrawing(true);
    }
}
