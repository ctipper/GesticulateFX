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
     * Rotate coordinate by angle theta
     *
     * @param x
     * @param y
     * @param theta
     * @return a point
     */
    public static CanvasPoint rot(double x, double y, double theta) {
        CanvasPoint vec = new CanvasPoint();
        double rx, ry;

        rx = Math.cos(theta) * x - Math.sin(theta) * y;
        ry = Math.sin(theta) * x + Math.cos(theta) * y;

        vec.setLocation(rx, ry);

        return vec;
    }

    /**
     * Compute dot product of a and b
     *
     * @param a
     * @param b
     * @return dot product
     */
    public static double dot(CanvasPoint a, CanvasPoint b) {
        return a.x * b.x + a.y * b.y;
    }

    /**
     * Compute the L2, or euclidean, norm of p.
     *
     * @param p
     * @return norm of p
     */
    public static double L2(CanvasPoint p) {
        return Math.hypot(p.x, p.y);
    }

    /**
     * Compute the L2, or euclidean, norm of (x, y).
     *
     * @param x
     * @param y
     * @return norm of (x, y)
     */
    public static double L2(double x, double y) {
        return Math.hypot(x, y);
    }

    /**
     * Rotate p by 90 degrees
     *
     * @param p
     * @return a point
     */
    public static CanvasPoint rot90(CanvasPoint p) {
        return new CanvasPoint(-p.y, p.x);
    }

    /**
     * Compute angle of arc defined by (x, y)
     *
     * @param x
     * @param y
     * @return an angle defined in radians
     */
    public static double declination(double x, double y) {
        CanvasPoint A = new CanvasPoint(x, y);
        double h1 = V2.L2(A);
        CanvasPoint q1 = new CanvasPoint(A.x / h1, A.y / h1);
        CanvasPoint q2 = new CanvasPoint(1, 0);

        double cos_t = V2.dot(q1, q2);
        double sin_t = V2.dot(V2.rot90(q1), q2);

        return Math.atan2(sin_t, cos_t);
    }

    /**
     * Normalise angle defined in radians
     *
     * @param angle
     * @return norm of angle defined in radians
     */
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

    /**
     * Determine quadrant of an arc defined by angle
     * 2 | 1
     * -----
     * 3 | 4
     * @param angle
     * @return integer index
     */
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
     * Determine quarter of an arc defined by angle
     *  \2/
     * 3 - 1
     *  /4\
     * @param angle
     * @return integer index
     */
    public static int side(double angle) {
        int offset = -1;
        angle = V2.norm_angle(angle);
        if ((angle >= Math.PI / 4) && (angle < 3 * Math.PI / 4)) {
            offset = 0;
        }
        if ((angle >= 3 * Math.PI / 4) && (angle < Math.PI)) {
            offset = 1;
        }
        if ((angle >= -Math.PI) && (angle < -3 * Math.PI / 4)) {
            offset = 1;
        }
        if ((angle >= -3 * Math.PI / 4) && (angle < -Math.PI / 4)) {
            offset = 2;
        }
        if ((angle >= -Math.PI / 4) && (angle < Math.PI / 4)) {
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
