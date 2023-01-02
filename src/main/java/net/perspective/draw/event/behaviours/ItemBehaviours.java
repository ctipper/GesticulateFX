/*
 * ItemBehaviours.java
 * 
 * Created on Oct 27, 2014 2:22:56 PM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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
