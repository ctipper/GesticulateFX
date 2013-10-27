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
import net.perspective.draw.event.HandlerType;
import net.perspective.draw.geom.FigureType;

/**
 *
 * @author ctipper
 */

public class ApplicationController implements Initializable {
    
    DrawingCanvas drawcanvas;
    
    void setCanvas(DrawingCanvas d) {
        drawcanvas = d;
    }

    @FXML
    private void handleWipeAction(ActionEvent event) {
        drawcanvas.initCanvas();
    }
    
    @FXML
    private void handleSelectionAction(ActionEvent event) {
        drawcanvas.changeHandler(HandlerType.SELECTION);
    }
    
    @FXML
    private void handleRotationAction(ActionEvent event) {
        drawcanvas.changeHandler(HandlerType.ROTATION);
    }
    
    @FXML
    private void handleLineAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.LINE);
        drawcanvas.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleCircleAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.CIRCLE);
        drawcanvas.changeHandler(HandlerType.FIGURE);
    }
    
    @FXML
    private void handleSquareAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.SQUARE);
        drawcanvas.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.TRIANGLE);
        drawcanvas.changeHandler(HandlerType.FIGURE);
    }

    @FXML
    private void handlePolygonAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.POLYGON);
        drawcanvas.changeHandler(HandlerType.SKETCH);
    }

    @FXML
    private void handleSketchAction(ActionEvent event) {
        drawcanvas.getView().setFigureType(FigureType.SKETCH);
        drawcanvas.changeHandler(HandlerType.SKETCH);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
}
