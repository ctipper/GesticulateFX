/*
 * StreetMap.java
 * 
 * Created on May 21, 2012, 4:11:43 PM
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
package net.perspective.draw.geom;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A structure for rendering an image of a street map
 * 
 * <p>A StreetMap is not allowed to rotate.
 * 
 * @author ctipper
 */

public class StreetMap extends Picture {

    private double latitude;
    private double longitude;
    private int zoom;
    private transient MapView mv;
    private transient EventHandler<InputEvent> filter;

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(StreetMap.class.getName());

    /** Creates a new instance of <code>StreetMap</code> */
    public StreetMap() {
        this(0, 0);
    }

    /**
     * Creates a new instance of <code>StreetMap</code>
     * 
     * @param x the x position
     * @param y the y position
     */
    public StreetMap(double x, double y) {
        super(x, y);
        mv = new MapView();
        mv.setManaged(true);
        filter = (InputEvent event) -> {
            logger.trace("Filtering out event {} ", event.getEventType());
            event.consume();
            InputEvent evt = (InputEvent) event.clone();
            InputEvent.fireEvent(mv.getParent(), evt);
        };
    }

    /**
     * Initialise {@link com.gluonhq.maps.MapView}
     */
    public void init() {
        mv.setCenter(latitude, longitude);
        mv.setZoom(zoom);
        mv.setCursor(javafx.scene.Cursor.OPEN_HAND);
    }

    /**
     * Set the latitude of the streetmap
     * 
     * @param latitude  the latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Return the latitude of the streetmap
     * 
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the longitude of the streetmap
     * 
     * @param longitude 
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Return the longitude of the streetmap
     * 
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the zoom level of the streetmap
     * 
     * @param zoom 
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    /**
     * Return the zoom level of the streetmap
     * 
     * @return zoom
     */
    public int getZoom() {
        return zoom;
    }

    /**
     * Zoom MapView
     * 
     * @param zoom 
     */
    public void adjustZoom(double zoom) {
        mv.setCenter(latitude, longitude);
        mv.setZoom(zoom);
    }

    /**
     * Get the current map zoom level
     * 
     * @return a zoom level
     */
    public double getMapZoom() {
        return mv.getMapZoom();
    }

    /**
     * Move the map by xy increment
     * 
     * @param diffx the x translation
     * @param diffy the y translation
     */
    public void moveMap(double diffx, double diffy) {
        mv.moveMap(diffx, diffy);
    }

    /**
     * Update the public properties of the streetmap 
     * 
     * @param drawarea  the {@link net.perspective.draw.DrawingArea}
     */
    public void updateProperties(DrawingArea drawarea) {
        this.setTransparency(drawarea.getTransparency());
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-left corner location (transformed)
     */
    public CanvasPoint[] getTop() {
        CanvasPoint s = new CanvasPoint(start.x, start.y);
        return new CanvasPoint[] { s, s };
    }

    /**
     * Returns the 2-tuple of top-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-right corner location (transformed)
     */
    public CanvasPoint[] getUp() {
        CanvasPoint up = new CanvasPoint(start.x + end.x, start.y);
        return new CanvasPoint[] { up, up };
    }

    /**
     * Returns the 2-tuple of bottom-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-left corner location (transformed)
     */
    public CanvasPoint[] getDown() {
        CanvasPoint down = new CanvasPoint(start.x, start.y + end.y);
        return new CanvasPoint[] { down, down };
    }

    /**
     * Returns the 2-tuple of bottom-right corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of bottom-right corner location (transformed)
     */
    public CanvasPoint[] getBottom() {
        CanvasPoint e = new CanvasPoint(start.x + end.x, start.y + end.y);
        return new CanvasPoint[] { e, e };
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    public Shape bounds() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, end.x, end.y);
        Area bounds = new Area(rect);
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(start.x, start.y);
        bounds.transform(transform);
        return bounds;
    }

    /**
     * Consolidate map properties
     */
    public void setLocation(double lat, double lon, double zm) {
        setLatitude(lat);
        setLongitude(lon);
        setZoom(Long.valueOf(Math.round(zm)).intValue());
        mv.setCenter(lat, lon);
        mv.setZoom(zoom);
        logger.debug("Lat {} Long {}, Zoom {}", getLatitude(), getLongitude(), getZoom());
    }

    /**
     * Set properties from MapView
     */
    public void setLocation() {
        // must calculate center of map
        Bounds bounds = mv.getLayoutBounds();
        CanvasPoint centre = new CanvasPoint(mv.getLayoutX() + bounds.getCenterX(),
                                             mv.getLayoutY() + bounds.getCenterY());
        MapPoint mp = mv.getMapPosition(centre.x, centre.y);
        setLatitude(mp.getLatitude());
        setLongitude(mp.getLongitude());
        setZoom(Long.valueOf(Math.round(mv.getMapZoom())).intValue());
        logger.trace("Lat {} Long {}, Zoom {}", getLatitude(), getLongitude(), getZoom());
    }

    /**
     * Ensure MapView is refreshed in a timely manner
     */
    public void refreshLayout() {
        mv.layout();
    }

    /**
     * Return lat/lon of layout point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the {@link com.gluonhq.maps.MapPoint}
     */
    public MapPoint getPosition(double x, double y) {
        MapPoint mp = mv.getMapPosition(x, y);
        return mp;
    }

    /**
     * Return lat/lon of centre point
     * 
     * @return the {@link com.gluonhq.maps.MapPoint}
     */
    public MapPoint getPosition() {
        // must calculate center of map
        Bounds bounds = mv.getLayoutBounds();
        CanvasPoint centre = new CanvasPoint(mv.getLayoutX() + bounds.getCenterX(),
                                             mv.getLayoutY() + bounds.getCenterY());
        return mv.getMapPosition(centre.x, centre.y);
    }

