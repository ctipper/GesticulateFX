/*
 * FigurePathFactory.java
 * 
 * Created on Oct 19, 2013 6:07:12 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import net.perspective.draw.util.CanvasPoint;
import org.jhotdraw.geom.Bezier;

/**
 *
 * @author ctipper
 */
public class FigurePathFactory implements PathFactory {

    Path2D.Double path;

    public Path2D.Double createPath(Figure figure) {
        CanvasPoint p0, p1, p2, p3;
        double x, y, w, h;
        CanvasPoint[] cPoints;
        
        path = new Path2D.Double();
        java.util.List<CanvasPoint> points = figure.getPoints();
        FigureType type = figure.getType();
        switch (type) {
            case LINE:
                if (points.size() > 1) {
                    path.moveTo(points.get(0).x, points.get(0).y);
                    path.lineTo(points.get(1).x, points.get(1).y);
                } else {
                    return null;
                }
            break;
            case SQUARE:
            case CIRCLE:
                if (points.size() > 3) {
                    x = y = w = h = 0;
                    p0 = points.get(0);
                    p1 = points.get(1);
                    p2 = points.get(2);
                    p3 = points.get(3);
                    // handles cases where clockwise/anti-clockwise points
                    if (p0.x < p2.x && p0.y < p2.y) {
                        x = p0.x;
                        y = p0.y;
                        w = p2.x - x;
                        h = p2.y - y;
                    } else if (p0.x > p2.x && p0.y > p2.y) {
                        x = p2.x;
                        y = p2.y;
                        w = p0.x - x;
                        h = p0.y - y;
                    } else if (p3.x > p1.x && p3.y > p1.y) {
                        x = p1.x;
                        y = p1.y;
                        w = p3.x - x;
                        h = p3.y - y;
                    } else if (p3.x < p1.x && p3.y < p1.y) {
                        x = p3.x;
                        y = p3.y;
                        w = p1.x - x;
                        h = p1.y - y;
                    }
                    if (type.equals(FigureType.CIRCLE)) {
                        path = new Path2D.Double(new Ellipse2D.Double(x, y, w, h));
                    } else {
                        path = new Path2D.Double(new RoundRectangle2D.Double(x, y, w, h, 5, 5));
                    }
                } else {
                    return null;
                }
                break;
            case TRIANGLE:
                if (points.size() > 2) {
                    path.moveTo(points.get(0).x, points.get(0).y);
                    path.lineTo(points.get(1).x, points.get(1).y);
                    path.lineTo(points.get(2).x, points.get(2).y);
                    path.closePath();
                } else {
                    return null;
                }
                break;
            case SKETCH:
                cPoints = new CanvasPoint[points.size()];
                points.toArray(cPoints);
                path = Bezier.fitBezierPath(cPoints, 0.75).toGeneralPath();
                break;
            case POLYGON:
                cPoints = new CanvasPoint[points.size()];
                points.toArray(cPoints);
                path = Bezier.fitBezierPath(cPoints, 0.75).toGeneralPath();
                path.closePath();
                break;
            default:
                return null;
        }
        return path;
    }
}
