/**
 * ImageItem.java
 * 
 * Created on Sep 24, 2010, 9:46:06 PM
 * 
 */
package net.perspective.draw;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.time.Instant;
import java.util.Date;
import javafx.scene.image.Image;

/**
 * 
 * @author ctipper
 */

public class ImageItem {

    private transient Image image;
    private String format;          // gif, jpg or png
    private int referenceCount;     // count of usage
    private Instant timestamp;      // creation or update time

    /** Creates a new instance of <code>ImageItem</code> */
    public ImageItem() {
        this.image = null;
        this.format = "";
        this.referenceCount = 0;
        this.timestamp = Instant.now();
    }

    /**
     * Creates a new instance of <code>ImageItem</code>
     * 
     * @param image
     * @param format
     * @param count
     */
    public ImageItem(Image image, String format, int count) {
        this.image = image;
        this.format = format;
        this.referenceCount = count;
        this.timestamp = Instant.now();
    }

    /**
     * Creates a new instance of <code>ImageItem</code>
     * 
     * copy constructor 
     * 
     * @param item 
     */
    public ImageItem(ImageItem item) {
        this(item.getImage(), item.getFormat(), item.getReferenceCount());
        item.setTimestamp(timestamp);
    }

    /**
     * Creates a new instance of <code>ImageItem</code>
     * 
     * 'network' copy constructor 
     * 
     * @param item
     * @param offset 
     */
    public ImageItem(ImageItem item, long offset) {
        this(item.getImage(), item.getFormat(), item.getReferenceCount());
        timestamp = Instant.ofEpochMilli(Instant.now().toEpochMilli() + offset);
        item.setTimestamp(timestamp);
    }

    /**
     * Creates a new instance of <code>ImageItem</code>
     * 
     * serialisation constructor 
     * 
     * @param timestamp 
     */
     @ConstructorProperties({ "timestamp" })
     public ImageItem(Instant timestamp) {
        this();
        this.timestamp = timestamp;
    }

    /**
     * Creates a new instance of <code>ImageItem</code>
     * 
     * @param image
     */
    public ImageItem(Image image) {
        this();
        this.image = image;
    }

    /**
     * Set the reference count for this image item
     * 
     * @param referenceCount
     */
    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    /**
     * Return the reference count for this image item
     * @return
     */
    public int getReferenceCount() {
        return referenceCount;
    }

    /**
     * Set the image
     * 
     * @param image
     */
    @Transient
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Get the image
     * 
     * @return
     */
    @Transient
    public Image getImage() {
        return image;
    }

    /**
     * Set the format
     * 
     * @param format  one of gif, jpg, png
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Get the format
     * 
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the timestamp creation or update time
     * 
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp.toInstant();
    }

    /**
     * Set the timestamp creation or update time
     * 
     * @param timestamp
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the timestamp
     * 
     * @return
     */
    public Instant getTimestamp() {
        return timestamp;
    }

}
