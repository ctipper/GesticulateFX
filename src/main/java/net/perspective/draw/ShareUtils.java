/*
 * ShareUtils.java
 * 
 * Created on Feb 24, 2016 5:28:27 PM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.stage.FileChooser;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.util.FileUtils;
import net.perspective.draw.workers.PNGWorker;
import net.perspective.draw.workers.SVGWorker;

/**
 *
 * @author ctipper
 */

@Singleton
public class ShareUtils {

    @Inject private Injector injector;
    @Inject private Gesticulate application;
    @Inject private CanvasView view;
    private final ExecutorService executor;
    private final double margin;

    /** Creates a new instance of <code>ShareUtils</code> */
    public ShareUtils() {
        this.executor = Executors.newCachedThreadPool();
        this.margin = 3.0;  // half max stroke width
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
            new FileChooser.ExtensionFilter("All Images", "*.*application"));
        File result = chooser.showSaveDialog(application.getStage());
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "svg");
        SVGWorker svgWorker = injector.getInstance(SVGWorker.class);
        svgWorker.setFile(file);
        svgWorker.setMargin(this.margin);
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
        executor.submit(pngWorker);
    }

    public void snapshotPNG(ApplicationController controller) {
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
        executor.submit(pngWorker);
    }

}
