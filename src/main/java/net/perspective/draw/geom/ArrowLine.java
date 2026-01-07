/*
 * ArrowLine.java
 * 
 * Created on Feb 1, 2012, 9:20:29 PM
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
    private double wedgeAngle;  // Pointiness of arrow.
    private double outerRadius;
    private double innerRadius;

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>ArrowLine</code> */
    public ArrowLine() {
        this(0.25, 10, 9.3);
    }

    /**
     * Creates a new instance of <code>ArrowLine</code>
     * 
     * @param line the {@link net.perspective.draw.geom.Figure}
     */
    public ArrowLine(Figure line) {
        this(0.25, 10, 9.3);
        this.line = line;
    }

    /**
     * Creates a new instance of <code>ArrowLine</code>
     * 
     * @param line the {@link net.perspective.draw.geom.Figure}
     * @param arrowType the {@link net.perspective.draw.geom.ArrowType}
     */
    @ConstructorProperties({"line", "arrowType"})
    public ArrowLine(Figure line, ArrowType arrowType) {
        this(0.25, 10, 9.3);
        this.line = line;
        this.arrowType = arrowType;
    }

    /**
     * Creates a new instance of <code>ArrowLine</code>
     * 
     * @param angle the angle in radians
     * @param outerRadius outer radius
     * @param innerRadius inner radius
     */
    public ArrowLine(double angle, double outerRadius, double innerRadius) {
        this.wedgeAngle = angle;
        this.outerRadius = outerRadius;
        this.innerRadius = innerRadius;
    }

    /**
     * Set the line
     * 
     * @param line the {@link net.perspective.draw.geom.Figure}
     */
    public void setLine(Figure line) {
        this.line = line;
    }

    /**
     * Get the line
     * 
     * @return the {@link net.perspective.draw.geom.Figure}
     */
    public Figure getLine() {
        return line;
    }

    /**
     * Set the path and point factories used after de-serialisation
     * 
     * <p>This should method should almost never be called; it is needed by XML Reader.
     */
    @Override
    public void setFactory() {
        line.setFactory();
    }

    /**
     * Set the untransformed TL coordinate of the figure
     * 
     * @param x the x position
     * @param y the y position
     */
    @Override
    public void setStart(double x, double y) {
        line.setStart(x, y);
    }

    /**
     * Return the untransformed TL coordinate of the figure
     * 
     * @return the figure start point
     */
    @Override
    public CanvasPoint getStart() {
        return line.getStart();
    }

    /**
     * Set the untransformed BR coordinate of the figure
     * 
     * @param x the width
     * @param y the height
     */
    @Override
    public void setEnd(double x, double y) {
        line.setEnd(x, y);
    }

    /** 
     * Return the untransformed BR coordinate of the figure
     * 
     * @return the dimensions
     */
    @Override
    public CanvasPoint getEnd() {
        return line.getEnd();
    }

    /**
     * Set a list of points
     * 
     * @param points the list of {@link net.perspective.draw.util.CanvasPoint}
     */
    @Override
    public void setPoints(List<CanvasPoint> points) {
        line.setPoints(points);
    }

    /**
     * Initialise points List
     * 
     * @param drawtype the {@link net.perspective.draw.enums.DrawingType}
     */
    @Override
    public void setPoints(DrawingType drawtype) {
        line.setPoints(drawtype);
    }

    /**
     * Initialise end points
     */
    @Override
    public void setEndPoints() {
        line.setEndPoints();
    }

    /**
     * Returns points list
     * 
     * @return the list of {@link net.perspective.draw.util.CanvasPoint}
     */
    @Override
    public List<CanvasPoint> getPoints() {
        return line.getPoints();
    }

    /**
     * Add a point to the List of points
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    @Override
    public void addPoint(double x, double y) {
        line.addPoint(x, y);
    }

    /**
     * Set the path from a List of points
     */
    @Override
    @Transient
    public void setPath() {
        line.setPath();
    }

    /**
     * Returns the path describing the figure
     * 
     * @return path the {@link java.awt.geom.Path2D.Double}
     */
    @Override
    @Transient
    public Path2D.Double getPath() {
        return line.getPath();
    }

    /**
     * Returns the figure type
     * 
     * @return the {@link net.perspective.draw.geom.FigureType}
     */
    @Override
    public FigureType getType() {
        return line.getType();
    }

    /**
     * Sets the arrow type
     * 
     * @param arrowType the {@link net.perspective.draw.geom.ArrowType}
     */
    public void setArrowType(ArrowType arrowType) {
        this.arrowType = arrowType;
    }

    /**
     * Returns the arrow type
     * 
     * @return the {@link net.perspective.draw.geom.ArrowType}
     */
    public ArrowType getArrowType() {
        return arrowType;
    }

    /**
     * Sets the arrow stroke
     * 
     * @param stroke the {@link java.awt.Stroke}
     */
    public void setArrowStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Returns the arrow stroke
     * 
     * @return the {@link java.awt.Stroke}
     */
    public Stroke getArrowStroke() {
        return stroke;
    }

    /**
     * Return the type of stroke
     * 
     * @return the {@link java.awt.Stroke}
     */
    @Transient
    @Override
    public Stroke getStroke() {
        return line.getStroke();
    }

    /**
     * Update the item properties, such as color, stroke etc.
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     */
    @Override
    public void updateProperties(DrawingArea drawarea) {
        this.setColor(drawarea.getColor());
        this.setArrowStroke(drawarea.getPlainStroke());
        this.setArrowType(drawarea.getArrow());
        line.updateProperties(drawarea);
    }

    /**
     * 
     * @return The 2-tuple of top-left corner location (transformed)
     *         second point may be normalised
     */
    @Override
    public CanvasPoint[] getTop() {
        return line.getTop();
    }

    /**
     * 
     * @return The 2-tuple of top-right corner location (transformed)
     *         second point may be normalised
     */
    @Override
    public CanvasPoint[] getUp() {
        return line.getUp();
    }

    /**
     * 
     * @return The 2-tuple of bottom-left corner location (transformed)
     *         second point may be normalised
     */
    @Override
    public CanvasPoint[] getDown() {
        return line.getDown();
    }

    /**
     * 
     * @return The 2-tuple of bottom-right corner location (transformed)
     *         second point may be normalised
     */
    @Override
    public CanvasPoint[] getBottom() {
        return line.getBottom();
    }

    /**
     * Returns the location of the item centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    @Override
    public CanvasPoint rotationCentre() {
        return line.rotationCentre();
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed {@link java.awt.Shape}
     */
    @Override
    public Shape bounds() {
        return line.bounds();
    }

    /**
     * Detect if a point lies within the bounds, a convenience method
     * 
     * @param x canvas coordinate
     * @param y canvas coordinate
     * @return a boolean property
     */
    @Override
    public boolean contains(double x, double y) {
        return line.contains(x, y);
    }

    /**
     * Translate the item
     * 
     * @param xinc x increment
     * @param yinc y increment
     */
    @Override
    public void moveTo(double xinc, double yinc) {
        line.moveTo(xinc, yinc);
    }

    /**
     * Provide a Path for FX canvas
     * 
     * @return the {@link javafx.scene.Node}
     */
    @Override
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
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * Render the item anchors to indicate selection
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     * @return the {@link javafx.scene.Node}
     */
    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        Node anchors = line.drawAnchors(drawarea);
        return anchors;
    }

    /**
     * Draw to a Java2d canvas for export
     * 
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
     */
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
     * @return the list of {@link net.perspective.draw.util.CanvasPoint}
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
     * @return the list of {@link net.perspective.draw.util.CanvasPoint}
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

    /**
     * Set stroke colour
     * 
     * @param color the {@link javafx.scene.paint.Color}
     */
    @Override
    public void setColor(Color color) {
        line.setColor(color);
    }

    /**
     * Return stroke colour
     * 
     * @return colour the {@link javafx.scene.paint.Color}
     */
    @Override
    @Transient
    public Color getColor() {
        return line.getColor();
    }

    @Deprecated
    @Override
    public void setColor(java.awt.Color color) {
        line.setColor(awtToFx(color));
    }

    @Deprecated
    @Override
    public java.awt.Color getAwtColor() {
        return fxToAwt(line.getColor());
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor the {@link javafx.scene.paint.Color}
     */
    @Override
    public void setFillColor(Color fillcolor) {
        line.setFillColor(fillcolor);
    }

    /**
     * Return the fill colour
     * 
     * @return the {@link javafx.scene.paint.Color}
     */
    @Transient
    @Override
    public Color getFillColor() {
        return line.getFillColor();
    }

    @Deprecated
    @Override
    public void setFillColor(java.awt.Color fillcolor) {
        line.setFillColor(awtToFx(fillcolor));
    }

    @Deprecated
    @Override
    public java.awt.Color getAwtFillColor() {
        return fxToAwt(line.getFillColor());
    }

    /**
     * Sets whether the shape is opaque
     * 
     * @param transparency 0 (clear) - 100 (opaque)
     */
    @Override
    public void setTransparency(int transparency) {
        line.setTransparency(transparency);
    }

    /**
     * Returns transparency
     * 
     * @return transparency 0 (clear) - 100 (opaque)
     */
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

    /**
     * Sets the rotation angle
     * 
     * @param angle the angle in radians
     */
    @Override
    public void setAngle(double angle) {
        line.setAngle(angle);
    }

    /**
     * Return the rotation angle 
     * 
     * @return angle the angle in radians
     */
    @Override
    @Transient
    public double getAngle() {
        return line.getAngle();
    }

    /**
     * Return a List of 2-tuples of vertices, second point normalised.
     * Note that the points may not be cyclical.
     * 
     * @return a List of 2-tuples representing transformed vertices
     */
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
