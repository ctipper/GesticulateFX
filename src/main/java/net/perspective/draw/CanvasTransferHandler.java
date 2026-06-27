/*
 * CanvasTransferHandler.java
 * 
 * Created on Nov 16, 2013 5:38:34 PM
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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
import java.util.ArrayList;
import java.util.List;
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
import net.perspective.draw.util.FileUtils;
import net.perspective.draw.util.SVGRead;

/**
 * 
 * @author ctipper
 */

@Singleton
public class CanvasTransferHandler {

    private final Provider<DrawingArea> drawareaProvider;
    private final Provider<CanvasView> viewProvider;
    @Inject MapController mapper;
    @Inject SVGRead svgRead;
    @Inject Provider<ShareUtils> shareProvider;
    @Inject Provider<Picture> pictureProvider;
    @Inject Provider<StreetMap> streetMapProvider;
    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.DrawItem";
    DataFlavor drawItemFlavor;
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

    /**
     * Import the contents of a transfer (clipboard paste or drag-and-drop) onto
     * the canvas.
     * <p>
     * The supported payload is resolved from the transfer by
     * {@link #resolveFlavor} and dispatched to the matching handler:
     * a serialized {@link DrawItem}, raw image data, a list of image files, or
     * SVG markup. Each successful import nudges the paste offset so repeated
     * pastes do not stack exactly on top of one another.
     *
     * @param t the transfer to import; its available {@link DataFlavor}s
     *          determine how it is handled
     * @return {@code true} if a supported item was imported, {@code false} if the
     *         transfer held nothing importable or a transfer error occurred
     */
    public boolean importData(Transferable t) {
        Payload payload = resolveFlavor(t);
        if (payload == null) {
            logger.trace("ImportData");
            return false;
        }
        try {
            boolean imported = switch (payload.flavor()) {
                case DRAWITEM -> importDrawItem(t);
                case IMAGEITEM -> importImageItem(t);
                case FILEITEM -> importFileItem(t);
                case SVGITEM -> importSvgItem(payload.svg());
            };
            if (imported) {
                shift = shift + 20.0;
                logger.debug("Item added to canvas");
            }
            return imported;
        } catch (UnsupportedFlavorException e) {
            logger.warn("importData: unsupported data flavor");
        } catch (IOException e) {
            logger.warn("importData: I/O exception");
        }
        return false;
    }

    /**
     * Paste a serialized {@link DrawItem} onto the canvas.
     */
    private boolean importDrawItem(Transferable t) throws UnsupportedFlavorException, IOException {
        DrawItem item = (DrawItem) t.getTransferData(drawItemFlavor);
        // add item to Canvas
        item.moveTo(shift, shift);
        item = checkDrawings(item);
        viewProvider.get().appendItemToCanvas(item);
        return true;
    }

    /**
     * Paste raw image data (e.g. copied from another application) onto the canvas.
     */
    private boolean importImageItem(Transferable t) throws UnsupportedFlavorException, IOException {
        java.awt.Image img = (java.awt.Image) t.getTransferData(DataFlavor.imageFlavor);
        placeImage(SwingFXUtils.toFXImage(toBufferedImage(img), null), "PNG");
        return true;
    }

