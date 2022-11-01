/**
 * Dropper.java
 * 
 * Created on 17-Jul-2019 14:41:16
 * 
 */

/**
 * Copyright (c) 2022 e-conomist
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
package net.perspective.draw;

import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.inject.Inject;
import net.perspective.draw.geom.ArrowType;

/**
 *
 * @author ctipper
 */

public class Dropper {

    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);
    java.util.List<java.util.List<Float>> dashes1 = Arrays.asList(
            Arrays.asList(1.5f, 1.5f), Arrays.asList(1.5f, 1.5f), Arrays.asList(2.0f, 2.0f),
            Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f), Arrays.asList(5.0f, 5.0f),
            Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f), Arrays.asList(10.0f, 10.0f));
    java.util.List<java.util.List<Float>> dashes2 = Arrays.asList(
            Arrays.asList(2.0f, 2.0f), Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f),
            Arrays.asList(5.0f, 5.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f));
    java.util.List<java.util.List<Float>> dashes3 = Arrays.asList(
            Arrays.asList(4.0f, 4.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f),
            Arrays.asList(16.0f, 16.0f), Arrays.asList(18.0f, 18.0f), Arrays.asList(20.0f, 20.0f));
    java.util.List<java.util.List<Float>> dashes4 = Arrays.asList(Arrays.asList(3.0f, 2.0f, 2.0f, 2.0f),
            Arrays.asList(5.0f, 3.0f, 3.0f, 3.0f),
            Arrays.asList(8.0f, 4.0f, 4.0f, 4.0f),
            Arrays.asList(10.0f, 5.0f, 5.0f, 5.0f),
            Arrays.asList(12.0f, 6.0f, 6.0f, 6.0f),
            Arrays.asList(16.0f, 8.0f, 8.0f, 8.0f),
            Arrays.asList(18.0f, 10.0f, 10.0f, 10.0f),
            Arrays.asList(24.0f, 12.0f, 12.0f, 12.0f),
            Arrays.asList(28.0f, 14.0f, 14.0f, 14.0f));

    /**
     * Creates a new instance of <code>Dropper</code>
     */
    @Inject
    public Dropper() {
    }

    /**
     * Select stroke using stroke ID and style
     * 
     * @param strokeId
     * @param strokeStyle
     * @return a stroke
     */
    protected BasicStroke selectStroke(Integer strokeId, String strokeStyle) {
        BasicStroke stroke;
        stroke = switch (strokeStyle) {
            case "style1" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case "style2" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
            case "style3" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes2.get(strokeId)), 0.0f);
            case "style4" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes3.get(strokeId)), 0.0f);
            case "style5" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes4.get(strokeId)), 0.0f);
            case "style6" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case "style7" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
            case "style8" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case "style9" -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f);
            default -> new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }; // Plain stroke
        // Arrow at start
        // Arrow at start
        // Arrow at both ends
        // Arrow at both ends
        return stroke;
    }

    /**
     * Get stroke Id from strokeType array
     * Uses linear search
     * 
     * @param stroke
     * @return stroke Id
     */
    public int getStrokeIdLinear(BasicStroke stroke) {
        float width = stroke.getLineWidth();
        int i = 0;
        do {
            if (strokeTypes.get(i) > width)
                break;
            i++;
        } while (i < strokeTypes.size());
        return i - 1;
    }

    /**
     * Get stroke Id from strokeType array
     * Uses binary search
     * 
     * @param stroke
     * @return stroke Id
     */
    public int getStrokeIdBinary(BasicStroke stroke) {
        float width = stroke.getLineWidth();
        int j = 0;
        int i = strokeTypes.size();
        int k = (i + j) / 2;
        while (true) {
            if (strokeTypes.get(k) <= width) {
                j = k;
            }
            if (strokeTypes.get(k) > width) {
                i = k;
            }
            if ((i - j) < 2)
                break;
            k = (i + j) / 2;
        }
        return j;
    }

    /**
     * Get stroke Id from strokeType array
     * Uses functional stream with filter
     * 
     * @param stroke
     * @return stroke Id
     */
    public int getStrokeIdFunctionalStream(BasicStroke stroke) {
        return IntStream.range(0, strokeTypes.size())
                .filter(i -> strokeTypes.get(i) <= stroke.getLineWidth())
                .max()
                .getAsInt();
    }

    /**
     * Get stroke style selector
     * 
     * @param stroke the sample stroke
     * @param arrow  arrow type
     * @return style Id
     */
    public String getStyleSelector(BasicStroke stroke, ArrowType arrow) {
        String styleId = "style1";
        int strokeId = this.getStrokeIdBinary(stroke);
        float[] dash = stroke.getDashArray();
        if (dash == null)
            dash = new float[0];

        if (dash.length == 0) {
            // return style1, style6, style8
            styleId = switch (arrow) {
                case NONE -> "style1";
                case END -> "style6";
                case BOTH -> "style8";
                default -> "style1";
            };
        }
        if (compareDashes(dash, getDashes(dashes1.get(strokeId)))) {
            // return style2, style7, style9
            styleId = switch (arrow) {
                case NONE -> "style2";
                case END -> "style7";
                case BOTH -> "style9";
                default -> "style2";
            };
        }
        if (compareDashes(dash, getDashes(dashes2.get(strokeId)))) {
            // return style3
            styleId = "style3";
        }
        if (compareDashes(dash, getDashes(dashes3.get(strokeId)))) {
            // return style4
            styleId = "style4";
        }
        if (compareDashes(dash, getDashes(dashes4.get(strokeId)))) {
            // return style5
            styleId = "style5";
        }
        return styleId;
    }

    /**
     * Convert List to float array 
     * see {@link java.awt.BasicStroke#getDashArray()}
     * 
     * @param items
     * @return 
     */
    private float[] getDashes(java.util.List<Float> items) {
        float[] value;
        value = new float[4];

        int i = 0;
        for (Float item : items) {
            value[i] = item;
            i++;
        }
        return value;
    }

    /**
     * Compare two sets of dash arrays
     * 
     * @param a  float array
     * @param b  float array
     * @return 
     */
    private boolean compareDashes(float[] a, float[] b) {
        if (a.length != b.length)
            return false;
        for (int i=0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }
}
