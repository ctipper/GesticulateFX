/**
 * WriteOutStreamer.java
 * 
 * Created on 23-Apr-2016 12:28:01
 * 
 */
package net.perspective.draw.workers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.serialise.ArrowTypePersistenceDelegate;
import net.perspective.draw.serialise.BasicStrokePersistenceDelegate;
import net.perspective.draw.serialise.FigurePersistenceDelegate;
import net.perspective.draw.serialise.FigureTypePersistenceDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class WriteOutStreamer extends Task<Object> {

    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private ShareUtils share;
    private File file;

    private static final Logger logger = LoggerFactory.getLogger(WriteOutStreamer.class.getName());

    @Inject
    public WriteOutStreamer() {
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

            /**
             * Create an empty pictures descriptor
             */

            updateProgress(0L, 2L);
            ZipEntry entry = new ZipEntry("content/pictures.xml");
            List<Integer> pictures = new ArrayList<>();
            zos.putNextEntry(entry);
            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            encoder.writeObject(pictures);
            encoder.finished();
            zos.closeEntry();
            updateProgress(1L, 2L);

            /**
             * Don't write out images
             */

            entry = new ZipEntry("content/canvas.xml");
            zos.putNextEntry(entry);

            List<DrawItem> drawings = view.getDrawings();
            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            encoder.setPersistenceDelegate(java.awt.BasicStroke.class,
                new BasicStrokePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.ArrowType.class,
                new ArrowTypePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.FigureType.class,
                new FigureTypePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.Figure.class,
                new FigurePersistenceDelegate());
            encoder.setExceptionListener((Exception ex) -> {
                logger.warn(ex.getMessage());
            });

            encoder.writeObject(drawings);
            encoder.finished();
            zos.closeEntry();
            updateProgress(2L, 2L);
        }
    }

}
