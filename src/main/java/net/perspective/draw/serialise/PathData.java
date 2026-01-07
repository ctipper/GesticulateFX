/**
 * PathData.java
 *
 * Created on 8 Aug 2025 19:08:26
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

/**
 *
 * @author ctipper
 */

public class PathData {

    private double[][] segments;

    public PathData() {
    }

    public PathData(double[][] segments) {
        this.segments = segments;
    }

    public double[][] getSegments() {
        return segments;
    }

    public void setSegments(double[][] segments) {
        this.segments = segments;
    }

}
