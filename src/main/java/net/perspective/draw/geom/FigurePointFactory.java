/*
 * FigurePointFactory.java
 * 
 * Created on Oct 27, 2013 9:35:37 PM
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

    private List<CanvasPoint> pentagram;

    public FigurePointFactory() {
        pentagram = new ArrayList<>();
    }

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

        points = new ArrayList<>();

        switch (description) {
            case POINT:
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(startX, startY));
                break;
            case LINE:
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(endX, endY));
                break;
            case HORIZONTAL:
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(endX, startY));
                break;
            case VERTICAL:
                points.add(new CanvasPoint(startX, startY));
                points.add(new CanvasPoint(startX, endY));
                break;
            case CIRCLE:
            case SQUARE:
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
            case PENTAGRAM:
                if (pentagram.isEmpty()) {
                    pentagram = makePentagram(pentagram);
                }
                width = endX - startX;
                height = endY - startY;
                for(CanvasPoint g : pentagram) {
                    points.add(new CanvasPoint(startX + width * g.getX(), startY + height * g.getY()));
                }
                break;
            case ISOGRAM:
                if (pentagram.isEmpty()) {
                    pentagram = makePentagram(pentagram);
                }
                width = endX - startX;
                height = endY - startY;
                maxLength = Math.max(Math.abs(width), Math.abs(height));
                for(CanvasPoint g : pentagram) {
                    points.add(new CanvasPoint(startX + maxLength * Math.signum(width) * g.getX(), startY + maxLength * Math.signum(height) * g.getY()));
                }
                break;
            case POLYGON:
            case SKETCH:
                break;
            case TEXT:
            case PICTURE:
            default:
                break;
        }
        return points;
    }

    /**
     * Record vertices of unit pentagram anti-clockwise
     * 
     * @return vertices of unit pentagram 
     */
    private List<CanvasPoint> makePentagram(List<CanvasPoint> gram) {
        double a = 2/(1 + Math.sqrt(5));
        double j = 1 - a * Math.cos(Math.PI/10);
        double l = a * Math.sin(Math.PI/10);

        double cx = 0.5d;
        double cy = a / 2 * (1 - j) / (l + a);
        double ex = l + a * (1 + j) / 2;
        double ey = 1 - j;
        double dx = (a * l * (j - 1) - 2 * (l + a) * (1 - l)) / (a * (j - 1) - 2 * (1 - l));
        double dy = -2 / a * dx + 2 * (l + a) / a;
        double fx = (1 + l - j) / 2;
        double fy = 1 - j;
        double gx = (a * (l + a) * (j - 1) - 2 * (l + a) * l) / (a * (j - 1) - 2 * (l + a));
        double gy = 2 / a * gx - 2 * l / a;

        // outer pairs
        CanvasPoint p = new CanvasPoint(l, 0);
        CanvasPoint q = new CanvasPoint(l + a, 0);
        CanvasPoint r = new CanvasPoint(1, 1 - j);
        CanvasPoint s = new CanvasPoint(0.5d, 1);
        CanvasPoint t = new CanvasPoint(0, 1 - j);

        // inner pairs
        CanvasPoint c = new CanvasPoint(cx, cy);
        CanvasPoint d = new CanvasPoint(dx, dy);
        CanvasPoint e = new CanvasPoint(ex, ey);
        CanvasPoint f = new CanvasPoint(fx, fy);
        CanvasPoint g = new CanvasPoint(gx, gy);

        // a = p--c--q--d--r--e--s--f--t--g--cycle;
        gram.add(p); // 0
        gram.add(c); // 1
        gram.add(q); // 2
        gram.add(d); // 3
        gram.add(r); // 4
        gram.add(e); // 5
        gram.add(s); // 6
        gram.add(f); // 7
        gram.add(t); // 8
        gram.add(g); // 9

        // reflection in line y = .5
        for(CanvasPoint z : gram) {
            z.y = 1 - z.y;
        }

        return gram;
    }

}
