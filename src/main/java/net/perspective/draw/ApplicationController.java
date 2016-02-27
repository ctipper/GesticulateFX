/*
 * ApplicationController.java
 * 
 * Created on Oct 19, 2013 5:26:45 PM
 * 
 */

package net.perspective.draw;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.HandlerType;

/**
 *
 * @author ctipper
 */

@Singleton
public class ApplicationController implements Initializable {
    
    @Inject private DrawingArea drawarea;
    @Inject private ShareUtils share;
    private BooleanProperty snapshotEnabled;

    @FXML
    private Button menubutton;
    @FXML
    private Button snapshotbutton;
    @FXML
    private GridPane appmenu;

    @FXML
    private void handleWipeAction(ActionEvent e) {
        drawarea.prepareDrawing();
    }
    
    @FXML
    private void handleOpenInAction(ActionEvent e) {
        // not implemented
    }
    
    @FXML
    private void handleSaveToAction(ActionEvent e) {
        // not implemented
    }
    
    @FXML
    private void handleSelectionAction(ActionEvent e) {
        drawarea.changeHandlers(HandlerType.SELECTION);
    }
    
    @FXML
    private void handleRotationAction(ActionEvent e) {
        drawarea.changeHandlers(HandlerType.ROTATION);
    }
    
    @FXML
    private void handleLineAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.LINE);
        drawarea.changeHandlers(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleCircleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ELLIPSE);
        drawarea.changeHandlers(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleSquareAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.RECTANGLE);
        drawarea.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ISOSCELES);
        drawarea.changeHandlers(HandlerType.FIGURE);
    }

    @FXML
    private void handlePolygonAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.POLYGON);
        drawarea.changeHandlers(HandlerType.SKETCH);
    }

    @FXML
    private void handleSketchAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.SKETCH);
        drawarea.changeHandlers(HandlerType.SKETCH);
    }
    
    @FXML
    private void handleTextAction(ActionEvent e) {
        // not implemented
    }
    
    @FXML
    private void handleMenuAction(ActionEvent e) {
        // not implemented
    }
    
    @FXML
    private void handleOpacityAction(ActionEvent e) {
        javafx.scene.control.ToggleButton button = (javafx.scene.control.ToggleButton) e.getSource();
        if (button.isSelected()) {
            drawarea.setTransparency(0);
        } else {
            drawarea.setTransparency(100);
        }
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
        share.snapshotPNG(this);
    }
    
    /**
     * Binds state of snapshot button
     * 
     * @return 
     */
    public BooleanProperty getSnapshotProperty() {
        return snapshotEnabled;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the sliding application menu
        appmenu.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        this.prepareSlideMenuAnimation();
        
        // bind a property to the snapshot button disable state
        this.snapshotEnabled = new SimpleBooleanProperty();
        this.snapshotEnabled.bindBidirectional(snapshotbutton.disableProperty());
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

}
