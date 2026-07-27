/*
 * Edge.java
 * 
 * Created on Mar 23, 2015 12:14:53 PM
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 * 
 * @author ctipper
 */

public class Edge extends Figure {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(Edge.class.getName());

    /** Creates a new instance of <code>Edge</code> */
    public Edge() {
        super();
    }

    /**
     * Creates a new instance of <code>Edge</code>
     * 
     * @param type the FigureType
     */
    @ConstructorProperties({"type"})
    public Edge(FigureType type) {
        super(type);
    }

    /**
     * Initialise points List
     * 
     * @param drawtype
     */
    @Override
    public void setPoints(DrawingType drawtype) {
        if (this.type.equals(FigureType.LINE)) {
            this.points = pointfactory.createPoints(drawtype, start.x, start.y, end.x, end.y);
        } else {
            this.points = new ArrayList<>();
        }
    }

    /**
     * Initialise end points
     */
    @Override
    public void setEndPoints() {
        try {
            switch (this.getType()) {
                case VECTOR -> {
                    var bounds = this.getPath().getBounds2D();
                    start = new CanvasPoint(bounds.getMinX(), bounds.getMinY());
                    end = new CanvasPoint(bounds.getMaxX(), bounds.getMaxY());
                }
                default -> {
                    if (points == null || points.isEmpty()) {
                        throw new IllegalStateException("Points list is null or empty for non-VECTOR type");
                    }

                    start = points.get(0);
                    end = points.get(points.size() - 1);
                }
            }
        } catch (IllegalStateException | NullPointerException | IndexOutOfBoundsException e) {
            logger.warn("Unexpected error in setEndPoints(): {}", e.getMessage());
            // Set defaults to avoid leaving them null
            start = new CanvasPoint(0, 0);
            end = new CanvasPoint(0, 0);
        }
    }

