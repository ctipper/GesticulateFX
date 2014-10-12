/*
 * PathFactory.java
 * 
 * Created on Oct 19, 2013 6:06:24 PM
 * 
 */
package net.perspective.draw.geom;

import gwt.awt.geom.GeneralPath;

/**
 *
 * @author ctipper
 */

public interface PathFactory {
    public GeneralPath createPath(Figure fig);    
}
