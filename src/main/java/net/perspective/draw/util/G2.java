/**
 * G2.java
 * 
 * Created on 29 Nov 2020 18:25:41
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
package net.perspective.draw.util;

import java.util.Arrays;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.TextController;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.text.Editor;

/**
 *
 * @author ctipper
 */

@Singleton
public class G2 {
    
    private final Provider<DrawingArea> drawareaProvider;
    private final Provider<ApplicationController> applicationProvider;
    private final Provider<TextController> textControllerProvider;

    /** 
     * Creates a new instance of <code>G2</code>
     * 
     * @param drawareaProvider 
     * @param applicationProvider
     * @param textControllerProvider 
     */
    @Inject
    public G2(Provider<DrawingArea> drawareaProvider, 
            Provider<ApplicationController> applicationProvider,
            Provider<TextController> textControllerProvider) {
        this.drawareaProvider = drawareaProvider;
        this.applicationProvider = applicationProvider;
        this.textControllerProvider = textControllerProvider;
    }

    /**
     * Highlight the text control or provide a visual cursor
     *
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @return a {@link javafx.scene.Group} of highlight paths, one per paragraph
     */
    @SuppressWarnings("deprecation")
    public Group highlightText(DrawItem item) {
        Group highlightGroup = new Group();
        Editor editor = textControllerProvider.get().getEditor();
        Group layout = drawareaProvider.get().getTextLayout(item);
        int caretStart = editor.getCaretStart();
        int caretEnd = editor.getCaretEnd();
        boolean isCaret = (caretStart == caretEnd);
        int offset = 0;
        var children = layout.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof TextFlow tf) {
                Bounds bounds = tf.getBoundsInParent();
                int tfLen = Boolean.TRUE.equals(tf.getProperties().get("empty")) ? 0 : paragraphLength(tf);
                boolean isLast = (i == children.size() - 1);
                Path path = new Path();
                if (isCaret) {
                    if (caretStart >= offset && (caretStart <= offset + tfLen || isLast)) {
                        PathElement[] carets = tf.getCaretShape(caretStart - offset, true);
                        path.getElements().addAll(Arrays.asList(carets));
                        path.setStroke(Color.BLACK);
                        path.setFill(Color.BLACK);
                    }
                } else {
                    if (caretEnd > offset && caretStart < offset + tfLen) {
                        int localStart = Math.max(0, caretStart - offset);
                        int localEnd = Math.min(tfLen, caretEnd - offset);
                        PathElement[] range = tf.getRangeShape(localStart, localEnd, false);
                        path.getElements().addAll(Arrays.asList(range));
                        path.setStroke(Color.TRANSPARENT);
                        path.setFill(Color.rgb(110, 165, 232, 0.3));
                    }
                }
                if (!path.getElements().isEmpty()) {
                    path.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    path.setStrokeLineCap(StrokeLineCap.ROUND);
                    path.setStrokeWidth(1);
                    path.getTransforms().add(new Translate(bounds.getMinX(), bounds.getMinY()));
                    highlightGroup.getChildren().add(path);
                }
                offset += tfLen + (isLast ? 0 : 1);
            }
        }
        CanvasPoint axis = item.rotationCentre();
        highlightGroup.getTransforms().add(new Rotate((item.getAngle() + (item.isVertical() ? -Math.PI / 2 : 0)) * 180 / Math.PI, axis.x, axis.y));
        highlightGroup.getTransforms().add(new Translate(axis.x, axis.y));
        return highlightGroup;
    }

    private int paragraphLength(TextFlow tf) {
        return tf.getChildren().stream()
            .filter(n -> n instanceof javafx.scene.text.Text)
            .mapToInt(n -> ((javafx.scene.text.Text) n).getText().length())
            .sum();
    }

    /**
     * Draw the background grid
     * 
     * @param bounds the canvas dimension
     * @return a group of {@link javafx.scene.shape.Line}
     */
    public Node drawGridLayout(CanvasPoint bounds) {
        Color minorColor, majorColor;
        /**
         * The width of a minor grid cell.
         */
        double width = 10;
        /**
         * The height of a minor grid cell.
         */
        double height = 10;
        Color c = Color.web(applicationProvider.get().getCanvasBackgroundColor());
        int r = (int) (c.getRed() * 255);
        int g = (int) (c.getGreen() * 255);
        int b = (int) (c.getBlue() * 255);
        int rgb = (r << 16) + (g << 8) + b;
        if (rgb < 0x80_8080) {
            /**
             * The colour for minor grid cells.
             */
            minorColor = new Color(((r <= 0xf8) ? r + 0x07 : 0xff) / 255.0, ((g <= 0xf8) ? g + 0x07 : 0xff) / 255.0, ((b <= 0xf8) ? b + 0x07 : 0xff) / 255.0, 1d);
            /**
             * The colour for major grid cells.
             */
            majorColor = new Color(((r <= 0xf1) ? r + 0x0e : 0xff) / 255.0, ((g <= 0xf1) ? g + 0x0e : 0xff) / 255.0, ((b <= 0xf1) ? b + 0x0e : 0xff) / 255.0, 1d);
        } else {
            /**
             * The colour for minor grid cells.
             */
            minorColor = new Color(((r >= 0x0c) ? r - 0x0c : 0) / 255.0, ((g >= 0x0c) ? g - 0x0c : 0) / 255.0, ((b >= 0x0c) ? b - 0x0c : 0) / 255.0, 1d);
            /**
             * The colour for major grid cells.
             */
            majorColor = new Color(((r >= 0x2d) ? r - 0x2d : 0) / 255.0, ((g >= 0x2d) ? g - 0x2d : 0) / 255.0, ((b >= 0x2d) ? b - 0x2d : 0) / 255.0, 1d);
        }
        /**
         * The spacing factor for a major grid cell.
         */
        int majorGridSpacing = 5;

        CanvasPoint origin = new CanvasPoint(0, 0);
        CanvasPoint point = new CanvasPoint();
        Group gridarea = new Group();
        gridarea.setMouseTransparent(true);
        gridarea.setId("gridArea");

        /**
         * vertical grid lines are only drawn, if they are at least two pixels apart on the view
         * coordinate system.
         */
        for (int i = (int) (origin.x / width), m = (int) ((origin.x + bounds.x) / width) + 1; i <= m; i++) {
            Color color = ((i % majorGridSpacing == 0) ? majorColor : minorColor);

            point.x = width * i;
            Line line = new Line((int) point.x, (int) 0,
                    (int) point.x, (int) (bounds.y));
            line.setStroke(color);
            line.setStrokeWidth(1.0);
            gridarea.getChildren().add(line);
        }

        /**
         * horizontal grid lines are only drawn, if they are at least two pixels apart on the view
         * coordinate system.
         */
        for (int i = (int) (origin.y / height), m = (int) ((origin.y + bounds.y) / height) + 1; i <= m; i++) {
            Color color = ((i % majorGridSpacing == 0) ? majorColor : minorColor);

            point.y = height * i;
            Line line = new Line((int) 0, (int) point.y,
                    (int) (bounds.x), (int) point.y);
            line.setStroke(color);
            line.setStrokeWidth(1.0);
            gridarea.getChildren().add(line);
        }

        return gridarea;
    }
}
