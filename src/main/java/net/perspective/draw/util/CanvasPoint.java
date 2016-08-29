/**
 * CanvasPoint.java
 *
 * Created on May 26, 2010, 3:16:19 PM
 *
 */
package net.perspective.draw.util;

import java.io.Serializable;

/**
 *
 * @author ctipper
 */

public class CanvasPoint implements Serializable, Cloneable {

    /**
     * x coordinate
     */
    public double x;

    /**
     * y coordinate
     */
    public double y;

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>CanvasPoint</code> */
    public CanvasPoint() {
        this(0.0, 0.0);
    }

    /**
     * Create new <code>CanvasPoint</code>
     * 
     * @param u x coordinate
     * @param v y coordinate
     */
    public CanvasPoint(double u, double v) {
        x = u;
        y = v;
    }

    /**
     * Set x coordinate
     * 
     * @param u
     */
    public void setX(double u) {
        x = u;
    }

    /**
     * Return x coordinate
     * 
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * Set y coordinate
     * 
     * @param v
     */
    public void setY(double v) {
        y = v;
    }

    /**
     * Return y coordinate
     * 
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * Set (x,y) position
     * 
     * @param u
     * @param v
     */
    public void setLocation(double u, double v) {
        x = u;
        y = v;
    }

    /**
     * Translate this instance
     * 
     * @param u x shift
     * @param v y shift
     */
    public void translate(double u, double v) {
        x += u;
        y += v;
    }

    /**
     * Scale this instance
     * 
     * @param sx x scale
     * @param sy y scale
     */
    public void scale(double sx, double sy) {
        x *= sx;
        y *= sy;
    }

    /**
     * Scale an instance uniformly
     * 
     * @param scale
     * @return an instance
     */
    public CanvasPoint grow(double scale) {
        x *= scale;
        y *= scale;
        return this;
    }

    /**
     * Translate an instance
     * 
     * @param dx x shift
     * @param dy y shift
     * @return a shifted instance
     */
    public CanvasPoint shifted(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    /**
     * Stop coordinate undershooting
     * 
     * @return a floored instance
     */
    public CanvasPoint floor() {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        return this;
    }

    /**
     * Rotate this instance
     * 
     * @param angle a radian angle
     */
    public void rotate(double angle) {
        CanvasPoint point = V2.rot(x, y, angle);
        x = point.x;
        y = point.y;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return "[x: " + String.valueOf(x) + " y: " + String.valueOf(y) + "]";
    }

}