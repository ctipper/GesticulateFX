/**
 * ReadInFunnel.java
 * 
 * Created on 23-Apr-2016 12:33:36
 * 
 */
package net.perspective.draw.workers;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class ReadInFunnel extends Task {

    @Inject private CanvasView view;
    private File file;
    private List<DrawItem> drawings;
    private boolean success = false;

    private static final Logger logger = LoggerFactory.getLogger(WriteOutStreamer.class.getName());

    @Inject
    public ReadInFunnel() {
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected Object call() throws Exception {
        success = false;
        logger.info("Open file started...");
        return new Deserialiser();
    }

    @Override
    public void done() {
        logger.info("Open completed.");
        Platform.runLater(() -> {
            if (success) {
                view.clearView();
                try {
                    for (DrawItem drawing : drawings) {
                        drawing = checkDrawings(drawing);
                        view.setNewItem(drawing);
                        view.appendItemToCanvas(view.getNewItem());
                    }
                } catch (ClassCastException e) {
                    logger.warn(e.getMessage());
                }
            }
        });
    }

    private DrawItem checkDrawings(DrawItem drawing) {
        if (drawing instanceof Figure) {
            ((Figure) drawing).setFactory();
            ((Figure) drawing).setEndPoints();
            ((Figure) drawing).setPath();
        }
        return drawing;
    }

    final class Deserialiser {

        XMLDecoder decoder;

        Deserialiser() {
            logger.info("Deserialisation initialised.");
            try {
                success = true;
                this.make();
            } catch (IOException | NullPointerException e) {
                logger.warn(e.getMessage());
                success = false;
            }
        }

        @SuppressWarnings("unchecked")
        public void make() throws IOException {
            try (ZipFile zf = new ZipFile(file)) {
                ZipEntry ze = zf.getEntry("content/canvas.xml");
                decoder = new XMLDecoder(new BufferedInputStream(zf.getInputStream(ze)));
                decoder.setExceptionListener((Exception ex) -> {
                    logger.warn(ex.getMessage());
                    success = false;
                });
                drawings = (ArrayList<DrawItem>) decoder.readObject();
            }
        }
    }
}
