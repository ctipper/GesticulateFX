/*
 * FigureFactoryImpl.java
 * 
 * Created on Feb 22, 2012, 2:27:01 PM
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
package net.perspective.draw.geom;

import javax.inject.Inject;
import net.perspective.draw.enums.DrawingType;

/**
 * 
 * @author ctipper
 */

public class FigureFactoryImpl implements FigureFactory {

    @Inject
    public FigureFactoryImpl() {
    }

    @Override
    public Figure createFigure(DrawingType drawType) {
        Figure item;

        item = switch (drawType) {
            case LINE, HORIZONTAL, VERTICAL -> new Edge(FigureType.LINE);
            case CIRCLE, ELLIPSE -> new Figure(FigureType.CIRCLE);
            case SQUARE, RECTANGLE -> new Figure(FigureType.SQUARE);
            case TRIANGLE, ISOSCELES -> new Figure(FigureType.TRIANGLE);
            case HEXAGON, ISOHEX -> new Figure(FigureType.HEXAGON);
            case PENTAGRAM, ISOGRAM -> new Figure(FigureType.PENTAGRAM);
            case POLYGON -> new Edge(FigureType.POLYGON);
            case SKETCH -> new Edge(FigureType.SKETCH);
            default -> new Figure();
        };
        return item;

    }

}
