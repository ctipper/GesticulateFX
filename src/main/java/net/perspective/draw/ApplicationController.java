/*
 * ApplicationController.java
 * 
 * Created on Oct 19, 2013 5:26:45 PM
 * 
 */

package net.perspective.draw;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.event.DrawAreaListener;

/**
 *
 * @author ctipper
 */

@Singleton
public class ApplicationController implements Initializable {

    @Inject private DrawingArea drawarea;
    @Inject private Gesticulate application;
    @Inject private ShareUtils share;
    @Inject private DrawAreaListener listener;
    private BooleanProperty snapshotEnabled;
    private BooleanProperty progressBarVisible;
    private When wireframeSelected;
    private SequentialTransition statusTransition;

    @FXML private GridPane appmenu;

    @FXML 
    private void handleWipeAction(ActionEvent e) {
        share.resetCanvasFile();
        drawarea.prepareDrawing();
    }

    @FXML
    private void handleReadInAction(ActionEvent e) {
        File file = share.chooseCanvas();
        share.readCanvas(file);
    }

    @FXML
    private void handleSaveAsAction(ActionEvent e) {
        share.exportCanvas();
        menubutton.fire();
     }

    @FXML
    private void handleSaveToAction(ActionEvent e) {
        File file = share.getCanvasFile();
        if (file != null) {
            share.writeCanvas(file);
        } else {
            share.exportCanvas();
        }
    }

    @FXML
    private void handleSelectionAction(ActionEvent e) {
        listener.changeHandlers(HandlerType.SELECTION);
    }

    @FXML
    private void handleRotationAction(ActionEvent e) {
        listener.changeHandlers(HandlerType.ROTATION);
    }

    @FXML
    private void handleLineAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.LINE);
        listener.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handleCircleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ELLIPSE);
        listener.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handleSquareAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.RECTANGLE);
        listener.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ISOSCELES);
        listener.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handlePolygonAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.POLYGON);
        listener.changeHandlers(HandlerType.SKETCH);
    }

    @FXML
    private void handleSketchAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.SKETCH);
        listener.changeHandlers(HandlerType.SKETCH);
    }

    @FXML
    private void handleTextAction(ActionEvent e) {
        // not implemented
    }

    @FXML
    private void handleOpacityAction(ActionEvent e) {
        drawarea.setTransparency(wireframeSelected.then(0).otherwise(100).intValue());
    }

    /**
     * Quit app menu item
     * 
     * It proved necessary to emit a window close event rather than
     * invoking Platform.exit() which is too simplistic for fx-guice
     * @param e 
     */
    @FXML
    private void handleOnQuitAction(ActionEvent e) {
        application.getStage().fireEvent(
                new WindowEvent(
                        application.getStage(),
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );
    }

    @FXML
    private void handlePdfExportAction(ActionEvent e) {
        share.exportPDF();
        menubutton.fire();
    }

    @FXML
    private void handleSvgExportAction(ActionEvent e) {
        share.exportSVG();
        menubutton.fire();
    }

    @FXML
    private void handlePngExportAction(ActionEvent e) {
        share.exportPNG();
        menubutton.fire();
    }

    @FXML
    private void handlePngSnapshotAction(ActionEvent e) {
        snapshotEnabled.setValue(true);
        share.snapshotPNG();
    }

    /**
     * Snapshot button enabled property
     * 
     * @return 
     */
    public BooleanProperty getSnapshotProperty() {
        return snapshotEnabled;
    }

    /**
     * Binary choice from wireframe button state
     * 
     * @return 
     */
    public When getWireframeWhen() {
        return wireframeSelected;
    }
    
    /**
     * Progress bar visible property
     * 
     * @return 
     */
    public BooleanProperty getProgressVisibleProperty() {
        return progressBarVisible;
    }

    /**
     * Progress bar progress property
     * 
     * @return 
     */
    public DoubleProperty getProgressProperty() {
        return progressbar.progressProperty();
    }
    
    /**
     * Set progress bar indeterminate
     * 
     * @return 
     */
    public void setProgressIndeterminate() {
        progressbar.setProgress(-1);
    }

    /**
     * Set status text to message
     * 
     * @param message 
     */
    public void setStatusMessage(String message) {
        statusTransition.stop();
        statusbar.setText(message);
        statusTransition.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the sliding application menu
        appmenu.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        this.prepareSlideMenuAnimation();

        // bind a property to the snapshot button disable state
        this.snapshotEnabled = new SimpleBooleanProperty();
        this.snapshotEnabled.bindBidirectional(snapshotbutton.disableProperty());
        
        // bind a property to the wireframe button selected state
        this.wireframeSelected = Bindings.when(wireframebutton.selectedProperty());
        
        // bind a property to the progress bar visible property
        this.progressBarVisible = new SimpleBooleanProperty();
        this.progressBarVisible.bindBidirectional(progressbar.visibleProperty());
        
        // set up the status message fade transition
        this.setupStatusTransition();
    }

    private void prepareSlideMenuAnimation() {
        TranslateTransition openNav = new TranslateTransition(new Duration(350), appmenu);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), appmenu);
        menubutton.setOnAction((ActionEvent evt) -> {
            if (appmenu.getTranslateX() != 0) {
                openNav.play();
            } else {
                closeNav.setToX(-(appmenu.getWidth()));
                closeNav.play();
            }
        });
    }

    private void setupStatusTransition() {
        FadeTransition ft = new FadeTransition(Duration.millis(2000), statusbar);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setCycleCount(1);
        statusTransition = new SequentialTransition(
                new PauseTransition(Duration.millis(3000)),
                ft
        );
    }

    @FXML
    private Button menubutton;
    @FXML
    private Button snapshotbutton;
    @FXML
    private ToggleButton wireframebutton;
    @FXML
    private Label statusbar;
    @FXML
    private ProgressBar progressbar;

}
