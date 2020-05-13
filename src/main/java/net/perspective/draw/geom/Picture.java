/*
 * Picture.java
 * 
 * Created on Feb 1, 2012, 9:23:24 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A structure for rendering an image that holds a reference to an external list 
 * of images.
 * 
 * @author ctipper
 */

public class Picture implements DrawItem, Serializable {

    @Inject private transient DrawingArea drawarea;
    @Inject private transient CanvasView view;
    protected int index;
    protected CanvasPoint start, end;   // start is _untransformed_ coord of TL, end is offset
    protected double scale;
    private int transparency;
    private boolean isVertical;
    private double angle;

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(Picture.class.getName());

    /** Creates a new instance of <code>Picture</code> */
    public Picture() {
        this(0, 0);
    }

    /**
     * Creates a new instance of <code>Picture</code>
     * 
     * @param x the x position
     * @param y the y position
     */
    public Picture(double x, double y) {
        start = new CanvasPoint(x, y);
        index = -1;
        transparency = 0;
        isVertical = false;
        angle = 0;
        scale = 1.0;
    }

    /**
     * Initialise the size and index of ImageItem 
     * @see net.perspective.draw.ImageItem
     * 
     * @param index  a position in list of ImageItems
     * @param width  the width of the image
     * @param height  the height of the image
     */
    public void setImage(int index, double width, double height) {
        this.index = index;
        end = new CanvasPoint(width, height);
        transparency = 0;
        isVertical = false;
        angle = 0;
        scale = 1.0;
    }

    /**
     * Set the position of the {@code ImageItem} in list
     * 
     * @param index 
     */
    public void setImageIndex(int index) {
        this.index = index;
    }

    /**
     * Return the position of the {@code ImageItem} in list
     * 
     * @return an index position
     */
    public int getImageIndex() {
        return index;
    }

    /**
     * Set the untransformed TL coordinate of the picture
     * 
     * @param x the x position
     * @param y the y position
     */
    public void setStart(double x, double y) {
        if (start == null) {
            start = new CanvasPoint();
        }
        start.setLocation(x, y);
    }

    /**
     * 
     * @param start
     * @deprecated
     */
    @Deprecated
    public void setStart(CanvasPoint start) {
        // This should method should almost never be called. Needed by XML Reader.
        this.start = start;
    }

    /**
     * Return the untransformed TL coordinate of the picture
     * 
     * @return the item start point
     */
    public CanvasPoint getStart() {
        return start;
    }

    /**
     * Set the dimensions of the picture
     * 
     * @param x the width
     * @param y the height
     */
    public void setEnd(double x, double y) {
        if (end == null) {
            end = new CanvasPoint();
        }
        end.setLocation(x, y);
    }

    /**
     * 
     * @param end
     * @deprecated
     */
    @Deprecated
    public void setEnd(CanvasPoint end) {
        // This should method should almost never be called. Needed by XML Reader.
        this.end = end;
    }

    /**
     * Return the dimensions of the item
     * 
     * @return the dimensions
     */
    public CanvasPoint getEnd() {
        return end;
    }

