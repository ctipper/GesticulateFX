/*
 * PDFWorker.java
 * 
 * Created on Mar 13, 2016 1:48:41 PM
 * 
 */
package net.perspective.draw.workers;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.concurrent.Task;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.util.CanvasPoint;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.fop.svg.PDFDocumentGraphics2D;
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class PDFWorker extends Task {

    @Inject private CanvasView view;
    private File file;
    private double margin;

    private static final Logger logger = LoggerFactory.getLogger(SVGWorker.class.getName());

    /** Creates a new instance of <code>PDFWorker</code> */
    @Inject
    public PDFWorker() {
        this.margin = 0.0;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    @Override
    protected Object call() throws Exception {
        logger.info("PDF export started...");
        return new Serialiser();
    }

    @Override
    public void done() {
        logger.info("PDF export completed.");
    }

    final class Serialiser {

        Serialiser() {
            logger.info("PDF export initialised.");
            this.make();
        }

        public void make() {
            // Calculate drawing bounds
            final CanvasPoint[] bounds = view.getBounds();
            CanvasPoint start = bounds[0].shifted(-margin, -margin).floor();
            CanvasPoint end = bounds[1].shifted(margin, margin);

            try {
                //Instantiate the EPSDocumentGraphics2D instance
                PDFDocumentGraphics2D g2 = new PDFDocumentGraphics2D(false);
                g2.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

                //Configure the G2D with the necessary fonts
                configure(g2, createAutoFontsConfiguration());

                //Set up the document size
                int width = (int) Math.ceil(end.x - start.x);
                int height = (int) Math.ceil(end.y - start.y);
                Dimension pageSize = new Dimension(width, height); // pixels become point sizes
                g2.transform(java.awt.geom.AffineTransform.getTranslateInstance(-start.x, -start.y));

                // Ask to render into the PDF Graphics2D implementation.
                view.getDrawings().stream().forEach((item) -> {
                    item.draw(g2);
                });

                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    g2.setupDocument(bos, pageSize.width, pageSize.height);
                    g2.finish();
                } catch (IOException e) {
                    logger.warn(e.getMessage());
                }
                
            } catch (ConfigurationException e) {
                logger.error(null, e);
            }
        }
        
        private void configure(PDFDocumentGraphics2D g2d, Configuration cfg)
                throws ConfigurationException {

            PDFDocumentGraphics2DConfigurator configurator = new PDFDocumentGraphics2DConfigurator();
            boolean useComplexScriptFeatures = false;
            configurator.configure(g2d, cfg, useComplexScriptFeatures);
        }

        private Configuration createAutoFontsConfiguration() {
            // Create a default configuration using auto-detection of fonts.
            // This can be a bit slow but covers most use cases.
            DefaultConfiguration c = new DefaultConfiguration("cfg");
            DefaultConfiguration fonts = new DefaultConfiguration("fonts");
            c.addChild(fonts);
            DefaultConfiguration autodetect = new DefaultConfiguration("auto-detect");
            fonts.addChild(autodetect);
            return c;

            /** 
             * You can also load the configuration from a file:
             * DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
             * return cfgBuilder.buildFromFile(configFile);
             */
        }
    }
    
}
