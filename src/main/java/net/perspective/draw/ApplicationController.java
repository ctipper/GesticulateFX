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
import javax.inject.Inject;
import net.perspective.draw.event.HandlerType;
import net.perspective.draw.geom.FigureType;

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
        drawarea.setFigureType(FigureType.LINE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleCircleAction(ActionEvent event) {
        drawarea.setFigureType(FigureType.CIRCLE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleSquareAction(ActionEvent event) {
        drawarea.setFigureType(FigureType.SQUARE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent event) {
        drawarea.setFigureType(FigureType.TRIANGLE);
        drawarea.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handlePolygonAction(ActionEvent event) {
        drawarea.setFigureType(FigureType.POLYGON);
        drawarea.changeHandler(HandlerType.SKETCH);
    }

    @FXML
    private void handleSketchAction(ActionEvent event) {
        drawarea.setFigureType(FigureType.SKETCH);
        drawarea.changeHandler(HandlerType.SKETCH);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
}
