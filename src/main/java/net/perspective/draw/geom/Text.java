/**
 * Text.java
 * 
 * Created on 26 May 2021 15:15:15
 * 
 */
package net.perspective.draw.geom;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.V2;

/**
 *
 * @author ctipper
 */

public class Text implements DrawItem, Serializable {

    private String text, font;
    private int style, size;
    private transient Color color;
    private int transparency;
    private boolean isVertical;
    private double angle;
    private CanvasPoint start, end;

    private static final HashMap<TextAttribute, Object> map = new HashMap<>();

    static {
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    private static final long serialVersionUID = 1L;

    /** Creates a new instance of <code>Text</code> */
    public Text() {
        this(0, 0);
    }

    /**
     * Creates a new instance of <code>Text</code>
     * 
     * @param x the x position
     * @param y the y position
     */
    public Text(double x, double y) {
        text = "Please insert text";
        color = null;
        start = new CanvasPoint(x, y);
        transparency = 0;
        isVertical = false;
        angle = 0;
    }

    /**
     * Set the untransformed TL coordinate of the item
     * 
     * @param x the x position
     * @param y the y position
     */
    public void setStart(double x, double y) {
        if (start == null) {
            start = new CanvasPoint();
        }
        start.setLocation(x, y);
    }

    /**
     * 
     * @param start
     * @deprecated
     */
    @Deprecated
    public void setStart(CanvasPoint start) {
        // This should method should almost never be called. Needed by XML Reader.
        this.start = start;
    }

    /**
     * Return the untransformed TL coordinate of the item
     * 
     * @return the item start point
     */
    public CanvasPoint getStart() {
        return start;
    }

    /**
     * Set the dimensions of the item
     * 
     * @param x the width
     * @param y the height
     */
    public void setEnd(double x, double y) {
        if (end == null) {
            end = new CanvasPoint();
        }
        end.setLocation(x, y);
    }

    /**
     * 
     * @param end
     * @deprecated
     */
    @Deprecated
    public void setEnd(CanvasPoint end) {
        // This should method should almost never be called. Needed by XML Reader.
        this.end = end;
    }

    /**
     * Return the dimensions of the item
     * 
     * @return the dimensions
     */
    public CanvasPoint getEnd() {
        return end;
    }

    /**
     * Set the text content of this item
     * 
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Return the text content of this item
     * 
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the font of this item
     * 
     * @param font
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * Return the font of this item 
     * 
     * @return fontname
     */
    public String getFont() {
        return font;
    }

    /**
     * Set the style of the text item
     * 
     * @param style
     */
    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * Return the style of the text item
     * 
     * @return style
     */
    public int getStyle() {
        return style;
    }

    /**
     * Set the font size
     * 
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Return the font size
     * 
     * @return fontsize
     */
    public int getSize() {
        return size;
    }

    /**
     * Update the text properties such as color, font etc.
     * 
     * @param canvas
     */
    public void updateProperties(DrawingArea drawarea) {
        this.setColor(drawarea.getColor());
        this.setTransparency(drawarea.getTransparency());
        this.setFont(drawarea.getFontFamily());
        // Verify that this is Rich Text
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(text);
        if (matcher.find()) {
            this.setStyle(java.awt.Font.PLAIN);
        } else {
            this.setStyle(drawarea.getFontStyle());
        }
        this.setSize(drawarea.getFontSize());
        this.setDimensions();
    }

    /**
     * Set the width and height of the item in points
     * 
     * @param canvas
     */
    public void setDimensions() {
        javafx.scene.text.TextFlow layout = this.getLayout();
        end = new CanvasPoint((double) layout.getLayoutBounds().getWidth(),
            (double) layout.getLayoutBounds().getHeight());
    }

    /**
     * Return a layout used to calculate item properties
     * 
     * @param canvas
     * @return a javafx.scene.text.Text
     */
    @Transient
    public javafx.scene.text.TextFlow getLayout() {
        javafx.scene.text.TextFlow tf;
        // Verify that this is Rich Text
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(text);
        if (matcher.find()) {
            TextFormatter formatter = new TextFormatter();
            tf = formatter.readFxFormattedText(this);
        } else {
            TextFormatter formatter = new TextFormatter();
            javafx.scene.text.Text tt = formatter.readFxText(this);
            tf = new javafx.scene.text.TextFlow(tt);
        }
        tf.autosize();
        return tf;
    }

    /**
     * Returns the 2-tuple of top-left corner location (transformed)
     * the second point is not normalised
     * 
     * @return the 2-tuple of top-left corner location (transformed)
     */
    public CanvasPoint[] getTop() {
        CanvasPoint s = new CanvasPoint(start.x, start.y);
        s = this.getTransform(s);
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
        up = this.getTransform(up);
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
        down = this.getTransform(down);
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
        e = this.getTransform(e);
        return new CanvasPoint[] { e, e };
    }

    private CanvasPoint getTransform(CanvasPoint point) {
        CanvasPoint centre = this.rotationCentre();

        point.translate(-centre.x, -centre.y);
        if (this.getAngle() != 0) {
            // rotate shape about centroid
            point.rotate(this.getAngle());
        }
        if (this.isVertical()) {
            // 90 degree positive rotation
            point.rotate(-Math.PI / 2);
        }
        point.translate(centre.x, centre.y);
        return point;
    }

    /**
     * rotate Text about an anchor with optional 90 deg correction
     */
    private AffineTransform getTransform() {
        CanvasPoint centre = this.rotationCentre();

        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(centre.x, centre.y);
        if (this.getAngle() != 0) {
            // rotate shape about centroid
            transform.rotate(this.getAngle());
        }
        if (this.isVertical()) {
            // 90 degree positive rotation
            transform.rotate(-Math.PI / 2);
        }
        return transform;
    }

    /**
     * Returns the location of the item pivot
     * 
     * @return canvas coordinates of axis of rotation
     */
    public CanvasPoint rotationCentre() {
        return start;
    }

    /**
     * Returns an area that specifies the transformed boundary
     * 
     * @return a transformed shape
     */
    public java.awt.Shape bounds() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, end.x, end.y);
        Area bounds = new Area(rect);
        AffineTransform transform = this.getTransform();
        bounds.transform(transform);
        return bounds;
    }

    /**
     * Detect if a point lies within the bounds, a convenience method
     * 
     * @param x canvas coordinate
     * @param y canvas coordinate
     * @return a boolean property
     */
    public boolean contains(double x, double y) {
        return this.bounds().contains(x, y);
    }

    /**
     * Translate the item
     * 
     * @param xinc x increment
     * @param yinc y increment
     */
    public void moveTo(double xinc, double yinc) {
        start.translate(xinc, yinc);
    }

    @Override
    public Node draw() {
        javafx.scene.text.TextFlow layout = getLayout();
        CanvasPoint axis = this.rotationCentre();
        layout.setLayoutX(axis.x);
        layout.setLayoutY(axis.y);
        layout.getTransforms().add(new Rotate((getAngle() + (isVertical() ? -Math.PI / 2 : 0)) * 180 / Math.PI, 0, 0));
        return layout;
    }

    @Override
    public Node drawAnchors(DrawingArea drawarea) {
        CanvasPoint pad = new CanvasPoint(7.0, 7.0);
        CanvasPoint axis = this.rotationCentre();
        Group anchors = new Group();
        anchors.setMouseTransparent(true);
        CanvasPoint u = this.getTransform(new CanvasPoint(getStart().x, getStart().y));
        Rectangle anchor_1 = new Rectangle();
        anchor_1.setX(u.x - pad.x);
        anchor_1.setY(u.y - pad.y);
        Color alphafill = new Color(1.0, 1.0, 1.0, 0.0);
        anchor_1.setFill(alphafill);
        anchor_1.setStroke(Color.web(drawarea.getThemeFillColor()));
        anchor_1.setStrokeWidth(1.0);
        anchor_1.getStrokeDashArray().addAll(Arrays.asList(1.0, 2.0));
        anchor_1.setWidth(getEnd().x + 2 * pad.x);
        anchor_1.setHeight(getEnd().y + 2 * pad.y);
        anchor_1.setArcWidth(7.0);
        anchor_1.setArcHeight(7.0);
        Rectangle anchor_2 = new Rectangle();
        anchor_2.setX(u.x - pad.x + 3);
        anchor_2.setY(u.y - pad.y + 3);
        anchor_2.setFill(alphafill);
        anchor_2.setStroke(Color.web(drawarea.getThemeFillColor()));
        anchor_2.setStrokeWidth(1.0);
        anchor_2.getStrokeDashArray().addAll(Arrays.asList(1.0, 2.0));
        anchor_2.setWidth(getEnd().x + 2 * pad.x - 6);
        anchor_2.setHeight(getEnd().y + 2 * pad.y - 6);
        anchor_2.setArcWidth(7.0);
        anchor_2.setArcHeight(7.0);
        anchors.getChildren().addAll(anchor_1, anchor_2);
        anchors.getTransforms().add(new Rotate((getAngle() + (isVertical() ? -Math.PI / 2 : 0)) * 180 / Math.PI, axis.x, axis.y));
        return anchors;
    }

    @Override
    public void draw(Graphics2D g2) {
        AffineTransform defaultTransform, transform, rotation;

        defaultTransform = g2.getTransform();

        transform = new AffineTransform();
        TextLayout layout = this.getLayout(g2);

        g2.setColor(fxToAwt(getColor(), ((float) getTransparency()) / 100));
        CanvasPoint axis = this.rotationCentre();
        CanvasPoint offset = new CanvasPoint(0.0, end.y - layout.getDescent());
        offset = V2.rot(offset.x, offset.y, getAngle());
        if (this.isVertical()) {
            offset = V2.rot(offset.x, offset.y, -Math.PI / 2);
            transform.setToTranslation(axis.x + offset.x, axis.y + offset.y);
            // 90 degree positive rotation
            rotation = new AffineTransform();
            rotation.setToRotation((float) -Math.PI / 2);
            transform.concatenate(rotation);
        } else {
            transform.setToTranslation(axis.x + offset.x, axis.y + offset.y);
        }
        rotation = new AffineTransform();
        rotation.setToRotation((float) getAngle());
        transform.concatenate(rotation);
        g2.transform(transform);
        layout.draw(g2, (float) 0.0, (float) 0.0);

        // reset graphics context
        g2.setTransform(defaultTransform);
    }

    /**
     * Return a layout used to calculate item properties
     * 
     * @param canvas
     * @return a TextLayout
     */
    public TextLayout getLayout(Graphics2D g2) {
        TextLayout layout;
        // Verify that this is Rich Text
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(text);
        if (matcher.find()) {
            FontRenderContext context = g2.getFontRenderContext();
            TextFormatter formatter = new TextFormatter();
            AttributedString as = formatter.readFormattedText(this);
            layout = new TextLayout(as.getIterator(), context);
        } else {
            java.awt.Font thisFont = new java.awt.Font(font, getConvertedFontStyle(), size);
            thisFont = thisFont.deriveFont(map);
            FontRenderContext context = g2.getFontRenderContext();
            layout = new TextLayout(text, thisFont, context);
        }
        return layout;
    }

    /**
     * Convert font style from rich text to plain text style
     * 
     * @return
     */
    private int getConvertedFontStyle() {
        int thisstyle = java.awt.Font.PLAIN;
        if ((style & TextFormatter.FONT_BOLD) == TextFormatter.FONT_BOLD) {
            thisstyle = thisstyle | java.awt.Font.BOLD;
        }
        if ((style & TextFormatter.FONT_ITALIC) == TextFormatter.FONT_ITALIC) {
            thisstyle = thisstyle | java.awt.Font.ITALIC;
        }
        if ((style & TextFormatter.FONT_UNDERLINED) == TextFormatter.FONT_UNDERLINED) {
            // No Formatting
        }
        return thisstyle;
    }

    /**
     * Set item colour
     * 
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Return item colour
     * 
     * @return
     */
    @Transient
    public Color getColor() {
        return color;
    }

    @Deprecated
    public void setColor(java.awt.Color color) {
        this.color = awtToFx(color);
    }

    @Deprecated
    public java.awt.Color getAwtColor() {
        return fxToAwt(getColor());
    }

    /**
     * Sets whether the shape is opaque
     * currently has no effect
     * 
     * @param transparency 0 (clear) - 100 (opaque)
     */
    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    /**
     * Returns transparency
     * 
     * @return transparency 0 (clear) - 100 (opaque)
     */
    public int getTransparency() {
        return transparency;
    }

    /**
     * Sets the rotation angle
     * 
     * @param angle The angle in radians
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Return the rotation angle 
     * 
     * @return angle
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Sets the shape to be perpendicular to baseline
     * 
     * @param isVertical a boolean property
     * @deprecated 
     */
    @Deprecated
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    /**
     * 
     * @return a boolean property
     * @deprecated 
     */
    @Deprecated
    public boolean isVertical() {
        return isVertical;
    }

    /**
     * 
     * @param start
     * @deprecated
     */
    @Deprecated
    public void setStartPoint(CanvasPoint start) {
        this.start = start;
    }

    /**
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getStartPoint() {
        return start;
    }

    /**
     * 
     * @param end
     * @deprecated
     */
    @Deprecated
    public void setEndPoint(CanvasPoint end) {
        this.end = end;
    }

    /**
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    @Transient
    public CanvasPoint getEndPoint() {
        return end;
    }

    /**
     * Transform and awt Color to a javafx Color
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link java.awt.Color}
     * @return {@link javafx.scene.paint.Color}
     */
    public static javafx.scene.paint.Color awtToFx(java.awt.Color color) {
        return new javafx.scene.paint.Color(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0);
    }

    /**
     * Transform a javafx Color to an awt Color
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link javafx.scene.paint.Color}
     * @return {@link java.awt.Color}
     */
    public static java.awt.Color fxToAwt(javafx.scene.paint.Color color) {
        return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
    }

    /**
     * Transform a javafx Color to an awt Color with an alpha channel
     * 
     * <p>javafx.scene.paint.Color not serialisable
     * 
     * @param color  a {@link javafx.scene.paint.Color}
     * @param opacity float
     * @return {@link java.awt.Color}
     */
    public static java.awt.Color fxToAwt(javafx.scene.paint.Color color, float opacity) {
        return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), opacity);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // deserialise colors from awt.Color
        Class<?> c = (Class<?>) in.readObject();
        this.color = awtToFx((java.awt.Color) in.readObject());
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        out.writeObject(java.awt.Color.class);
        out.writeObject(fxToAwt(getColor()));
    }

}
