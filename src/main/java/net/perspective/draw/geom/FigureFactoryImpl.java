/*
 * FigureFactoryImpl.java
 * 
 * Created on Feb 22, 2012, 2:27:01 PM
 * 
 */
package net.perspective.draw.geom;

/**
 *
 * @author ctipper
 */
import net.perspective.draw.enums.DrawingType;

public class FigureFactoryImpl implements FigureFactory {

    public Figure createFigure(DrawingType drawType) {
        Figure drawItem;

        switch (drawType) {
            case LINE:
                drawItem = new Edge(FigureType.LINE);
                break;
            case CIRCLE:
            case ELLIPSE:
                drawItem = new Figure(FigureType.CIRCLE);
                break;
            case SQUARE:
            case RECTANGLE:
                drawItem = new Figure(FigureType.SQUARE);
                break;
            case TRIANGLE:
                drawItem = new Figure(FigureType.TRIANGLE);
                break;
            case POLYGON:
                drawItem = new Edge(FigureType.POLYGON);
                break;
            case SKETCH:
                drawItem = new Edge(FigureType.SKETCH);
                break;
            default:
                drawItem = new Figure();
                break;
        }
        return drawItem;

    }
}
