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
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import net.perspective.draw.enums.ContainsType;
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
    
    public Edge(FigureType t) {
        super(t);
    }
    
    public void setPoints(DrawingType drawtype) {
        if (this.type.equals(FigureType.LINE)) {
            this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
        } else {
            this.points = new ArrayList<>();
        }
    }

    public void setEndPoints() {
        start = points.get(0);
        end = points.get(points.size() - 1);
    }

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

    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY());
        s = new CanvasPoint[] { b, b };
        return s;
    }

    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(), bound.getY());
        up = new CanvasPoint[] { b, b };
        return up;
    }

    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY() + bound.getHeight());
        down = new CanvasPoint[] { b, b };
        return down;
    }

    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(),
            bound.getY() + bound.getHeight());
        e = new CanvasPoint[] { b, b };
        return e;
    }

    public CanvasPoint rotationCentre() {
        GeneralPath pa = (GeneralPath) this.getPath().clone();
        pa.closePath();
        Area area = new Area(pa);
        Rectangle2D bound = area.getBounds2D();
        return new CanvasPoint(bound.getCenterX(), bound.getCenterY());
    }
    
    public Shape bounds() {
        Shape bounds;

        if (type.equals(FigureType.LINE)) {
            // need to give line extent
            double length = V2.L2(new CanvasPoint(end.x - start.x, end.y - start.y));
            Rectangle2D rectangle = new Rectangle2D.Double(0, -3.0, length, 6.0);
            double a = Math.atan2(end.y - start.y, end.x - start.x);

            Area area = new Area(rectangle);
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(start.x, start.y);
            transform.rotate(a);
            area.transform(transform);
            bounds = area;
        } else {
            bounds = super.bounds();
        }
        return bounds;
    }

    /*
     * Return 2-point array of vertices, second point normalized.
     * Note that the points may not be cyclical.
     */
    public List<CanvasPoint[]> getVertices() {
        List<CanvasPoint[]> vert = new ArrayList<>();
        List<CanvasPoint[]> vertices = new ArrayList<>();
        vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(start.x, start.y) });
        vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(end.x, end.y) });
        for (CanvasPoint[] p : vert) {
            CanvasPoint[] point = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
            vertices.add(point);
        }
        return vertices;
    }

}
