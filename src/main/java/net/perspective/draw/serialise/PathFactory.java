/**
 * PathFactory.java
 *
 * Created on 8 Aug 2025 18:46:01
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
package net.perspective.draw.serialise;

import java.awt.geom.Path2D;

/**
 *
 * @author ctipper
 */

public class PathFactory {

    public static Path2D.Double createPath(int windingRule, PathData pathData) {
        Path2D.Double path = new Path2D.Double(windingRule);
        for (double[] segment : pathData.getSegments()) {
            int type = (int) segment[0];
            switch (type) {
                case 0 ->
                    path.moveTo(segment[1], segment[2]);
                case 1 ->
                    path.lineTo(segment[1], segment[2]);
                case 2 ->
                    path.quadTo(segment[1], segment[2], segment[3], segment[4]);
                case 3 ->
                    path.curveTo(segment[1], segment[2], segment[3], segment[4], segment[5], segment[6]);
                case 4 ->
                    path.closePath();
            }
        }
        return path;
    }

}
