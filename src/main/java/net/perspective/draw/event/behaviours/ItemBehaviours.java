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

    boolean selectItem(BehaviourContext context, Figure item, int index);
    
    void alterItem(BehaviourContext context, Figure item, double xinc, double yinc);
}
