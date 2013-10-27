/*
 * PointFactory.java
 * 
 * Created on Oct 27, 2013 9:34:15 PM
 * 
 */
package net.perspective.draw.geom;

import java.util.List;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public interface PointFactory {

    public List<CanvasPoint> createPoints(FigureType description, double... coords);
}
