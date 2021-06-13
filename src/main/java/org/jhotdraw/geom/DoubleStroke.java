/*
 * @(#)DoubleStroke.java
 * 
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw. MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.jhotdraw.geom;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

/**
 * Draws a double stroke (an outline of an outline).
 * The inner width of a DoubleStroke defines the distance between the two
 * outlines being drawn. The outline width of a DoubleStroke defines the
 * thickness of the outline.
 * 
 * @author Werner Randelshofer
 * @version $Id: DoubleStroke.java 785 2013-12-01 19:16:30Z rawcoder $
 */
public class DoubleStroke implements Stroke, Serializable {

    transient private BasicStroke outlineStroke;
    private double innerWidth;
    private double outlineWidth;
    private double miterLimit;
    private float[] dashes;
    private float dashPhase;

    private static final long serialVersionUID = 1L;

    public DoubleStroke(double innerWidth, double outlineWidth) {
        this(innerWidth, outlineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 10f, null, 0f);
    }

    public DoubleStroke(double innerWidth, double outlineWidth, int cap, int join, double miterLimit, float[] dashes, float dashPhase) {
        this.innerWidth = innerWidth;
        this.outlineWidth = outlineWidth;
        this.miterLimit = miterLimit;
        // outlineStroke = new BasicStroke(outlineWidth, cap, join, miterLimit, dashes, dashPhase);
        outlineStroke = new BasicStroke((float) outlineWidth, cap, BasicStroke.JOIN_BEVEL, (float) miterLimit, dashes, dashPhase);
    }

    @Override
    public Shape createStrokedShape(Shape s) {
        BezierPath bp = new BezierPath();
        Path2D.Double left = new Path2D.Double();
        Path2D.Double right = new Path2D.Double();

        double[] coords = new double[6];
        // FIXME - We only do a flattened path
        for (PathIterator i = s.getPathIterator(null, 0.1d); !i.isDone(); i.next()) {
            int type = i.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    if (bp.size() != 0) {
                        traceStroke(bp, left, right);
                    }
                    bp.clear();
                    bp.setClosed(false);
                    bp.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    if (coords[0] != bp.get(bp.size() - 1).x[0]
                            || coords[1] != bp.get(bp.size() - 1).y[0]) {
                        bp.lineTo(coords[0], coords[1]);
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    bp.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    bp.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    bp.setClosed(true);
                    break;
            }
        }
        if (bp.size() != 0) {
            traceStroke(bp, left, right);
        }

        // Note: This could be extended to use different stroke objects for
        // the inner and the outher path.
        right.append(left, false);
        return outlineStroke.createStrokedShape(right);

    }

