/*
 * PointFactory.java
 * 
 * Created on Oct 27, 2013 9:34:15 PM
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

import java.util.List;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.util.CanvasPoint;

/**
 * Defines interface that produces a list of points describing a geometric figure.
 * 
 * @author ctipper
 */

public interface PointFactory {

    public List<CanvasPoint> createPoints(DrawingType description, double... coords);

}
