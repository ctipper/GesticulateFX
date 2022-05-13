/*
 * StreetMap.java
 * 
 * Created on May 21, 2012, 4:11:43 PM
 * 
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
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
     * Update the public properties of the streetmap 
     * 
     * @param canvas  the {@link net.perspective.draw.DrawingArea}
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

    public void setCenter() {
        MapPoint mp = mv.getCenter();
        setLatitude(mp.getLatitude());
        setLongitude(mp.getLongitude());
        setZoom((int) Math.round(mv.getZoom()));
    }

    /**
     * Provide MapView for FX canvas
     * 
     * @return a MapView
     */
    @Override
    public Node draw() {
        mv.setCenter(latitude, longitude);
        mv.setZoom(zoom);
        mv.setPrefWidth(end.x);
        mv.setPrefHeight(end.y);
        mv.setCursor(javafx.scene.Cursor.OPEN_HAND);
        final Group copyright = createCopyright();
        StackPane bp = new StackPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                copyright.setLayoutX(getWidth() - copyright.prefWidth(-1));
                copyright.setLayoutY(getHeight() - copyright.prefHeight(-1));
            }
        };
        bp.getChildren().addAll(mv, copyright);
        bp.setLayoutX(start.x);
        bp.setLayoutY(start.y);
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
        this.mv = new com.gluonhq.maps.MapView();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

}
