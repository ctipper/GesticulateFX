/**
 * ReadInFunnel.java
 * 
 * Created on 23-Apr-2016 12:33:36
 * 
 */
package net.perspective.draw.workers;

import com.google.inject.Injector;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.ImageItem;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Edge;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.FileUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class ReadInFunnel extends Task<Object> {

    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private ShareUtils share;
    private File file;
    private List<DrawItem> drawings;
    private List<ImageItem> pictures;
    private boolean success;

    private static final Logger logger = LoggerFactory.getLogger(ReadInFunnel.class.getName());

    @Inject
    public ReadInFunnel() {
        success = false;
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
                drawarea.prepareDrawing();
                for (ImageItem picture : pictures) {
                    int index = view.setImageItem(picture);
                }
                try {
                    for (DrawItem drawing : drawings) {
                        drawing = checkDrawings(drawing);
                        view.setNewItem(drawing);
                        view.resetNewItem();
                    }
                } catch (ClassCastException e) {
                    logger.warn(e.getMessage());
                }
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
                    controller.setStatusMessage("Opened document");
                }
            });
        });
    }

    private DrawItem checkDrawings(DrawItem drawing) {

        if (drawing instanceof ArrowLine) {
            DrawItem item = new Edge();
            try {
                BeanUtils.copyProperties(item, ((ArrowLine) drawing).getLine());
                ((ArrowLine) drawing).setLine((Edge) item);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
            ((ArrowLine) drawing).setFactory();
            ((ArrowLine) drawing).setEndPoints();
            ((ArrowLine) drawing).setPath();
        } else if (drawing instanceof Edge) {
            ((Edge) drawing).setFactory();
            ((Edge) drawing).setEndPoints();
            ((Edge) drawing).setPath();
        } else if (drawing instanceof Figure) {
            switch (((Figure) drawing).getType()) {
                case LINE:
                case SKETCH:
                case POLYGON:
                    DrawItem item = new Edge();
                    try {
                        BeanUtils.copyProperties(item, drawing);
                        drawing = item;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.trace(e.getMessage());
                    }
                    ((Edge) drawing).setFactory();
                    ((Edge) drawing).setEndPoints();
                    ((Edge) drawing).setPath();
                    break;
                default:
                    ((Figure) drawing).setFactory();
                    ((Figure) drawing).setEndPoints();
                    ((Figure) drawing).setPath();
                    break;
            }
        }

        if (drawing instanceof Grouped) {
            DrawItem item = new Grouped();
            for (DrawItem shape : ((Grouped) drawing).getShapes()) {
                ((Grouped) item).addShape(checkDrawings(shape));
            }
            item.setAngle(drawing.getAngle());
            ((Grouped) item).setTransparency(((Grouped) drawing).getTransparency());
            drawing = item;
        }

        if (drawing instanceof Picture) {
            DrawItem item = injector.getInstance(Picture.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
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
                updateProgress(0L, 3L);
                ZipEntry ze = zf.getEntry("content/pictures.xml");
                decoder = new XMLDecoder(new BufferedInputStream(zf.getInputStream(ze)));
                decoder.setExceptionListener((Exception ex) -> {
                    logger.warn(ex.getMessage());
                });
                pictures = (ArrayList<ImageItem>) decoder.readObject();
                updateProgress(1L, 3L);

                int index = 0;
                for (ImageItem picture : pictures) {
                    ze = zf.getEntry("images/" + FileUtils.getImageName(index));
                    try {
                        Image img = new Image(new BufferedInputStream(zf.getInputStream(ze)));
                        picture.setImage(img);
                    } catch (IOException e) {
                        logger.warn("Can't read image file.");
                    }
                    index++;
                }
                updateProgress(2L, 3L);

                ze = zf.getEntry("content/canvas.xml");
                decoder = new XMLDecoder(new BufferedInputStream(zf.getInputStream(ze)));
                decoder.setExceptionListener((Exception ex) -> {
                    logger.warn(ex.getMessage());
                    success = false;
                });
                drawings = (ArrayList<DrawItem>) decoder.readObject();
                updateProgress(3L, 3L);
            }
        }
    }

}
