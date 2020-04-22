/*
 * PointFactory.java
 * 
 * Created on Oct 27, 2013 9:34:15 PM
 * 
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