    /**
     * Set the relative scale of the picture
     * 
     * @param scale 
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Return the relative scale of the picture
     * 
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * Update the public properties of the picture 
     * 
     * @param canvas  the {@link net.perspective.draw.DrawingCanvas}
     */
    public void updateProperties(DrawingArea canvas) {
        this.setTransparency(canvas.getTransparency());
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-left corner location (transformed)
     */
    public CanvasPoint[] getTop() {
        CanvasPoint s[];
        CanvasPoint[] p = getVertex(ContainsType.TL);
        s = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return s;
    }

    /**
     * Returns the 2-tuple of top-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-right corner location (transformed)
     */
    public CanvasPoint[] getUp() {
        CanvasPoint up[];
        CanvasPoint[] p = getVertex(ContainsType.TR);
        up = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return up;
    }

    /**
     * Returns the 2-tuple of bottom-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-left corner location (transformed)
     */
    public CanvasPoint[] getDown() {
        CanvasPoint down[];
        CanvasPoint[] p = getVertex(ContainsType.BL);
        down = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return down;
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        CanvasPoint[] p = getVertex(ContainsType.BR);
        e = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
        return e;
    }

    private CanvasPoint getTransform(CanvasPoint point) {
        CanvasPoint centre = this.rotationCentre();

        point.translate(-centre.x, -centre.y);
        if (this.getAngle() != 0) {
            // rotate shape about centroid
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
     * rotate Picture about centroid with optional 90 deg correction
     */
    private AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();
        AffineTransform transform = new AffineTransform();
        transform.setToRotation(this.getAngle() + (this.isVertical() ? -Math.PI / 2 : 0), centre.x, centre.y);
        // translate to start
        transform.translate(start.x, start.y);
        return transform;
    }

    /**
     * Returns the location of the picture centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    public CanvasPoint rotationCentre() {
        return new CanvasPoint(start.x + (scale * end.x) / 2.0, start.y + (scale * end.y) / 2.0);
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    public Shape bounds() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, end.x, end.y);
        Area bounds = new Area(rect);
        AffineTransform transform = this.getTransform();
        transform.scale(scale, scale);
        bounds.transform(transform);
        return bounds;
    }

    /**
     * Detect if a point lies within the bounds, a convenience method
     * 
     * @param x  canvas coordinate
     * @param y  canvas coordinate
     * @return a boolean property
     */
    public boolean contains(double x, double y) {
        return this.bounds().contains(x, y);
    }

    /**
     * Translate the picture
     * 
     * @param xinc  x increment
     * @param yinc  y increment
     */
    public void moveShape(double xinc, double yinc) {
        start.translate(xinc, yinc);
    }

    /**
     * Provide ImageVew for FX canvas
     * 
     * @return an FX Path
     */
    @Override
    public Node draw() {
        ImageView iv = new ImageView();
        Image image = view.getImageItem(index).getImage();
        iv.setImage(image);
        iv.setFitWidth(image.getWidth() * Math.abs(scale));
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setRotate(180 * this.getAngle() / Math.PI);
        iv.setX(start.x + (scale >= 0 ? 0 : end.x * scale ));
        iv.setY(start.y + (scale >= 0 ? 0 : end.y * scale ));
        iv.setCursor(javafx.scene.Cursor.OPEN_HAND);
        return iv;
    }

    /**
     * Render the picture anchors to indicate selection
     * 
     * @return an FX Shape node
     */
    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        Group anchors = new Group();
        anchors.setMouseTransparent(true);
        anchors.getChildren().add(this.anchor(drawarea, start.x, start.y));
        anchors.getChildren().add(this.anchor(drawarea, start.x + scale * end.x, start.y));
        anchors.getChildren().add(this.anchor(drawarea, start.x, start.y + scale * end.y));
        anchors.getChildren().add(this.anchor(drawarea, start.x + scale * end.x, start.y + scale * end.y));
        return anchors;
    }

    protected javafx.scene.shape.Shape anchor(DrawingArea drawarea, double x, double y) {
        CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
        Circle anchor = new Circle();
        anchor.setCenterX(u.x);
        anchor.setCenterY(u.y);
        anchor.setRadius(5.0);
        anchor.setFill(Color.web(drawarea.getThemeBackgroundColor()));
        anchor.setStroke(Color.web(drawarea.getThemeAccentColor()));
        anchor.setStrokeWidth(1.0);
        return anchor;
    }

    /**
     * Draw to a Java2d canvas for export
     * 
     * @param g2 Java2d graphics context
     */
    public void draw(Graphics2D g2) {
        AffineTransform defaultTransform, transform;

        defaultTransform = g2.getTransform();

        transform = this.getTransform();
        transform.scale(scale, scale);
        g2.transform(transform);

        try {
            // Retrieve image
            Image image = view.getImageItem(index).getImage();
            BufferedImage img = SwingFXUtils.fromFXImage(image, null);
            if (img == null) {
                throw new NullPointerException();
            }
            g2.drawImage(img, 0, 0, null);
        } catch (IndexOutOfBoundsException e) {
            logger.error("Picture: Index {0} Out of Bounds.", index);
            // reset graphics context
            g2.setTransform(defaultTransform);
            this.sketch(g2);
        } catch (NullPointerException e) {
            logger.warn("Picture: Image not found.");
            // reset graphics context
            g2.setTransform(defaultTransform);
            this.sketch(g2);
        }

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    /**
     * Render a shadowed version of the picture
     * 
     * @param g2  the graphics context
     * @param drawarea  the {@link net.perspective.draw.DrawingArea}
     */
    private void sketch(Graphics2D g2) {
        AffineTransform defaultTransform;

        defaultTransform = g2.getTransform();

        g2.transform(this.getTransform());

        g2.setColor(Figure.fxToAwt(Color.web(drawarea.getThemeFillColor()), ((float) getTransparency()) / 100));
        g2.fill(new Rectangle2D.Double(0, 0, scale * end.x, scale * end.y));

        // reset graphics context
        g2.setTransform(defaultTransform);
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
     * Sets the shape to be perpendicular to baseline
     * 
     * @param isVertical  A boolean property
     */
    @Deprecated
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    /**
     * 
     * @return a boolean property
     */
    @Deprecated
    public boolean isVertical() {
        return this.isVertical;
    }

    /**
     * Sets the rotation angle
     * 
     * @param angle  The angle in radians
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
        CanvasPoint p1 = new CanvasPoint(), p2 = new CanvasPoint(), p3 = new CanvasPoint();
        if (scale < 0) {
            p1.setLocation(1d, 1d);
            p2.setLocation(1d + getEnd().x, 1d);
            p3.setLocation(1d + getEnd().x, 1d + getEnd().y);
        } else {
            p1.setLocation(1d, 1d);
            p2.setLocation(1d, 1d + getEnd().y);
            p3.setLocation(1d + getEnd().x, 1d + getEnd().y);
        }
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
        double sx, sy, ex, ey;
        double width = Math.abs(scale * end.x);
        double height = Math.abs(scale * end.y);
        double side = 0.25 * (width + height); // half average side
        CanvasPoint cxy = this.rotationCentre();
        // determine real vertices
        CanvasPoint p1 = new CanvasPoint(start.x, start.y);                 // TL
        CanvasPoint p2 = new CanvasPoint(start.x, start.y + scale * end.y); // BL
        CanvasPoint p3 = new CanvasPoint(start.x + scale * end.x, start.y + scale * end.y); // BR
        CanvasPoint p4 = new CanvasPoint(start.x + scale * end.x, start.y); // TR
        // determine virtual vertices
        if (p1.x < p3.x) {
            sx = cxy.x - side;
            ex = cxy.x + side;
        } else {
            sx = cxy.x + side;
            ex = cxy.x - side;
        }
        if (p1.y < p3.y) {
            sy = cxy.y - side;
            ey = cxy.y + side;
        } else {
            sy = cxy.y + side;
            ey = cxy.y - side;
        }
        switch (contains) {
            case TL:
                p = new CanvasPoint[] { p1, new CanvasPoint(sx, sy) };
                break;
            case BL:
                p = new CanvasPoint[] { p2, new CanvasPoint(sx, ey) };
                break;
            case BR:
                p = new CanvasPoint[] { p3, new CanvasPoint(ex, ey) };
                break;
            case TR:
                p = new CanvasPoint[] { p4, new CanvasPoint(ex, sy) };
                break;
            default:
                p = new CanvasPoint[] { p1, new CanvasPoint(sx, sy) };
                break;
        }
        return p;
    }

    /*
     * Return 2-point array of vertices, second point normalised.
     * Note that the points may not be cyclical.
     * 
     * Refer to Figure.java and FigurePointFactory.java for details
     */
    public List<CanvasPoint[]> getVertices() {
        double sx, sy, ex, ey;
        List<CanvasPoint[]> vert = new ArrayList<>();
        List<CanvasPoint[]> vertices = new ArrayList<>();
        // determine average dimension
        double width = Math.abs(scale * end.x);
        double height = Math.abs(scale * end.y);
        double side = 0.25 * (width + height); // half average side
        CanvasPoint cxy = this.rotationCentre();
        // determine real vertices
        CanvasPoint p1 = new CanvasPoint(start.x, start.y);                 // TL
        CanvasPoint p2 = new CanvasPoint(start.x, start.y + scale * end.y); // BL
        CanvasPoint p3 = new CanvasPoint(start.x + scale * end.x, start.y + scale * end.y); // BR
        CanvasPoint p4 = new CanvasPoint(start.x + scale * end.x, start.y); // TR
        // determine virtual vertices
        if (p1.x < p3.x) {
            sx = cxy.x - side;
            ex = cxy.x + side;
        } else {
            sx = cxy.x + side;
            ex = cxy.x - side;
        }
        if (p1.y < p3.y) {
            sy = cxy.y - side;
            ey = cxy.y + side;
        } else {
            sy = cxy.y + side;
            ey = cxy.y - side;
        }
        // combine real and virtual vertices
        vert.add(new CanvasPoint[] { p1, new CanvasPoint(sx, sy) });
        vert.add(new CanvasPoint[] { p2, new CanvasPoint(sx, ey) });
        vert.add(new CanvasPoint[] { p3, new CanvasPoint(ex, ey) });
        vert.add(new CanvasPoint[] { p4, new CanvasPoint(ex, sy) });
        // transform real and virtual vertices
        for (CanvasPoint[] p : vert) {
            CanvasPoint[] point = new CanvasPoint[] { this.getTransform(p[0]), this.getTransform(p[1]) };
            vertices.add(point);
        }
        return vertices;
    }

    /**
     * 
     * @param start
     * @deprecated
     */
    @Deprecated
    public void setStartPoint(CanvasPoint start) {
        this.start = start;
    }

    /**
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getStartPoint() {
        return start;
    }

    /**
     * 
     * @param end
     * @deprecated
     */
    @Deprecated
    public void setEndPoint(CanvasPoint end) {
        this.end = end;
    }

    /**
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getEndPoint() {
        return end;
    }

}
