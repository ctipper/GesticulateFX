/*
 * Gesticulate.java
 * 
 * Created on Oct 19, 2013 5:26:43 PM
 * 
 */
package net.perspective.draw;

import javafx.scene.SceneBuilder;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.stage.StageBuilder;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.*;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import javax.inject.Singleton;
import net.perspective.draw.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
public class Gesticulate extends GuiceApplication {

    @Inject 
    private GuiceFXMLLoader fxmlLoader;
    
//    @Inject 
//    private CanvasFactory canvasFactory;

    @Inject
    private DrawingArea drawingarea;
    
    private Timeline timeline;
    
    // parameters for sizing the stage
    private final Screen screen = Screen.getPrimary();
    private final Rectangle2D screenSize = screen.getVisualBounds();
    private final int xWidth = 1_000;
    private final int yHeight = (int) (screenSize.getMaxY() * .8);
    private final int frameLeft = (int) (screenSize.getMaxX() - xWidth) / 3;
    private final int frameTop = (int) (screenSize.getMaxY() - yHeight) / 5;
    
    // timer refresh rate, in milliseconds
    static final int duration = 25;
    private static final Duration REFRESH_FREQUENCY = Duration.millis(duration);
    
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
        
        final ApplicationController controller = (ApplicationController) result.getController();
        final Parent root = result.getRoot();

        // Put the loaded user interface onto the primary stage.
        StageBuilder.create()
        .title("Gesticulate")
        .resizable(true)
        .scene(SceneBuilder.create()
            .root(root)
            .build())
        .applyTo(primaryStage);
        
        // Size the primary stage
        sizeStage(primaryStage);

        // Show the primary stage
        primaryStage.show();

        // Initialise the scroll area
        Scene scene = primaryStage.getScene();
        final ScrollPane pane = (ScrollPane) scene.lookup("#scroll");
        pane.setFitToWidth(true);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Initialize the canvas and apply handlers
        drawingarea.init(pane.getWidth(), pane.getHeight());
        
        // Apply drawingarea to controller
        controller.setDrawArea(drawingarea);
        
        // Install the canvas
        pane.setContent(drawingarea.getCanvas());
        setOnResize(pane);
        
        // Setup timer
        timeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent actionEvent) {
                        drawingarea.repaint();
                    }
                }
            ),
            new KeyFrame(
                REFRESH_FREQUENCY
            )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    public void setOnResize(ScrollPane p) {
        p.heightProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                drawingarea.getCanvas().setHeight((double) new_val);
            }
        });
        p.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                drawingarea.getCanvas().setWidth((double) new_val);
            }
        });
    }
    
    public void sizeStage(Stage stage) {
        stage.setX(frameLeft);
        stage.setY(frameTop);
        stage.setWidth(xWidth);
        stage.setHeight(yHeight);
    }
    
    @Override
    public void stop() {
        timeline.stop();
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
        }
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
}