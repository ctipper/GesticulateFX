/*
 * ArrowLine.java
 * 
 * Created on Feb 1, 2012, 9:20:29 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;

/**
 * 
 * @author ctipper
 */

public class ArrowLine extends Figure {

    public Figure line;
    public ArrowType arrowType;
    /**
     * Pointiness of arrow.
     */
    private double wedgeAngle;
    private double outerRadius;
    private double innerRadius;

    private static final long serialVersionUID = 1L;

    public ArrowLine() {
        this(0.25, 10, 9.3);
    }

    public ArrowLine(Figure line) {
        this(0.25, 10, 9.3);
        this.line = line;
    }

    @ConstructorProperties({"line", "arrowType"})
    public ArrowLine(Figure line, ArrowType arrowType) {
        this(0.25, 10, 9.3);
        this.line = line;
        this.arrowType = arrowType;
    }

    public ArrowLine(double angle, double outerRadius, double innerRadius) {
        this.wedgeAngle = angle;
        this.outerRadius = outerRadius;
        this.innerRadius = innerRadius;
    }

    public void setLine(Figure line) {
        this.line = line;
    }

    public Figure getLine() {
        return line;
    }

    @Override
    public void setFactory() {
        // This should method should almost never be called. Needed by XMLEncoder.
        line.setFactory();
    }

    @Override
    public void setStart(double x, double y) {
        line.setStart(x, y);
    }

    @Override
    public CanvasPoint getStart() {
        return line.getStart();
    }

    @Override
    public void setEnd(double x, double y) {
        line.setEnd(x, y);
    }

    @Override
    public CanvasPoint getEnd() {
        return line.getEnd();
    }

    @Override
    public void setPoints(List<CanvasPoint> points) {
        line.setPoints(points);
    }

    @Override
    public void setPoints(DrawingType drawtype) {
        line.setPoints(drawtype);
    }

    @Override
    public void setEndPoints() {
        line.setEndPoints();
    }

    @Override
    public List<CanvasPoint> getPoints() {
        return line.getPoints();
    }

    @Override
    public void addPoint(double x, double y) {
        line.addPoint(x, y);
    }

    @Override
    public FigureType getType() {
        return line.getType();
    }

    public void setArrowType(ArrowType arrowType) {
        this.arrowType = arrowType;
    }

    public ArrowType getArrowType() {
        return arrowType;
    }

    public void setArrowStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getArrowStroke() {
        return stroke;
    }

    @Transient
    public Stroke getStroke() {
        return line.getStroke();
    }

    @Override
    @Transient
    public void setPath() {
        line.setPath();
    }

    @Override
    @Transient
    public Path2D.Double getPath() {
        return line.getPath();
    }

    @Override
    public void updateProperties(DrawingArea drawarea) {
        this.setColor(drawarea.getColor());
        this.setArrowStroke(drawarea.getPlainStroke());
        this.setArrowType(drawarea.getArrow());
        line.updateProperties(drawarea);
    }

    @Override
    public CanvasPoint[] getTop() {
        return line.getTop();
    }

    @Override
    public CanvasPoint[] getUp() {
        return line.getUp();
    }

    @Override
    public CanvasPoint[] getDown() {
        return line.getDown();
    }

    @Override
    public CanvasPoint[] getBottom() {
        return line.getBottom();
    }

    @Override
    public CanvasPoint rotationCentre() {
        return line.rotationCentre();
    }

    @Override
    public Shape bounds() {
        return line.bounds();
    }

    @Override
    public boolean contains(double x, double y) {
        return line.contains(x, y);
    }

    @Override
    public void moveShape(double xinc, double yinc) {
        line.moveShape(xinc, yinc);
    }

