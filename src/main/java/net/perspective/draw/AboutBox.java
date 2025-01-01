/**
 * AboutBox.java
 * 
 * Created on 13 Aug 2019 10:29:20
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 *
 * @author ctipper
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
        this.setHeaderText("Gesticulate FX");
        this.getDialogPane().setGraphic(image);
        this.getDialogPane().getButtonTypes().add(OkButtonType);
        Label[] aboutLabel = new Label[6];
        aboutLabel[0] = new Label("A freehand drawing tool");
        aboutLabel[1] = new Label("Version 1.0");
        // higher java versions use runtime property
        String[] elements = System.getProperty("java.specification.version").split("\\.");
        if (Integer.parseInt(elements[0]) < 2) {
            aboutLabel[2] = new Label("JRE " + System.getProperty("java.version"));
        } else {
            aboutLabel[2] = new Label("JRE " + System.getProperty("java.runtime.version"));
        }
        aboutLabel[3] = new Label("JFX " + System.getProperty("javafx.version"));
        aboutLabel[4] = new Label("Copyright © 2025 Christopher Tipper");
        aboutLabel[5] = new Label("All rights reserved.");
        aboutLabel[0].setStyle("-fx-font-size: 16px;");
        aboutLabel[1].setStyle("-fx-font-size: 13px;");
        aboutLabel[2].setStyle("-fx-font-size: 12px;");
        aboutLabel[3].setStyle("-fx-font-size: 12px;");
        aboutLabel[4].setStyle("-fx-font-size: 12px;");
        aboutLabel[5].setStyle("-fx-font-size: 12px;");
        GridPane pane = new GridPane();
        GridPane.setConstraints(aboutLabel[0], 0, 1, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[1], 0, 2, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[2], 0, 3, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(aboutLabel[3], 0, 5, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[4], 0, 6, 1, 1, HPos.CENTER, VPos.BASELINE);
        GridPane.setConstraints(aboutLabel[5], 0, 7, 1, 1, HPos.CENTER, VPos.BASELINE);
        aboutLabel[2].setPadding(new Insets(6, 0, 0, 0));
        aboutLabel[3].setPadding(new Insets(0, 0, 6, 0));
        pane.getChildren().addAll(aboutLabel[0], aboutLabel[1], aboutLabel[2], 
            aboutLabel[3], aboutLabel[4], aboutLabel[5]);
        pane.setAlignment(Pos.CENTER);
        this.getDialogPane().setContent(pane);
        this.getDialogPane().setPrefWidth(300);
        this.setResizable(false);
    }
}
