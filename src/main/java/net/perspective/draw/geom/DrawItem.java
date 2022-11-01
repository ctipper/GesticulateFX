/*
 * DrawItem.java
 * 
 * A canvas item that renders itself and may be manipulated.
 * 
 * Created on Feb 1, 2012, 3:52:57 PM
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

import java.awt.Graphics2D;
import java.awt.Shape;
import javafx.scene.Node;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.util.CanvasPoint;

/**
 * 
 * @author ctipper
 */

public interface DrawItem {

    /**
     * Set the untransformed TL coordinate of the item
     * 
     * @param x the x position
     * @param y the y position
     */
    public void setStart(double x, double y);

    /**
     * Return the untransformed TL coordinate of the item
     * 
     * @return the item start point
     */
    public CanvasPoint getStart();

    /**
     * Set the dimensions of the item
     * 
     * @param x the width
     * @param y the height
     */
    public void setEnd(double x, double y);

    /**
     * Return the dimensions of the item
     * 
     * @return the dimensions
     */
    public CanvasPoint getEnd();

    /**
     * Update the item properties, such as color, stroke etc.
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     */
    void updateProperties(DrawingArea drawarea);

    /**
     * Returns the location of the item centre point
     * 
     * @return canvas coordinates of axis of rotation
     */
    CanvasPoint rotationCentre();

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    Shape bounds();

    /**
     * Detect if a point lies within the bounds, a convenience method
     * 
     * @param x canvas coordinate
     * @param y canvas coordinate
     * @return a boolean property
     */
    boolean contains(double x, double y);

    /**
     * Translate the item
     * 
     * @param xinc x increment
     * @param yinc y increment
     */
    void moveTo(double xinc, double yinc);

    /**
     * Sets the rotation angle
     * 
     * @param angle the angle in radians
     */
    void setAngle(double angle);

    /**
     * Return the rotation angle 
     * 
     * @return angle the angle in radians
     */
    double getAngle();

    /**
     * Sets the shape to be perpendicular to baseline
     * 
     * @param isVertical a boolean property
     * @deprecated 
     */
    @Deprecated
    void setVertical(boolean isVertical);

    /**
     * 
     * @return a boolean property
     * @deprecated 
     */
    @Deprecated
    boolean isVertical();

    /**
     * 
     * @return The 2-tuple of top-left corner location (transformed)
     *         second point may be normalised
     */
    CanvasPoint[] getTop();

    /**
     * 
     * @return The 2-tuple of top-right corner location (transformed)
     *         second point may be normalised
     */
    CanvasPoint[] getUp();

    /**
     * 
     * @return The 2-tuple of bottom-left corner location (transformed)
     *         second point may be normalised
     */
    CanvasPoint[] getDown();

    /**
     * 
     * @return The 2-tuple of bottom-right corner location (transformed)
     *         second point may be normalised
     */
    CanvasPoint[] getBottom();

    /**
     * Provide a Path for FX canvas
     * 
     * @return the {@link javafx.scene.Node}
     */
    Node draw();

    /**
     * Render the item anchors to indicate selection
     * 
     * @param drawarea the {@link net.perspective.draw.DrawingArea}
     * @return the {@link javafx.scene.Node}
     */
    Node drawAnchors(DrawingArea drawarea);

    /**
     * Render the item to the g2d canvas
     * 
     * @param g2 g2 graphics context {@link java.awt.Graphics2D}
     */
    void draw(Graphics2D g2);

}
