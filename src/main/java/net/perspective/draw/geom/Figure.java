/*
 * Figure.java
 * 
 * Created on Oct 19, 2013 5:59:14 PM
 * 
 */
package net.perspective.draw.geom;

import gwt.awt.Shape;
import gwt.awt.geom.*;
import java.io.IOException;
import java.io.Serializable;
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

public class Figure implements Serializable {

    List<CanvasPoint> points;
    FigureType type;
    transient GeneralPath path;
    transient PathFactory factory;
    String color;
    double width;
    double angle;
    private boolean closed;
    private static final long serialVersionUID = 1L;

    public Figure() {
        this.angle = 0;
        this.closed = false;
    }

    public Figure(FigureType t) {
        this();
        this.type = t;
        this.points = new ArrayList<>();
        this.factory = new FigurePathFactory();
        this.path = new GeneralPath();
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
        this.angle = a;
    }

    public double getAngle() {
        return this.angle;
    }

    public void setPath() {
        this.path = factory.createPath(this);
        switch (this.type) {
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
            case POLYGON:
                this.setClosed(true);
                break;
            default:
                this.setClosed(false);
                break;
        }
    }

    public GeneralPath getPath() {
        return this.path;
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
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(centre.x, centre.y);
        if (this.getAngle() != 0) {
            // rotate figure about centroid
            transform.rotate(this.getAngle());
        }
        transform.translate(-centre.x, -centre.y);
        return transform;
    }

    public CanvasPoint rotationCentre() {
        GeneralPath p = (GeneralPath) this.getPath().clone();
        p.closePath();
        Area area = new Area(p);
        Rectangle2D bound = area.getBounds2D();
        return new CanvasPoint(bound.getCenterX(), bound.getCenterY());
    }

    public Shape bounds() {
        AffineTransform trans = this.getTransform();
        GeneralPath p = (GeneralPath) this.getPath().clone();
        p.transform(trans);
        return p;
    }

    public boolean contains(double x, double y) {
        return this.bounds().intersects(x - 5, y - 5, 10, 10);
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

        at = this.getTransform();

        context.setStroke(Color.web(this.getColor()));
        context.setFill(Color.web(this.getColor()));
        context.setLineWidth(this.getStroke());
        context.setLineJoin(StrokeLineJoin.ROUND);
        context.setLineCap(StrokeLineCap.SQUARE);
        this.drawPath(context, at);
        if (this.isClosed()) {
            context.closePath();
            context.fill();
        }
        context.stroke();
    }

    public void sketch(GraphicsContext context) {
        context.setStroke(Color.LIGHTGREY);
        context.setLineWidth(this.getStroke());
        context.setLineJoin(StrokeLineJoin.ROUND);
        context.setLineCap(StrokeLineCap.SQUARE);
        this.drawPath(context, new AffineTransform());
        if (this.isClosed()) {
            context.closePath();
        }
        context.stroke();
    }
    
    private void drawPath(GraphicsContext context, AffineTransform at) {
        double[] coords = {0, 0, 0, 0, 0, 0};
        context.beginPath();
        PathIterator iterator = this.getPath().getPathIterator(at);
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
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.factory = new FigurePathFactory();
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }
}
