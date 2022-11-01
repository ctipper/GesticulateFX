/*
 * ShareUtils.java
 * 
 * Created on Feb 24, 2016 5:28:27 PM
 * 
 */

/**
 * Copyright (c) 2022 e-conomist
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
package net.perspective.draw;

import com.google.inject.Injector;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.stage.FileChooser;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.util.FileUtils;
import net.perspective.draw.workers.ImageLoadWorker;
import net.perspective.draw.workers.PDFWorker;
import net.perspective.draw.workers.PNGWorker;
import net.perspective.draw.workers.ReadInFunnel;
import net.perspective.draw.workers.SVGWorker;
import net.perspective.draw.workers.WriteOutStreamer;

/**
 * 
 * @author ctipper
 */

@Singleton
public class ShareUtils {

    @Inject private Injector injector;
    @Inject private Gesticulate application;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    private ImageLoadWorker imageLoader;
    private List<File> imageFiles;
    private File canvasfile;
    private final double margin;
    public final ExecutorService executor;

    public ShareUtils() {
        this.executor = Executors.newCachedThreadPool();
        this.margin = 5.0;  // half max stroke width
    }

    public void resetCanvasFile() {
        this.canvasfile = null;
    }

    public File getCanvasFile() {
        return canvasfile;
    }

    /**
     * Set image file array
     * 
     * @param imageFiles the imageFiles to set
     */
    public void setImageFiles(List<File> imageFiles) {
        this.imageFiles = imageFiles;
    }

    /**
     * Get image file array
     * 
     * @return the imageFiles
     */
    public List<File> getImageFiles() {
        return imageFiles;
    }

    /**
     * Open file chooser for images
     * 
     * @return
     */
    public List<File> chooseImages() {
        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Choose Pictures...");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("All Documents", "*.*"));
        List<File> result = chooser.showOpenMultipleDialog(application.getStage());
        if (result == null) {
            return null;
        }
        return result;
    }

    /**
     * Load images
     */
    public void readPictures() {
        if (this.getImageFiles() != null) {
            imageLoader = injector.getInstance(ImageLoadWorker.class);
            controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
            controller.setProgressIndeterminate();
            executor.submit(imageLoader);
        }
        controller.setSelectionMode();
    }

    public File chooseCanvas() {
        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Open...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("GST", "*.gst"),
            new FileChooser.ExtensionFilter("All Documents", "*.*"));
        File result = chooser.showOpenDialog(application.getStage());
        if (result == null) {
            return null;
        }
        return result;
    }

    public void exportCanvas() {
        // Detect empty canvas
        if (view.getDrawings().isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Save As...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("GST", "*.gst"),
            new FileChooser.ExtensionFilter("All Documents", "*.*"));
        File result = chooser.showSaveDialog(application.getStage());
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "gst");
        this.writeCanvas(file);
    }

    public void loadCanvas(String path) {
        final File file = new File(path);
        this.readCanvas(file);
    }

    public void readCanvas(File file) {
        ReadInFunnel reader = injector.getInstance(ReadInFunnel.class);
        reader.setFile(file);
        this.canvasfile = file;
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.getProgressProperty().bind(reader.progressProperty());
        executor.submit(reader);
    }

    public void writeCanvas(File file) {
        WriteOutStreamer streamer = injector.getInstance(WriteOutStreamer.class);
        streamer.setFile(file);
        this.canvasfile = file;
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.getProgressProperty().bind(streamer.progressProperty());
        executor.submit(streamer);
    }

    public void exportPDF() {
        // Detect empty canvas
        if (view.getDrawings().isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Export PDF...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("All Documents", "*.*"));
        File result = chooser.showSaveDialog(application.getStage());
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "pdf");
        PDFWorker pdfWorker = injector.getInstance(PDFWorker.class);
        pdfWorker.setFile(file);
        pdfWorker.setMargin(this.margin);
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.setProgressIndeterminate();
        executor.submit(pdfWorker);
    }

    public void exportSVG() {
        // Detect empty canvas
        if (view.getDrawings().isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Export SVG...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SVG", "*.svg"),
            new FileChooser.ExtensionFilter("All Images", "*.*"));
        File result = chooser.showSaveDialog(application.getStage());
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "svg");
        SVGWorker svgWorker = injector.getInstance(SVGWorker.class);
        svgWorker.setFile(file);
        svgWorker.setMargin(this.margin);
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.setProgressIndeterminate();
        executor.submit(svgWorker);
    }

    public void exportPNG() {
        // Detect empty canvas
        if (view.getDrawings().isEmpty()) {
            return;
        }

        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Export Image...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("All Images", "*.*"));
        File result = chooser.showSaveDialog(application.getStage());
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "png");
        PNGWorker pngWorker = injector.getInstance(PNGWorker.class);
        pngWorker.setFile(file);
        pngWorker.setOpacity(false);
        pngWorker.setMargin(this.margin);
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.setProgressIndeterminate();
        executor.submit(pngWorker);
    }

    public void snapshotPNG() {
        // Detect empty canvas
        if (view.getDrawings().isEmpty()) {
            controller.getSnapshotProperty().setValue(false);
            return;
        }

        Date d = new Date();
        // Snap Shot 2015-04-14 at 17.17.29
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd' at 'HH.mm.ss");
        TimeZone tz = TimeZone.getDefault();
        df.setTimeZone(tz);
        String now = df.format(d);

        // Name file and add timestamp, save to Desktop
        String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Snap Shot " + now + ".png";
        File file = new File(path);

        PNGWorker pngWorker = injector.getInstance(PNGWorker.class);
        pngWorker.setFile(file);
        pngWorker.setMargin(this.margin);
        controller.getProgressVisibleProperty().setValue(Boolean.TRUE);
        controller.setProgressIndeterminate();
        executor.submit(pngWorker);
    }

}
