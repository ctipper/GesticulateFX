/**
 * ImageLoadWorker.java
 * 
 * Created on Jan 12, 2010, 10:11:10 PM
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
package net.perspective.draw.workers;

import com.google.inject.Injector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.FileUtils;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class ImageLoadWorker extends Task<Object> {

    private List<Image> images;
    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private ShareUtils share;
    private List<File> imageFiles;
    private double shift;
    private boolean success;
    private double pageWidth;      // canvas width pixels
    private double pageHeight;     // canvas height pixels

    private static final Logger logger = LoggerFactory.getLogger(ImageLoadWorker.class.getName());

    @Inject
    public ImageLoadWorker() {
        this.shift = 20.0;
        success = false;
    }

    @Override
    protected Object call() throws Exception {
        pageWidth = drawarea.getScene().getWidth();
        pageHeight = drawarea.getScene().getHeight();
        this.imageFiles = share.getImageFiles();
        return new ImageLoader();
    }

    @Override
    public void done() {
        logger.info("Reading images complete.");
        Platform.runLater(() -> {
            if (!images.isEmpty()) {
                for (Image image : images) {
                    Picture picture = injector.getInstance(Picture.class);
                    picture.setStart(shift, shift);
                    ImageItem item = new ImageItem(image);
                    item.setFormat(FileUtils.getExtension(imageFiles.get(images.indexOf(image))));
                    int index = view.setImageItem(item);
                    double width = (double) image.getWidth();
                    double height = (double) image.getHeight();
                    double scale = getScale(width, height);
                    logger.trace("Image relative scale: {}", scale);
                    picture.setImage(index, width, height);
                    picture.setScale(scale);
                    view.setNewItem(picture);
                    view.resetNewItem();
                    shift = shift + 10.0;
                }
            }
            share.setImageFiles(null);
        });
        CompletableFuture.runAsync(() -> {
            try {
                // introduce a minimum visible interval
                if (success) {
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
            }
        }, share.executor).thenRun(() -> {
            Platform.runLater(() -> {
                controller.getProgressVisibleProperty().setValue(Boolean.FALSE);
                controller.getProgressProperty().unbind();
                if (success) {
                    controller.setStatusMessage("Read Pictures");
                }
            });
        });
    }

    final class ImageLoader {

        ImageLoader() {
            logger.info("Reading images initialised.");
            try {
                success = true;
                this.make();
            } catch (IOException e) {
                logger.warn(e.getMessage());
                success = false;
            }
        }

        public void make() throws IOException {
            images = new ArrayList<>();
            for (File file : imageFiles) {
                if (FileUtils.getExtension(file.getName()).equals("svg")) {
                    images.add(SwingFXUtils.toFXImage(rasterize(file), null));
                } else {
                    images.add(SwingFXUtils.toFXImage(ImageIO.read(file), null));
                }
            }
        }

    }

    /**
     * Transcode SVG document 
     * 
     * @see <a href="https://stackoverflow.com/a/11436655">How to get a BufferedImage from a SVG?</a>
     * 
     * @param svgFile the file
     * @return a buffered image
     * @throws IOException 
     */
    public BufferedImage rasterize(File svgFile) throws IOException {

        final BufferedImage[] imagePointer = new BufferedImage[1];

        // Rendering hints can't be set programatically, so
        // we override defaults with a temporary stylesheet.
        // These defaults emphasize quality and precision, and
        // are more similar to the defaults of other SVG viewers.
        // SVG documents can still override these defaults.
        String css = "svg {"
            + "shape-rendering: geometricPrecision;"
            + "text-rendering:  geometricPrecision;"
            + "color-rendering: optimizeQuality;"
            + "image-rendering: optimizeQuality;"
            + "}";
        Path cssFile = Files.createTempFile(Files.createTempDirectory("temp-dir"), "batik-default-override-", ".css");
        FileUtils.writeStringToFile(cssFile.toFile(), css);
        TranscodingHints transcoderHints = new TranscodingHints();
        transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
            SVGDOMImplementation.getDOMImplementation());
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
            SVGConstants.SVG_NAMESPACE_URI);
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        transcoderHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toUri().toString());
        transcoderHints.put(SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.TRUE);

        try {
            TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));

            ImageTranscoder t = new ImageTranscoder() {

                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out)
                    throws TranscoderException {
                    imagePointer[0] = image;
                }
            };
            t.setTranscodingHints(transcoderHints);
            t.transcode(input, null);
        } catch (TranscoderException ex) {
            logger.error("Couldn't convert {}", svgFile.getName());
        } finally {
            cssFile.toFile().deleteOnExit();
        }

        return imagePointer[0];
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

}
