/*
 * ApplicationController.java
 * 
 * Created on Oct 19, 2013 5:26:45 PM
 * 
 */

package net.perspective.draw;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javax.inject.Inject;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.HandlerType;

/**
 *
 * @author ctipper
 */

public class ApplicationController implements Initializable {
    
    @Inject private DrawingArea drawarea;
    
    @FXML
    private void handleWipeAction(ActionEvent event) {
        drawarea.prepareDrawing();
    }
    
    @FXML
    private void handleSelectionAction(ActionEvent event) {
        drawarea.changeHandler(HandlerType.SELECTION);
    }
    
    @FXML
    private void handleRotationAction(ActionEvent event) {
        drawarea.changeHandler(HandlerType.ROTATION);
    }
    
    @FXML
    private void handleLineAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.LINE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleCircleAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.CIRCLE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleSquareAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.SQUARE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.TRIANGLE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handlePolygonAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.POLYGON);
        drawarea.changeHandler(HandlerType.SKETCH);
    }

    @FXML
    private void handleSketchAction(ActionEvent event) {
        drawarea.setDrawType(DrawingType.SKETCH);
        drawarea.changeHandler(HandlerType.SKETCH);
    }
    
    @FXML
    private void handleOpacityAction(InputEvent event) {
        javafx.scene.control.ToggleButton button = (javafx.scene.control.ToggleButton) event.getSource();
        if (button.isSelected()) {
            drawarea.setTransparency(0);
        } else {
            drawarea.setTransparency(100);
        }
        event.consume();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
}
