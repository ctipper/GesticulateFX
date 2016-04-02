/*
 * Figure.java
 * 
 * Created on Oct 19, 2013 5:59:14 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.G2;

/**
 *
 * @author ctipper
 */

public class Figure implements Serializable {

    protected List<CanvasPoint> points;
    protected FigureType type;
    protected CanvasPoint start, end;       // start, end are _untransformed_ canvas coordinates of TL/BR corners
    protected transient Path2D.Double path;
    protected transient PointFactory pointfactory;
    protected transient PathFactory pathfactory;
    protected transient Stroke stroke;
    private transient Color color, fillcolor;
    private int transparency;
    private double angle;
    private boolean closed;                 // closed indicates to draw() whether figure should be filled
    
    private static final long serialVersionUID = 1L;

    public Figure() {
        this.type = FigureType.NONE;
        this.transparency = 0;
        this.angle = 0;
        this.closed = false;
    }

    public Figure(FigureType type) {
        this();
        this.type = type;
        this.points = new ArrayList<>();
        this.pathfactory = new FigurePathFactory();
        this.pointfactory = new FigurePointFactory();
        this.path = new Path2D.Double();
    }
    
    /**
     * Set the untransformed TL coordinate of the item
     * 
     * @param x the x position
     * @param y the y position
     */
    public void setStart(double x, double y) {
        this.start = new CanvasPoint(x,y);
    }

    /**
     * Return the untransformed TL coordinate of the figure
     * 
     * @return the item start point
     */
    public CanvasPoint getStart() {
        return start;
    }

    /**
     * Set the untransformed BR coordinate of the figure
     * 
     * @param x the width
     * @param y the height
     */
    public void setEnd(double x, double y) {
        this.end = new CanvasPoint(x,y);
    }

    /** 
     * Return the untransformed BR coordinate of the figure
     * 
     * @return the dimensions
     */
    public CanvasPoint getEnd() {
        return end;
    }

    /**
     * Initialise points List
     * 
     * @param drawtype
     */
    public void setPoints(DrawingType drawtype) {
        this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
    }
    
    /**
     * Returns points list
     * 
     * @return a List of points
     */
    public List<CanvasPoint> getPoints() {
        return this.points;
    }

    /**
     * Initialise end points
     */
    public void setEndPoints() {
        switch (this.type) {
            case CIRCLE:
            case SQUARE:
                start = new CanvasPoint(points.get(0).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
                break;
            case TRIANGLE:
                start = new CanvasPoint(points.get(1).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
                break;
            default:
                start = points.get(0);
                end = points.get(points.size() - 1);
                break;
        }
    }
    
    /**
     * Add a point to the List of points
     * 
     * @param x
     * @param y 
     */
    public void addPoint(double x, double y) {
        this.points.add(new CanvasPoint(x, y));
    }
    
    /**
     * Set the path from a List of points
     */
    public void setPath() {
        this.path = pathfactory.createPath(this);
        this.setClosed(true);
    }

    /**
     * Returns the path describing the figure
     * 
     * @return path
     */
    public Path2D.Double getPath() {
        return this.path;
    }

    /**
     * Sets the figure type
     * 
     * @param type
     */
    public void setType(FigureType type) {
        this.type = type;
    }

    /**
     * Retsurns the figure type
     * 
     * @return type
     */
    public FigureType getType() {
        return this.type;
    }

    /**
     * Update the figure properties, such as color, stroke etc.
     * 
     * @param drawarea
     */
    public void updateProperties(DrawingArea drawarea) {
        this.setColor(drawarea.getColor());
        this.setFillColor(drawarea.getFillColor());
        this.setStroke(drawarea.getStroke());
        this.setTransparency(drawarea.getTransparency());
    }

    /**
     *  
     * @return The 2-tuple of top-left corner location (transformed)
     *         second point may be normalized
     */
    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        CanvasPoint[] p = getVertex(ContainsType.TL);
        s = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return s;
    }

    /**
     *  
     * @return The 2-tuple of top-right corner location (transformed)
     *         second point may be normalized
     */
    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        CanvasPoint[] p = getVertex(ContainsType.TR);
        up = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return up;
    }

