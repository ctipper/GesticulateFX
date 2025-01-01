/*
 * Picture.java
 * 
 * Created on Feb 1, 2012, 9:23:24 PM
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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

    @Inject protected transient DrawingArea drawarea;
    @Inject protected transient CanvasView view;
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
    @Override
    public void setStart(double x, double y) {
        if (start == null) {
            start = new CanvasPoint();
        }
        start.setLocation(x, y);
    }

    /**
     * 
     * @param start the start point
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
    @Override
    public CanvasPoint getStart() {
        return start;
    }

    /**
     * Set the dimensions of the picture
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
     * 
     * @param end the end point
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
    @Override
    public CanvasPoint getEnd() {
        return end;
    }

    /**
     * Set the relative scale of the picture
     * 
     * @param scale the scale factor
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Return the relative scale of the picture
     * 
     * @return the scale factor
     */
    public double getScale() {
        return scale;
    }

    /**
     * Update the public properties of the picture 
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     */
    @Override
    public void updateProperties(DrawingArea drawarea) {
        this.setTransparency(drawarea.getTransparency());
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point is not normalised
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
     * the second point is not normalised
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
     * the second point is not normalised
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
     * the second point is not normalised
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
    @Override
    public CanvasPoint rotationCentre() {
        return new CanvasPoint(start.x + (scale * end.x) / 2.0, start.y + (scale * end.y) / 2.0);
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    @Override
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
    @Override
    public boolean contains(double x, double y) {
        return this.bounds().contains(x, y);
    }

    /**
     * Translate the picture
     * 
     * @param xinc  x increment
     * @param yinc  y increment
     */
    @Override
    public void moveTo(double xinc, double yinc) {
        start.translate(xinc, yinc);
    }

    /**
     * Provide an ImageView for FX canvas
     * 
     * @return the {@link javafx.scene.Node}
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
        iv.setMouseTransparent(true);
        return iv;
    }

    /**
     * Render the picture anchors to indicate selection
     * 
     * @return the {@link javafx.scene.Node}
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
        anchor.setFill(Color.web(drawarea.getCanvasBackgroundColor()));
        anchor.setStroke(Color.web(drawarea.getThemeAccentColor()));
        anchor.setStrokeWidth(1.0);
        return anchor;
    }

    /**
     * Render the Picture to the g2d canvas
     * 
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
     */
    @Override
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
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
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
    @Override
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    /**
     * 
     * @return a boolean property
     */
    @Deprecated
    @Override
    public boolean isVertical() {
        return this.isVertical;
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
        p = switch (contains) {
            case TL -> new CanvasPoint[] { p1, new CanvasPoint(sx, sy) };
            case BL -> new CanvasPoint[] { p2, new CanvasPoint(sx, ey) };
            case BR -> new CanvasPoint[] { p3, new CanvasPoint(ex, ey) };
            case TR -> new CanvasPoint[] { p4, new CanvasPoint(ex, sy) };
            default -> new CanvasPoint[] { p1, new CanvasPoint(sx, sy) };
        };
        return p;
    }

    /*
     * Return 2-point array of vertices, second point normalised.
     * Note that the points may not be cyclical.
     * 
     * Refer to Figure.java and FigurePointFactory.java for details
     * 
     * @return the list of vertices
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
     * @param start the start point
     * @deprecated
     */
    @Deprecated
    public void setStartPoint(CanvasPoint start) {
        this.start = start;
    }

    /**
     * 
     * @return the start point
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getStartPoint() {
        return start;
    }

    /**
     * 
     * @param end the end point
     * @deprecated
     */
    @Deprecated
    public void setEndPoint(CanvasPoint end) {
        this.end = end;
    }

    /**
     * 
     * @return the end point
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getEndPoint() {
        return end;
    }

}
