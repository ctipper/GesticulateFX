/*
 * Figure.java
 * 
 * Created on Oct 19, 2013 5:59:14 PM
 * 
 */

/**
 * Copyright (c) 2022 Christopher Tipper
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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;

/**
 * A stroked vector shape defined by a list of points and a property
 * 
 * @author ctipper
 */

public class Figure implements DrawItem, Serializable {

    protected List<CanvasPoint> points;
    protected CanvasPoint start, end;       // start, end are _untransformed_ canvas coordinates of TL/BR corners
    protected FigureType type;
    protected transient Path2D.Double path;
    protected transient PointFactory pointfactory;
    protected transient PathFactory pathfactory;
    protected transient Stroke stroke;
    private transient Color color, fillcolor;
    private int transparency;
    private boolean isVertical;
    protected double angle;
    private boolean closed;                 // closed indicates to draw() whether figure should be filled

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>Figure</code> */
    public Figure() {
        this.type = FigureType.NONE;
        this.transparency = 0;
        this.angle = 0;
        this.closed = false;
    }

    /**
     * Creates a new instance of <code>Figure</code>
     * 
     * @param type the {@link net.perspective.draw.geom.FigureType}
     */
    @ConstructorProperties({"type"})
    public Figure(FigureType type) {
        this();
        this.type = type;
        this.points = new ArrayList<>();
        this.pathfactory = new FigurePathFactory();
        this.pointfactory = new FigurePointFactory();
        this.path = new Path2D.Double();
    }

    /**
     * Set the path and point factories used after de-serialisation
     * 
     * <p>This should method should almost never be called; it is needed by XML Reader.
     */
    public void setFactory() {
        this.pointfactory = new FigurePointFactory();
        this.pathfactory = new FigurePathFactory();
    }

    /**
     * Set the untransformed TL coordinate of the figure
     * 
     * @param x the x position
     * @param y the y position
     */
    @Override
    public void setStart(double x, double y) {
        if (start == null) {
            start = new CanvasPoint();
        }
        start.setLocation(x, y);
    }

    /**
     * Return the untransformed TL coordinate of the figure
     * 
     * @return the figure start point
     */
    @Override
    public CanvasPoint getStart() {
        return start;
    }

    /**
     * Set the untransformed BR coordinate of the figure
     * 
     * @param x the width
     * @param y the height
     */
    @Override
    public void setEnd(double x, double y) {
        if (end == null) {
            end = new CanvasPoint();
        }
        end.setLocation(x, y);
    }

    /** 
     * Return the untransformed BR coordinate of the figure
     * 
     * @return the dimensions
     */
    @Override
    public CanvasPoint getEnd() {
        return end;
    }

    /**
     * Set a list of points
     * 
     * @param points the list of {@link net.perspective.draw.util.CanvasPoint}
     */
    public void setPoints(List<CanvasPoint> points) {
        this.points = points;
    }

    /**
     * Initialise points List
     * 
     * @param drawtype the {@link net.perspective.draw.enums.DrawingType}
     */
    public void setPoints(DrawingType drawtype) {
        this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
    }

