/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
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

public class FigureHandler extends HandlerAdapter {

    DocView view;

    public FigureHandler(DrawingCanvas c) {
        super(c);
        view = c.getView();
    }

    @Override
    public void upEvent() {
        view.addDrawItemToCanvas(view.getNewItem());
    }

    @Override
    public void downEvent() {
        java.util.List<CanvasPoint> points;
        CanvasPoint point;
        Figure item = new Figure();
        item.setStroke(6.0);
        item.setColor("#4860E0");
        item.setType(view.getFigureType());
        point = new CanvasPoint(c.getStartX(), c.getStartY());
        points = new ArrayList<>();
        switch (view.getFigureType()) {
            case LINE:
                points.add(point);
                point = new CanvasPoint(c.getStartX(), c.getStartY());
                points.add(point);
                item.setPoints(points);
                break;
            case CIRCLE:
            case SQUARE:
                for (int i = 0; i < 4; i++) {
                    points.add(point);
                    point = new CanvasPoint(c.getStartX(), c.getStartY());
                }
                item.setPoints(points);
                item.setClosed(true);
                break;
            case TRIANGLE:
                for (int i = 0; i < 3; i++) {
                    points.add(point);
                    point = new CanvasPoint(c.getStartX(), c.getStartY());
                }
                item.setPoints(points);
                item.setClosed(true);
                break;
            case SKETCH:
                points.add(point);
                item.setPoints(points);
                break;
            case POLYGON:
                points.add(point);
                item.setPoints(points);
                item.setClosed(true);
                break;
            default:
                break;
        }
        item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    @Override
    public void moveEvent() {
        java.util.List<CanvasPoint> points;
        CanvasPoint p0, p1, p2, p3;
        view.setOldItem(view.getNewItem());
        Figure item = new Figure();
        item.setStroke(6.0);
        item.setColor("#4860E0");
        item.setType(view.getFigureType());
        CanvasPoint point = new CanvasPoint(c.getTempX(), c.getTempY());
        switch (view.getFigureType()) {
            case LINE:
                if (view.getOldItem().getType().equals(FigureType.LINE)) {
                    points = view.getOldItem().getPoints();
                    points.set(1, point);
                    item.setPoints(points);
                } else {
                    points = new ArrayList<>();
                    points.add(point);
                    points.add(point);
                    item.setPoints(points);
                }
                break;
            case CIRCLE:
            case SQUARE:
                if ((view.getOldItem().getType().equals(FigureType.SQUARE))
                    || (view.getOldItem().getType().equals(FigureType.CIRCLE))) {
                    points = view.getOldItem().getPoints();
                    p0 = points.get(0);
                    double w = (double) c.getTempX() - p0.getX();
                    double h = (double) c.getTempY() - p0.getY();
                    double l = Math.max(Math.abs(w), Math.abs(h));
                    points = new ArrayList<>();
                    p1 = new CanvasPoint(p0.getX(), p0.getY() + l * Math.signum(h));
                    p2 = new CanvasPoint(p0.getX() + l * Math.signum(w), p0.getY() + l * Math.signum(h));
                    p3 = new CanvasPoint(p0.getX() + l * Math.signum(w), p0.getY());
                    points.add(p0);
                    points.add(p1);
                    points.add(p2);
                    points.add(p3);
                    item.setPoints(points);
                } else {
                    points = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        points.add(point);
                        point = new CanvasPoint(c.getTempX(), c.getTempY());
                    }
                    item.setPoints(points);
                }
                item.setClosed(true);
                break;
            case TRIANGLE:
                if (view.getOldItem().getType().equals(
                    FigureType.TRIANGLE)) {
                    points = view.getOldItem().getPoints();
                    p0 = points.get(0);
                    p1 = points.get(1);
                    p2 = points.get(2);
                    double w = (double) c.getTempX() - p1.getX();
                    double h = (double) c.getTempY() - p0.getY();
                    double l = Math.max(Math.abs(w), Math.abs(h));
                    points = new ArrayList<>();
                    p0 = new CanvasPoint(p1.getX() + l * Math.signum(w) / 2, p0.getY());
                    p1 = new CanvasPoint(p1.getX(), p0.getY() + l * Math.signum(h));
                    p2 = new CanvasPoint(p1.getX() + l * Math.signum(w), p0.getY() + l * Math.signum(h));
                    points.add(p0);
                    points.add(p1);
                    points.add(p2);
                    item.setPoints(points);
                } else {
                    points = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        points.add(point);
                        point = new CanvasPoint(c.getTempX(), c.getTempY());
                    }
                    item.setPoints(points);
                }
                item.setClosed(true);
                break;
            case SKETCH:
            case POLYGON:
                if ((view.getOldItem().getType().equals(FigureType.SKETCH))
                    || (view.getOldItem().getType().equals(FigureType.POLYGON))) {
                    points = view.getOldItem().getPoints();
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
                break;
            default:
                break;
        }
        item.setPath();
        view.setNewItem(item);
    }
}
