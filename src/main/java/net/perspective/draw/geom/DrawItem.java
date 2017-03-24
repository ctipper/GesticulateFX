/*
 * DrawItem.java
 * 
 * A canvas item that renders itself and may be manipulated.
 * 
 * Created on Feb 1, 2012, 3:52:57 PM
 * 
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
     * @param canvas
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
    void moveShape(double xinc, double yinc);

    /**
     * Sets the rotation angle
     * 
     * @param a The angle in radians
     */
    void setAngle(double angle);

    /**
     * Return the rotation angle 
     * 
     * @return angle
     */
    double getAngle();

    /**
     * Sets the shape to be perpendicular to baseline
     * 
     * @param isVertical a boolean property
     * @deprecated 
     */
    @SuppressWarnings("deprecation")
    void setVertical(boolean isVertical);

    /**
     * 
     * @return a boolean property
     * @deprecated 
     */
    @SuppressWarnings("deprecation")
    boolean isVertical();

    /**
     *  
     * @return The 2-tuple of top-left corner location (transformed)
     *         second point may be normalized
     */
    CanvasPoint[] getTop();

    /**
     *  
     * @return The 2-tuple of top-right corner location (transformed)
     *         second point may be normalized
     */
    CanvasPoint[] getUp();

    /**
     *  
     * @return The 2-tuple of bottom-left corner location (transformed)
     *         second point may be normalized
     */
    CanvasPoint[] getDown();

    /**
     * 
     * @return The 2-tuple of bottom-right corner location (transformed)
     *         second point may be normalized
     */
    CanvasPoint[] getBottom();

    /**
     * Render a shadowed version of the item without transforming
     * 
     * @return an FX Node
     */
    Node draw();

    /**
     * Render the item anchors to indicate selection
     * 
     * @return an FX Node
     */
    Node drawAnchors();

    /**
     * Render the item to the g2d canvas
     * 
     * @param g2
     */
    void draw(Graphics2D g2);

}
