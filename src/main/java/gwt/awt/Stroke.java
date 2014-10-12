/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package gwt.awt;

import java.io.Serializable;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public abstract class Stroke implements Serializable {

    protected float line_width;
    protected int line_join;
    protected int end_cap;
    
    private static final long serialVersionUID = 1L;

    /**
     * Joins path segments by extending their outside edges until
     * they meet.
     */
    public final static int JOIN_MITER = 0;

    /**
     * Joins path segments by rounding off the corner at a radius
     * of half the line width.
     */
    public final static int JOIN_ROUND = 1;

    /**
     * Joins path segments by connecting the outer corners of their
     * wide outlines with a straight segment.
     */
    public final static int JOIN_BEVEL = 2;

    /**
     * Ends unclosed subpaths and dash segments with no added
     * decoration.
     */
    public final static int CAP_BUTT = 0;

    /**
     * Ends unclosed subpaths and dash segments with a round
     * decoration that has a radius equal to half of the width
     * of the pen.
     */
    public final static int CAP_ROUND = 1;

    /**
     * Ends unclosed subpaths and dash segments with a square
     * projection that extends beyond the end of the segment
     * to a distance equal to half of the line width.
     */
    public final static int CAP_SQUARE = 2;

    public float getLineWidth() {
        return line_width;
    }

    public int getEndCap() {
        return end_cap;
    }

    public int getLineJoin() {
        return line_join;
    }
    
    public StrokeLineJoin getJoin() {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        switch (line_join) {
            case JOIN_MITER:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.MITER;
                break;
            case JOIN_ROUND:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
            case JOIN_BEVEL:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.BEVEL;
                break;
            default:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
        }
        return jfxjoin;
    }

    public StrokeLineCap getCap() {
        javafx.scene.shape.StrokeLineCap jfxcap;
        switch (end_cap) {
            case CAP_BUTT:
                jfxcap = javafx.scene.shape.StrokeLineCap.BUTT;
                break;
            case CAP_ROUND:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
            case CAP_SQUARE:
                jfxcap = javafx.scene.shape.StrokeLineCap.SQUARE;
                break;
            default:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
        }
        return jfxcap;
    }

}
