/*
 * Edge.java
 * 
 * Created on Mar 23, 2015 12:14:53 PM
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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

import java.awt.Shape;
import java.awt.geom.*;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 * 
 * @author ctipper
 */

public class Edge extends Figure {

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>Edge</code> */
    public Edge() {
        super();
    }

    /**
     * Creates a new instance of <code>Edge</code>
     * 
     * @param type the FigureType
     */
    @ConstructorProperties({"type"})
    public Edge(FigureType type) {
        super(type);
    }

    /**
     * Initialise points List
     * 
     * @param drawtype
     */
    @Override
    public void setPoints(DrawingType drawtype) {
        if (this.type.equals(FigureType.LINE)) {
            this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
        } else {
            this.points = new ArrayList<>();
        }
    }

    /**
     * Initialise end points
     */
    @Override
    public void setEndPoints() {
        start = points.get(0);
        end = points.get(points.size() - 1);
    }

    /**
     * Set the path from a List of points
     */
    @Override
    public void setPath() {
        this.path = pathfactory.createPath(this);
        switch (this.type) {
            case POLYGON -> this.setClosed(true);
            default -> this.setClosed(false);
        }
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point may be normalised
     * 
     * @return the 2-tuple of top-left corner location (transformed)
     */
    @Override
    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY());
        s = new CanvasPoint[] { b, b };
        return s;
    }

    /**
     * Returns the 2-tuple of top-right corner location (transformed)
     * the second point may be normalised
     * 
     * @return the 2-tuple of top-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(), bound.getY());
        up = new CanvasPoint[] { b, b };
        return up;
    }

    /**
     * Returns the 2-tuple of bottom-left corner location (transformed)
     * the second point may be normalised
     * 
     * @return the 2-tuple of bottom-left corner location (transformed)
     */
    @Override
    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY() + bound.getHeight());
        down = new CanvasPoint[] { b, b };
        return down;
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * second point may be normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(),
            bound.getY() + bound.getHeight());
        e = new CanvasPoint[] { b, b };
        return e;
    }

    /**
     * Returns the location of the figure centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    @Override
    public CanvasPoint rotationCentre() {
        CanvasPoint center;
        Path2D.Double pa = (Path2D.Double) this.getPath().clone();
        pa.closePath();
        Area area = new Area(pa);
        Rectangle2D bound = area.getBounds2D();
        if (bound.getBounds2D().getWidth() < 4.0
            && bound.getBounds2D().getHeight() < 4.0) {
            center = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
        } else if (points.size() < 3) {
            center = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
        } else {
            center = new CanvasPoint(bound.getCenterX(), bound.getCenterY());
        }
        return center;
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    @Override
    public java.awt.Shape bounds() {
        Shape bounds;
        Rectangle2D rectangle;

        if (type.equals(FigureType.LINE)) {
            // to give line extent
            double length = V2.L2(new CanvasPoint(end.x - start.x, end.y - start.y));
            rectangle = new Rectangle2D.Double(-2.0, -2.0, length + 4.0, 4.0);
            double a = Math.atan2(end.y - start.y, end.x - start.x);

            Area area = new Area(rectangle);
            java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
            transform.setToTranslation(start.x, start.y);
            transform.rotate(a);
            area.transform(transform);
            bounds = area;
        } else {
            bounds = super.bounds();
            // to give zero bound shapes extent
            if (bounds.getBounds2D().getWidth() < 4.0
                && bounds.getBounds2D().getHeight() < 4.0) {
                rectangle = new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0);
                Area area = new Area(rectangle);
                AffineTransform transform = new AffineTransform();
                transform.setToTranslation(start.x, start.y);
                area.transform(transform);
                bounds = area;
            }
        }
        return bounds;
    }

    /**
     * Return the rotation angle
     * 
     * @return angle the angle in radians
     */
    @Override
    public double getAngle() {
        if (!this.getType().equals(FigureType.LINE)) {
            return angle;
        } else {
            CanvasPoint centre = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
            CanvasPoint A = new CanvasPoint(1, 0);
            CanvasPoint B = new CanvasPoint(end.x - centre.x, end.y - centre.y);

            double h1 = V2.L2(A);
            CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
            double h2 = V2.L2(B);
            CanvasPoint q2 = new CanvasPoint(B.x / h2, B.y / h2);

            double cos_t = V2.dot(q1, q2);
            double sin_t = V2.dot(V2.rot90(q1), q2);

            double theta = Math.atan2(sin_t, cos_t);

            return theta;
        }
    }
}
