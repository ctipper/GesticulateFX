/*
 * CanvasTransferHandler.java
 * 
 * Created on Nov 16, 2013 5:38:34 PM
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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

import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;

/**
 * 
 * @author ctipper
 */

@Singleton
public class CanvasTransferHandler {

    private final Provider<DrawingArea> drawareaProvider;
    private final Provider<CanvasView> viewProvider;
    @Inject MapController mapper;
    @Inject Provider<Picture> pictureProvider;
    @Inject Provider<StreetMap> streetMapProvider;
    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.DrawItem";
    DataFlavor drawItemFlavor;
    String dataflavor = "";
    private double shift;
    private double pageWidth;      // canvas width pixels
    private double pageHeight;     // canvas height pixels

    public static int COPY = 1;
    public static int MOVE = 2;

    private static final Logger logger = LoggerFactory.getLogger(CanvasTransferHandler.class.getName());

    @Inject
    public CanvasTransferHandler(Provider<DrawingArea> drawareaProvider, Provider<CanvasView> viewProvider) {
        this.drawareaProvider = drawareaProvider;
        this.viewProvider = viewProvider;
        //Try to create a DataFlavor for drawItems.
        try {
            drawItemFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            logger.warn("mimeType failed in CanvasTransferHandler");
        }
        shift = 10.0;
    }

    public boolean importData(Transferable t) {
        if (hasDrawItemFlavor(t.getTransferDataFlavors())) {
            try {
                if (dataflavor.equals("drawitem")) {
                    DrawItem item = (DrawItem) t.getTransferData(drawItemFlavor);
                    // add item to Canvas
                    item.moveTo(shift, shift);
                    item = checkDrawings(item);
                    viewProvider.get().appendItemToCanvas(item);
                } else if (dataflavor.equals("imageitem")) {
                    java.awt.Image img = (java.awt.Image) t.getTransferData(DataFlavor.imageFlavor);
                    Image image = SwingFXUtils.toFXImage(toBufferedImage(img), null);
                    Picture picture = pictureProvider.get();
                    picture.moveTo(shift, shift);
                    ImageItem item = new ImageItem(image);
                    item.setFormat("PNG");
                    int index = viewProvider.get().setImageItem(item);
                    double width = (double) image.getWidth();
                    double height = (double) image.getHeight();
                    double scale = getScale(width, height);
                    logger.debug("Image relative scale: {}", scale);
                    picture.setImage(index, width, height);
                    picture.setScale(scale);
                    viewProvider.get().setNewItem(picture);
                    viewProvider.get().resetNewItem();
                }
                shift = shift + 20.0;
                logger.debug("Item added to canvas");
                return true;
            } catch (UnsupportedFlavorException e) {
                logger.warn("importData: unsupported data flavor");
            } catch (IOException e) {
                logger.warn("importData: I/O exception");
            }
        }
        logger.trace("ImportData");
        return false;
    }

    protected Transferable createTransferable() {
        int selected = viewProvider.get().getSelected();
        if (selected == -1) {
            return null;
        }
        DrawItem data = viewProvider.get().getDrawings().get(selected);
        logger.trace("Item createTransferable");
        return new DrawItemTransferable(data);
    }

    protected void exportDone(Transferable data, int action) {
        if (action == MOVE) {
            viewProvider.get().deleteSelectedItem();
            logger.debug("Removed selected item");
            shift = 0;
        } else {
            shift = 20.0;
            logger.debug("Copied selected item");
        }
        logger.trace("ExportDone");
    }

    protected boolean hasDrawItemFlavor(DataFlavor[] flavors) {
        for (DataFlavor f : flavors) {
            if (drawItemFlavor.equals(f)) {
                logger.debug(mimeType);
                dataflavor = "drawitem";
                return true;
            } else if (DataFlavor.imageFlavor.equals(f)) {
                logger.debug("image/x-java-image;class=java.awt.Image");
                dataflavor = "imageitem";
                return true;
            }
        }

        return false;
    }

    private DrawItem checkDrawings(DrawItem drawing) {
        if (drawing instanceof Picture && !(drawing instanceof StreetMap)) {
            var item = pictureProvider.get();
            try {
                BeanUtils.copyProperties(item, drawing);
                return item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        } else if (drawing instanceof StreetMap streetmap) {
            var item = streetMapProvider.get();
            try {
                BeanUtils.copyProperties(item, streetmap);
                item.init();
                item.filterHandlers();
                mapper.copyMap(item);
                return item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        }
        
        if (drawing instanceof Grouped grouped) {
            var item = new Grouped();
            for (DrawItem shape : grouped.getDrawItems()) {
                item.addDrawItem(checkDrawings(shape));
            }
            item.setAngle(grouped.getAngle());
            item.setTransparency(grouped.getTransparency());
            item.setScale(grouped.getScale());
            return item;
        }

        return drawing;
    }

    /**
     * Get a representative scale for images larger than page size
     * 
     * 1. ImageWidth &gt; pageWidth resize to 80% pageWidth
     * 2. ImageHeight &gt; pageHeight resize to 80% pageHeight
     * 3. if imageSize &lt; pageSize do nothing
     * 
     * @param width
     * @param height
     * @return 
     */
    private double getScale(double width, double height) {
        pageWidth = drawareaProvider.get().getScene().getWidth();
        pageHeight = drawareaProvider.get().getScene().getHeight();
        if ((width <= pageWidth) && (height <= pageHeight)) {
            return 1d;
        }
        if ((width <= pageWidth) && (height > pageHeight)) {
            return 0.8 * pageHeight / height;
        }
        if ((width > pageWidth) && (height <= pageHeight)) {
            return 0.8 * pageWidth / width;
        }
        if ((width > pageWidth) && (height > pageHeight)) {
            double ratio_w = pageWidth / width;
            double ratio_h = pageHeight / height;
            if (ratio_w <= ratio_h) {
                return 0.8 * pageWidth / width;
            } else {
                return 0.8 * pageHeight / height;
            }
        }
        return 1d;
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     *
     * @see <a href="https://stackoverflow.com/a/13605411">https://stackoverflow.com</a>
     */
    public BufferedImage toBufferedImage(java.awt.Image img) {
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

}