    /**
     *  
     * @return The 2-tuple of bottom-left corner location (transformed)
     *         second point may be normalized
     */
    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        CanvasPoint[] p = getVertex(ContainsType.BL);
        down = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return down;
    }

    /**
     * 
     * @return The 2-point array of bottom-right corner location (transformed)
     *         second point may be normalized
     */
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        CanvasPoint[] p = getVertex(ContainsType.BR);
        e = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return e;
    }

    protected CanvasPoint getTransform(CanvasPoint point) {
        CanvasPoint centre = this.rotationCentre();
        
        point.translate(-centre.x, -centre.y);
        if (this.getAngle() != 0) {
            // rotate shape about centroid
            point.rotate(this.getAngle());
        }
        point.translate(centre.x, centre.y);
        return point;
    }

    protected java.awt.geom.AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();
        
        java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
        transform.setToTranslation(centre.x, centre.y);
        if (this.getAngle() != 0) {
            // rotate figure about centroid
            transform.rotate(this.getAngle());
        }
        transform.translate(-centre.x, -centre.y);
        return transform;
    }

    /**
     * Returns the location of the item centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
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
     * @param x canvas coordinate
     * @param y canvas coordinate
     * @return a boolean property
     */
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
     * @param xinc x increment
     * @param yinc y increment
     */
    public void moveFigure(double xinc, double yinc) {
        points.stream().forEach((p) -> {
            p.translate(xinc, yinc);
        });
        this.setEndPoints();
        this.setPath();
    }

    /**
     * Provide a Path for FX canvas
     * 
     * @return an FX Path
     */
    public Path draw() {
        java.awt.geom.AffineTransform at;
        
        at = this.getTransform();
        Path fxpath = drawPath(at);
        fxpath.setStroke(getColor());
        fxpath.setStrokeWidth(getLineWidth((BasicStroke) getStroke()));
        fxpath.setStrokeLineJoin(getLineJoin((BasicStroke) getStroke()));
        fxpath.setStrokeLineCap(getLineCap((BasicStroke) getStroke()));
        if (this.isClosed()) {
            Color alphafill = Color.color(getFillColor().getRed(), 
                getFillColor().getGreen(), 
                getFillColor().getBlue(),
                ((double) getTransparency()) / 100);
            fxpath.setFill(alphafill);
        }
        return fxpath;
    }
    
    protected Path drawPath(java.awt.geom.AffineTransform at) {
        double[] coords = {0, 0, 0, 0, 0, 0};
        Path fxpath = new Path();

        PathIterator iterator = this.getPath().getPathIterator(at);
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
     * Render the figure anchors to indicate selection
     * 
     * @return an FX Shape node
     */
    public Node drawAnchors() {
        Group anchors = new Group();
        switch (this.type) {
            case LINE:
            case SKETCH:
            case POLYGON:
                // end points marked
                anchors.getChildren().add(this.anchor(start.x, start.y));
                anchors.getChildren().add(this.anchor(end.x, end.y));
                break;
            case NONE:
                break;
            default:
                anchors.getChildren().add(this.anchor(start.x, start.y));
                anchors.getChildren().add(this.anchor(end.x, start.y));
                anchors.getChildren().add(this.anchor(start.x, end.y));
                anchors.getChildren().add(this.anchor(end.x, end.y));
                break;
        }
        return anchors;
    }
    
    protected javafx.scene.shape.Shape anchor(double x, double y) {
        CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
        Circle anchor = new Circle();
        anchor.setCenterX(u.x);
        anchor.setCenterY(u.y);
        anchor.setRadius(5.0);
        anchor.setFill(Color.web("white"));
        anchor.setStroke(Color.web("black"));
        anchor.setStrokeWidth(1.0);
        return anchor;
    }
    
    /**
     * Draw to a Java2d canvas for export
     * 
     * @param g2 Java2d graphics context
     */
    public void draw(Graphics2D g2) {
        java.awt.geom.AffineTransform defaultTransform, transform;
        
        defaultTransform = g2.getTransform();

        transform = this.getTransform();
        g2.transform(transform);

        if (this.isClosed()) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getTransparency() / 100);
            g2.setComposite(ac);
            g2.setColor(G2.fxToAwt(this.fillcolor, ((float) getTransparency()) / 100));
            g2.fill(this.getPath());
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
            g2.setComposite(ac);
        }
        g2.setStroke(this.getStroke());
        g2.setColor(G2.fxToAwt(this.color));
        g2.draw(this.getPath());

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    public double getLineWidth(BasicStroke stroke) {
        return (double) (stroke.getLineWidth());
    }
    
    public StrokeLineJoin getLineJoin(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        switch (stroke.getLineJoin()) {
            case BasicStroke.JOIN_MITER:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.MITER;
                break;
            case BasicStroke.JOIN_ROUND:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
            case BasicStroke.JOIN_BEVEL:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.BEVEL;
                break;
            default:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
        }
        return jfxjoin;
    }

    public StrokeLineCap getLineCap(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineCap jfxcap;
        switch (stroke.getEndCap()) {
            case BasicStroke.CAP_BUTT:
                jfxcap = javafx.scene.shape.StrokeLineCap.BUTT;
                break;
            case BasicStroke.CAP_ROUND:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
            case BasicStroke.CAP_SQUARE:
                jfxcap = javafx.scene.shape.StrokeLineCap.SQUARE;
                break;
            default:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
        }
        return jfxcap;
    }

    protected void setClosed(boolean closed) {
        this.closed = closed;
    }

    protected boolean isClosed() {
        return this.closed;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Deprecated
    public void setColor(java.awt.Color color) {
        this.color = G2.awtToFx(color);
    }

    public Color getColor() {
        return this.color;
    }

    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
    }

    @Deprecated
    public void setFillColor(java.awt.Color fillcolor) {
        this.fillcolor = G2.awtToFx(fillcolor);
    }

    public Color getFillColor() {
        return this.fillcolor;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getStroke() {
        return this.stroke;
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
        return this.transparency;
    }

    /**
     * Sets the rotation angle
     * 
     * @param a The angle in radians
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Return the rotation angle 
     * 
     * @return angle
     */
    public double getAngle() {
        return this.angle;
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
     * Return 2-point array of vertex, second point normalized.
     * 
     * @return 2-tuple representing vertex coords
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
                switch (contains) {
                    case TL:
                        p = new CanvasPoint[] { new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side) };
                        break;
                    case BL:
                        p = new CanvasPoint[] { new CanvasPoint(x, y + height), new CanvasPoint(cxy.x - side, cxy.y + side) };
                        break;
                    case BR:
                        p = new CanvasPoint[] { new CanvasPoint(x + width, y + height), new CanvasPoint(cxy.x + side, cxy.y + side) };
                        break;
                    case TR:
                        p = new CanvasPoint[] { new CanvasPoint(x + width, y), new CanvasPoint(cxy.x + side, cxy.y - side) };
                        break;
                    default:
                        p = new CanvasPoint[] { new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side) };
                        break;
                }
                break;
            default:
                p = new CanvasPoint[] { new CanvasPoint(x, y), new CanvasPoint(cxy.x - side, cxy.y - side) };
                break;
        }
        return p;
    }

    /**
     * Return 2-point array of vertices, second point normalized.
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
                double width = Math.abs(start.x - end.x);
                double height = Math.abs(start.y - end.y);
                double side = 0.25 * (width + height); // half average side
                CanvasPoint cxy = this.rotationCentre();
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
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(sx, sy) });
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, end.y), new CanvasPoint(sx, ey) });
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(ex, ey) });
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, start.y), new CanvasPoint(ex, sy) });
                break;
            default:
                vert.add(new CanvasPoint[] { new CanvasPoint(start.x, start.y), new CanvasPoint(start.x, start.y) });
                vert.add(new CanvasPoint[] { new CanvasPoint(end.x, end.y), new CanvasPoint(end.x, end.y) });
                break;
        }
        vert.stream().map((p) -> new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) }).forEach((point) -> {
            vertices.add(point);
        });
        return vertices;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // deserialise colors from awt.Color
        Class<?> c = (Class<?>) in.readObject();
        this.color = G2.awtToFx((java.awt.Color) in.readObject());
        c = (Class<?>) in.readObject();
        this.fillcolor = G2.awtToFx((java.awt.Color) in.readObject());
        
        // deserialise Stroke
        this.setStroke(readStroke(in));
        this.pointfactory = new FigurePointFactory();
        this.pathfactory = new FigurePathFactory();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        out.writeObject(java.awt.Color.class);
        out.writeObject(G2.fxToAwt(this.color));
        out.writeObject(java.awt.Color.class);
        out.writeObject(G2.fxToAwt(this.fillcolor));
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
            if (stroke instanceof BasicStroke) {
                BasicStroke s = (BasicStroke) stroke;
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
