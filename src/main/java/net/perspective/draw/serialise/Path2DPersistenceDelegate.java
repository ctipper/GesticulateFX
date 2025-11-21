/**
 * Path2DPersistenceDelegate.java
 *
 * Created on 8 Aug 2025 10:20:23
 *
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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
import java.beans.*;
import java.util.Arrays;

/**
 *
 * @author ctipper
 */

public class Path2DPersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!(oldInstance instanceof Path2D.Double) || !(newInstance instanceof Path2D.Double)) {
            return false;
        }

        Path2D.Double oldPath = (Path2D.Double) oldInstance;
        Path2D.Double newPath = (Path2D.Double) newInstance;

        // Quick checks first
        if (oldPath.getWindingRule() != newPath.getWindingRule()) {
            return false;
        }

        // Compare path data (you might want to cache this)
        double[][] oldData = getPathData(oldPath);
        double[][] newData = getPathData(newPath);

        return Arrays.deepEquals(oldData, newData);
    }

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Path2D.Double path = (Path2D.Double) oldInstance;
        return new Expression(path, PathFactory.class, "createPath", new Object[] {
            path.getWindingRule(),
            new PathData(getPathData(path)) // Wrap in PathData
        });
    }

    // Helper method to extract path data
    private double[][] getPathData(Path2D.Double path) {
        java.util.List<double[]> pathData = new java.util.ArrayList<>();
        double[] coords = new double[6];
        java.awt.geom.PathIterator iterator = path.getPathIterator(null);

        while (!iterator.isDone()) {
            int segmentType = iterator.currentSegment(coords);

            switch (segmentType) {
                case java.awt.geom.PathIterator.SEG_MOVETO ->
                    pathData.add(new double[] { 0, coords[0], coords[1] });
                case java.awt.geom.PathIterator.SEG_LINETO ->
                    pathData.add(new double[] { 1, coords[0], coords[1] });
                case java.awt.geom.PathIterator.SEG_QUADTO ->
                    pathData.add(new double[] { 2, coords[0], coords[1], coords[2], coords[3] });
                case java.awt.geom.PathIterator.SEG_CUBICTO ->
                    pathData.add(new double[] { 3, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5] });
                case java.awt.geom.PathIterator.SEG_CLOSE ->
                    pathData.add(new double[] { 4 });
            }
            iterator.next();
        }

        return pathData.toArray(double[][]::new);
    }

    public Path2DPersistenceDelegate() {
    }

}
