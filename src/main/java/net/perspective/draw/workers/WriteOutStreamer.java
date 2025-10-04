/**
 * WriteOutStreamer.java
 * 
 * Created on 23-Apr-2016 12:28:01
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
package net.perspective.draw.workers;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.serialise.ArrowLinePersistenceDelegate;
import net.perspective.draw.serialise.ArrowTypePersistenceDelegate;
import net.perspective.draw.serialise.BasicStrokePersistenceDelegate;
import net.perspective.draw.serialise.FigurePersistenceDelegate;
import net.perspective.draw.serialise.FigureTypePersistenceDelegate;
import net.perspective.draw.serialise.InstantPersistenceDelegate;
import net.perspective.draw.serialise.TextPersistenceDelegate;
import net.perspective.draw.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class WriteOutStreamer extends Task<Object> {

    private final CanvasView view;
    private final ApplicationController controller;
    @Inject ShareUtils share;
    private File file;

    private static final Logger logger = LoggerFactory.getLogger(WriteOutStreamer.class.getName());

    @Inject
    public WriteOutStreamer(CanvasView view, ApplicationController controller) {
        this.view = view;
        this.controller = controller;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected Object call() throws Exception {
        logger.info("Save started...");
        return new Serialiser();
    }

    @Override
    public void done() {
        logger.info("Save completed.");
        CompletableFuture.runAsync(() -> {
            try {
                // introduce a minimum visible interval
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }, share.executor).thenRun(() -> {
            Platform.runLater(() -> {
                controller.getProgressVisibleProperty().setValue(Boolean.FALSE);
                controller.getProgressProperty().unbind();
                controller.setStatusMessage("Saved document");
            });
        });
    }

    final class Serialiser {

        net.perspective.draw.serialise.XMLEncoder encoder;
        ZipOutputStream zos = null;

        Serialiser() {
            logger.info("Serialisation initialised.");
            try {
                this.make();
            } catch (IOException e) {
                logger.warn(e.getMessage());
            } finally {
                if (zos != null) {
                    try {
                        zos.close();
                    } catch (IOException e) {
                        logger.info(e.getMessage());
                    }
                }
            }
        }

        public void make() throws IOException {
            FileOutputStream fos = new FileOutputStream(file);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            updateProgress(0L, 3L);

            /**
             * Create pictures descriptor
             */
            ZipEntry entry = new ZipEntry("content/pictures.xml");
            zos.putNextEntry(entry);

            List<ImageItem> pictures = view.getImageItems();
            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            encoder.setPersistenceDelegate(java.time.Instant.class,
                new InstantPersistenceDelegate());
            encoder.writeObject(pictures);
            encoder.finished();
            zos.closeEntry();
            updateProgress(1L, 3L);

            /**
             * write out images
             */
            for (int index = 0; index < pictures.size(); index++) {
                BufferedImage img = SwingFXUtils.fromFXImage(pictures.get(index).getImage(), null); // retrieve image
                entry = new ZipEntry("images/" + FileUtils.getImageName(index));
                zos.putNextEntry(entry);
                try {
                    ImageIO.write(img, "png", zos);
                } catch (IllegalArgumentException e) {
                    logger.info("Image file blank.");
                }
                zos.closeEntry();
            }
            updateProgress(2L, 3L);

            /**
             * write out drawings
             */
            entry = new ZipEntry("content/canvas.xml");
            zos.putNextEntry(entry);

            List<DrawItem> drawings = view.getDrawings();
            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            encoder.setPersistenceDelegate(java.awt.BasicStroke.class,
                new BasicStrokePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.ArrowType.class,
                new ArrowTypePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.ArrowLine.class,
                new ArrowLinePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.FigureType.class,
                new FigureTypePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.Figure.class,
                new FigurePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.Text.class,
                new TextPersistenceDelegate());
            encoder.setExceptionListener((Exception ex) -> {
                logger.warn(ex.getMessage());
            });

            encoder.writeObject(drawings);
            encoder.finished();
            zos.closeEntry();
            updateProgress(3L, 3L);
        }
    }

}
