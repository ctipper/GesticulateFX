/**
 * AboutBox.java
 * 
 * Created on 13 Aug 2019 10:29:20
 * 
 */
package net.perspective.draw;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Christopher G D Tipper
 */

public class AboutBox extends Dialog<ButtonType> {

    /** Creates a new instance of <code>AboutBox</code> */
    public AboutBox() {
        super();
        this.initComponents();
        setResultConverter(buttonType -> {
            return buttonType;
        });
    }
    
    private void initComponents() {
        ButtonType OkButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        final ImageView image = new ImageView("images/gesticulate-48.png");
        image.setFitWidth(48);
        image.setFitHeight(48);
        setHeaderText("Gesticulate FX");
        getDialogPane().setGraphic(image);
        getDialogPane().getButtonTypes().add(OkButtonType);
        Label[] aboutLabel = new Label[5];
        aboutLabel[0] = new Label("A freehand drawing tool");
        aboutLabel[1] = new Label("Version 0.0.8.0");
        // higher java versions use runtime property
        String[] elements = System.getProperty("java.specification.version").split("\\.");
        if (Integer.valueOf(elements[0]) < 2) {
            aboutLabel[2] = new Label("JRE " + System.getProperty("java.version"));
        } else {
            aboutLabel[2] = new Label("JRE " + System.getProperty("java.runtime.version"));
        }
        aboutLabel[3] = new Label("Copyright © 2019 Christopher Tipper");
        aboutLabel[4] = new Label("All rights reserved.");
        aboutLabel[0].setStyle("-fx-font-size: 16px;");
        aboutLabel[1].setStyle("-fx-font-size: 13px;");
        aboutLabel[2].setStyle("-fx-font-size: 12px;");
        aboutLabel[3].setStyle("-fx-font-size: 12px;");
        aboutLabel[4].setStyle("-fx-font-size: 12px;");
        GridPane pane = new GridPane();
        GridPane.setConstraints(aboutLabel[0], 0, 1, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[1], 0, 2, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[2], 0, 3, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(aboutLabel[3], 0, 5, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[4], 0, 6, 1, 1, HPos.CENTER, VPos.BASELINE);
        aboutLabel[2].setPrefHeight(35.0);
        pane.getChildren().addAll(aboutLabel[0], aboutLabel[1], aboutLabel[2], aboutLabel[3], aboutLabel[4]);
        getDialogPane().setContent(pane);
        setResizable(false);
    }
}