    protected void traceStroke(BezierPath bp, Path2D.Double left, Path2D.Double right) {
        // XXX - We only support straight line segments here
        // Corners of the current and the previous thick line
        double[] currentCorners = new double[8];
        double[] prevCorners = new double[8];

        Point2D.Double intersect;

        // Remove duplicate nodes from bezier path.
        if (bp.isClosed()) {
            BezierPath.Node prev = bp.get(bp.size() - 1);
            for (int i = 0; i < bp.size(); i++) {
                BezierPath.Node node = bp.get(i);
                if (prev.x[0] == node.x[0] && prev.y[0] == node.y[0]) {
                    bp.remove(i--);

                } else {
                    prev = node;
                }
            }
        } else {
            BezierPath.Node prev = bp.get(0);
            for (int i = 1; i < bp.size(); i++) {
                BezierPath.Node node = bp.get(i);
                if (prev.x[0] == node.x[0] && prev.y[0] == node.y[0]) {
                    bp.remove(i--);

                } else {
                    prev = node;
                }
            }
        }

        // Handle the first point of the bezier path
        if (bp.isClosed() && bp.size() > 1) {
            prevCorners = computeThickLine(
                    bp.get(bp.size() - 1).x[0], bp.get(bp.size() - 1).y[0],
                    bp.get(0).x[0], bp.get(0).y[0],
                    innerWidth, prevCorners);
            currentCorners = computeThickLine(
                    bp.get(0).x[0], bp.get(0).y[0],
                    bp.get(1).x[0], bp.get(1).y[0],
                    innerWidth, currentCorners);

            intersect = Geom.intersect(
                    prevCorners[0], prevCorners[1],
                    prevCorners[4], prevCorners[5],
                    currentCorners[0], currentCorners[1],
                    currentCorners[4], currentCorners[5], miterLimit);

            if (intersect != null) {
                right.moveTo(intersect.x, intersect.y);
            } else {
                right.moveTo(prevCorners[4], prevCorners[5]);
                right.lineTo(currentCorners[0], currentCorners[1]);
            }

            intersect = Geom.intersect(
                    prevCorners[2], prevCorners[3],
                    prevCorners[6], prevCorners[7],
                    currentCorners[2], currentCorners[3],
                    currentCorners[6], currentCorners[7], miterLimit);
            if (intersect != null) {
                left.moveTo(intersect.x, intersect.y);
            } else {
                left.moveTo(prevCorners[6], prevCorners[7]);
                left.lineTo(currentCorners[2], currentCorners[3]);
            }
        } else {
            if (bp.size() > 1) {
                currentCorners = computeThickLine(
                        bp.get(0).x[0], bp.get(0).y[0],
                        bp.get(1).x[0], bp.get(1).y[0],
                        innerWidth, currentCorners);
                right.moveTo(currentCorners[0], currentCorners[1]);
                left.moveTo(currentCorners[2], currentCorners[3]);
            }
        }

        // Handle points in the middle of the bezier path
        for (int i = 1, n = bp.size() - 1; i < n; i++) {
            double[] tmp = prevCorners;
            prevCorners = currentCorners;
            currentCorners = computeThickLine(
                    bp.get(i).x[0], bp.get(i).y[0],
                    bp.get(i + 1).x[0], bp.get(i + 1).y[0],
                    innerWidth, tmp);
            intersect = Geom.intersect(
                    prevCorners[0], prevCorners[1],
                    prevCorners[4], prevCorners[5],
                    currentCorners[0], currentCorners[1],
                    currentCorners[4], currentCorners[5], miterLimit);
            if (intersect != null) {
                right.lineTo(intersect.x, intersect.y);
            } else {
                right.lineTo(prevCorners[4], prevCorners[5]);
                right.lineTo(currentCorners[0], currentCorners[1]);
            }

            intersect = Geom.intersect(
                    prevCorners[2], prevCorners[3],
                    prevCorners[6], prevCorners[7],
                    currentCorners[2], currentCorners[3],
                    currentCorners[6], currentCorners[7], miterLimit);
            if (intersect != null) {
                left.lineTo(intersect.x, intersect.y);
            } else {
                left.lineTo(prevCorners[6], prevCorners[7]);
                left.lineTo(currentCorners[2], currentCorners[3]);
            }
        }

        // Handle the last point of the bezier path
        if (bp.isClosed() && bp.size() > 0) {
            double[] tmp = prevCorners;
            prevCorners = currentCorners;
            currentCorners = computeThickLine(
                    bp.get(bp.size() - 1).x[0], bp.get(bp.size() - 1).y[0],
                    bp.get(0).x[0], bp.get(0).y[0],
                    //bp.get(1).x[0], bp.get(1).y[0],
                    innerWidth, tmp);
            intersect = Geom.intersect(
                    prevCorners[0], prevCorners[1],
                    prevCorners[4], prevCorners[5],
                    currentCorners[0], currentCorners[1],
                    currentCorners[4], currentCorners[5], miterLimit);
            if (intersect != null) {
                right.lineTo(intersect.x, intersect.y);
            } else {
                right.lineTo(prevCorners[4], prevCorners[5]);
                right.lineTo(currentCorners[0], currentCorners[1]);
            }

            intersect = Geom.intersect(
                    prevCorners[2], prevCorners[3],
                    prevCorners[6], prevCorners[7],
                    currentCorners[2], currentCorners[3],
                    currentCorners[6], currentCorners[7], miterLimit);
            if (intersect != null) {
                left.lineTo(intersect.x, intersect.y);
            } else {
                left.lineTo(prevCorners[6], prevCorners[7]);
                left.lineTo(currentCorners[2], currentCorners[3]);
            }

            right.closePath();
            left.closePath();

        } else {
            if (bp.size() > 1) {
                right.lineTo(currentCorners[4], currentCorners[5]);
                left.lineTo(currentCorners[6], currentCorners[7]);
            }
        }
    }

