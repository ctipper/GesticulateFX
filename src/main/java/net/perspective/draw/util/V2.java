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

    // Compute the L2, or euclidean, norm of (x, y).
    public static double L2(double x, double y) {
        return Math.hypot(x, y);
    }

    public static CanvasPoint rot90(CanvasPoint p) {
        return new CanvasPoint(-p.y, p.x);
    }

    public static double declination(double x, double y) {
        CanvasPoint A = new CanvasPoint(x, y);
        double h1 = V2.L2(A);
        CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
        CanvasPoint q2 = new CanvasPoint(1, 0);

        double cos_t = V2.dot(q1, q2);
        double sin_t = V2.dot(V2.rot90(q1), q2);

        return Math.atan2(sin_t, cos_t);
    }

    public static double norm_angle(double angle) {
        // condition angle
        while (angle > Math.PI || angle < -Math.PI) {
            if (angle > Math.PI) {
                angle -= 2 * Math.PI;
            }
            if (angle < -Math.PI) {
                angle += 2 * Math.PI;
            }
        }
        return angle;
    }

    public static int quadrant(double angle) {
        int offset = -1;
        angle = V2.norm_angle(angle);
        
        if ((angle >= 0) && (angle < Math.PI / 2)) {
            offset = 0;
        }
        if ((angle >= Math.PI / 2) && (angle < Math.PI)) {
            offset = 1;
        }
        if (((angle >= -Math.PI) && (angle < -Math.PI / 2))
            || (Math.abs(angle) == Math.PI)) {
            offset = 2;
        }
        if ((angle >= -Math.PI / 2) && (angle < 0)) {
            offset = 3;
        }
        return offset;
    }

    /**
     * Prevent instance creation
     */
    private V2() {
    }

}
