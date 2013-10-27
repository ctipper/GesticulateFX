/*
 * V2.java
 * 
 * Vector geometry primitives
 * 
 * Created on 15-Nov-2009, 13:34:54
 * 
 */
package net.perspective.draw.util;

/**
 *
 * @author ctipper
 */
public class V2 {

    /**
     * Prevent instance creation
     */
    private V2() {
    }

    public static CanvasPoint rot(double x, double y, double theta) {
        CanvasPoint vec = new CanvasPoint();
        double rx, ry;

        rx = Math.cos(theta) * x - Math.sin(theta) * y;
        ry = Math.sin(theta) * x + Math.cos(theta) * y;

        vec.setLocation(rx, ry);

        return vec;
    }

    public static double dot(CanvasPoint a, CanvasPoint b) {
        return a.x * b.x + a.y * b.y;
    }

    // Compute the L2, or euclidean, norm of p.
    public static double L2(CanvasPoint p) {
        return Math.hypot(p.x, p.y);
    }

    public static CanvasPoint rot90(CanvasPoint p) {
        return new CanvasPoint(-p.y, p.x);
    }
}