    /**
     * Place a loaded image onto the canvas as a scaled {@link Picture}.
     *
     * @param image the image to add
     * @param format the source format recorded on the {@link ImageItem}
     */
    private void placeImage(Image image, String format) {
        Picture picture = pictureProvider.get();
        picture.moveTo(shift, shift);
        ImageItem item = new ImageItem(image);
        item.setFormat(format);
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

    /**
     * Paste image files dropped/copied from the file manager (jpg, jpeg, png,
     * gif, svg). Unsupported entries are ignored; returns {@code false} when the
     * selection holds no supported image files.
     */
    private boolean importFileItem(Transferable t) throws UnsupportedFlavorException, IOException {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        List<File> imageFiles = new ArrayList<>();
        for (File file : files) {
            switch (FileUtils.getExtension(file.getName()).toLowerCase()) {
                case "jpg", "jpeg", "png", "gif", "svg" -> imageFiles.add(file);
            }
        }
        if (imageFiles.isEmpty()) {
            logger.debug("importData: no supported image files in selection");
            return false;
        }
        // ImageLoadWorker handles threading and the progress indicator
        shareProvider.get().readPictures(imageFiles);
        return true;
    }

    /**
     * Paste SVG markup (already captured during {@link #resolveFlavor}) by
     * rasterizing it in memory and placing it on the canvas.
     */
    private boolean importSvgItem(String svg) throws IOException {
        BufferedImage buffered = svgRead.rasterize(svg);
        if (buffered == null) {
            logger.debug("importData: could not rasterize pasted SVG");
            return false;
        }
        placeImage(SwingFXUtils.toFXImage(buffered, null), "svg");
        return true;
    }

    /**
     * Determine whether pasted text is SVG markup.
     *
     * @param text the clipboard string
     * @return {@code true} if the text looks like an SVG document
     */
    private boolean isSvgMarkup(String text) {
        if (text == null) {
            return false;
        }
        String head = text.stripLeading();
        head = head.substring(0, Math.min(head.length(), 512)).toLowerCase();
        return head.startsWith("<svg")
            || head.startsWith("<!doctype svg")
            || (head.startsWith("<?xml") && head.contains("<svg"));
    }

    /**
     * Wrap the currently selected drawing in a {@link Transferable} for export
     * (copy or cut).
     *
     * @return a transferable for the selected item, or {@code null} if nothing
     *         is selected
     */
    protected Transferable createTransferable() {
        int selected = viewProvider.get().getSelected();
        if (selected == -1) {
            return null;
        }
        DrawItem data = viewProvider.get().getDrawings().get(selected);
        logger.trace("Item createTransferable");
        return new DrawItemTransferable(data);
    }

    /**
     * Finish an export started by {@link #createTransferable}, after the data
     * has been handed to the clipboard.
     * <p>
     * On {@link #MOVE} the source item is removed from the canvas and the paste
     * offset is reset; on {@link #COPY} the item is left in place.
     *
     * @param data   the transferable that was exported
     * @param action the completed action, either {@link #COPY} or {@link #MOVE}
     */
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

    /**
     * Resolve which supported payload a transfer offers, for {@link #importData}
     * to dispatch on.
     * <p>
     * A single transfer may advertise several flavors at once, so they are
     * matched in priority order regardless of their order in the array: a native
     * {@link DrawItem} first, then a file list, then raw image data, and finally
     * SVG markup carried as text. Image files are preferred over raw image data
     * so they load via {@code ImageLoadWorker} (svg-aware, threaded, with a
     * progress indicator) rather than being decoded eagerly; SVG markup is the
     * lowest priority and only matches when the string actually looks like SVG.
     * The SVG text is read once here and carried on the returned {@link Payload}
     * so the import does not re-read the clipboard.
     *
     * @param t the transfer whose flavors (and, for SVG, content) are inspected
     * @return the resolved {@link Payload}, or {@code null} if nothing importable was found
     */
    private Payload resolveFlavor(Transferable t) {
        DataFlavor[] flavors = t.getTransferDataFlavors();
        for (DataFlavor f : flavors) {
            if (drawItemFlavor.equals(f)) {
                logger.debug(mimeType);
                return new Payload(Flavor.DRAWITEM, null);
            }
        }
        for (DataFlavor f : flavors) {
            if (DataFlavor.javaFileListFlavor.equals(f)) {
                logger.debug("application/x-java-file-list;class=java.util.List");
                return new Payload(Flavor.FILEITEM, null);
            }
        }
        for (DataFlavor f : flavors) {
            if (DataFlavor.imageFlavor.equals(f)) {
                logger.debug("image/x-java-image;class=java.awt.Image");
                return new Payload(Flavor.IMAGEITEM, null);
            }
        }
        for (DataFlavor f : flavors) {
            if (DataFlavor.stringFlavor.equals(f)) {
                try {
                    String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                    if (isSvgMarkup(text)) {
                        logger.debug("image/svg+xml;class=java.lang.String");
                        return new Payload(Flavor.SVGITEM, text);
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    logger.warn("resolveFlavor: could not read clipboard text");
                }
                break;
            }
        }

        return null;
    }

    /** A resolved clipboard payload: its kind, plus the captured SVG markup for {@link Flavor#SVGITEM}. */
    private record Payload(Flavor flavor, String svg) {
    }

    /** Supported clipboard payloads, resolved by {@link #resolveFlavor}. */
    private enum Flavor {
        DRAWITEM, IMAGEITEM, FILEITEM, SVGITEM
    }

    private DrawItem checkDrawings(DrawItem drawing) {
        if (drawing instanceof Picture picture && !(drawing instanceof StreetMap)) {
            var item = pictureProvider.get();
            try {
                BeanUtils.copyProperties(item, picture);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        } else if (drawing instanceof StreetMap streetmap) {
            var item = streetMapProvider.get();
            try {
                BeanUtils.copyProperties(item, streetmap);
                item.init();
                item.filterHandlers();
                item = mapper.copyMap(item);
                drawing = item;
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
            drawing = item;
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
