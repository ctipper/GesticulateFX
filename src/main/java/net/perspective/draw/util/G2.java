/**
 * G2.java
 * 
 * Created on 29 Nov 2020 18:25:41
 * 
 */
package net.perspective.draw.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javax.inject.Inject;

/**
 *
 * @author ctipper
 */

public class G2 {

    /** Creates a new instance of <code>G2</code> */
    @Inject
    public G2() {
    }

    /**
     * Draw the background grid
     * 
     * @param isDarkMode is dark mode
     * @param bounds the canvas dimension
     * @return a group of {@link javafx.scene.shape.Line}
     */
    public Node drawGridLayout(boolean isDarkMode, CanvasPoint bounds) {
        Color minorColor, majorColor;
        /**
         * The width of a minor grid cell.
         */
        double width = 10;
        /**
         * The height of a minor grid cell.
         */
        double height = 10;
        if (isDarkMode) {
            /**
             * The colour for minor grid cells.
             */
            minorColor = Color.web("#414141");
            /**
             * The colour for major grid cells.
             */
            majorColor = Color.web("#484848");
        } else {
            /**
             * The colour for minor grid cells.
             */
            minorColor = Color.web("#f3f3f3");
            /**
             * The colour for major grid cells.
             */
            majorColor = Color.web("#d2d2d2");
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