    /**
     * Mark the path dirty so it is rebuilt lazily on next {@link #getPath()} call.
     * VECTOR paths are managed externally and are never marked dirty here.
     */
    @Override
    public void setPath() {
        switch (this.getType()) {
            case VECTOR -> this.setClosed(true);
            case POLYGON -> { this.pathDirty = true; this.setClosed(true); }
            default -> { this.pathDirty = true; this.setClosed(false); }
        }
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
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY());
        s = new CanvasPoint[] { b, b };
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
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(), bound.getY());
        up = new CanvasPoint[] { b, b };
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
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX(), bound.getY() + bound.getHeight());
        down = new CanvasPoint[] { b, b };
        return down;
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * second point may be normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getBottom() {
        CanvasPoint e[];
        Rectangle2D bound = this.getBounds2D();
        CanvasPoint b = new CanvasPoint(bound.getX() + bound.getWidth(),
            bound.getY() + bound.getHeight());
        e = new CanvasPoint[] { b, b };
        return e;
    }

    /**
     * Returns the location of the figure centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    @Override
    public CanvasPoint rotationCentre() {
        CanvasPoint centre;
        Path2D.Double pa = (Path2D.Double) this.getPath().clone();
        pa.closePath();
        Area area = new Area(pa);
        Rectangle2D bound = area.getBounds2D();
        if (bound.getWidth() < 4.0 && bound.getHeight() < 4.0) {
            centre = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
        } else if (points.size() < 3) {
            centre = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
        } else {
            centre = new CanvasPoint(bound.getCenterX(), bound.getCenterY());
        }
        return centre;
    }

    /**
     * Translate the figure
     * 
     * @param xinc  x increment
     * @param yinc  y increment
     */
    @Override
    public void moveTo(double xinc, double yinc) {
        super.moveTo(xinc, yinc);
        if (this.type == FigureType.VECTOR) {
            setPath(this.translatePath(getPath(), xinc, yinc));
        }
    }

    private Path2D.Double translatePath(Path2D.Double path, double xinc, double yinc) {
        AffineTransform transform = AffineTransform.getTranslateInstance(xinc, yinc);
        return (Path2D.Double) path.createTransformedShape(transform);
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    @Override
    public java.awt.Shape bounds() {
        Shape bounds;
        Rectangle2D rectangle;

        if (type.equals(FigureType.LINE)) {
            // to give line extent
            double length = V2.L2(new CanvasPoint(end.x - start.x, end.y - start.y));
            rectangle = new Rectangle2D.Double(-2.0, -2.0, length + 4.0, 4.0);
            double a = Math.atan2(end.y - start.y, end.x - start.x);

            Area area = new Area(rectangle);
            java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
            transform.setToTranslation(start.x, start.y);
            transform.rotate(a);
            area.transform(transform);
            bounds = area;
        } else {
            bounds = super.bounds();
            // to give zero bound shapes extent
            Rectangle2D boundingBox = bounds.getBounds2D();
            if (boundingBox.getWidth() < 4.0 && boundingBox.getHeight() < 4.0) {
                rectangle = new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0);
                Area area = new Area(rectangle);
                AffineTransform transform = new AffineTransform();
                transform.setToTranslation(start.x, start.y);
                area.transform(transform);
                bounds = area;
            }
        }
        return bounds;
    }

    /**
     * Return the rotation angle
     * 
     * @return angle the angle in radians
     */
    @Override
    public double getAngle() {
        if (!this.getType().equals(FigureType.LINE)) {
            return angle;
        } else {
            CanvasPoint centre = new CanvasPoint(.5 * (start.x + end.x), .5 * (start.y + end.y));
            CanvasPoint A = new CanvasPoint(1, 0);
            CanvasPoint B = new CanvasPoint(end.x - centre.x, end.y - centre.y);

            double h1 = V2.L2(A);
            CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
            double h2 = V2.L2(B);
            CanvasPoint q2 = new CanvasPoint(B.x / h2, B.y / h2);

            double cos_t = V2.dot(q1, q2);
            double sin_t = V2.dot(V2.rot90(q1), q2);

            double theta = Math.atan2(sin_t, cos_t);

            return theta;
        }
    }

    @Override
    public Shape getRotateBounds() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, RotateIcon.ROTATE_WIDTH, RotateIcon.ROTATE_HEIGHT);
        Area bounds = new Area(rect);
        AffineTransform transform = new AffineTransform();
        switch (this.getType()) {
            case SKETCH, POLYGON -> {
                Path2D.Double p = this.getPath();
                CanvasPoint centre = this.rotationCentre();
                double top = p.getBounds2D().getMinY();
                double x = centre.x;
                double y = centre.y;
                // step out along the up-vector until the winding number is zero
                // (outside the outline); cap at the top of the bounds
                while (y > top && p.contains(x, y)) {
                    y -= 4.0;
                }
                transform.translate(x, y);
                if (start.y > end.y) {
                    transform.rotate(Math.PI);
                }
                transform.translate(-RotateIcon.ROTATE_WIDTH / 2, -RotateIcon.ROTATE_HEIGHT - 6);
            }
            case LINE -> {
                transform.setToTranslation(start.x, start.y);
                if (start.y > end.y) {
                    transform.rotate(Math.PI);
                }
                transform.translate(-RotateIcon.ROTATE_WIDTH / 2, -RotateIcon.ROTATE_HEIGHT - 6);
            }
            default -> {
                transform.setToTranslation((start.x + end.x) / 2, start.y);
                if (start.y > end.y) {
                    transform.rotate(Math.PI);
                }
                transform.translate(-RotateIcon.ROTATE_WIDTH / 2, -RotateIcon.ROTATE_HEIGHT - 6);
            }
        }
        bounds.transform(transform);
        transform = this.getTransform();
        bounds.transform(transform);
        return bounds;
    }

    /**
     * Create the rotation-handle anchor for an edge.
     * <p>
     * Placement depends on the figure type. For {@code SKETCH} and {@code POLYGON}
     * the pivot starts at the rotation centre and steps up the up-vector until it
     * leaves the outline (winding number zero), capped at the top of the bounds.
     * For {@code LINE} it is anchored at the start point. Other types fall back to
     * {@link Figure#rotateAnchor()}. In each case the glyph is transformed to follow
     * the shape's rotation, then centred horizontally and lifted clear of the edge.
     *
     * @return the rotation handle {@link javafx.scene.Group}
     */
    @Override
    protected javafx.scene.Group rotateAnchor() {
        javafx.scene.Group glyph = RotateIcon.path();
        switch (this.getType()) {
            case SKETCH, POLYGON -> {
                Path2D.Double p = this.getPath();
                CanvasPoint centre = this.rotationCentre();
                double top = p.getBounds2D().getMinY();
                double x = centre.x;
                double y = centre.y;
                // step out along the up-vector until the winding number is zero
                // (outside the outline); cap at the top of the bounds
                while (y > top && p.contains(x, y)) {
                    y -= 4.0;
                }
                // transform the pivot so the anchor follows the shape's rotation
                CanvasPoint u = this.getTransform(new CanvasPoint(x, y));
                glyph.getTransforms().add(new Translate(u.x, u.y));
                glyph.getTransforms().add(new Rotate(this.angle * 180 / Math.PI, 0, 0));
                if (start.y > end.y) {
                    glyph.getTransforms().add(new Rotate(180, 0, 0));
                }
                glyph.getTransforms().add(new Translate(-RotateIcon.ROTATE_WIDTH / 2, -RotateIcon.ROTATE_HEIGHT - 6));
            }
            case LINE -> {
                // end points marked
                glyph.getTransforms().add(new Translate(start.x, start.y));
                if (start.y > end.y) {
                    glyph.getTransforms().add(new Rotate(180, 0, 0));
                }
                glyph.getTransforms().add(new Translate(-RotateIcon.ROTATE_WIDTH / 2, -RotateIcon.ROTATE_HEIGHT - 6));
            }
            default -> glyph = super.rotateAnchor();
        }
        return glyph;
    }

}
