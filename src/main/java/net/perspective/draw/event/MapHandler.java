/**
 * MapHandler.java
 * 
 * Created on 10 May 2022 16:15:57
 * 
 */

/**
 * Copyright (c) 2022 Christopher Tipper
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
package net.perspective.draw.event;

import javax.inject.Inject;
import net.perspective.draw.MapController;

/**
 *
 * @author ctipper
 */

public class MapHandler implements Handler {

    @Inject private DrawAreaListener listener;
    @Inject private MapController mapper;

    /** Creates a new instance of <code>MapHandler</code> */
    public MapHandler() {
    }

    @Override
    public void upEvent() {

    }

    @Override
    public void downEvent() {
        
    }

    @Override
    public void clickEvent() {
       if (listener.getRightClick()) {
           mapper.putLocation(listener.getStartX(), listener.getStartY());
       } 
    }

    @Override
    public void hoverEvent() {
        
    }

    @Override
    public void dragEvent() {
        mapper.moveMap();
    }

    @Override
    public void zoomEvent() {
        mapper.setZoom(mapper.getMapZoom() + listener.getWheel());
        mapper.setZoomSlider();
    }

}
