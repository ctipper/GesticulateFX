/*
 * PathFactory.java
 * 
 * Created on Oct 19, 2013 6:06:24 PM
 * 
 */
package net.perspective.draw.geom;

import java.awt.geom.Path2D;

/**
 * 
 * @author ctipper
 */

public interface PathFactory {

    public Path2D.Double createPath(Figure figure);

}