    private double[] computeThickLine(double[] seg, int offset, double corners[]) {
        return computeThickLine(seg[0 + offset], seg[1 + offset], seg[2 + offset], seg[3 + offset], innerWidth, corners);
    }

    private double[] computeThickLine(double x1, double y1, double x2, double y2, double thickness, double corners[]) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        // line length
        double lineLength = Math.sqrt(dx * dx + dy * dy);

        double scale = thickness / (2d * lineLength);

        // The x and y increments from an endpoint needed to create a rectangle...
        double ddx = -scale * dy;
        double ddy = scale * dx;
        /*
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
         */
        // Now we can compute the corner points...
        corners[0] = x1 + ddx;
        corners[1] = y1 + ddy;
        corners[2] = x1 - ddx;
        corners[3] = y1 - ddy;
        corners[4] = x2 + ddx;
        corners[5] = y2 + ddy;
        corners[6] = x2 - ddx;
        corners[7] = y2 - ddy;

        return corners;
    }

    public void setInnerWidth(float i) {
        innerWidth = i;
    }

    public double getInnerWidth() {
        return innerWidth;
    }

    public void setOutlineWidth(float o) {
        outlineWidth = o;
    }

    public double getOutlineWidth() {
        return outlineWidth;
    }

    public void setMiterLimit(double m) {
        miterLimit = m;
    }

    public double getMiterLimit() {
        return miterLimit;
    }

    public void setDashes(float[] d) {
        dashes = d;
    }

    public float[] getDashes() {
        return dashes;
    }

    public void setDashPhase(float p) {
       dashPhase = p;
    }

    public float getDashPhase() {
        return dashPhase;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        outlineStroke = (BasicStroke) readStroke(in);
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        writeStroke(outlineStroke, out);
    }

    public static Stroke readStroke(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        Stroke result = null;
        boolean isNull = stream.readBoolean();
        if (!isNull) {
            Class<?> c = (Class<?>) stream.readObject();
            if (c.equals(BasicStroke.class)) {
                float width = stream.readFloat();
                int cap = stream.readInt();
                int join = stream.readInt();
                float miterLimit = stream.readFloat();
                float[] dash = (float[]) stream.readObject();
                float dashPhase = stream.readFloat();
                result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
            } else {
                result = (Stroke) stream.readObject();
            }
        }
        return result;
   }

    public static void writeStroke(Stroke stroke,
            ObjectOutputStream stream) throws IOException {

        if (stroke != null) {
            stream.writeBoolean(false);
            if (stroke instanceof BasicStroke) {
                BasicStroke s = (BasicStroke) stroke;
                stream.writeObject(BasicStroke.class);
                stream.writeFloat(s.getLineWidth());
                stream.writeInt(s.getEndCap());
                stream.writeInt(s.getLineJoin());
                stream.writeFloat(s.getMiterLimit());
                stream.writeObject(s.getDashArray());
                stream.writeFloat(s.getDashPhase());
            } else {
                stream.writeObject(stroke.getClass());
                stream.writeObject(stroke);
            }
        } else {
            stream.writeBoolean(true);
        }
    }

}
