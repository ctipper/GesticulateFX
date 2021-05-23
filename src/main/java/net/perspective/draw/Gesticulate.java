/*
 * Gesticulate.java
 * 
 * Created on Oct 19, 2013 5:26:43 PM
 * 
 */
package net.perspective.draw;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.awt.Desktop;
import java.awt.desktop.OpenFilesEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.*;
import javax.inject.Inject;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.perspective.draw.event.*;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.event.behaviours.GroupedItemBehaviour;
import net.perspective.draw.event.behaviours.PictureItemBehaviour;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureFactoryImpl;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.G2;
import net.perspective.draw.workers.ImageLoadWorker;
import net.perspective.draw.workers.PDFWorker;
import net.perspective.draw.workers.PNGWorker;
import net.perspective.draw.workers.ReadInFunnel;
import net.perspective.draw.workers.SVGWorker;
import net.perspective.draw.workers.WriteOutStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * 
 * @author ctipper
 */

public class Gesticulate extends GuiceApplication {

    @Inject private GuiceFXMLLoader fxmlLoader;
    @Inject private ApplicationController controller;
    @Inject private DrawingArea drawarea;
    @Inject private ShareUtils share;
    private Stage stage;
    private Properties userPrefs;

    // parameters for sizing the stage
    private final Screen screen = Screen.getPrimary();
    private final Rectangle2D screenSize = screen.getVisualBounds();
    private final int sceneWidth = 1_000;
    private final int sceneHeight = (int) (screenSize.getMaxY() * .8);
    private final int frameLeft = (int) (screenSize.getMaxX() - sceneWidth) / 3;
    private final int frameTop = (int) (screenSize.getMaxY() - sceneHeight) / 5;

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    private static final Logger logger = LoggerFactory.getLogger(Gesticulate.class.getName());

    @Override
    public void init(final List<Module> modules) throws Exception {
        modules.add(new FxmlModule());
    }

    /**
     * 
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        Result result = fxmlLoader.load(getClass().getResource("/fxml/Application.fxml"));

        final Parent root = result.getRoot();

        // Put the loaded user interface onto the primary stage.
        Scene scene = new Scene(root);

        primaryStage.setTitle("GesticulateFX");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            stop();
        });

        // Size the primary stage
        this.sizeStage(primaryStage);

        // Show the primary stage
        primaryStage.show();
        this.stage = primaryStage;

        // Initialise the scroll area
        final ScrollPane pane = (ScrollPane) scene.lookup("#scrollarea");

        // retrieve user preferences
        this.userPrefs = getUserPreferences();

        // Initialize the canvas and apply handlers
        drawarea.init(pane.getWidth(), pane.getHeight());

        // set the theme from user preferences
        if (Boolean.parseBoolean(userPrefs.getProperty("darkTheme"))) {
            controller.getThemeProperty().setValue(true); // non default value triggers event
        } else {
            controller.setAppStyles(false);
            resetStylesheets(false);
        }

        // Install the canvas
        pane.setContent(drawarea.getScene());
        this.setOnResize(pane);

        // open canvas from file if requested
        final Parameters parameters = getParameters();
        final List<String> args = parameters.getRaw();
        final String file = !args.isEmpty() ? args.get(0) : "";
        if (file.length() != 0) {
            share.loadCanvas(file);
        }
        if (MAC_OS_X) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setOpenFileHandler((OpenFilesEvent e) -> {
                share.readCanvas(e.getFiles().get(0));
            });
        }
    }

    public void setOnResize(ScrollPane pane) {
        pane.heightProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawarea.getScene().setHeight((double) new_val);
            drawarea.redrawGrid();
        });
        pane.widthProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawarea.getScene().setWidth((double) new_val);
            drawarea.redrawGrid();
        });
    }

    public void sizeStage(Stage stage) {
        stage.setX(frameLeft);
        stage.setY(frameTop);
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);
    }

    public Stage getStage() {
        return this.stage;
    }

    public void resetStylesheets(Boolean mode) {
        if (mode) {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add("/stylesheets/jmetro-dark.css");
            stage.getScene().getStylesheets().add("/stylesheets/application-dark.css");
        } else {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add("/stylesheets/jmetro-light.css");
            stage.getScene().getStylesheets().add("/stylesheets/application.css");
        }
    }

    /**
     * Set the canvas grid
     * 
     * @param gridEnabled 
     */
    public void drawGrid(boolean gridEnabled) {
        drawarea.setGrid(gridEnabled);
        drawarea.setSnapTo(gridEnabled);
        drawarea.redrawGrid();
    }

    @Override
    public void stop() {
        userPrefs.setProperty("darkTheme", controller.getThemeProperty().getValue().toString());
        setUserPreferences(userPrefs);
        logger.trace("set user preferences");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Configuration data directory
     *
     * @return directory
     */
    public String configDir() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        return appDirs.getUserConfigDir("GesticulateFX", null, "ctipper", true) + System.getProperty("file.separator");
    }

    public void setUserPreferences(Properties prefs) {
        AppDirs appDirs = AppDirsFactory.getInstance();
        Path paramPath = Paths.get(appDirs.getUserConfigDir("GesticulateFX", null, "ctipper", true), "userprefs.properties");
        if (Files.exists(paramPath, NOFOLLOW_LINKS)) {
            setUserProperties(prefs);
        } else {
            // create preferences storage
            Paths.get(configDir()).toFile().mkdirs();
            setUserProperties(prefs);
        }
    }

    public Properties getUserPreferences() {
        Properties prefs = new Properties();
        AppDirs appDirs = AppDirsFactory.getInstance();
        Path paramPath = Paths.get(appDirs.getUserConfigDir("GesticulateFX", null, "ctipper", true), "userprefs.properties");
        if (Files.exists(paramPath, NOFOLLOW_LINKS)) {
            prefs = getUserProperties();
        } else {
            // create preferences
            prefs.setProperty("darkTheme", "false");
        }
        return prefs;
    }

    private void setUserProperties(Properties prefs) {
        try (FileWriter out = new FileWriter(configDir() + "userprefs.properties")) {
            prefs.store(out, "---User Preferences---");
        } catch (IOException ex) {
            logger.error(null, ex);
        }
    }

    private Properties getUserProperties() {
        Properties props = new Properties();
        try (FileReader fin = new FileReader(configDir() + "userprefs.properties")) {
            props.load(fin);
        } catch (FileNotFoundException ex) {
            logger.debug("userprefs.properties not found.");
        } catch (IOException ex) {
            logger.warn("userprefs.properties not correct.");
        }
        return props;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static class FxmlModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ApplicationController.class);
            bind(DrawingArea.class);
            bind(CanvasView.class);
            bind(CanvasTransferHandler.class);
            bind(Dropper.class);
            bind(DrawAreaListener.class);
            bind(FigureHandler.class);
            bind(RotationHandler.class);
            bind(SelectionHandler.class);
            bind(SketchHandler.class);
            bind(BehaviourContext.class);
            bind(FigureItemBehaviour.class);
            bind(PictureItemBehaviour.class);
            bind(GroupedItemBehaviour.class);
            bind(FigureFactory.class).to(FigureFactoryImpl.class);
            bind(Picture.class);
            bind(ShareUtils.class);
            bind(ReadInFunnel.class);
            bind(WriteOutStreamer.class);
            bind(ImageLoadWorker.class);
            bind(PDFWorker.class);
            bind(SVGWorker.class);
            bind(PNGWorker.class);
            bind(G2.class);
        }
    }

}
