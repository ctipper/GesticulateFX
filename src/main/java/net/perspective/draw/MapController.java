/**
 * MapController.java
 * 
 * Created on 20 May 2022 15:35:15
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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
package net.perspective.draw;

import com.gluonhq.maps.MapPoint;
import com.google.inject.Injector;
import java.nio.IntBuffer;
import java.time.Instant;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.StreetMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MapController
 *
 * @author ctipper
 */

@Singleton
public class MapController {

    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    private Slider zoomSlider;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button quiticon;
    private TextField geoLocation;
    private StreetMap map;
    private int mapindex;

    private final String SVG_QUIT_A = "M3.7732425 22.718011L22.702213 3.7890409";
    private final String SVG_QUIT_B = "M3.7732425 3.7890409L22.702213 22.718011";
    private final String SVG_QUIT_C = "M0.77324252 25.718011H25.679783V0.81147092H0.77324252Z";

    public static final int MIN_ZOOM = 2;
    public static final int MAX_ZOOM = 20;

    private static final Logger logger = LoggerFactory.getLogger(MapController.class.getName());

    /** Creates a new instance of <code>MapController</code> */
    @Inject
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
        streetmap.init();
        Image image = createCompatibleImage(width, height);
        ImageItem img = new ImageItem(image);
        int index = view.setImageItem(img);
        streetmap.setImage(index, width, height);
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
        if (view.getDrawings().get(mapindex) instanceof StreetMap streetMap) {
            map = streetMap;
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
            if (item instanceof StreetMap streetMap) {
                streetMap.filterHandlers();
                resizeMap(streetMap);
            }
            view.setMapping(false);
            mapindex = -1;
            removeSlider();
        }
    }

    /**
     * Set keyboard and mouse handlers on exiting mapping mode
     */
    public void quitMapping() {
        drawarea.changeHandlers(HandlerType.SELECTION);
        view.setEditing(KeyHandlerType.MOVE);
    }

    /**
     * Finalise map background image
     * 
     * @param item an {@link net.perspective.draw.geom.StreetMap}
     */
    public void resizeMap(StreetMap item) {
        item.setLocation();
        Image image = createCompatibleImage(item, (int) item.getEnd().getX(), (int) item.getEnd().getY());
        view.replaceImage(item.getImageIndex(), image);
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
        zoomSlider.setValue(map.getMapZoom());
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setBlockIncrement(1f);
        zoomSlider.setFocusTraversable(false);
        zoomSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            setZoom(newValue.doubleValue());
        });
        drawarea.getCanvas().getChildren().add(zoomSlider);

        int zsize = 18;
        zoomInButton = new Button("+");
        zoomInButton.setLayoutX(left + 4);
        zoomInButton.setLayoutY(top + 155);
        zoomInButton.setMinWidth(zsize);
        zoomInButton.setMinHeight(zsize);
        zoomInButton.setStyle("-fx-background-radius:0.333333em;-fx-border-color:transparent;-fx-padding:0;-fx-background-size:0;");
        zoomInButton.setFocusTraversable(false);
        zoomInButton.setOnAction(e -> {
            zoomIn();
        });
        drawarea.getCanvas().getChildren().add(zoomInButton);

        zoomOutButton = new Button("-");
        zoomOutButton.setLayoutX(left + 10 + zsize);
        zoomOutButton.setLayoutY(top + 155);
        zoomOutButton.setMinWidth(zsize);
        zoomOutButton.setMinHeight(zsize);
        zoomOutButton.setStyle("-fx-background-radius:0.333333em;-fx-border-color:transparent;-fx-padding:0;-fx-background-size:0;");
        zoomOutButton.setFocusTraversable(false);
        zoomOutButton.setOnAction(e -> {
            zoomOut();
        });
        drawarea.getCanvas().getChildren().add(zoomOutButton);

        /**
         * close map glyph
         */
        int qsize = 26;
        Group icon = getQuitGlyph();
        quiticon = new Button();
        quiticon.setMinWidth(qsize);
        quiticon.setMinHeight(qsize);
        quiticon.setStyle("-fx-background-radius:0.333333em;-fx-background-color:transparent;-fx-border-color:transparent;-fx-padding:0;-fx-background-size:0;");
        quiticon.setLayoutX((int) map.getUp()[0].x - qsize - 10);
        quiticon.setLayoutY((int) map.getUp()[0].y + 10);
        quiticon.setAlignment(Pos.CENTER);
        quiticon.setFocusTraversable(false);
        quiticon.setMnemonicParsing(false);
        quiticon.getStyleClass().add("menuicon");
        quiticon.setGraphic(icon);
        quiticon.setOnAction(this::handleQuitAction);
        drawarea.getCanvas().getChildren().add(quiticon);

        geoLocation = new TextField();
        geoLocation.setMinWidth(180);
        geoLocation.setMinHeight(30);
        geoLocation.setLayoutX((int) map.getUp()[0].x - qsize - 196);
        geoLocation.setLayoutY((int) map.getUp()[0].y + 10);
        geoLocation.setFocusTraversable(true);
        geoLocation.setText("");
        drawarea.getCanvas().getChildren().add(geoLocation);
    }

    /**
     * Set the map zoom level
     * 
     * @param zoom
     */
    public void setZoom(double zoom) {
        if (zoom > getMaxZoom() || zoom < getMinZoom()) {
            return;
        }
        this.setPosition();
        map.adjustZoom(Long.valueOf(Math.round(zoom)).doubleValue());
    }

    /**
     * Get the map zoom level
     * 
     * @return the zoom level
     */
    public double getMapZoom() {
        return map.getMapZoom();
    }

    /**
     * Set the zoom slider to current zoom level
     */
    public void setZoomSlider() {
        zoomSlider.setValue(map.getMapZoom());
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
        drawarea.getCanvas().getChildren().remove(quiticon);       // remove quiticon if necessary
        drawarea.getCanvas().getChildren().remove(geoLocation);       // remove geoLocation if necessary
    }

    /**
     * Centre the map
     */
    private void setPosition() {
        MapPoint mp = map.getPosition();
        map.setLatitude(mp.getLatitude());
        map.setLongitude(mp.getLongitude());
    }

    /**
     * Write out map location in Lat/Lon
     * 
     * @param x
     * @param y 
     */
    public void putLocation(double x, double y) {
        if (view.isMapping()) {
            MapPoint loc = map.getPosition(x, y);
            geoLocation.setText(String.format("%.6f %.6f", new Object[] { loc.getLatitude(), loc.getLongitude() }));
            logger.debug("Geolocation: {}, {}", new Object[] { loc.getLatitude(), loc.getLongitude() });
        }
    }

    /**
     * Pan the map
     * 
     * @param diffx  x increment
     * @param diffy  y increment
     */
    public void moveMap(int diffx, int diffy) {
        map.moveMap(diffx, diffy);
        this.moveMap();
    }

    /**
     * Refresh map layout
     */
    public void moveMap() {
        if (view.isMapping()) {
            map.refreshLayout();
        }
    }

    /**
     * Define the palette menu info icon
     * 
     * @return
     */
    private Group getQuitGlyph() {
        SVGPath path_a = new SVGPath();
        path_a.setContent(SVG_QUIT_A);
        path_a.getStyleClass().add("svgPath");
        path_a.setFill(Color.TRANSPARENT);
        SVGPath path_b = new SVGPath();
        path_b.setContent(SVG_QUIT_B);
        path_b.getStyleClass().add("svgPath");
        path_b.setFill(Color.TRANSPARENT);
        SVGPath path_c = new SVGPath();
        path_c.setContent(SVG_QUIT_C);
        path_c.getStyleClass().add("svgPath");
        path_c.setFill(Color.TRANSPARENT);
        if (drawarea.isDarkModeEnabled()) {
            path_a.setStyle("-fx-stroke:#3a3a3a;-fx-stroke-width:4.5;-fx-stroke-line-cap:butt;");
            path_b.setStyle("-fx-stroke:#3a3a3a;-fx-stroke-width:4.5;-fx-stroke-line-cap:butt;");
            path_c.setStyle("-fx-stroke:#3a3a3a;");
        } else {
            path_a.setStyle("-fx-stroke:#a8a8a8;-fx-stroke-width:4.5;-fx-stroke-line-cap:butt;");
            path_b.setStyle("-fx-stroke:#a8a8a8;-fx-stroke-width:4.5;-fx-stroke-line-cap:butt;");
            path_c.setStyle("-fx-stroke:#a8a8a8;");
        }
        Group glyph = new Group();
        glyph.getChildren().addAll(path_a, path_b, path_c);
        return glyph;
    }

    private void handleQuitAction(ActionEvent t) {
        quitMapping();
    }

    /**
     * Initialise StreetMap image for use by copy procedure
     * 
     * @param item a {@link net.perspective.draw.geom.StreetMap}
     * @return an {@link net.perspective.draw.geom.StreetMap}
     */
    public StreetMap copyMap(StreetMap item) {
        view.getImageItems().add(new ImageItem(Instant.now()));
        item.setImageIndex(view.getImageItems().size() - 1);
        Image image = createCompatibleImage(item, (int) item.getEnd().getX(), (int) item.getEnd().getY());
        view.getImageItem(item.getImageIndex()).setImage(image);
        return item;
    }

    /**
     * Build a greyScale image for backing store
     * 
     * @param width
     * @param height
     * @return 
     */
    private Image createCompatibleImage(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();
        pixelWriter.setPixels(0, 0, width, height, format, getPixelArray(width, height, 211), 0, width);
        return image;
    }

    /**
     * Take a snapshot from StreetMap and render as Image
     * @param map
     * @param width
     * @param height
     * @return 
     */
    private Image createCompatibleImage(StreetMap map, int width, int height) {
        final WritableImage image = new WritableImage(width, height);
        Callback<SnapshotResult, Void> callback = (SnapshotResult capture) -> {
            PixelWriter pixelWriter = image.getPixelWriter();
            PixelReader pixelReader = capture.getImage().getPixelReader();
            // mixing int and double is treacherous
            int w = Math.min(width, (int) capture.getImage().getWidth());
            int h = Math.min(height, (int) capture.getImage().getHeight());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color color = pixelReader.getColor(x, y);
                    pixelWriter.setColor(x, y, color);
                }
            }
            return null;
        };
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        map.getSnapshot(callback, params, null);
        return image;
    }
            

    /** 
     * Build a pixel array adapting method 
     * @see <a href="https://stackoverflow.com/a/28426889">Improve javaFx processing performance</a>
     * with loop optimisation from follow-up
     * @see <a href="https://community.oracle.com/tech/developers/discussion/comment/12511325#Comment_12511325">community.oracle.com</a>
     * @param width
     * @param height
     * @param value
     * @return 
     */
    private int[] getPixelArray(int width, int height, int value) {
        int[] pixels = new int[width * height]; // Buffer for all pixels
        int alpha = 0xFF << 24;
        for (int y = 0; y < height; y++) {
            final int depthOffset = y * width;
            for (int x = 0; x < width; x++) {
                final int index = x + depthOffset;
                int newArgb = alpha | (value << 16) | (value << 8) | value;
                pixels[index] = newArgb;
            }
        }
        return pixels;
    }

}
