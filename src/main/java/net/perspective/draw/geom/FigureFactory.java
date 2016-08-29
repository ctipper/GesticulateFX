/*
 * FigureFactory.java
 * 
 * Created on Feb 22, 2012, 2:25:38 PM
 * 
 */
package net.perspective.draw.geom;

import net.perspective.draw.enums.DrawingType;

/**
 *
 * @author ctipper
 */

public interface FigureFactory {

    Figure createFigure(DrawingType drawType);
}
