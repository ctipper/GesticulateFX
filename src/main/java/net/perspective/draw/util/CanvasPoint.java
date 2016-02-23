/**
 * CanvasPoint.java
 *
 * Created on May 26, 2010, 3:16:19 PM
 *
 */
package net.perspective.draw.util;

/**
 *
 * @author ctipper
 */
import java.io.Serializable;

public class CanvasPoint implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    
    public double x, y;

    public CanvasPoint() {
        this(0.0, 0.0);
    }

    public CanvasPoint(double u, double v) {
        x = u;
        y = v;
    }

    public void setX(double u) {
        x = u;
    }

    public double getX() {
        return x;
    }

    public void setY(double v) {
        y = v;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double u, double v) {
        x = u;
        y = v;
    }

    public void translate(double u, double v) {
        x += u;
        y += v;
    }

    public void scale(double sx, double sy) {
        x *= sx;
        y *= sy;
    }
    
    public CanvasPoint scale(double scale) {
        return new CanvasPoint(x * scale, y * scale);
    }

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
