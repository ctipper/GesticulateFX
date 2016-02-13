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
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.*;
import javax.inject.Inject;
import net.perspective.draw.event.*;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.util.FileUtils;
import net.perspective.draw.workers.SVGWorker;

/**
 *
 * @author ctipper
 */
public class Gesticulate extends GuiceApplication {

    @Inject private GuiceFXMLLoader fxmlLoader;
    @Inject private DrawingArea drawingarea;
    private Stage stage;
    
    // parameters for sizing the stage
    private final Screen screen = Screen.getPrimary();
    private final Rectangle2D screenSize = screen.getVisualBounds();
    private final int sceneWidth = 1_000;
    private final int sceneHeight = (int) (screenSize.getMaxY() * .8);
    private final int frameLeft = (int) (screenSize.getMaxX() - sceneWidth) / 3;
    private final int frameTop = (int) (screenSize.getMaxY() - sceneHeight) / 5;
    
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
        primaryStage.setTitle("Gesticulate");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            System.exit(0);
        });
        
        // Size the primary stage
        this.sizeStage(primaryStage);

        // Show the primary stage
        primaryStage.show();
        this.stage = primaryStage;

        // Initialise the scroll area
        final ScrollPane pane = (ScrollPane) scene.lookup("#scroll");
        pane.setFitToWidth(true);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Initialize the canvas and apply handlers
        drawingarea.init(pane.getWidth(), pane.getHeight());
        this.initialiseToolbar(scene);
        
        // Install the canvas
        pane.setContent(drawingarea.getScene());
        this.setOnResize(pane);
    }
    
    public void setOnResize(ScrollPane pane) {
        pane.heightProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawingarea.getScene().setHeight((double) new_val);
        });
        pane.widthProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            drawingarea.getScene().setWidth((double) new_val);
        });
    }
    
    public void sizeStage(Stage stage) {
        stage.setX(frameLeft);
        stage.setY(frameTop);
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);
    }

    private void initialiseToolbar(Scene scene) {
        // Toolbar state
        Object button = scene.lookup("#buttsketch");
        if (button instanceof ToggleButton) {
            ((ToggleButton) button).setSelected(true);
        }
    }
    
    public void exportSVG() {
        File result;

        FileChooser chooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        chooser.setInitialDirectory(userDirectory);
        chooser.setTitle("Export SVG...");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SVG", "*.svg"),
            new FileChooser.ExtensionFilter("All Images", "*.*"));
        result = chooser.showSaveDialog(stage);
        if (result == null) {
            return;
        }

        // wrangle filename with correct extension
        final File file = FileUtils.cleanseFileName(result, "svg");

        // launch progress bar
//        jProgressBar.setValue(0);
//        jProgressBar.setVisible(true);
//        jProgressBar.setIndeterminate(true);
        SVGWorker svgThread = new SVGWorker(file);
        svgThread.execute();
//        this.setStatus("SVG export started.");
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
            bind(DrawingArea.class);
            bind(CanvasView.class);
            bind(FigureHandler.class);
            bind(RotationHandler.class);
            bind(SelectionHandler.class);
            bind(SketchHandler.class);
            bind(BehaviourContext.class);
        }
    }
}