/**
 * SVGLoadWorker.java
 *
 * Created on Jun 28, 2026
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
package net.perspective.draw.workers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.inject.Inject;
import javax.inject.Provider;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.Messages;
import net.perspective.draw.util.SVGRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rasterizes a single in-memory SVG document (e.g. pasted from the clipboard)
 * onto the canvas off the FX thread, since transcoding can be slow.
 *
 * @author ctipper
 */

public class SVGLoadWorker extends Task<Object> {

    private Image image;
    private final DrawingArea drawarea;
    private final CanvasView view;
    private final ApplicationController controller;
    @Inject ShareUtils share;
    @Inject SVGRead svgRead;
    @Inject Provider<Picture> pictureProvider;
    private String markup;
    private double shift;
    private boolean success;

    private static final Logger logger = LoggerFactory.getLogger(SVGLoadWorker.class.getName());

    @Inject
    public SVGLoadWorker(DrawingArea drawarea, CanvasView view, ApplicationController controller) {
        this.drawarea = drawarea;
        this.view = view;
        this.controller = controller;
        this.shift = 20.0;
        success = false;
    }

    /**
     * Set the SVG document markup to rasterize.
     *
     * @param markup the markup
     */
    public void setMarkup(String markup) {
        this.markup = markup;
    }

    @Override
    protected Object call() throws Exception {
        return new SVGLoader();
    }

    @Override
    public void done() {
        logger.info("Reading SVG complete.");
        Platform.runLater(() -> {
            if (image != null) {
                Picture picture = pictureProvider.get();
                picture.setStart(shift, shift);
                ImageItem item = new ImageItem(image);
                item.setFormat("svg");
                int index = view.setImageItem(item);
                double width = (double) image.getWidth();
                double height = (double) image.getHeight();
                double scale = drawarea.fitScale(width, height);
                logger.trace("Image relative scale: {}", scale);
                picture.setImage(index, width, height);
                picture.setScale(scale);
                view.setNewItem(picture);
                view.resetNewItem();
            }
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
                    controller.setStatusMessage(Messages.get("status.readPictures"));
                }
            });
        });
    }

    final class SVGLoader {

        SVGLoader() {
            logger.info("Reading SVG initialised.");
            try {
                success = true;
                this.make();
            } catch (IOException e) {
                logger.warn(e.getMessage());
                success = false;
            }
        }

        public void make() throws IOException {
            if (markup == null) {
                return;
            }
            BufferedImage buffered = svgRead.rasterize(markup);
            if (buffered == null) {
                // a failed transcode (e.g. malformed or externally-referencing SVG)
                logger.warn("Couldn't rasterize pasted SVG");
                return;
            }
            image = SwingFXUtils.toFXImage(buffered, null);
        }

    }

}
