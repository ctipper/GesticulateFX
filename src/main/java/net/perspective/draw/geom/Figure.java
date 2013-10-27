/*
 * Figure.java
 * 
 * Created on Oct 19, 2013 5:59:14 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class Figure {

    List<CanvasPoint> points;
    FigureType type;
    PathFactory factory;
    GeneralPath path;
    String color;
    double width;
    double angle;
    boolean closed;

    public Figure() {
        this.points = new ArrayList<>();
        this.factory = new FigurePathFactory();
        this.path = new GeneralPath();
        this.angle = 0;
        this.closed = false;
    }

    public void setPoints(List<CanvasPoint> points) {
        this.points = points;
    }

    public List<CanvasPoint> getPoints() {
        return this.points;
    }

    public void addPoint(CanvasPoint points) {
        this.points.add(points);
    }

    public void setType(FigureType t) {
        this.type = t;
    }

    public FigureType getType() {
        return this.type;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setStroke(double width) {
        this.width = width;
    }

    public double getStroke() {
        return this.width;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }

    public void setAngle(double a) {
        angle = a;
    }

    public double getAngle() {
        return angle;
    }

    public void setPath() {
        this.path = factory.createPath(this);
    }

    public boolean contains(double x, double y) {
        return path.intersects(x - 3, y - 3, 6, 6);
    }

    public void moveFigure(double xinc, double yinc) {
        for (CanvasPoint point : points) {
            point.translate(xinc, yinc);
        }
        this.setPath();
    }

    private CanvasPoint getTransform(CanvasPoint point) {
        CanvasPoint centre = this.rotationCentre();

        point.translate(-centre.x, -centre.y);
        if (this.getAngle() != 0) {
            // rotate shape about centroid
            point.rotate(this.getAngle());
        }
        point.translate(centre.x, centre.y);
        return point;
    }

    protected AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();
        AffineTransform translate = new AffineTransform();
        translate.setToTranslation(centre.x, centre.y);
        if (this.getAngle() != 0) {
            // rotate figure about centroid
            translate.rotate(this.getAngle());
        }
        translate.translate(-centre.x, -centre.y);
        return translate;
    }

    public CanvasPoint rotationCentre() {
        GeneralPath p = (GeneralPath) path.clone();
        p.closePath();
        Area area = new Area(p);
        Rectangle2D bound = area.getBounds2D();
        return new CanvasPoint(bound.getCenterX(), bound.getCenterY());
    }

    public void drawAnchors(GraphicsContext context) {
        if (!(this.type.equals(FigureType.SKETCH)
                || this.type.equals(FigureType.POLYGON))) {
            for (CanvasPoint point : points) {
                this.anchor(context, point.getX(), point.getY());
            }
        } else {
            CanvasPoint start = points.get(0);
            CanvasPoint end = points.get(points.size() - 1);
            this.anchor(context, start.getX(), start.getY());
            this.anchor(context, end.getX(), end.getY());
        }
    }

    protected void anchor(GraphicsContext context, double x, double y) {
        CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
        context.setLineWidth(1.0);
        context.setFill(Color.WHITE);
        context.fillOval(u.x-3, u.y-3, 6.0, 6.0);
        context.setStroke(Color.BLACK);
        context.strokeOval(u.x-3, u.y-3, 6.0, 6.0);
    }

    public void draw(GraphicsContext context) {
        AffineTransform at;
        double[] coords = {0, 0, 0, 0, 0, 0};

        at = this.getTransform();

        context.setStroke(Color.web(this.getColor()));
        context.setFill(Color.web(this.getColor()));
        context.setLineWidth(this.getStroke());
        context.setLineJoin(StrokeLineJoin.ROUND);
        context.setLineCap(StrokeLineCap.SQUARE);
        context.beginPath();
        PathIterator iterator = path.getPathIterator(at);
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    context.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    context.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    context.quadraticCurveTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    context.bezierCurveTo(coords[0], coords[1], coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    context.closePath();
                    break;
                default:
                    break;
            }
            iterator.next();
        }
        if (this.isClosed()) {
            context.fill();
        }
        context.stroke();
    }

    public void sketch(GraphicsContext context) {
        context.setStroke(Color.web(this.getColor()));
        context.setLineWidth(this.getStroke());
        context.beginPath();
        boolean start = true;
        for (CanvasPoint p : points) {
            if (start) {
                context.moveTo(p.getX(), p.getY());
                start = false;
            } else {
                context.lineTo(p.getX(), p.getY());
            }
        }
        if (closed) {
            context.closePath();
        }
        context.stroke();
    }
}
