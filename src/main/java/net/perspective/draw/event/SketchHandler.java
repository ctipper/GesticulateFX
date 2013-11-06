/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
 * 
 */
package net.perspective.draw.event;

import java.util.ArrayList;
import java.util.List;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingCanvas;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class SketchHandler extends Handler  {
    
    private final CanvasView view;

    public SketchHandler(DrawingCanvas c) {
        super(c);
        this.view = c.getView();
    }

    public void upEvent() {
        view.setDrawing(false);
        view.addDrawItemToCanvas(view.getNewItem());
    }

    public void downEvent() {
        List<CanvasPoint> points;
        CanvasPoint point;

        // Create Figure
        Figure item = new Figure(view.getFigureType());
        item.setStroke(6.0);
        item.setColor("#4860E0");
        // Initialise sketch
        point = new CanvasPoint(c.getStartX(), c.getStartY());
        points = new ArrayList<>();
        points.add(point);
	    item.setPoints(points);
    	item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    public void dragEvent() {
        List<CanvasPoint> points;
        CanvasPoint point;

        view.setOldItem(view.getNewItem());
        // Create Figure
        Figure item = new Figure(view.getFigureType());
        item.setStroke(6.0);
        item.setColor("#4860E0");
        // continue sketch
        point = new CanvasPoint(c.getTempX(), c.getTempY());
        points = view.getOldItem().getPoints();
        points.add(point);
        item.setPoints(points);
        item.setPath();
        view.setNewItem(item);
    }
}