    /**
     * Initialise end points
     */
    public void setEndPoints() {
        switch (this.type) {
            case CIRCLE, SQUARE -> {
                start = new CanvasPoint(points.get(0).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
            }
            case TRIANGLE -> {
                start = new CanvasPoint(points.get(1).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
            }
            case HEXAGON -> {
                start = new CanvasPoint(points.get(1).x, points.get(0).y);
                end = new CanvasPoint(points.get(4).x, points.get(3).y);
            }
            default -> {
                start = points.get(0);
                end = points.get(points.size() - 1);
            }
        }
    }

    /**
     * Returns points list
     * 
     * @return the list of {@link net.perspective.draw.util.CanvasPoint}
     */
    public List<CanvasPoint> getPoints() {
        return this.points;
    }

    /**
     * Add a point to the List of points
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(double x, double y) {
        this.points.add(new CanvasPoint(x, y));
    }

    /**
     * Set the path from a List of points
     */
    @Transient
    public void setPath() {
        this.path = pathfactory.createPath(this);
        this.setClosed(true);
    }

    /**
     * Returns the path describing the figure
     * 
     * @return path the {@link java.awt.geom.Path2D.Double}
     */
    @Transient
    public Path2D.Double getPath() {
        return this.path;
    }

    /**
     * Sets the figure type
     * 
     * @param type the {@link net.perspective.draw.geom.FigureType}
     */
    public void setType(FigureType type) {
        this.type = type;
    }

    /**
     * Returns the figure type
     * 
     * @return the {@link net.perspective.draw.geom.FigureType}
     */
    public FigureType getType() {
        return this.type;
    }

    /**
     * Update the figure properties, such as color, stroke etc.
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     */
    @Override
    public void updateProperties(DrawingArea drawarea) {
        this.setColor(drawarea.getColor());
        this.setFillColor(drawarea.getFillColor());
        this.setStroke(drawarea.getStroke());
        this.setTransparency(drawarea.getTransparency());
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
        CanvasPoint[] p = getVertex(ContainsType.TL);
        s = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
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
        CanvasPoint[] p = getVertex(ContainsType.TR);
        up = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
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
        CanvasPoint[] p = getVertex(ContainsType.BL);
        down = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return down;
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * the second point may be normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        CanvasPoint[] p = getVertex(ContainsType.BR);
        e = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return e;
    }

    protected CanvasPoint getTransform(CanvasPoint point) {
        CanvasPoint centre = this.rotationCentre();

        point.translate(-centre.x, -centre.y);
        if (this.getAngle() != 0 && !this.getType().equals(FigureType.LINE)) {
            // rotate point about centroid
            point.rotate(this.getAngle());
        }
        if (this.isVertical()) {
            // 90 degree positive rotation
            point.rotate(-Math.PI / 2);
        }
        point.translate(centre.x, centre.y);
        return point;
    }

    /**
     * rotate Figure about centroid with optional 90 deg correction
     * 
     * @return the {@link java.awt.geom.AffineTransform}
     */
    protected java.awt.geom.AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();
        java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
        transform.setToRotation((!this.getType().equals(FigureType.LINE) ? this.getAngle() : 0) + (this.isVertical() ? -Math.PI / 2 : 0), centre.x, centre.y);
        return transform;
    }


    /**
     * Returns the location of the figure centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    @Override
    public CanvasPoint rotationCentre() {
        CanvasPoint p = new CanvasPoint();
        double x, y, w, h;
        if (start.x < end.x && start.y < end.y) {
            x = start.x;
            y = start.y;
            w = end.x - start.x;
            h = end.y - start.y;
            p = new CanvasPoint(x + w / 2, y + h / 2);
        } else if (start.x >= end.x && start.y >= end.y) {
            x = end.x;
            y = end.y;
            w = start.x - end.x;
            h = start.y - end.y;
            p = new CanvasPoint(x + w / 2, y + h / 2);
        } else if (start.x >= end.x && start.y < end.y) {
            x = end.x;
            y = start.y;
            w = start.x - end.x;
            h = end.y - start.y;
            p = new CanvasPoint(x + w / 2, y + h / 2);
        } else if (start.x < end.x && start.y >= end.y) {
            x = start.x;
            y = end.y;
            w = end.x - start.x;
            h = start.y - end.y;
            p = new CanvasPoint(x + w / 2, y + h / 2);
        }
        return p;
    }

    /**
     * Return the 2D bounding box
     * 
     * @return 2D bounding box for figure
     */
    protected Rectangle2D getBounds2D() {
        java.awt.Shape area = this.bounds();
        Rectangle2D bound = area.getBounds2D();
        return bound;
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    @Override
    public java.awt.Shape bounds() {
        // Get transformed path
        java.awt.geom.AffineTransform transform = this.getTransform();
        Path2D.Double p = (Path2D.Double) this.getPath().clone();
        p.transform(transform);
        return p;
    }

    /**
     * Detect if a point lies within the bounds, a convenience method
     * 
     * @param x  canvas coordinate
     * @param y  canvas coordinate
     * @return a boolean property
     */
    @Override
    public boolean contains(double x, double y) {
        if (this.type.equals(FigureType.NONE)) {
            return false;
        } else {
            return this.bounds().intersects(x - 5, y - 5, 10, 10);
        }
    }

    /**
     * Translate the figure
     * 
     * @param xinc  x increment
     * @param yinc  y increment
     */
    @Override
    public void moveTo(double xinc, double yinc) {
        points.stream().forEach((p) -> {
            p.translate(xinc, yinc);
        });
        this.setEndPoints();
        this.setPath();
    }

    /**
     * Provide a Path for FX canvas
     * 
     * @return the {@link javafx.scene.Node}
     */
    @Override
    public Node draw() {
        java.awt.geom.AffineTransform at;

        at = this.getTransform();
        Path fxpath = drawPath(this.getPath(), at);
        fxpath.setStroke(getColor());
        fxpath.setStrokeWidth(getLineWidth((BasicStroke) getStroke()));
        fxpath.setStrokeLineJoin(getLineJoin((BasicStroke) getStroke()));
        fxpath.setStrokeLineCap(getLineCap((BasicStroke) getStroke()));
        fxpath.getStrokeDashArray().addAll(getDashes(((BasicStroke) getStroke()).getDashArray()));
        double dashOffset = Float.valueOf(((BasicStroke) getStroke()).getDashPhase()).doubleValue();
        fxpath.setStrokeDashOffset(dashOffset);
        if (this.isClosed()) {
            Color alphafill = Color.color(getFillColor().getRed(), 
                getFillColor().getGreen(), 
                getFillColor().getBlue(),
                ((double) getTransparency()) / 100);
            fxpath.setFill(alphafill);
        }
        fxpath.setCursor(Cursor.OPEN_HAND);
        return fxpath;
    }

    private List<Double> getDashes(float[] items) {
        List<Double> dashes = new ArrayList<>();
        if (items == null) {
            return dashes;
        }
        for (int i=0; i < items.length; i++) {
            dashes.add(Float.valueOf(items[i]).doubleValue());
        }
        return dashes;
    }

    protected Path drawPath(Path2D.Double path, java.awt.geom.AffineTransform at) {
        double[] coords = {0, 0, 0, 0, 0, 0};
        Path fxpath = new Path();

        PathIterator iterator = path.getPathIterator(at);
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                MoveTo moveTo = new MoveTo();
                moveTo.setX(coords[0]);
                moveTo.setY(coords[1]);
                fxpath.getElements().add(moveTo);
                break;
            case PathIterator.SEG_LINETO:
                LineTo lineTo = new LineTo();
                lineTo.setX(coords[0]);
                lineTo.setY(coords[1]);
                fxpath.getElements().add(lineTo);
                break;
            case PathIterator.SEG_QUADTO:
                QuadCurveTo quadCurveTo = new QuadCurveTo();
                quadCurveTo.setX(coords[2]);
                quadCurveTo.setY(coords[3]);
                quadCurveTo.setControlX(coords[0]);
                quadCurveTo.setControlY(coords[1]);
                fxpath.getElements().add(quadCurveTo);
                break;
            case PathIterator.SEG_CUBICTO:
                CubicCurveTo cubicTo = new CubicCurveTo();
                cubicTo.setX(coords[4]);
                cubicTo.setY(coords[5]);
                cubicTo.setControlX1(coords[0]);
                cubicTo.setControlY1(coords[1]);
                cubicTo.setControlX2(coords[2]);
                cubicTo.setControlY2(coords[3]);
                fxpath.getElements().add(cubicTo);
                break;
            case PathIterator.SEG_CLOSE:
                ClosePath closePath = new ClosePath();
                fxpath.getElements().add(closePath);
                break;
            default:
                break;
            }
            iterator.next();
        }
        return fxpath;
    }

