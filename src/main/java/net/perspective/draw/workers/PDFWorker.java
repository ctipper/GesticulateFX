/*
 * PDFWorker.java
 * 
 * Created on Mar 13, 2016 1:48:41 PM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.util.CanvasPoint;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfiguration;
import org.apache.fop.pdf.Version;
import org.apache.fop.svg.PDFDocumentGraphics2D;
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class PDFWorker extends Task<Object> {

    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private ShareUtils share;
    private File file;
    private double margin;

    private static final Logger logger = LoggerFactory.getLogger(SVGWorker.class.getName());

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
        CompletableFuture.runAsync(() -> {
            try {
                // introduce a minimum visible interval
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }, share.executor).thenRun(() -> {
            Platform.runLater(() -> {
                controller.getProgressVisibleProperty().setValue(Boolean.FALSE);
                controller.setStatusMessage("Exported to PDF");
            });
        });
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

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                //Instantiate the EPSDocumentGraphics2D instance
                PDFDocumentGraphics2D g2 = new PDFDocumentGraphics2D(false);
                g2.getPDFDocument().setPDFVersion(Version.V1_6);
                g2.getPDFDocument().setColorSpace(ColorSpace.CS_sRGB);
                if (g2.getPDFDocument().getProfile().isTransparencyAllowed() != null) {
                    logger.warn("Transparency is not enabled");
                }
                g2.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

                //Configure the graphic context (add fonts here)
                DefaultConfiguration cfg = new DefaultConfiguration("cfg");
                configure(g2, cfg);

                //Set up the document size
                int width = (int) Math.ceil(end.x - start.x);
                int height = (int) Math.ceil(end.y - start.y);
                Dimension pageSize = new Dimension(width, height); // pixels become point sizes

                g2.setupDocument(bos, pageSize.width, pageSize.height);
                g2.transform(java.awt.geom.AffineTransform.getTranslateInstance(-start.x, -start.y));
                g2.setDeviceDPI(72.0f);

                // Ask to render into the PDF Graphics2D implementation.
                view.getDrawings().stream().forEach((item) -> {
                    item.draw(g2);
                });

                g2.finish();
            } catch (IOException e) {
                logger.warn(e.getMessage());
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
    }

}
