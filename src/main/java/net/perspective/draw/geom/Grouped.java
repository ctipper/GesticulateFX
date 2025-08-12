/*
 * Grouped.java
 * 
 * Created on Feb 1, 2012, 10:49:06 PM
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
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A structure for managing groups of shapes
 * 
 * @author ctipper
 */

public class Grouped implements DrawItem, Serializable {

    public List<DrawItem> shapes;
    private int transparency;
    private boolean isVertical;
    private double angle;
    private CanvasPoint start, end;

    private static final Logger logger = LoggerFactory.getLogger(Grouped.class.getName());

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>Grouped</code> */
    public Grouped() {
        transparency = 0;
        isVertical = false;
        angle = 0;
    }

    /**
     * Insert a list of shapes
     * 
     * @param shapes the list of {@link net.perspective.draw.geom.DrawItem}
     */
    public void setDrawItems(List<DrawItem> shapes) {
        this.shapes = shapes;
    }

    /**
     * Retrieve the list of shapes
     * 
     * @return shapes the list of {@link net.perspective.draw.geom.DrawItem}
     */
    public List<DrawItem> getDrawItems() {
        return shapes;
    }

    /**
     * Add a shape to the group
     * 
     * @param shape the {@link net.perspective.draw.geom.DrawItem}
     */
    public void addDrawItem(DrawItem shape) {
        if (shapes == null) {
            shapes = new ArrayList<>();
        }
        shapes.add(shape);
        this.setBounds();
    }

    /**
     * Remove a shape from the list
     * 
     * @param shape the {@link net.perspective.draw.geom.DrawItem}
     */
    public void removeShape(DrawItem shape) {
        if (shapes != null) {
            shapes.remove(shape);
            if (!shapes.isEmpty()) {
                this.setBounds();
            }
        }
    }

    /**
     * Set the bounds of the group taking into account orientation of all enclosed shapes
     */
    private void setBounds() {
        CanvasPoint topleft, bottomright;
        List<CanvasPoint> points = new ArrayList<>();

        for (DrawItem shape : shapes) {
            points.add(shape.getTop()[0]);
            points.add(shape.getBottom()[0]);
            points.add(shape.getUp()[0]);
            points.add(shape.getDown()[0]);
        }
        try {
            topleft = (CanvasPoint) points.get(0).clone();
            bottomright = (CanvasPoint) points.get(1).clone();

            for (CanvasPoint point : points) {
                topleft.x = Math.min(point.x, topleft.x);
                topleft.y = Math.min(point.y, topleft.y);
                bottomright.x = Math.max(point.x, bottomright.x);
                bottomright.y = Math.max(point.y, bottomright.y);
            }

            this.start = new CanvasPoint(topleft.x, topleft.y);
            this.end = new CanvasPoint(bottomright.x, bottomright.y);
        } catch (CloneNotSupportedException ex) {
            logger.error(null, ex);
        }
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
     * Update the item properties no properties set
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     */
    @Override
    public void updateProperties(DrawingArea drawarea) {
        // no properties to set
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-left corner location (transformed)
     */
    @Override
    public CanvasPoint[] getTop() {
        CanvasPoint s = new CanvasPoint(start.x, start.y);
        s = getTransform(s);
        return new CanvasPoint[] { s, s };
    }

    /**
     * Returns the 2-tuple of top-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getUp() {
        CanvasPoint up = new CanvasPoint(end.x, start.y);
        up = getTransform(up);
        return new CanvasPoint[] { up, up };
    }

    /**
     * Returns the 2-tuple of bottom-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-left corner location (transformed)
     */
    @Override
    public CanvasPoint[] getDown() {
        CanvasPoint down = new CanvasPoint(start.x, end.y);
        down = getTransform(down);
        return new CanvasPoint[] { down, down };
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    @Override
    public CanvasPoint[] getBottom() {
        CanvasPoint e = new CanvasPoint(end.x, end.y);
        e = this.getTransform(e);
        return new CanvasPoint[] { e, e };
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
     * rotate Group about centroid with optional 90 deg correction
     */
    private java.awt.geom.AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();
        java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
        transform.setToRotation(this.getAngle() + (this.isVertical() ? -Math.PI / 2 : 0), centre.x, centre.y);
        return transform;
    }

    /**
     * Returns the location of the picture centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    @Override
    public CanvasPoint rotationCentre() {
        return new CanvasPoint((end.x - start.x) / 2.0 + start.x, (end.y - start.y) / 2.0 + start.y);
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    @Override
    public java.awt.Shape bounds() {
        Rectangle2D rect = new Rectangle2D.Double(start.x, start.y, end.x - start.x, end.y - start.y);
        Area bounds = new Area(rect);
        java.awt.geom.AffineTransform transform = this.getTransform();
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
     * Translate the group
     * 
     * @param xinc  x increment
     * @param yinc  y increment
     */
    @Override
    public void moveTo(double xinc, double yinc) {
        shapes.stream().forEach((shape) -> {
            shape.moveTo(xinc, yinc);
        });
        this.setBounds();
    }

    /**
     * Provide a Group for FX canvas
     * 
     * @return the {@link javafx.scene.Node}
     */
    @Override
    public Node draw() {
        Group group = new Group();
        for (DrawItem shape : shapes) {
            group.getChildren().add(shape.draw());
        }
        CanvasPoint c = this.rotationCentre();
        group.getTransforms().add(new Rotate(this.angle * 180 / Math.PI, c.x, c.y));
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
        Group anchors = new Group();
        anchors.setMouseTransparent(true);
        anchors.getChildren().add(this.anchor(drawarea, start.x, start.y));
        anchors.getChildren().add(this.anchor(drawarea, end.x, start.y));
        anchors.getChildren().add(this.anchor(drawarea, start.x, end.y));
        anchors.getChildren().add(this.anchor(drawarea, end.x, end.y));
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
     * Render the item to the g2d canvas
     * 
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
     */
    @Override
    public void draw(Graphics2D g2) {
        java.awt.geom.AffineTransform defaultTransform;

        defaultTransform = g2.getTransform();
        g2.transform(this.getTransform());

        for (DrawItem shape : shapes) {
            shape.draw(g2);
        }

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
