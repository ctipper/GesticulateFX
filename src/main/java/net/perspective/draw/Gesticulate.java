/*
 * Gesticulate.java
 * 
 * Created on Oct 19, 2013 5:26:43 PM
 * 
 */
package net.perspective.draw;

import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
public class Gesticulate extends Application {

    DrawingCanvas drawcanvas;
    private Timeline timeline;
    
    // canvas size, in px (width set dynamically)
    static final int height = 1_300;
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
    public void start(Stage stage) throws Exception {
        URL location = getClass().getResource("/fxml/Application.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = (Parent) fxmlLoader.load(location.openStream());

        // create the scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        // size the stage
        this.setStage(stage);
        stage.show();

        // init the scroll area
        final ScrollPane pane = (ScrollPane) scene.lookup("#scroll");
        pane.setFitToWidth(true);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        drawcanvas = new DrawingCanvas(pane.getWidth(), pane.getHeight());
        
        // set up controller
        ApplicationController controller = (ApplicationController)fxmlLoader.getController();
        controller.setCanvas(drawcanvas);
        
        // install the canvas
        pane.setContent(drawcanvas.getCanvas());
        this.setOnResize(pane);
        // init, canvas handlers
        drawcanvas.clear();
        drawcanvas.setSmoothed(true);
        drawcanvas.setHandlers();
        
        // need to set up timer loop
        timeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent actionEvent) {
                        drawcanvas.repaint();
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
                drawcanvas.getCanvas().setHeight((double) new_val);
            }
        });
        p.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                drawcanvas.getCanvas().setWidth((double) new_val);
            }
        });
    }
    
    public void setStage(Stage stage) {
        stage.setX(frameLeft);
        stage.setY(frameTop);
        stage.setWidth(xWidth);
        stage.setHeight(yHeight);
        stage.setTitle("Gesticulate");
    }
    
    @Override
    public void stop() {
        timeline.stop();
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