    /**
     * Provide a Path for FX canvas
     * 
     * @return an FX Path
     */
    public Node draw() {
        java.awt.geom.AffineTransform at;
        Group group = new Group();

        group.getChildren().add(line.draw());

        at = this.getTransform();

        Path2D.Double e = getTransformedEndDecoratorPath();
        Path fxpath = drawPath(e, at);
        fxpath.setStroke(getColor());
        fxpath.setStrokeWidth(getLineWidth((BasicStroke) getStroke()));
        fxpath.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        fxpath.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        fxpath.setFill(getColor());
        group.getChildren().add(fxpath);
        if (getArrowType() == ArrowType.BOTH) {
            Path2D.Double s = getTransformedStartDecoratorPath();
            Path fxpath_2 = drawPath(s, at);
            fxpath_2.setStroke(getColor());
            fxpath_2.setStrokeWidth(getLineWidth((BasicStroke) getStroke()));
            fxpath_2.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            fxpath_2.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            fxpath_2.setFill(getColor());
            group.getChildren().add(fxpath_2);
        }
        group.setCursor(javafx.scene.Cursor.OPEN_HAND);
        return group;
    }

    @Override
    public void draw(Graphics2D g2) {
        AffineTransform defaultTransform, transform;

        line.draw(g2);

        defaultTransform = g2.getTransform();

        transform = this.getTransform();
        g2.transform(transform);

        Path2D.Double e = getTransformedEndDecoratorPath();
        g2.setColor(fxToAwt(getColor()));
        g2.fill(e);
        g2.setStroke(getArrowStroke());
        g2.draw(e);

        if (getArrowType() == ArrowType.BOTH) {
            Path2D.Double s = getTransformedStartDecoratorPath();
            g2.setColor(fxToAwt(getColor()));
            g2.fill(s);
            g2.setStroke(getArrowStroke());
            g2.draw(s);
        }

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        Node anchors = line.drawAnchors(drawarea);
        return anchors;
    }

    private Path2D.Double getTransformedStartDecoratorPath() {
        Path2D.Double decorator = getDecoratorPath();
        double strokeWidth = (double) ((BasicStroke) getArrowStroke()).getLineWidth();
        CanvasPoint p1 = getP1();

        AffineTransform transform = new AffineTransform();
        transform.translate(line.getStart().getX(), line.getStart().getY());
        transform.rotate(Math.atan2(line.getStart().getX() - p1.x, p1.y - line.getStart().getY()));
        if (strokeWidth > 1f) {
            transform.scale(1d + (strokeWidth - 1d) / 2d, 1d + (strokeWidth - 1d) / 2d);
        }
        decorator.transform(transform);

        return decorator;
    }

    private Path2D.Double getTransformedEndDecoratorPath() {
        Path2D.Double decorator = getDecoratorPath();
        double strokeWidth = (double) ((BasicStroke) getArrowStroke()).getLineWidth();
        CanvasPoint p2 = getP2();

        AffineTransform transform = new AffineTransform();
        transform.translate(line.getEnd().getX(), line.getEnd().getY());
        transform.rotate(Math.atan2(line.getEnd().getX() - p2.x, p2.y - line.getEnd().getY()));
        if (strokeWidth > 1f) {
            transform.scale(1d + (strokeWidth - 1d) / 2d, 1d + (strokeWidth - 1d) / 2d);
        }
        decorator.transform(transform);

        return decorator;
    }

    private Path2D.Double getDecoratorPath() {
        double offset = 0;

        Path2D.Double decorator = new Path2D.Double();
        decorator.moveTo(outerRadius * Math.sin(-wedgeAngle), offset + outerRadius * Math.cos(-wedgeAngle));
        decorator.lineTo(0, offset);
        decorator.lineTo(outerRadius * Math.sin(wedgeAngle), offset + outerRadius * Math.cos(wedgeAngle));
        if (innerRadius != 0) {
            decorator.lineTo(0, innerRadius + offset);
            decorator.closePath();
        }

        return decorator;
    }

    /**
     * reference for start of line
     * 
     * @return reference point
     */
    private CanvasPoint getP1() {
        CanvasPoint point;

        if (line.getType().equals(FigureType.LINE)) {
            point = new CanvasPoint(line.getEnd().getX(), line.getEnd().getY());
        } else {
            List<CanvasPoint> coords = P_1();
            int size = coords.size();
            point = new CanvasPoint(coords.get(size - 1).x, coords.get(size - 1).y);
        }
        return point;
    }

