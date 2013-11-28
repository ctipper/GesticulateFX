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
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class SketchHandler extends Handler  {
    
    private final CanvasView view;

    public SketchHandler(DrawingArea c) {
        super(c);
        this.view = c.getView();
    }

    public void upEvent() {
        view.setDrawing(false);
        view.addItemToCanvas(view.getNewItem());
        view.setNewItem(null);
    }

    public void downEvent() {
        List<CanvasPoint> points;
        CanvasPoint point;

        // Create Figure
        Figure item = new Figure(canvas.getFigureType());
        item.setStroke(canvas.getStroke());
        item.setColor(canvas.getColor());
        // Initialise sketch
        point = new CanvasPoint(canvas.getStartX(), canvas.getStartY());
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

        view.setPreviousItem(view.getNewItem());
        // Create Figure
        Figure item = new Figure(canvas.getFigureType());
        item.setStroke(canvas.getStroke());
        item.setColor(canvas.getColor());
        // continue sketch
        point = new CanvasPoint(canvas.getTempX(), canvas.getTempY());
        points = view.getPreviousItem().getPoints();
        points.add(point);
        item.setPoints(points);
        item.setPath();
        view.setNewItem(item);
    }
}
