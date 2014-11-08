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
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class Figure implements Serializable {

    private List<CanvasPoint> points;
    private FigureType type;
    private CanvasPoint start, end;
    private transient GeneralPath path;
    private transient PointFactory pointfactory;
    private transient PathFactory pathfactory;
    private transient Stroke stroke;
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
        this.path = new GeneralPath();
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

    public void setPoints() {
        if (!this.type.equals(FigureType.SKETCH)
            && !this.type.equals(FigureType.POLYGON)) {
            this.points = pointfactory.createPoints(this.type, start.x, start.y, end.x, end.y);
        } else {
            this.points = new ArrayList<>();
        }
    }
    
    public List<CanvasPoint> getPoints() {
        return this.points;
    }

    public void addPoint(double x, double y) {
        this.points.add(new CanvasPoint(x, y));
    }
    
    public void setEndPoints() {
        switch (this.type) {
            case CIRCLE:
            case ELLIPSE:
            case SQUARE:
            case RECTANGLE:
                start = new CanvasPoint(points.get(0).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
                break;
            case TRIANGLE:
            case ISOSCELES:
                start = new CanvasPoint(points.get(1).x, points.get(0).y);
                end = new CanvasPoint(points.get(2).x, points.get(2).y);
                break;
            default:
                start = points.get(0);
                end = points.get(points.size() - 1);
                break;
        }
    }
    
    public FigureType getType() {
        return this.type;
    }

    public void setType(FigureType t) {
        this.type = t;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFillColor() {
        return fillcolor;
    }

    public void setFillColor(String c) {
        fillcolor = c;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke s) {
        stroke = s;
    }

    public double getAngle() {
        return this.angle;
    }

    public void setAngle(double a) {
        this.angle = a;
    }

    public GeneralPath getPath() {
        return this.path;
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

    public CanvasPoint getTop() {
        CanvasPoint s;
        switch (this.getType()) {
            case SKETCH:
            case POLYGON:
            case LINE:
                Rectangle2D bound = this.getBounds2D();
                s = new CanvasPoint(bound.getX(), bound.getY());
                break;
            default:
                s = new CanvasPoint(start.x, start.y);
                s = this.getTransform(s);
                break;
        }
        return s;
    }

    public CanvasPoint getUp() {
        CanvasPoint up;
        switch (this.getType()) {
            case SKETCH:
            case POLYGON:
            case LINE:
                Rectangle2D bound = this.getBounds2D();
                up = new CanvasPoint(bound.getX() + bound.getWidth(), bound.getY());
                break;
            default:
                up = new CanvasPoint(end.x, start.y);
                up = this.getTransform(up);
                break;
        }
        return up;
    }

    public CanvasPoint getDown() {
        CanvasPoint down;
        switch (this.getType()) {
            case SKETCH:
            case POLYGON:
            case LINE:
                Rectangle2D bound = this.getBounds2D();
                down = new CanvasPoint(bound.getX(), bound.getY() + bound.getHeight());
                break;
            default:
                down = new CanvasPoint(start.x, end.y);
                down = this.getTransform(down);
                break;
        }
        return down;
    }

    public CanvasPoint getBottom() {
        CanvasPoint bottom;
        switch (this.getType()) {
            case SKETCH:
            case POLYGON:
                Rectangle2D bound = this.getBounds2D();
                bottom = new CanvasPoint(bound.getX() + bound.getWidth(),
                                         bound.getY() + bound.getHeight());
                break;
            default:
                bottom = new CanvasPoint(end.x, end.y);
                bottom = this.getTransform(bottom);
                break;
        }
        return bottom;
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

    public void moveFigure(double xinc, double yinc) {
        for (CanvasPoint p : points) {
            p.translate(xinc, yinc);
        }
        this.setEndPoints();
        this.setPath();
    }

    public CanvasPoint rotationCentre() {
        GeneralPath p = (GeneralPath) this.getPath().clone();
        p.closePath();
        Area area = new Area(p);
        Rectangle2D bound = area.getBounds2D();
        return new CanvasPoint(bound.getCenterX(), bound.getCenterY());
    }

    public Rectangle2D getBounds2D() {
        java.awt.Shape area = this.bounds();
        Rectangle2D bound = area.getBounds2D();
        return bound;
    }

    public java.awt.Shape bounds() {
        AffineTransform trans = this.getTransform();
        GeneralPath p = (GeneralPath) this.getPath().clone();
        p.transform(trans);
        return p;
    }

    public boolean contains(double x, double y) {
        return this.bounds().intersects(x - 5, y - 5, 10, 10);
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
    
    public Path draw() {
        AffineTransform at;
        at = this.getTransform();
        Path path = drawPath(at);
        path.setStroke(Color.web(this.getColor()));
        path.setStrokeWidth(getLineWidth());
        path.setStrokeLineJoin(getLineJoin());
        path.setStrokeLineCap(getLineCap());
        if (this.isClosed() && !"white".equals(this.getFillColor())) {
            path.setFill(Color.web(this.getFillColor()));
        }
        return path;
    }
    
    private Path drawPath(AffineTransform at) {
        double[] coords = {0, 0, 0, 0, 0, 0};
        Path path = new Path();

        PathIterator iterator = this.getPath().getPathIterator(at);
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                MoveTo moveTo = new MoveTo();
                moveTo.setX(coords[0]);
                moveTo.setY(coords[1]);
                path.getElements().add(moveTo);
                break;
            case PathIterator.SEG_LINETO:
                LineTo lineTo = new LineTo();
                lineTo.setX(coords[0]);
                lineTo.setY(coords[1]);                
                path.getElements().add(lineTo);
                break;
            case PathIterator.SEG_QUADTO:
                QuadCurveTo quadCurveTo = new QuadCurveTo();
                quadCurveTo.setX(coords[2]);
                quadCurveTo.setY(coords[3]);
                quadCurveTo.setControlX(coords[0]);
                quadCurveTo.setControlY(coords[1]);                
                path.getElements().add(quadCurveTo);
                break;
            case PathIterator.SEG_CUBICTO:
                CubicCurveTo cubicTo = new CubicCurveTo();
                cubicTo.setX(coords[4]);
                cubicTo.setY(coords[5]);
                cubicTo.setControlX1(coords[0]);
                cubicTo.setControlY1(coords[1]);
                cubicTo.setControlX2(coords[2]);
                cubicTo.setControlY2(coords[3]);
                path.getElements().add(cubicTo);
                break;
            case PathIterator.SEG_CLOSE:
                ClosePath closePath = new ClosePath();
                path.getElements().add(closePath);
                break;
            default:
                break;
            }
            iterator.next();
        }
        return path;
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
