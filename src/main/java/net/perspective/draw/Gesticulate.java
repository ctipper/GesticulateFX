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

import java.awt.Desktop;
import java.awt.desktop.OpenFilesEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.application.Platform.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Subscription;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.perspective.draw.event.keyboard.KeyListener;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Gesticulate
 *
 * @author ctipper
 */

@Singleton
public class Gesticulate extends Application {
 
    private DrawAppComponent appComponent;
    private ApplicationController controller;
    @Inject Provider<DrawingArea> drawareaProvider;
    @Inject KeyListener keylistener;
    @Inject ShareUtils share;
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
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        appComponent = DaggerDrawAppComponent.builder()
                .drawAppModule(new DrawAppModule())
                .build();
        appComponent.inject(this);
    }

    /**
     * Construct the user interface
     * 
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        try {
            FxAppComponent fxApp = appComponent.fxApp()
                .application(this)
                .mainWindow(primaryStage)
                .build();
            FXMLLoader loader = fxApp.loader(getClass().getResource("/fxml/Application.fxml"));
            loader.setControllerFactory(param -> appComponent.applicationController());
            final Parent root = loader.load();
            controller = appComponent.applicationController();
            controller.setApplication(this);
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
            drawareaProvider.get().init(pane.getWidth(), pane.getHeight());
            logger.trace("initialized stage");

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
            drawareaProvider.get().setTheme();

            // Install the canvas
            pane.setContent(drawareaProvider.get().getScene());
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
        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", e.getMessage());
            e.printStackTrace();
            // Show error dialog or exit gracefully
            Platform.exit();
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            e.printStackTrace();
            // Show error dialog or exit gracefully
            Platform.exit();
        }
    }

    /**
     * Resize scrollpane on window resize
     * 
     * @param pane
     */
    public void setOnResize(ScrollPane pane) {
        pane.heightProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawareaProvider.get().getScene().setHeight((double) new_val);
            drawareaProvider.get().redrawGrid();
        });
        pane.widthProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawareaProvider.get().getScene().setWidth((double) new_val);
            drawareaProvider.get().redrawGrid();
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
        drawareaProvider.get().setGrid(gridEnabled);
        drawareaProvider.get().setSnapTo(gridEnabled);
        drawareaProvider.get().redrawGrid();
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

}
