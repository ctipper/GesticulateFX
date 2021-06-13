/*
 * @(#)Shapes.java
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

import java.awt.Shape;
import java.awt.geom.*;

/**
 * Shapes.
 * 
 * @author Werner Randelshofer
 * @version $Id: Shapes.java 785 2013-12-01 19:16:30Z rawcoder $
 */
public class Shapes {

    /** Creates a new instance. */
    private Shapes() {
    }

    /**
     * Returns true, if the outline of this bezier path contains the specified
     * point.
     * 
     * @param p The point to be tested.
     * @param tolerance The tolerance for the test.
     */
    public static boolean outlineContains(Shape shape, Point2D.Double p, double tolerance) {
        double[] coords = new double[6];
        double prevX = 0, prevY = 0;
        double moveX = 0, moveY = 0;
        for (PathIterator i = new FlatteningPathIterator(shape.getPathIterator(new AffineTransform(), tolerance), tolerance); !i.isDone(); i.next()) {
            switch (i.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE:
                    if (Geom.lineContainsPoint(
                            prevX, prevY, moveX, moveY,
                            p.x, p.y, tolerance)) {
                        return true;
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    break;
                case PathIterator.SEG_LINETO:
                    if (Geom.lineContainsPoint(
                            prevX, prevY, coords[0], coords[1],
                            p.x, p.y, tolerance)) {
                        return true;
                    }
                    break;
                case PathIterator.SEG_MOVETO:
                    moveX = coords[0];
                    moveY = coords[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    break;
                default:
                    break;
            }
            prevX = coords[0];
            prevY = coords[1];
        }
        return false;
    }

}

