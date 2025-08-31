/*
 * Gesticulate.java
 * 
 * Created on Oct 19, 2013 5:26:43 PM
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
package net.perspective.draw;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.awt.Desktop;
import java.awt.desktop.OpenFilesEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.application.Platform.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.*;
import javafx.util.Subscription;
import javax.inject.Inject;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.perspective.draw.event.*;
import net.perspective.draw.event.behaviours.*;
import net.perspective.draw.event.keyboard.*;
import net.perspective.draw.geom.*;
import net.perspective.draw.text.Editor;
import net.perspective.draw.text.TextEditor;
import net.perspective.draw.util.G2;
import net.perspective.draw.workers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Gesticulate
 *
 * @author ctipper
 */
public class Gesticulate extends GuiceApplication {

    @Inject private GuiceFXMLLoader fxmlLoader;
    @Inject private ApplicationController controller;
    @Inject private DrawingArea drawarea;
    @Inject private KeyListener keylistener;
    @Inject private ShareUtils share;
    private Stage stage;
    private Properties userPrefs;

    // parameters for sizing the stage
    private final Screen screen = Screen.getPrimary();
    private final Rectangle2D screenSize = screen.getVisualBounds();
    private final int sceneWidth = 1_036;
    private final int sceneHeight = (screenSize.getMaxY() < 960 ? (int) screenSize.getMaxY() : (int) (1_080 * .8 + 126));
    private final int frameLeft = (int) (screenSize.getMaxX() - sceneWidth) / 3;
    private final int frameTop = (int) (screenSize.getMaxY() - sceneHeight) / 5;

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    protected static final boolean MM_SYSTEM_THEME = true;

    private static final Logger logger = LoggerFactory.getLogger(Gesticulate.class.getName());

    /**
     * Init the application
     * 
     * @param modules
     * @throws Exception
     */
    @Override
    public void init(final List<Module> modules) throws Exception {
        modules.add(new FxmlModule());
    }

    /**
     * Construct the user interface
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
        // keyboard events are consumed by the scene
        keylistener.initializeHandlers(scene);

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
        if (MM_SYSTEM_THEME && userPrefs.getProperty("systemTheme").equals("System")) {
            // setSystemTheme();
            controller.setThemeType("System");
        } else if (Boolean.parseBoolean(userPrefs.getProperty("darkTheme"))) {
            controller.getThemeProperty().setValue(true); // non default value triggers event
            controller.setThemeType("Dark");
        } else {
            controller.setAppStyles(false);
            resetStylesheets(false);
            controller.setThemeType("Light");
        }

        // configure canvas background
        var canvasColor = Optional.ofNullable(this.userPrefs.getProperty("canvasColor"))
                .orElseGet(controller::getThemeBackgroundColor);
        controller.setCanvasBackgroundColor(canvasColor);
        controller.setBackgroundPickerColor(canvasColor);
        controller.adjustThemeFillColor(canvasColor);
        drawarea.setTheme();

        // Install the canvas
        pane.setContent(drawarea.getScene());
        this.setOnResize(pane);

        // open canvas from file if requested
        final Parameters parameters = getParameters();
        final List<String> args = parameters.getRaw();
        final List<File> files = new ArrayList<>();
        for (String arg : args) {
            files.add(new File(arg));
        }
        if (!files.isEmpty()) {
            share.loadCanvas(files);
        }
        if (MAC_OS_X) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setOpenFileHandler((OpenFilesEvent e) -> {
                share.loadCanvas(e.getFiles());
            });
        }
    }

    /**
     * Resize scrollpane on window resize
     * 
     * @param pane
     */
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

    /**
     * Set the size of the stage
     * 
     * @param stage the stage
     */
    public void sizeStage(Stage stage) {
        stage.setX(frameLeft);
        stage.setY(frameTop);
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);
    }

    /**
     * Retrieve the stage
     * 
     * @return the stage
     */
    public Stage getStage() {
        return this.stage;
    }

    private final Preferences preferences = Platform.getPreferences();
    private Consumer<ColorScheme> csmr = null;
    private Subscription csub = Subscription.EMPTY;

    /**
     * Set the application theme
     */
    public void setSystemTheme() {
        ColorScheme colorScheme = preferences.getColorScheme();
        final boolean isDark = colorScheme.equals(ColorScheme.DARK);
        if (isDark) {
            controller.getThemeProperty().setValue(isDark); // non default value triggers event
        } else {
            controller.setAppStyles(false);
            resetStylesheets(false);
        }
        // initialise the theme listener
        csmr = (var darkTheme) -> {
            controller.getThemeProperty().setValue(darkTheme.equals(ColorScheme.DARK));
        };
        csub = preferences.colorSchemeProperty().subscribe(csmr);
    }

    /**
     * Remove theme listener
     */
    public void deregisterThemeListener() {
        csub.unsubscribe();
    }

    /**
     * Reset aopplication stylesheets
     * 
     * @param isDark is dark theme selected
     */
    public void resetStylesheets(Boolean isDark) {
        if (isDark) {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add("/stylesheets/jmetro-dark.css");
            stage.getScene().getStylesheets().add("/stylesheets/application-dark.css");
            stage.getScene().getStylesheets().add("/stylesheets/style.css"); // maps stylesheet
        } else {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add("/stylesheets/jmetro-light.css");
            stage.getScene().getStylesheets().add("/stylesheets/application.css");
            stage.getScene().getStylesheets().add("/stylesheets/style.css"); // maps stylesheet
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

    /**
     * Quit the application
     */
    @Override
    public void stop() {
        userPrefs.setProperty("darkTheme", controller.getThemeProperty().getValue().toString());
        userPrefs.setProperty("systemTheme", controller.getComboThemeProperty().getValue());
        userPrefs.setProperty("canvasColor", controller.getCanvasBackgroundColor());
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

    /**
     * Persist the user preferences
     * 
     * @param prefs the user preferences as properties
     */
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

    /**
     * Retrieve the user preferences
     * 
     * @return user preferencies as properties
     */
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
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as
     * fallback in case the application can not be launched through deployment artifacts, e.g., in
     * IDEs with limited FX support.
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
            bind(TextHandler.class);
            bind(MapHandler.class);
            bind(KeyListener.class);
            bind(DummyKeyHandler.class);
            bind(MapKeyHandler.class);
            bind(MoveKeyHandler.class);
            bind(TextKeyHandler.class);
            bind(BehaviourContext.class);
            bind(FigureItemBehaviour.class);
            bind(PictureItemBehaviour.class);
            bind(GroupedItemBehaviour.class);
            bind(MapItemBehaviour.class);
            bind(TextItemBehaviour.class);
            bind(FigureFactory.class).to(FigureFactoryImpl.class);
            bind(MapController.class);
            bind(TextController.class);
            bind(Editor.class).to(TextEditor.class);
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
