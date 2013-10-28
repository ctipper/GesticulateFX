/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
 * 
 */
package net.perspective.draw.event;

import java.util.ArrayList;
import net.perspective.draw.DocView;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class SketchHandler extends HandlerAdapter  {
    
    private final DocView view;

    public SketchHandler(DrawingCanvas c) {
        super(c);
        this.view = c.getView();
    }

    @Override
    public void upEvent() {
        view.setDrawing(false);
        view.addDrawItemToCanvas(view.getNewItem());
    }

    @Override
    public void downEvent() {
        FigureType type;
        java.util.List<CanvasPoint> points;
        CanvasPoint point;

        view.setDrawing(true);
        type = view.getFigureType();
        Figure item = new Figure();
        item.setStroke(6.0);
        item.setColor("#4860E0");
        item.setType(type);
        point = new CanvasPoint(c.getStartX(), c.getStartY());
        points = new ArrayList<>();
        points.add(point);
	    item.setPoints(points);
        if (type.equals(FigureType.POLYGON)) {
            item.setClosed(true);
        }
    	item.setPath();
        view.setNewItem(item);
    }

    @Override
    public void moveEvent() {
        java.util.List<CanvasPoint> points;
        FigureType type;
        
        view.setOldItem(view.getNewItem());
        if (view.getOldItem() instanceof Figure) {
            type = ((Figure) view.getOldItem()).getType();
        } else {
            type = FigureType.NONE;
        }
        Figure item = new Figure();
        item.setStroke(6.0);
        item.setColor("#4860E0");
        item.setType(type);
        CanvasPoint point = new CanvasPoint(c.getTempX(), c.getTempY());
        if ((type.equals(FigureType.SKETCH)) || (type.equals(FigureType.POLYGON))) {
            points = ((Figure) view.getOldItem()).getPoints();
            points.add(point);
            item.setPoints(points);
        } else {
            points = new ArrayList<>();
            points.add(point);
            item.setPoints(points);
        }
        if (view.getFigureType().equals(FigureType.POLYGON)) {
            item.setClosed(true);
        }
        item.setPath();
        view.setNewItem(item);
    }
}
