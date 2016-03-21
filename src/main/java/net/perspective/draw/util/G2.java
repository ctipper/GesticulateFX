/*
 * G2.java
 * 
 * Created on Mar 21, 2016 1:35:22 PM
 * 
 */
package net.perspective.draw.util;

import java.awt.BasicStroke;
import java.awt.Stroke;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 *
 * @author ctipper
 */

public class G2 {

    /**
     * Prevent instance creation
     */
    private G2() {
    }

    public static double getLineWidth(BasicStroke stroke) {
        return (double) (stroke.getLineWidth());
    }
    
    public static StrokeLineJoin getLineJoin(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineJoin jfxjoin;
        switch (stroke.getLineJoin()) {
            case BasicStroke.JOIN_MITER:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.MITER;
                break;
            case BasicStroke.JOIN_ROUND:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
            case BasicStroke.JOIN_BEVEL:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.BEVEL;
                break;
            default:
                jfxjoin = javafx.scene.shape.StrokeLineJoin.ROUND;
                break;
        }
        return jfxjoin;
    }

    public static StrokeLineCap getLineCap(BasicStroke stroke) {
        javafx.scene.shape.StrokeLineCap jfxcap;
        switch (stroke.getEndCap()) {
            case BasicStroke.CAP_BUTT:
                jfxcap = javafx.scene.shape.StrokeLineCap.BUTT;
                break;
            case BasicStroke.CAP_ROUND:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
            case BasicStroke.CAP_SQUARE:
                jfxcap = javafx.scene.shape.StrokeLineCap.SQUARE;
                break;
            default:
                jfxcap = javafx.scene.shape.StrokeLineCap.ROUND;
                break;
        }
        return jfxcap;
    }

}
