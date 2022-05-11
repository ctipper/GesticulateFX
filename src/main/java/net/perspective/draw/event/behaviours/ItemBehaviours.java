/*
 * ItemBehaviours.java
 * 
 * Created on Oct 27, 2014 2:22:56 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import net.perspective.draw.geom.DrawItem;

/**
 * 
 * @author ctipper
 */

public interface ItemBehaviours {

    boolean selectItem(BehaviourContext context, DrawItem item, int index);

    void editItem(BehaviourContext context, DrawItem item, int index);

    void hoverItem(BehaviourContext context, DrawItem item);

    void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc);

}