    /**
     * Provide callback snapshot of MapView content
     * 
     * @param callback a {@link javafx.util.Callback}
     * @param params the {@link javafx.scene.SnapshotParameters}
     * @param image an {@link javafx.scene.image.WritableImage}
     */
    public void getSnapshot(Callback<SnapshotResult, Void> callback, SnapshotParameters params, WritableImage image) {
        Platform.runLater(() -> {
            mv.snapshot(callback, params, null);
        });
    }

    /**
     * map handlers need to be consumed
     */
    public void filterHandlers() {
        mv.addEventFilter(MouseEvent.MOUSE_PRESSED, filter);
        mv.addEventFilter(MouseEvent.MOUSE_DRAGGED, filter);
        mv.addEventFilter(MouseEvent.MOUSE_RELEASED, filter);
        mv.addEventFilter(ScrollEvent.SCROLL_STARTED, filter);
        mv.addEventFilter(ScrollEvent.SCROLL_FINISHED, filter);
        mv.addEventFilter(ScrollEvent.SCROLL, filter);
        mv.addEventFilter(ZoomEvent.ZOOM_STARTED, filter);
        mv.addEventFilter(ZoomEvent.ZOOM_FINISHED, filter);
        mv.addEventFilter(ZoomEvent.ZOOM, filter);
    }
    
    public void resetHandlers() {
        mv.removeEventFilter(MouseEvent.MOUSE_PRESSED, filter);
        mv.removeEventFilter(MouseEvent.MOUSE_DRAGGED, filter);
        mv.removeEventFilter(MouseEvent.MOUSE_RELEASED, filter);
        mv.removeEventFilter(ScrollEvent.SCROLL_STARTED, filter);
        mv.removeEventFilter(ScrollEvent.SCROLL_FINISHED, filter);
        mv.removeEventFilter(ScrollEvent.SCROLL, filter);
        mv.removeEventFilter(ZoomEvent.ZOOM_STARTED, filter);
        mv.removeEventFilter(ZoomEvent.ZOOM_FINISHED, filter);
        mv.removeEventFilter(ZoomEvent.ZOOM, filter);
    }

    /**
     * Provide MapView for FX canvas
     * 
     * @return a MapView
     */
    @Override
    public Node draw() {
        bp = getContainer();
        bp.relocate(start.x, start.y);
        bp.setPrefSize(end.x, end.y);
        return bp;
    }

    private transient StackPane bp = null;
    private StackPane getContainer() {
        if (bp == null) {
            final Group copyright = createCopyright();
            bp = new StackPane() {
                @Override
                protected void layoutChildren() {
                    super.layoutChildren();
                    copyright.setLayoutX(getWidth() - copyright.prefWidth(-1));
                    copyright.setLayoutY(getHeight() - copyright.prefHeight(-1));
                }
            };
            bp.getChildren().addAll(mv, copyright);
            bp.setManaged(true);
        }
        return bp;
    }

    /**
     * Render the picture anchors to indicate selection
     * 
     * @return an FX Shape node
     */
    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        Group anchors = new Group();
        anchors.setMouseTransparent(true);
        anchors.getChildren().add(super.anchor(drawarea, start.x, start.y));
        anchors.getChildren().add(super.anchor(drawarea, start.x + end.x, start.y));
        anchors.getChildren().add(super.anchor(drawarea, start.x, start.y + end.y));
        anchors.getChildren().add(super.anchor(drawarea, start.x + end.x, start.y + end.y));
        return anchors;
    }

    /**
     * Render the streetmap to the canvas
     * 
     * @param g2  the graphics context
     */
    public void draw(Graphics2D g2) {
        AffineTransform defaultTransform, transform;

        defaultTransform = g2.getTransform();

        transform = new AffineTransform();
        transform.setToTranslation(start.x, start.y);

        g2.transform(transform);

        try {
            // Retrieve image
            javafx.scene.image.Image image = view.getImageItem(index).getImage();
            BufferedImage img = SwingFXUtils.fromFXImage(image, null);
            g2.drawImage(img, 0, 0, null);
        } catch (IndexOutOfBoundsException e) {
            logger.error("Picture: Index {0} Out of Bounds.", index);
            // reset graphics context
            g2.setTransform(defaultTransform);
            this.sketch(g2);
        } catch (NullPointerException e) {
            logger.warn("Picture: Image not found.");
            // reset graphics context
            g2.setTransform(defaultTransform);
            this.sketch(g2);
        }

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    /**
     * Render a shadowed version of the streetmap
     * 
     * @param g2  the graphics context
     */
    public void sketch(Graphics2D g2) {
        AffineTransform defaultTransform, transform;

        defaultTransform = g2.getTransform();

        transform = new AffineTransform();
        transform.setToTranslation(start.x, start.y);

        g2.transform(transform);

        g2.setColor(Figure.fxToAwt(Color.web(drawarea.getThemeFillColor()), ((float) getTransparency()) / 100));
        g2.fill(new Rectangle2D.Double(0, 0, end.x, end.y));

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    private Group createCopyright() {
        final Label copyright = new Label(
                """
                Map data \u00a9 OpenStreetMap contributors, CC-BY-SA.
                Imagery  \u00a9 OpenStreetMap.""");
        copyright.getStyleClass().add("copyright");
        copyright.setAlignment(Pos.CENTER);
        copyright.setMaxWidth(Double.MAX_VALUE);
        return new Group(copyright);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mv = new MapView();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

}
