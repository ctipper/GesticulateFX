/*
 *  SVGWorker.java
 * 
 *  Created on Nov 8, 2012 8:31:03 PM
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

import java.io.*;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGHints;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.ShareUtils;
import net.perspective.draw.util.CanvasPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class SVGWorker extends Task<Object> {

    private final Provider<CanvasView> viewProvider;
    private final Provider<ApplicationController> controllerProvider;
    @Inject ShareUtils share;
    private File file;
    private double margin;

    private static final Logger logger = LoggerFactory.getLogger(SVGWorker.class.getName());

    @Inject
    public SVGWorker(Provider<CanvasView> viewProvider,
            Provider<ApplicationController> controllerProvider) {
        this.viewProvider = viewProvider;
        this.controllerProvider = controllerProvider;
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
        logger.info("SVG export started...");
        return new Serialiser();
    }

    @Override
    public void done() {
        logger.info("SVG export completed.");
        CompletableFuture.runAsync(() -> {
            try {
                // introduce a minimum visible interval
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }, share.executor).thenRun(() -> {
            Platform.runLater(() -> {
                controllerProvider.get().getProgressVisibleProperty().setValue(Boolean.FALSE);
                controllerProvider.get().setStatusMessage("Exported to SVG");
            });
        });
    }

    final class Serialiser {

        Serialiser() {
            logger.info("SVG export initialised.");
            this.make();
        }

        public void make() {
            // Calculate drawing bounds
            final CanvasPoint[] bounds = viewProvider.get().getBounds();
            CanvasPoint start = bounds[0].shifted(-margin, -margin).floor();
            CanvasPoint end = bounds[1].shifted(margin, margin);

            // Calculate dimensions
            int width = (int) Math.ceil(end.x - start.x);
            int height = (int) Math.ceil(end.y - start.y);

            // Create an instance of the JFreeSVG SVGGraphics2D
            SVGGraphics2D g2 = new SVGGraphics2D(width, height);

            // Add metadata
            g2.setRenderingHint(SVGHints.KEY_ELEMENT_ID, "gesticulate-canvas");
            g2.setRenderingHint(SVGHints.KEY_ELEMENT_TITLE, "Gesticulate Drawing");
            g2.setRenderingHint(SVGHints.KEY_IMAGE_HANDLING, SVGHints.VALUE_IMAGE_HANDLING_EMBED);

            // Apply transform to account for margins and drawing bounds
            g2.translate(-start.x, -start.y);

            // Ask to render into the SVG Graphics2D implementation.
            viewProvider.get().getDrawings().stream().forEach((item) -> {
                item.draw(g2);
            });

            // Generate the SVG output
            String svgElement = g2.getSVGElement();

            // Add viewBox attribute
            // JFreeSVG generates width/height attributes directly in pixels, but we want to add viewBox
            // for better responsiveness
            String viewBox = String.format("viewBox=\"0 0 %d %d\"", width, height);
            String widthAttr = String.format("width='%.1f'", Float.valueOf(width));
            String heightAttr = String.format("height='%.1f'", Float.valueOf(height));

            // Replace fixed dimensions with responsive ones
            svgElement = svgElement.replace(widthAttr, "width=\"100%\"");
            svgElement = svgElement.replace(heightAttr, "height=\"100%\"");

            // Add viewBox immediately after <svg
            int insertPos = svgElement.indexOf("<svg") + 4;
            svgElement = svgElement.substring(0, insertPos) + " " + viewBox + svgElement.substring(insertPos);

            // Write to file
            try (Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                out.write(svgElement);
            } catch (IOException e) {
                logger.warn("Error writing SVG file: {}", e.getMessage());
            }

            // Clean up
            g2.dispose();
        }
    }

}