    /**
     * Render the figure's anchors to indicate selection
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     * @return the {@link javafx.scene.Node}
     */
    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        Group anchors = new Group();
        anchors.setMouseTransparent(true);
        switch (this.type) {
            case LINE, SKETCH, POLYGON -> {
                // end points marked
                anchors.getChildren().add(this.anchor(drawarea, start.x, start.y));
                anchors.getChildren().add(this.anchor(drawarea, end.x, end.y));
            }
            case NONE -> {
            }
            default -> {
                CanvasPoint center = this.rotationCentre();
                anchors.getChildren().add(this.anchor(drawarea, start.x, start.y));
                anchors.getChildren().add(this.anchor(drawarea, end.x, start.y));
                anchors.getChildren().add(this.anchor(drawarea, start.x, end.y));
                anchors.getChildren().add(this.anchor(drawarea, end.x, end.y));
                anchors.getChildren().add(this.edgeAnchor(drawarea, center.x, start.y));
                anchors.getChildren().add(this.edgeAnchor(drawarea, start.x, center.y));
                anchors.getChildren().add(this.edgeAnchor(drawarea, center.x, end.y));
                anchors.getChildren().add(this.edgeAnchor(drawarea, end.x, center.y));
            }
        }
        return anchors;
    }

    protected javafx.scene.shape.Shape anchor(DrawingArea drawarea, double x, double y) {
        CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
        Circle anchor = new Circle();
        anchor.setCenterX(u.x);
        anchor.setCenterY(u.y);
        anchor.setRadius(5.0);
        anchor.setFill(Color.web(drawarea.getCanvasBackgroundColor()));
        anchor.setStroke(Color.web(drawarea.getThemeAccentColor()));
        anchor.setStrokeWidth(1.0);
        return anchor;
    }

    protected javafx.scene.shape.Shape edgeAnchor(DrawingArea drawarea, double x, double y) {
        CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
        javafx.scene.shape.Rectangle anchor = new javafx.scene.shape.Rectangle();
        anchor.setX(u.x - 4.0);
        anchor.setY(u.y - 4.0);
        anchor.setWidth(8.0);
        anchor.setHeight(8.0);
        anchor.setFill(Color.web(drawarea.getCanvasBackgroundColor()));
        anchor.setStroke(Color.web(drawarea.getThemeAccentColor()));
        anchor.setStrokeWidth(1.0);
        anchor.getTransforms().add(new Rotate(this.angle * 180 / Math.PI, u.x, u.y));
        return anchor;
    }

    /**
     * Draw to a Java2d canvas for export
     * 
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
     */
    @Override
    public void draw(Graphics2D g2) {
        java.awt.geom.AffineTransform defaultTransform, transform;

        defaultTransform = g2.getTransform();

        transform = this.getTransform();
        g2.transform(transform);

        if (isClosed()) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getTransparency() / 100);
            g2.setComposite(ac);
            g2.setColor(fxToAwt(getFillColor(), ((float) getTransparency()) / 100));
            g2.fill(getPath());
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
            g2.setComposite(ac);
        }
        g2.setStroke(getStroke());
        g2.setColor(fxToAwt(getColor()));
        g2.draw(getPath());

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    public double getLineWidth(BasicStroke stroke) {
        return stroke.getLineWidth();
    }

    public StrokeLineJoin getLineJoin(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        jfxjoin = switch (stroke.getLineJoin()) {
            case BasicStroke.JOIN_MITER -> javafx.scene.shape.StrokeLineJoin.MITER;
            case BasicStroke.JOIN_ROUND -> javafx.scene.shape.StrokeLineJoin.ROUND;
            case BasicStroke.JOIN_BEVEL -> javafx.scene.shape.StrokeLineJoin.BEVEL;
            default -> javafx.scene.shape.StrokeLineJoin.ROUND;
        };
        return jfxjoin;
    }

    public StrokeLineCap getLineCap(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineCap jfxcap;
        jfxcap = switch (stroke.getEndCap()) {
            case BasicStroke.CAP_BUTT -> javafx.scene.shape.StrokeLineCap.BUTT;
            case BasicStroke.CAP_ROUND -> javafx.scene.shape.StrokeLineCap.ROUND;
            case BasicStroke.CAP_SQUARE -> javafx.scene.shape.StrokeLineCap.SQUARE;
            default -> javafx.scene.shape.StrokeLineCap.ROUND;
        };
        return jfxcap;
    }

    protected void setClosed(boolean closed) {
        this.closed = closed;
    }

    protected boolean isClosed() {
        return closed;
    }

    /**
     * Set stroke colour
     * 
     * @param color the {@link javafx.scene.paint.Color}
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Return stroke colour
     * 
     * @return colour the {@link javafx.scene.paint.Color}
     */
    @Transient
    public Color getColor() {
        return color;
    }

    @Deprecated
    public void setColor(java.awt.Color color) {
        this.color = awtToFx(color);
    }

    @Deprecated
    public java.awt.Color getAwtColor() {
        return fxToAwt(getColor());
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor the {@link javafx.scene.paint.Color}
     */
    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
    }

    /**
     * Return the fill colour
     * 
     * @return the {@link javafx.scene.paint.Color}
     */
    @Transient
    public Color getFillColor() {
        return fillcolor;
    }

    @Deprecated
    public void setFillColor(java.awt.Color fillcolor) {
        this.fillcolor = awtToFx(fillcolor);
    }

    @Deprecated
    public java.awt.Color getAwtFillColor() {
        return fxToAwt(getFillColor());
    }

    /**
     * Set the type of stroke
     * 
     * @param stroke
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Return the type of stroke
     * 
     * @return a stroke
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets whether the shape is opaque
     * 
     * @param transparency 0 (clear) - 100 (opaque)
     */
    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    /**
     * Returns transparency
     * 
     * @return transparency 0 (clear) - 100 (opaque)
     */
    public int getTransparency() {
        return transparency;
    }

    /**
     * Sets the rotation angle
     * 
     * @param angle the angle in radians
     */
    @Override
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Return the rotation angle 
     * 
     * @return angle the angle in radians
     */
    @Override
    public double getAngle() {
        return angle;
    }

    /**
     * Sets the shape to be perpendicular to baseline
     * 
     * @param isVertical a boolean property
     * @deprecated 
     */
    @Deprecated
    @Override
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    /**
     * 
     * @return a boolean property
     * @deprecated 
     */
    @Deprecated
    @Override
    public boolean isVertical() {
        return isVertical;
    }

    /**
     * Signed area of a region using Gauss' formula (or Shoelace algorithm)
     * 
     * @return a signed area for three vertex region
     */
    public double sgnd_area() {
        CanvasPoint p1 = points.get(0);
        CanvasPoint p2 = points.get(1);
        CanvasPoint p3 = points.get(2);
        double _area = (p1.x * p2.y - p2.x * p1.y) + (p2.x * p3.y - p3.x * p2.y) + (p3.x * p1.y - p1.x * p3.y);
        return Math.signum(_area);
    }

    /**
     * Return the 2-tuple of vertices, second point normalised.
     * 
     * @return 2-tuple representing vertex points
     */
    private CanvasPoint[] getVertex(ContainsType contains) {
        CanvasPoint p[];
        double x = Math.min(start.x, end.x);
        double y = Math.min(start.y, end.y);
        double width = Math.abs(start.x - end.x);
        double height = Math.abs(start.y - end.y);
        double side = 0.25 * (width + height); // half average side
        CanvasPoint cxy = this.rotationCentre();
        switch (type) {
            case CIRCLE:
            case SQUARE:
            case TRIANGLE:
            case HEXAGON:
                p = switch (contains) {
                    case TL ->
                        new CanvasPoint[]{new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side)};
                    case BL ->
                        new CanvasPoint[]{new CanvasPoint(x, y + height), new CanvasPoint(cxy.x - side, cxy.y + side)};
                    case BR ->
                        new CanvasPoint[]{new CanvasPoint(x + width, y + height), new CanvasPoint(cxy.x + side, cxy.y + side)};
                    case TR ->
                        new CanvasPoint[]{new CanvasPoint(x + width, y), new CanvasPoint(cxy.x + side, cxy.y - side)};
                    default ->
                        new CanvasPoint[]{new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side)};
                };
                break;
            default:
                p = new CanvasPoint[] { new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side) };
                break;
        }
        return p;
    }

    /**
     * Return a List of 2-tuples of vertices, second point normalised.
     * Note that the points may not be cyclical.
     * 
     * @return a List of 2-tuples representing transformed vertices
     */
    public List<CanvasPoint[]> getVertices() {
        double sx, sy, ex, ey;
        List<CanvasPoint[]> vert = new ArrayList<>();
        List<CanvasPoint[]> vertices = new ArrayList<>();
        switch (this.getType()) {
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
            case HEXAGON:
                // determine average dimension
                double width = Math.abs(start.x - end.x);
                double height = Math.abs(start.y - end.y);
                double side = 0.25 * (width + height); // half average side
                CanvasPoint cxy = this.rotationCentre();
                // determine virtual vertices
                if (start.x < end.x) {
                    sx = cxy.x - side;
                    ex = cxy.x + side;
                } else {
                    sx = cxy.x + side;
                    ex = cxy.x - side;
                }
                if (start.y < end.y) {
                    sy = cxy.y - side;
                    ey = cxy.y + side;
                } else {
                    sy = cxy.y + side;
                    ey = cxy.y - side;
                }
                // combine real and virtual vertices
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(sx, sy) }); // TL
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, end.y), new CanvasPoint(sx, ey) });   // BL
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(ex, ey) });     // BR
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, start.y), new CanvasPoint(ex, sy) });   // TR
                break;
            default:
                // combine real and virtual vertices
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(start.x, start.y) });
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(end.x, end.y) });
                break;
        }
        // transform real and virtual vertices
        for (CanvasPoint[] p : vert) {
            CanvasPoint[] point = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
            vertices.add(point);
        }
        return vertices;
    }

    /**
     * Return a List of 2-tuples of edge mid-points, second point normalised.
     * Note that the points may not be cyclical.
     * 
     * @return a List of 2-tuples representing transformed vertices
     */
    public List<CanvasPoint[]> getEdges() {
        double sx, sy, ex, ey;
        List<CanvasPoint[]> vert = new ArrayList<>();
        List<CanvasPoint[]> vertices = new ArrayList<>();
        switch (this.getType()) {
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
            case HEXAGON:
                // determine average dimension
                double width = Math.abs(start.x - end.x);
                double height = Math.abs(start.y - end.y);
                // determine offsets
                double side = 0.25 * (width + height); // half average side
                CanvasPoint cxy = this.rotationCentre();
                // determine virtual vertices
                if (start.x < end.x) {
                    sx = cxy.x - side;
                    ex = cxy.x + side;
                } else {
                    sx = cxy.x + side;
                    ex = cxy.x - side;
                }
                if (start.y < end.y) {
                    sy = cxy.y - side;
                    ey = cxy.y + side;
                } else {
                    sy = cxy.y + side;
                    ey = cxy.y - side;
                }
                // combine real and virtual edges
                vert.add(new CanvasPoint[] { new CanvasPoint(cxy.x, start.y), new CanvasPoint(cxy.x, sy) }); // TT
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, cxy.y), new CanvasPoint(sx, cxy.y) }); // LL
                vert.add(new CanvasPoint[] { new CanvasPoint(cxy.x, end.y), new CanvasPoint(cxy.x, ey) });   // BB
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, cxy.y), new CanvasPoint(ex, cxy.y) });   // RR
                break;
            default:
                // combine real and virtual vertices
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(start.x, start.y) });
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(end.x, end.y) });
                break;
        }
        // transform real and virtual vertices
        for (CanvasPoint[] p : vert) {
            CanvasPoint[] point = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
            vertices.add(point);
        }
        return vertices;
    }

    /**
     * Transform and awt Color to a javafx Color
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link java.awt.Color}
     * @return {@link javafx.scene.paint.Color}
     */
    public static Color awtToFx(java.awt.Color color) {
        return new Color(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0);
    }

    /**
     * Transform a javafx Color to an awt Color
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link javafx.scene.paint.Color}
     * @return {@link java.awt.Color}
     */
    public static java.awt.Color fxToAwt(Color color) {
        return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
    }

    /**
     * Transform a javafx Color to an awt Color with an alpha channel
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link javafx.scene.paint.Color}
     * @param opacity float
     * @return {@link java.awt.Color}
     */
    public static java.awt.Color fxToAwt(Color color, float opacity) {
        return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), opacity);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // deserialise colors from awt.Color
        Class<?> c = (Class<?>) in.readObject();
        this.color = awtToFx((java.awt.Color) in.readObject());
        c = (Class<?>) in.readObject();
        this.fillcolor = awtToFx((java.awt.Color) in.readObject());

        // deserialise Stroke
        this.setStroke(readStroke(in));
        this.pointfactory = new FigurePointFactory();
        this.pathfactory = new FigurePathFactory();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        out.writeObject(java.awt.Color.class);
        out.writeObject(fxToAwt(getColor()));
        out.writeObject(java.awt.Color.class);
        out.writeObject(fxToAwt(getFillColor()));
        writeStroke(this.getStroke(), out);
    }

    public static Stroke readStroke(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        Stroke result = null;
        boolean isNull = stream.readBoolean();
        if (!isNull) {
            Class<?> c = (Class<?>) stream.readObject();
            if (c.equals(BasicStroke.class)) {
                float width = stream.readFloat();
                int cap = stream.readInt();
                int join = stream.readInt();
                float miterLimit = stream.readFloat();
                float[] dash = (float[]) stream.readObject();
                float dashPhase = stream.readFloat();
                result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
            } else {
                result = (Stroke) stream.readObject();
            }
        }
        return result;
    }

    public static void writeStroke(Stroke stroke,
            ObjectOutputStream stream) throws IOException {

        if (stroke != null) {
            stream.writeBoolean(false);
            if (stroke instanceof BasicStroke s) {
                stream.writeObject(BasicStroke.class);
                stream.writeFloat(s.getLineWidth());
                stream.writeInt(s.getEndCap());
                stream.writeInt(s.getLineJoin());
                stream.writeFloat(s.getMiterLimit());
                stream.writeObject(s.getDashArray());
                stream.writeFloat(s.getDashPhase());
            } else {
                stream.writeObject(stroke.getClass());
                stream.writeObject(stroke);
            }
        } else {
            stream.writeBoolean(true);
        }
    }

}
