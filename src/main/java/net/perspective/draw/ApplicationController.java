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
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.HandlerType;

/**
 *
 * @author ctipper
 */

public class ApplicationController implements Initializable {
    
    @Inject private Gesticulate application;
    @Inject private DrawingArea drawarea;
    
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
        application.exportSVG();
    }
    
    @FXML
    private void handlePngExportAction(ActionEvent e) {
        application.exportPNG();
    }
    
    @FXML
    private void handlePngSnapshotAction(ActionEvent e) {
        application.snapshotPNG();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
}
