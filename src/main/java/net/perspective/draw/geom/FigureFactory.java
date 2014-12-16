/*
 * FigureFactory.java
 * 
 * Created on Feb 22, 2012, 2:25:38 PM
 * 
 */
package net.perspective.draw.geom;

/**
 *
 * @author ctipper
 */
import net.perspective.draw.enums.DrawingType;

public interface FigureFactory {

    Figure createFigure(DrawingType drawType);
}
