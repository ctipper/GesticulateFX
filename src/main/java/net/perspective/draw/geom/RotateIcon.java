/**
 * RotateIcon.java
 * 
 * Created on 22 Jul 2026 18:10:33
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw.geom;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * The rotation-handle glyph used to mark rotatable draw items.
 * <p>
 * Builds the icon as an SVG-path {@link javafx.scene.Group} and exposes its fixed
 * width and height, so callers can centre it without measuring the un-styled bounds.
 * The dimension constants are derived from the {@code SVG_PATH_*} geometry and must be
 * regenerated if those paths change.
 *
 * @author ctipper
 */

public final class RotateIcon {

    public static final double ROTATE_WIDTH = 13.123588562011719;   // derived from SVG_PATH_* bounds
    public static final double ROTATE_HEIGHT = 15.940189361572266;  // derived from SVG_PATH_* bounds
    private static final String SVG_PATH_1 = "m 11.118995,11.200627 c 1.126633,-1.117248 1.757431,-2.6401208 1.750779,-4.2267908 -0.0089,-2.133499 -1.144822,-4.103882 -2.9887851,-5.176743 -0.907089,-0.527771 -1.939377,-0.79954503 -2.988785,-0.80082703 -2.14859,-0.002639 -4.164795,1.10102903 -5.176743,2.98878503 -2.13767997,3.987778 0.658997,8.8315738 5.176743,8.9663548 0.698975,0.02084 1.394791,-0.101822 2.044479,-0.360459";
    private static final String SVG_PATH_2 = "M 11.118995,11.200627 10.122734,8.7099662";
    private static final String SVG_PATH_3 = "M 11.118995,11.200627 14.10778,10.702489";
    private static final String SVG_PATH_4 = "m 4.8996809,6.9738362 h 3.985046";
    private static final String SVG_PATH_5 = "M 6.8922039,8.9663592 V 4.9813132";
    private static final String SVG_PATH_6 = "m 6.8922039,12.951406 v 3.985046";

    private RotateIcon() {
        // static utility, not instantiable
    }

    /**
     * Build a fresh rotation-symbol icon.
     *
     * @return the icon as a {@link javafx.scene.Group} of SVG paths
     */
    public static Group path() {
        SVGPath path_1 = new SVGPath();
        path_1.setContent(SVG_PATH_1);
        path_1.getStyleClass().add("svgPath");
        path_1.setFill(Color.TRANSPARENT);
        SVGPath path_2 = new SVGPath();
        path_2.setContent(SVG_PATH_2);
        path_2.getStyleClass().add("svgPath");
        path_2.setFill(Color.TRANSPARENT);
        SVGPath path_3 = new SVGPath();
        path_3.setContent(SVG_PATH_3);
        path_3.getStyleClass().add("svgPath");
        path_3.setFill(Color.TRANSPARENT);
        SVGPath path_4 = new SVGPath();
        path_4.setContent(SVG_PATH_4);
        path_4.getStyleClass().add("svgThinPath");
        path_4.setFill(Color.TRANSPARENT);
        SVGPath path_5 = new SVGPath();
        path_5.setContent(SVG_PATH_5);
        path_5.getStyleClass().add("svgThinPath");
        path_5.setFill(Color.TRANSPARENT);
        SVGPath path_6 = new SVGPath();
        path_6.setContent(SVG_PATH_6);
        path_6.getStyleClass().add("svgPath");
        path_6.setFill(Color.TRANSPARENT);
        Group glyph = new Group();
        glyph.getChildren().addAll(path_1, path_2, path_3, path_4, path_5, path_6);
        return glyph;
    }

}
