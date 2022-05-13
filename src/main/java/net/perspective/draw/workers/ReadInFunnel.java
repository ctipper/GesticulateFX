/**
 * ReadInFunnel.java
 * 
 * Created on 23-Apr-2016 12:33:36
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
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
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
import net.perspective.draw.geom.StreetMap;
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

        if (drawing instanceof ArrowLine arrowLine) {
            DrawItem item = new Edge();
            try {
                BeanUtils.copyProperties(item, arrowLine.getLine());
                arrowLine.setLine((Edge) item);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
            arrowLine.setFactory();
            arrowLine.setEndPoints();
            arrowLine.setPath();
        } else if (drawing instanceof Edge edge) {
            edge.setFactory();
            edge.setEndPoints();
            edge.setPath();
        } else if (drawing instanceof Figure figure) {
            switch (figure.getType()) {
                case LINE, SKETCH, POLYGON -> {
                    DrawItem item = new Edge();
                    try {
                        BeanUtils.copyProperties(item, drawing);
                        drawing = item;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.trace(e.getMessage());
                    }
                    figure.setFactory();
                    figure.setEndPoints();
                    figure.setPath();
                }
                default -> {
                    figure.setFactory();
                    figure.setEndPoints();
                    figure.setPath();
                }
            }
        }

        if (drawing instanceof Picture && !(drawing instanceof StreetMap)) {
            DrawItem item = injector.getInstance(Picture.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        } else if (drawing instanceof StreetMap) {
            DrawItem item = injector.getInstance(StreetMap.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        }

        if (drawing instanceof Grouped grouped) {
            DrawItem item = new Grouped();
            for (DrawItem shape : grouped.getShapes()) {
                ((Grouped) item).addShape(checkDrawings(shape));
            }
            item.setAngle(drawing.getAngle());
            ((Grouped) item).setTransparency(grouped.getTransparency());
            drawing = item;
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
