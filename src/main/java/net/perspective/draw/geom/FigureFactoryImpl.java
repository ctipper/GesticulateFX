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

    public Figure createFigure(DrawingType drawType) {
        Figure item;

        switch (drawType) {
            case LINE:
            case HORIZONTAL:
            case VERTICAL:
                item = new Edge(FigureType.LINE);
                break;
            case CIRCLE:
            case ELLIPSE:
                item = new Figure(FigureType.CIRCLE);
                break;
            case SQUARE:
            case RECTANGLE:
                item = new Figure(FigureType.SQUARE);
                break;
            case TRIANGLE:
            case ISOSCELES:
                item = new Figure(FigureType.TRIANGLE);
                break;
            case HEXAGON:
            case ISOHEX:
                item = new Figure(FigureType.HEXAGON);
                break;
            case POLYGON:
                item = new Edge(FigureType.POLYGON);
                break;
            case SKETCH:
                item = new Edge(FigureType.SKETCH);
                break;
            default:
                item = new Figure();
                break;
        }
        return item;

    }

}
