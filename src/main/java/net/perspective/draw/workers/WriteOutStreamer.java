/**
 * WriteOutStreamer.java
 * 
 * Created on 23-Apr-2016 12:28:01
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ItemStore;
import net.perspective.draw.ItemStore.SaveState;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.serialise.ArrowLinePersistenceDelegate;
import net.perspective.draw.serialise.ArrowTypePersistenceDelegate;
import net.perspective.draw.serialise.BasicStrokePersistenceDelegate;
import net.perspective.draw.serialise.FigurePersistenceDelegate;
import net.perspective.draw.serialise.FigureTypePersistenceDelegate;
import net.perspective.draw.serialise.InstantPersistenceDelegate;
import net.perspective.draw.serialise.Path2DPersistenceDelegate;
import net.perspective.draw.serialise.TextPersistenceDelegate;
import net.perspective.draw.util.FileUtils;
import net.perspective.draw.util.Messages;

/**
 * 
 * @author ctipper
 */

public class WriteOutStreamer extends Task<Object> {

    private final CanvasView view;
    private final ApplicationController controller;
    @Inject ItemStore store;
    @Inject ShareUtils share;
    private File file;
    private SaveState state;
    private List<ImageItem> pictures;

    private static final Logger logger = LoggerFactory.getLogger(WriteOutStreamer.class.getName());

    @Inject
    public WriteOutStreamer(CanvasView view, ApplicationController controller) {
        this.view = view;
        this.controller = controller;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Capture the document to be written. Call on the FX thread, before submitting the task.
     *
     * <p>{@link #call()} runs on a worker thread, and the image list is plain FX-thread state — a
     * paste landing mid-write would have the writer iterating a list under mutation. The item list
     * is safe to read off-thread, being a published snapshot, but the <em>items</em> are live and
     * the EDT may still edit one in place. {@link ItemStore#saveState()} records the revision so
     * that can at least be detected; see {@link #make()}.</p>
     */
    public void captureDocument() {
        state = store.saveState();
        pictures = new ArrayList<>(view.getImageItems());
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
                controller.setStatusMessage(Messages.get("status.savedDocument"));
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

            List<DrawItem> drawings = state.items();
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
            encoder.setPersistenceDelegate(java.awt.geom.Path2D.Double.class,
                new Path2DPersistenceDelegate());
            encoder.setExceptionListener((Exception ex) -> {
                logger.warn(ex.getMessage());
            });

            encoder.writeObject(drawings);
            encoder.finished();
            zos.closeEntry();
            updateProgress(3L, 3L);

            /*
             * The capture fixed which items exist and in what order, but not the items themselves:
             * the EDT may have edited one in place while its fields were being read, giving an
             * item that serialized half-old and half-new. A moved revision is the only evidence of
             * that, and only holds because every EDT mutation republishes through replaceItem.
             */
            if (store.revision() != state.revision()) {
                logger.warn("Document changed during save ({} -> {}); the written file may not "
                    + "match either state.", state.revision(), store.revision());
            }
        }
    }

}
