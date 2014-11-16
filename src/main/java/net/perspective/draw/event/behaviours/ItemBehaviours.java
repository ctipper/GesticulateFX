/*
 * ItemBehaviours.java
 * 
 * Created on Oct 27, 2014 2:22:56 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public interface ItemBehaviours {

    boolean selectItem(Figure item, int index, double startx, double starty);
    
    void alterItem(Figure item, double xinc, double yinc);
}
