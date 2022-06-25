/*
 * FigureFactoryImpl.java
 * 
 * Created on Feb 22, 2012, 2:27:01 PM
 * 
 */
package net.perspective.draw.geom;

import net.perspective.draw.enums.DrawingType;

/**
 * 
 * @author ctipper
 */

public class FigureFactoryImpl implements FigureFactory {

    @Override
    public Figure createFigure(DrawingType drawType) {
        Figure item;

        item = switch (drawType) {
            case LINE, HORIZONTAL, VERTICAL -> new Edge(FigureType.LINE);
            case CIRCLE, ELLIPSE -> new Figure(FigureType.CIRCLE);
            case SQUARE, RECTANGLE -> new Figure(FigureType.SQUARE);
            case TRIANGLE, ISOSCELES -> new Figure(FigureType.TRIANGLE);
            case HEXAGON, ISOHEX -> new Figure(FigureType.HEXAGON);
            case POLYGON -> new Edge(FigureType.POLYGON);
            case SKETCH -> new Edge(FigureType.SKETCH);
            default -> new Figure();
        };
        return item;

    }

}
