/**
 * MapHandler.java
 * 
 * Created on 10 May 2022 16:15:57
 * 
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
        mapper.setPosition(listener.getTempX(), listener.getTempY());
        mapper.setZoom(mapper.getMapZoom() + listener.getWheel());
        mapper.setZoomSlider();
    }

}
