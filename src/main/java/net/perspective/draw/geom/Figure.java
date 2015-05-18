/*
 * Figure.java
 * 
 * Created on Oct 19, 2013 5:59:14 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class Figure implements Serializable {

    protected List<CanvasPoint> points;
    protected FigureType type;
    protected CanvasPoint start, end;
    protected transient Path2D.Double path;
    protected transient PointFactory pointfactory;
    protected transient PathFactory pathfactory;
    protected transient Stroke stroke;
    private String color, fillcolor;
    private double angle;
    // closed indicates to draw() whether shape should be filled
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
        this.pathfactory = new FigurePathFactory();
        this.pointfactory = new FigurePointFactory();
        this.path = new Path2D.Double();
    }
    
    public void setStart(double x, double y) {
        this.start = new CanvasPoint(x,y);
    }

    public CanvasPoint getStart() {
        return start;
    }

    public void setEnd(double x, double y) {
        this.end = new CanvasPoint(x,y);
    }

    public CanvasPoint getEnd() {
        return end;
    }

    public void setPoints(DrawingType drawtype) {
        this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
    }
    
    public List<CanvasPoint> getPoints() {
        return this.points;
    }

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
    
    public void addPoint(double x, double y) {
        this.points.add(new CanvasPoint(x, y));
    }
    
    public void setPath() {
        this.path = pathfactory.createPath(this);
        this.setClosed(true);
    }

    public Path2D.Double getPath() {
        return this.path;
    }

    public void setType(FigureType t) {
        this.type = t;
    }

    public FigureType getType() {
        return this.type;
    }

    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        CanvasPoint[] p = getVertex(ContainsType.TL);
        s = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return s;
    }

    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        CanvasPoint[] p = getVertex(ContainsType.TR);
        up = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return up;
    }

    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        CanvasPoint[] p = getVertex(ContainsType.BL);
        down = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return down;
    }

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

    public Rectangle2D getBounds2D() {
        java.awt.Shape area = this.bounds();
        Rectangle2D bound = area.getBounds2D();
        return bound;
    }

    public java.awt.Shape bounds() {
        // Get transformed path
        AffineTransform transform = this.getTransform();
        Path2D.Double p = (Path2D.Double) this.getPath().clone();
        p.transform(transform);
        return p;
    }

    public boolean contains(double x, double y) {
        return this.bounds().intersects(x - 5, y - 5, 10, 10);
    }

    public void moveFigure(double xinc, double yinc) {
        for (CanvasPoint p : points) {
            p.translate(xinc, yinc);
        }
        this.setEndPoints();
        this.setPath();
    }

    public Path draw() {
        AffineTransform at;
        at = this.getTransform();
        Path fxpath = drawPath(at);
        fxpath.setStroke(Color.web(this.getColor()));
        fxpath.setStrokeWidth(getLineWidth());
        fxpath.setStrokeLineJoin(getLineJoin());
        fxpath.setStrokeLineCap(getLineCap());
        if (this.isClosed() && !"white".equals(this.getFillColor())) {
            fxpath.setFill(Color.web(this.getFillColor()));
        }
        return fxpath;
    }
    
    private Path drawPath(AffineTransform at) {
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
    
    protected Shape anchor(double x, double y) {
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
    
    public double getLineWidth() {
        return (double) ((BasicStroke) this.getStroke()).getLineWidth();
    }
    
    public StrokeLineJoin getLineJoin() {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        switch (((BasicStroke) this.getStroke()).getLineJoin()) {
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

    public StrokeLineCap getLineCap() {
        javafx.scene.shape.StrokeLineCap jfxcap;
        switch (((BasicStroke) this.getStroke()).getEndCap()) {
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

    private boolean isClosed() {
        return this.closed;
    }

    public String getColor() {
        return this.color;
    }

    public void setFillColor(String c) {
        this.fillcolor = c;
    }

    public String getFillColor() {
        return this.fillcolor;
    }

    public void setStroke(Stroke s) {
        this.stroke = s;
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setAngle(double a) {
        this.angle = a;
    }

    public double getAngle() {
        return this.angle;
    }

    public double sgnd_area() {
        CanvasPoint p1 = points.get(0);
        CanvasPoint p2 = points.get(1);
        CanvasPoint p3 = points.get(2);
        double _area = (p1.x * p2.y - p2.x * p1.y) + (p2.x * p3.y - p3.x * p2.y) + (p3.x * p1.y - p1.x * p3.y);
        return Math.signum(_area);
    }
    
    /* 
     * Return 2-point array of vertex, second point normalized.
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

    /*
     * Return 2-point array of vertices, second point normalized.
     * Note that the points may not be cyclical.
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
        for (CanvasPoint[] p : vert) {
            CanvasPoint[] point = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
            vertices.add(point);
        }
        return vertices;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.setStroke(readStroke(in));
        this.pointfactory = new FigurePointFactory();
        this.pathfactory = new FigurePathFactory();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
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
