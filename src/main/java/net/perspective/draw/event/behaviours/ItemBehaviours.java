/*
 * ItemBehaviours.java
 * 
 * Created on Oct 27, 2014 2:22:56 PM
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw.event.behaviours;

import net.perspective.draw.ItemId;
import net.perspective.draw.geom.DrawItem;

/**
 *
 * @author ctipper
 */

public interface ItemBehaviours {

    /**
     * Select the item, setting the containment type on the context
     *
     * @param context  a {@link net.perspective.draw.event.behaviours.BehaviourContext}
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param id  the slot id of the item
     * @return item is selected
     */
    boolean selectItem(BehaviourContext context, DrawItem item, ItemId id);

    /**
     * Enter the item's editing mode
     *
     * @param context  a {@link net.perspective.draw.event.behaviours.BehaviourContext}
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param id  the slot id of the item
     */
    void editItem(BehaviourContext context, DrawItem item, ItemId id);

    /**
     * Set the cursor for the pointer's position over the item
     *
     * @param context  a {@link net.perspective.draw.event.behaviours.BehaviourContext}
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     */
    void hoverItem(BehaviourContext context, DrawItem item);

    /**
     * Transform the item according to the containment type set by selection
     *
     * @param context  a {@link net.perspective.draw.event.behaviours.BehaviourContext}
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param xinc  an x increment
     * @param yinc  a y increment
     */
    void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc);

}
