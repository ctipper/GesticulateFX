/**
 * MapController.java
 * 
 * Created on 20 May 2022 15:35:15
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.StreetMap;

/**
 *
 * @author ctipper
 */

@Singleton
public class MapController {

    @Inject private Injector injector;
    @Inject private CanvasView view;
    private int mapindex;

    /** Creates a new instance of <code>MapController</code> */
    public MapController() {
        mapindex = -1;
    }

    /**
     * Insert a map
     */
    public void createMap() {
        int width = 900, height = 675;

        StreetMap streetmap = injector.getInstance(StreetMap.class);
        streetmap.setStart(20.0, 20.0);
        streetmap.setEnd(width, height);
        streetmap.setLocation(50.0, 9, 4);
        view.setNewItem(streetmap);
        view.resetNewItem();
        view.setSelected(view.getDrawings().indexOf(streetmap));
        this.initMap();
    }
    
    public void initMap() {
        view.setMapping(true);
        mapindex = view.getSelected();
        // Remove MapView event filters
        if (view.getDrawings().get(mapindex) instanceof StreetMap) {
            ((StreetMap) view.getDrawings().get(mapindex)).resetHandlers();
        } 
    }
    
    /**
     * MapView event handlers need to be consumed when mapping control is not active.
     */
    public void finaliseMap() {
        if (mapindex != -1) {
            DrawItem item = view.getDrawings().get(mapindex);
            if (item instanceof StreetMap) {
                ((StreetMap) item).setLocation();
                ((StreetMap) item).filterHandlers();
            }
            view.setMapping(false);
            mapindex = -1;
        }
    }
}
