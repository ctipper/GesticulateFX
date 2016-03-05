/*
 * Edge.java
 * 
 * Created on Mar 23, 2015 12:14:53 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.Shape;
import java.awt.geom.*;
import java.io.Serializable;
import java.util.ArrayList;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 *
 * @author ctipper
 */

public class Edge extends Figure implements Serializable {

    private static final long serialVersionUID = 1L;

    public Edge() {
        super();
    }
    
    public Edge(FigureType type) {
        super(type);
    }
    
    @Override
    public void setPoints(DrawingType drawtype) {
        if (this.type.equals(FigureType.LINE)) {
            this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
        } else {
            this.points = new ArrayList<>();
        }
    }

    @Override
    public void setEndPoints() {
        start = points.get(0);
        end = points.get(points.size() - 1);
    }

    @Override
    public void setPath() {
        this.path = pathfactory.createPath(this);
        switch (this.type) {
            case SKETCH:
            case LINE:
                this.setClosed(false);
                break;
            default:
                this.setClosed(true);
                break;
        }
    }

    @Override
    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY());
        s = new CanvasPoint[] { b, b };
        return s;
    }

    @Override
    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(), bound.getY());
        up = new CanvasPoint[] { b, b };
        return up;
    }

    @Override
    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY() + bound.getHeight());
        down = new CanvasPoint[] { b, b };
        return down;
    }

    @Override
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(),
            bound.getY() + bound.getHeight());
        e = new CanvasPoint[] { b, b };
        return e;
    }

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
        } else {
            center = new CanvasPoint(bound.getCenterX(), bound.getCenterY());
        }
        return center;
    }
    
    @Override
    public Shape bounds() {
        Shape bounds;
        Rectangle2D rectangle;

        if (type.equals(FigureType.LINE)) {
            // need to give line extent
            double length = V2.L2(new CanvasPoint(end.x - start.x, end.y - start.y));
            rectangle = new Rectangle2D.Double(-2.0, -2.0, length + 2.0, 4.0);
            double a = Math.atan2(end.y - start.y, end.x - start.x);

            Area area = new Area(rectangle);
            java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
            transform.setToTranslation(start.x, start.y);
            transform.rotate(a);
            area.transform(transform);
            bounds = area;
        } else {
            bounds = super.bounds();
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

}