    /**
     * reference for end of line
     * 
     * @return reference point
     */
    private CanvasPoint getP2() {
        CanvasPoint point;

        if (line.getType().equals(FigureType.LINE)) {
            point = new CanvasPoint(line.getStart().getX(), line.getStart().getY());
        } else {
            List<CanvasPoint> coords = P_2();
            int size = coords.size();
            if (size > 2) {
                // retrieve penultimate coord
                point = new CanvasPoint(coords.get(size - 2).x, coords.get(size - 2).y);
            } else {
                // line start
                point = new CanvasPoint(coords.get(0).x, coords.get(0).y);
            }
        }
        return point;
    }

    /**
     * Collect first two points
     * 
     * @return list of points
     */
    private List<CanvasPoint> P_1() {
        CanvasPoint point;
        double[] pt1 = { 0, 0, 0, 0, 0, 0 };
        List<CanvasPoint> P1 = new ArrayList<>();

        Path2D.Double p = this.getPath();
        PathIterator iterator = p.getPathIterator(new AffineTransform());
        for (int i = 0; i < 2; i++) {
            if (!iterator.isDone()) {
                iterator.currentSegment(pt1);
                point = new CanvasPoint(pt1[0], pt1[1]);
                P1.add(point);
                iterator.next();
            }
        }
        return P1;
    }

    /**
     * Collect last three points
     * 
     * @return list of points
     */
    private List<CanvasPoint> P_2() {
        CanvasPoint point;
        double[] pt1 = { 0, 0, 0, 0, 0, 0 };
        List<CanvasPoint> P2 = new ArrayList<>();

        Path2D.Double p = this.getPath();
        PathIterator iterator = p.getPathIterator(new AffineTransform());
        int i = 0;
        while (!iterator.isDone()) {
            iterator.currentSegment(pt1);
            point = new CanvasPoint(pt1[0], pt1[1]);
            if (i < 3) {
                P2.add(point);
            } else {
                P2.add(point);
                P2.remove(0);
            }
            i++;
            iterator.next();
        }
        return P2;
    }

    @Override
    public void setColor(Color color) {
        line.setColor(color);
    }

    @Override
    @Transient
    public Color getColor() {
        return line.getColor();
    }

    @Deprecated
    public void setColor(java.awt.Color color) {
        line.setColor(awtToFx(color));
    }

    @Deprecated
    public java.awt.Color getAwtColor() {
        return fxToAwt(line.getColor());
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor
     */
    public void setFillColor(Color fillcolor) {
        line.setFillColor(fillcolor);
    }

    /**
     * Return the fill colour
     * 
     * @return
     */
    @Transient
    public Color getFillColor() {
        return line.getFillColor();
    }

    @Deprecated
    public void setFillColor(java.awt.Color fillcolor) {
        line.setFillColor(awtToFx(fillcolor));
    }

    @Deprecated
    public java.awt.Color getAwtFillColor() {
        return fxToAwt(line.getFillColor());
    }

    @Override
    public void setTransparency(int transparency) {
        line.setTransparency(transparency);
    }

    @Override
    @Transient
    public int getTransparency() {
        return line.getTransparency();
    }

    @Override
    @Deprecated
    public void setVertical(boolean isVertical) {
        line.setVertical(isVertical);
    }

    @Override
    @Deprecated
    @Transient
    public boolean isVertical() {
        return line.isVertical();
    }

    @Override
    public void setAngle(double angle) {
        line.setAngle(angle);
    }

    @Override
    @Transient
    public double getAngle() {
        return line.getAngle();
    }

    @Override
    public List<CanvasPoint[]> getVertices() {
        return line.getVertices();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.setArrowStroke(readStroke(in));
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
        writeStroke(this.getArrowStroke(), out);
    }

}
