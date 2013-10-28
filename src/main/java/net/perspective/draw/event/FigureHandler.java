/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

import net.perspective.draw.DocView;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigurePointFactory;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class FigureHandler extends HandlerAdapter {

    private final DocView view;
    private final FigurePointFactory pointFactory;

    public FigureHandler(DrawingCanvas c) {
        super(c);
        this.view = c.getView();
        this.pointFactory = new FigurePointFactory();
    }

    @Override
    public void upEvent() {
        view.setDrawing(false);
        view.addDrawItemToCanvas(view.getNewItem());
    }

    @Override
    public void downEvent() {
        view.setDrawing(true);
    }

    @Override
    public void moveEvent() {
        java.util.List<CanvasPoint> points;
        FigureType type;
        
        type = view.getFigureType();
        Figure item = new Figure();
        item.setStroke(6.0);
        item.setColor("#4860E0");
        item.setType(type);
        points = pointFactory.createPoints(type, c.getStartX(), c.getStartY(), 
                c.getTempX(), c.getTempY());
        item.setPoints(points);
        if ((item.getType().equals(FigureType.SQUARE)) ||
            (item.getType().equals(FigureType.CIRCLE)) ||
            (item.getType().equals(FigureType.TRIANGLE)) ||
            (item.getType().equals(FigureType.POLYGON))) {
            item.setClosed(true);
        }
        item.setPath();
        view.setNewItem(item);
    }
}
