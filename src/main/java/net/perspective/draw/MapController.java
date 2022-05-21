/**
 * MapController.java
 * 
 * Created on 20 May 2022 15:35:15
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    protected Slider zoomSlider;
    protected Button zoomInButton;
    protected Button zoomOutButton;
    protected TextField geoLocation;
    private StreetMap map;
    private int mapindex;

    public static final int MIN_ZOOM = 2;
    public static final int MAX_ZOOM = 20;

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
    
    /**
     * Initialise selected map
     */
    public void initMap() {
        view.setMapping(true);
        mapindex = view.getSelected();
        // Remove MapView event filters
        if (view.getDrawings().get(mapindex) instanceof StreetMap) {
            map = ((StreetMap) view.getDrawings().get(mapindex));
            map.resetHandlers();
            initializeZoomSlider(map);
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
            removeSlider();
        }
    }

    /**
     * Place the map zoom slider on the canvas
     * 
     * @param map 
     */
    public void initializeZoomSlider(StreetMap map) {
        int left = (int) map.getStart().x;
        int top = (int) map.getStart().y;

        zoomSlider = new Slider();
        zoomSlider.setOrientation(Orientation.VERTICAL);
        zoomSlider.setLayoutX(left + 14);
        zoomSlider.setLayoutY(top + 10);
        zoomSlider.setMinWidth(30);
        zoomSlider.setMinHeight(150);
        zoomSlider.setMin(getMinZoom());
        zoomSlider.setMax(getMaxZoom());
        zoomSlider.setValue(map.getZoom());
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setBlockIncrement(1f);
        zoomSlider.setFocusTraversable(false);
        zoomSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            setZoom(newValue.doubleValue());
        });
        drawarea.getCanvas().getChildren().add(zoomSlider);

        int size = 18;
        zoomInButton = new Button("+");
        zoomInButton.setLayoutX(left + 4);
        zoomInButton.setLayoutY(top + 155);
        zoomInButton.setMinWidth(size);
        zoomInButton.setMinHeight(size);
        zoomInButton.setStyle("-fx-background-radius:0.333333em;-fx-border-color:transparent;-fx-padding:0;-fx-background-size:0;");
        zoomInButton.setFocusTraversable(false);
        zoomInButton.setOnAction(e -> {
            zoomIn();
        });
        drawarea.getCanvas().getChildren().add(zoomInButton);

        zoomOutButton = new Button("-");
        zoomOutButton.setLayoutX(left + 10 + size);
        zoomOutButton.setLayoutY(top + 155);
        zoomOutButton.setMinWidth(size);
        zoomOutButton.setMinHeight(size);
        zoomOutButton.setStyle("-fx-background-radius:0.333333em;-fx-border-color:transparent;-fx-padding:0;-fx-background-size:0;");
        zoomOutButton.setFocusTraversable(false);
        zoomOutButton.setOnAction(e -> {
            zoomOut();
        });
        drawarea.getCanvas().getChildren().add(zoomOutButton);
    }

    /**
     * Set the map zoom level
     * 
     * @param zoom
     */
    public void setZoom(double zoom) {
        map.adjustZoom(zoom);
    }

    /**
     * Zoom in map by one level
     */
    public void zoomIn() {
        zoomSlider.increment();
    }

    /**
     * Zoom out by one level
     */
    public void zoomOut() {
        zoomSlider.decrement();
    }

    public int getMinZoom() {
        return MIN_ZOOM;
    }

    public int getMaxZoom() {
        return MAX_ZOOM;
    }

    public void removeSlider() {
        drawarea.getCanvas().getChildren().remove(zoomSlider);     // remove zoomSlider if necessary
        drawarea.getCanvas().getChildren().remove(zoomInButton);   // remove zoomInButton if necessary
        drawarea.getCanvas().getChildren().remove(zoomOutButton);  // remove zoomOutButton if necessary
    }

}
