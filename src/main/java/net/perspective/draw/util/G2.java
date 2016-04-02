/*
 * G2.java
 * 
 * Created on Apr 2, 2016 6:19:05 PM
 * 
 */
package net.perspective.draw.util;

/**
 *
 * @author ctipper
 */

public class G2 {

    /**
     * javafx.scene.paint.Color not serialisable
     * 
     * @param color javafx.scene.paint.Color
     * @return java.awt.Color
     */
    public static java.awt.Color fxToAwt(javafx.scene.paint.Color color){
        return new java.awt.Color((float) color.getRed(), 
            (float) color.getGreen(), 
            (float) color.getBlue(), 
            (float) color.getOpacity());
    }
    
    /**
     * javafx.scene.paint.Color not serialisable
     * 
     * @param color javafx.scene.paint.Color
     * @param opacity float
     * @return java.awt.Color
     */
    public static java.awt.Color fxToAwt(javafx.scene.paint.Color color, float opacity) {
        return new java.awt.Color((float) color.getRed(), 
            (float) color.getGreen(), 
            (float) color.getBlue(), opacity);
    }

    /**
     * javafx.scene.paint.Color not serialisable
     * 
     * @param color java.awt.Color
     * @return javafx.scene.paint.Color
     */
    public static javafx.scene.paint.Color awtToFx(java.awt.Color color){
        return new javafx.scene.paint.Color(color.getRed() / 255.0, 
            color.getGreen() / 255.0, 
            color.getBlue() / 255.0, 
            color.getAlpha() / 255.0);
    }

    /**
     * Prevent instance creation
     */
    private G2() {
    }

}
