/*
 * FigurePointFactory.java
 * 
 * Created on Oct 27, 2013 9:35:37 PM
 * 
 */
package net.perspective.draw.geom;

import java.util.ArrayList;
import java.util.List;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;

/**
 * Produces a list of points describing a geometric figure.
 * 
 * @author ctipper
 */

public class FigurePointFactory implements PointFactory {

    @Override
    public List<CanvasPoint> createPoints(DrawingType description, double... coords) {
        List<CanvasPoint> points;
        double startX = 0.0, startY = 0.0, endX = 0.0, endY = 0.0;
        CanvasPoint p0, p1, p2, p3, p4, p5;
        double x, y, width, height, maxLength;

        if (coords.length > 0) {
            startX = coords[0];
            startY = coords[1];
        }

        if (coords.length == 4) {
            endX = coords[2];
            endY = coords[3];
        }

        switch (description) {
            case LINE:
                points = new ArrayList<>();
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(endX, endY));
                break;
            case HORIZONTAL:
                points = new ArrayList<>();
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(endX, startY));
                break;
            case VERTICAL:
                points = new ArrayList<>();
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(startX, endY));
                break;
            case CIRCLE:
            case SQUARE:
                points = new ArrayList<>();
                x = startX;
                y = startY;
                width = endX - startX;
                height = endY - startY;
                maxLength = Math.max(Math.abs(width), Math.abs(height));
                p0 = new CanvasPoint(x, y);
                p1 = new CanvasPoint(x, y + maxLength * Math.signum(height));
                p2 = new CanvasPoint(x + maxLength * Math.signum(width), y + maxLength * Math.signum(height));
                p3 = new CanvasPoint(x + maxLength * Math.signum(width), y);
                points.add(p0);
                points.add(p1);
                points.add(p2);
                points.add(p3);
                break;
            case ELLIPSE:
            case RECTANGLE:
                points = new ArrayList<>();
                p0 = new CanvasPoint(startX, startY);
                p1 = new CanvasPoint(startX, endY);
                p2 = new CanvasPoint(endX, endY);
                p3 = new CanvasPoint(endX, startY);
                points.add(p0);
                points.add(p1);
                points.add(p2);
                points.add(p3);
                break;
            case TRIANGLE:
                points = new ArrayList<>();
                width = endX - startX;
                height = endY - startY;
                maxLength = Math.max(Math.abs(width), Math.abs(height));
                p0 = new CanvasPoint(startX + maxLength * Math.signum(width) / 2, startY);
                p1 = new CanvasPoint(startX, startY + maxLength * Math.signum(height));
                p2 = new CanvasPoint(startX + maxLength * Math.signum(width), startY + maxLength * Math.signum(height));
                points.add(p0);
                points.add(p1);
                points.add(p2);
                break;
            case ISOSCELES:
                points = new ArrayList<>();
                width = endX - startX;
                height = endY - startY;
                p0 = new CanvasPoint(startX + width / 2, startY);
                p1 = new CanvasPoint(startX, startY + height);
                p2 = new CanvasPoint(startX + width, startY + height);
                points.add(p0);
                points.add(p1);
                points.add(p2);
                break;
            case HEXAGON:
                points = new ArrayList<>();
                width = endX - startX;
                height = endY - startY;
                p0 = new CanvasPoint(startX + width / 4, startY);
                p1 = new CanvasPoint(startX, startY + height / 2);
                p2 = new CanvasPoint(startX + width / 4, endY);
                p3 = new CanvasPoint(startX + 3 * width / 4, endY);
                p4 = new CanvasPoint(endX, startY + height / 2);
                p5 = new CanvasPoint(startX + 3 * width / 4, startY);
                points.add(p0);
                points.add(p1);
                points.add(p2);
                points.add(p3);
                points.add(p4);
                points.add(p5);
                break;
            case ISOHEX:
                points = new ArrayList<>();
                width = endX - startX;
                height = endY - startY;
                maxLength = Math.max(Math.abs(width), Math.abs(height));
                double w = maxLength * Math.signum(width);
                double h = maxLength * Math.sin(Math.PI / 3) * Math.signum(height);
                p0 = new CanvasPoint(startX + w / 4, startY);
                p1 = new CanvasPoint(startX, startY + h / 2);
                p2 = new CanvasPoint(startX + w / 4, startY + h);
                p3 = new CanvasPoint(startX + 3 * w / 4, startY + h);
                p4 = new CanvasPoint(startX + w, startY + h / 2);
                p5 = new CanvasPoint(startX + 3 * w / 4, startY);
                points.add(p0);
                points.add(p1);
                points.add(p2);
                points.add(p3);
                points.add(p4);
                points.add(p5);
                break;
            case SKETCH:
            case POLYGON:
                points = new ArrayList<>();
                break;
            default:
                points = new ArrayList<>();
                break;
        }
        return points;
    }

}
