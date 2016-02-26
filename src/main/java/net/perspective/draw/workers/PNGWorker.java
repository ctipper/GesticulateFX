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
import javafx.concurrent.Task;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class PNGWorker extends Task {

    @Inject private CanvasView view;
    protected File file;
    private boolean opacity;
    
    private static final Logger logger = LoggerFactory.getLogger(PNGWorker.class.getName());

    /** Creates a new instance of 
     * <code>PNGWorker</code> 
     */
    @Inject
    public PNGWorker() {
        this.opacity = true;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setOpacity(boolean opacity) {
        this.opacity = opacity;
    }

    @Override
    protected Object call() throws Exception {
        logger.info("PNG export started...");
        return new Serialiser(this.opacity);
    }

    @Override
    public void done() {
        logger.info("PNG export completed.");
    }

    final class Serialiser {
        
        private final boolean opacity;

        Serialiser(boolean opacity) {
            logger.info("PNG export initialised.");
            this.opacity = opacity;
            this.make();
        }

        public void make() {
            double scale = 1.375;
            double margin = 6.0;  // max stroke width
            
            // Calculate draw area
            final CanvasPoint[] bounds = view.getBounds();
            CanvasPoint start = bounds[0].scale(scale);
            CanvasPoint end = bounds[1].scale(scale);
            start.translate(-margin, -margin);
            end.translate(margin, margin);
            
            // render canvas
            BufferedImage img = new BufferedImage((int) end.x, (int) end.y, BufferedImage.TYPE_INT_ARGB);
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
            // scale and translate
            g2.transform(java.awt.geom.AffineTransform.getTranslateInstance(0, 0));
            g2.transform(java.awt.geom.AffineTransform.getScaleInstance(scale, scale));
            // Render image
            view.getDrawings().stream().forEach((item) -> {
                item.draw(g2);
            });
            g2.dispose();
            
            // crop image
            BufferedImage image = img.getSubimage((int) start.x, (int) start.y, (int) (end.x - start.x), (int) (end.y - start.y));
            
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
