/*
 * PNGWorker.java
 * 
 * Created on Apr 14, 2015 4:19:24 PM
 * 
 */
package net.perspective.draw.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class PNGWorker extends Task<Object> {

    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    protected File file;
    private boolean opacity;
    private double margin;
    
    private static final Logger logger = LoggerFactory.getLogger(PNGWorker.class.getName());

    /** Creates a new instance of 
     * <code>PNGWorker</code> 
     */
    @Inject
    public PNGWorker() {
        this.opacity = true;
        this.margin = 0.0;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setOpacity(boolean opacity) {
        this.opacity = opacity;
    }
    
    public void setMargin(double margin) {
        this.margin = margin;
    }

    @Override
    protected Object call() throws Exception {
        logger.info("PNG export started...");
        return new Serialiser(this.opacity);
    }

    @Override
    public void done() {
        logger.info("PNG export completed.");
        Platform.runLater(() -> {
            controller.getSnapshotProperty().setValue(false);
        });
    }

    final class Serialiser {
        
        private final boolean opacity;
        double scale = 1.375;

        Serialiser(boolean opacity) {
            logger.info("PNG export initialised.");
            this.opacity = opacity;
            this.make();
        }

        public void make() {
            // Calculate draw area
            final CanvasPoint[] bounds = view.getBounds();
            CanvasPoint start = bounds[0].shifted(-margin, -margin).grow(scale).floor();
            CanvasPoint end = bounds[1].shifted(margin, margin).grow(scale);

            // render canvas
            BufferedImage img = new BufferedImage((int) Math.ceil(end.x), (int) Math.ceil(end.y), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setBackground(Color.WHITE);
            if (opacity) {
                // fill background
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, img.getWidth(), img.getHeight());
            }
            // grow and translate
            g2.transform(java.awt.geom.AffineTransform.getTranslateInstance(0, 0));
            g2.transform(java.awt.geom.AffineTransform.getScaleInstance(scale, scale));
            // Render image
            view.getDrawings().stream().forEach((item) -> {
                item.draw(g2);
            });
            g2.dispose();

            // crop image
            int width = (int) Math.ceil(end.x - start.x);
            int height = (int) Math.ceil(end.y - start.y);
            BufferedImage image = img.getSubimage((int) Math.floor(start.x), (int) Math.floor(start.y), width, height);

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                ImageIO.write(image, "png", bos);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to write PNG.");
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }

}
