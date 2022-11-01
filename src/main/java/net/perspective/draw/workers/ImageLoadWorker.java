/**
 * ImageLoadWorker.java
 * 
 * Created on Jan 12, 2010, 10:11:10 PM
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
package net.perspective.draw.workers;

import com.google.inject.Injector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.FileUtils;
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
                try(FileInputStream in = new FileInputStream(file)) {
                    images.add(new Image(in));
                }
            }
        }

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
