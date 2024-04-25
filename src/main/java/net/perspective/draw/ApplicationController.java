/*
 * ApplicationController.java
 * 
 * Created on Oct 19, 2013 5:26:45 PM
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.enums.KeyHandlerType;

/**
 * 
 * @author ctipper
 */

@Singleton
public class ApplicationController implements Initializable {

    @Inject private DrawingArea drawarea;
    @Inject private Gesticulate application;
    @Inject private CanvasView view;
    @Inject private ShareUtils share;
    @Inject private MapController mapper;
    private BooleanProperty snapshotEnabled;
    private BooleanProperty progressBarVisible;
    private BooleanProperty themeProperty;
    private SimpleStringProperty comboThemeProperty;
    private When outlineSelected;
    private SequentialTransition statusTransition;
    private ReadOnlyStringWrapper strokeTypeProperty;
    private ReadOnlyStringWrapper strokeStyleProperty;
    private ReadOnlyStringWrapper fontFamilyProperty, fontSizeProperty;
    private SimpleStringProperty comboFontProperty, comboFontSizeProperty;
    private SimpleObjectProperty<Color> pickerColorProperty, pickerFillColorProperty;
    private ReadOnlyObjectWrapper<Color> colorProperty, fillColorProperty;
    private SimpleStringProperty themeBackgroundColor, themeFillColor, themeAccentColor, canvasBackgroundColor;
    private BooleanProperty lineType, sketchType;
    private BooleanProperty dropperEnabled;
    private BooleanProperty oneToOneEnabled;
    private BooleanProperty gridProperty;
    private BooleanProperty guideProperty;
    private BooleanProperty boldProperty;
    private BooleanProperty italicProperty;
    private BooleanProperty underlinedProperty;
    private Dialog<ButtonType> aboutBox;
    java.util.List<String> fontFamily = Arrays.asList("Serif", "SansSerif", "Monospaced");
    java.util.List<String> fontSize = Arrays.asList("8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72");

    private final String SVG_HORIZONTAL = "M0.000000 11.792053L22.415894 11.792053";
    private final String SVG_VERTICAL = "M11.207947 23.000000L11.207947 0.584106";
    private final String SVG_INFO_A = "M9.853952797256 11.684608723148C9.505249291706 12.312275033138 8.97521996327 13.051526464904 8.654412738164 13.051526464904 8.389398073946 13.051526464904 8.403346214168 12.758615520242 8.500983195722 12.39596387447L9.937641638588 7.081722449888 9.853952797256 6.998033608556001 6.645880546196 7.402529674994001 6.53429542442 7.86281830232 7.343287557296 7.946507143652C7.6222503617360005 7.974403424096 7.678042922624 8.155729246982 7.5804059410699995 8.532329032976001L6.506399143976 12.507548996246C6.213488199314 13.58155579334 6.548243564642 14.167377682664 7.482768959516 14.167377682664 8.83573856105 14.167377682664 9.686575114592 13.09337088557 10.272397003916 11.921727106921999M9.268130907932001 6.202989615902C9.979486059254 6.202989615902 10.467670967023999 5.742700988576001 10.467670967023999 4.975553276366 10.467670967023999 4.292094405488001 9.965537919032 3.957339040160001 9.351819749264 3.957339040160001 8.528879476166 3.957339040160001 8.13833154995 4.58500535015 8.13833154995 5.184775379696001 8.13833154995 5.8124416896860005 8.598620177276 6.202989615902 9.268130907932001 6.202989615902";
    private final String SVG_INFO_B = "M8.360382 0.503647C3.683502 0.503647-0.107849 4.294998-0.107849 8.971878-0.107849 13.648758 3.683502 17.440109 8.360382 17.440109 13.037262 17.440109 16.828613 13.648758 16.828613 8.971878 16.828613 4.294998 13.037262 0.503647 8.360382 0.503647Z";
    private final String SVG_PAL = "M11.377778 2.1333336C6.039629199999999 2.1333336 1.711111299999999 6.312592199999999 1.711111299999999 11.466667000000001 1.711111299999999 16.620742 6.0396291999999985 20.8 11.377778 20.8 12.269259 20.8 12.988888999999999 20.105186 12.988888999999999 19.244444 12.988888999999999 18.84 12.833147999999998 18.477038 12.570000999999998 18.202223 12.317592999999997 17.927407 12.167222999999998 17.56963 12.167222999999998 17.170371 12.167222999999998 16.30963 12.886851999999998 15.614815 13.778332999999998 15.614815H15.674073999999997C18.638519999999996 15.614815 21.044444999999996 13.291852 21.044444999999996 10.42963 21.044444999999996 5.8459261 16.715926999999997 2.1333336000000003 11.377777999999996 2.1333336000000003ZM5.4703702 11.466667C4.5788889 11.466667 3.8592589999999998 10.771851999999999 3.8592589999999998 9.911111 3.8592589999999998 9.0503705 4.5788889 8.3555554 5.4703702 8.3555554 6.3618516 8.3555554 7.0814815 9.0503705 7.0814815 9.911111 7.0814815 10.771851999999999 6.3618516 11.466667 5.4703702 11.466667ZM8.6925927 7.3185186C7.8011113000000005 7.3185186 7.081481500000001 6.6237034999999995 7.081481500000001 5.7629629 7.081481500000001 4.902222399999999 7.8011113000000005 4.2074073 8.6925927 4.2074073 9.5840741 4.2074073 10.303704 4.902222399999999 10.303704 5.7629629 10.303704 6.6237034999999995 9.5840741 7.3185186 8.6925927 7.3185186ZM14.062963 7.3185186C13.171482 7.3185186 12.451852 6.6237034999999995 12.451852 5.7629629 12.451852 4.902222399999999 13.171482000000001 4.2074073 14.062963 4.2074073 14.954445 4.2074073 15.674074 4.902222399999999 15.674074 5.7629629 15.674074 6.6237034999999995 14.954445 7.3185186 14.062963 7.3185186ZM17.285185 11.466667000000001C16.393704 11.466667000000001 15.674074 10.771852 15.674074 9.911111000000002 15.674074 9.050370500000001 16.393704 8.355555400000002 17.285185 8.355555400000002 18.176667 8.355555400000002 18.896296999999997 9.050370500000001 18.896296999999997 9.911111000000002 18.896296999999997 10.771852000000003 18.176667 11.466667000000001 17.285185 11.466667000000001Z";
    private final String SVG_SKETCH = "M9.683105 12.160538C9.387039 11.739883,8.870743 11.533356,8.366211 11.633789C7.209183 11.864105,6.822739 13.242813,7.049316 14.530975C7.212357 15.457993,7.598648 16.360352,8.366211 16.901367C11.324356 18.986465,15.644501 15.517441,14.160538 10.975342C13.918884 10.235748,13.520340 9.555481,12.975342 9.000000C9.410904 5.367126,3.067444 7.514481,1.781738 12.950684C0.468903 18.501633,5.099258 23.668060,11.000000 23.485840C17.038956 23.299332,21.461807 17.802231,21.008408 11.633789C20.603149 6.120422,16.446732 1.645233,11.000000 0.571884C7.478043 -0.122177,3.828094 0.742279,0.991592 2.942276";
    private final String SVG_POLYGON = "M18.730026 5.361984L13.638016 0.269974C4.441025 0.341461,-2.007233 9.368988,0.907990 18.092010C1.734192 20.564240,3.469177 22.750504,6.000000 23.184021C7.693268 23.474060,11.145432 21.827255,13.638016 20.638016C15.358093 19.817337,18.366989 18.381744,18.730026 18.092010C22.815308 14.831955,22.815308 8.622040,18.730026 5.361984Z";

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

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
        drawarea.changeHandlers(HandlerType.SELECTION);
        view.setEditing(KeyHandlerType.MOVE);
    }

    @FXML
    private void handleRotationAction(ActionEvent e) {
        drawarea.changeHandlers(HandlerType.ROTATION);
        view.setEditing(KeyHandlerType.MOVE);
    }

    @FXML
    private void handleLineAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.LINE);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleLineTypeAction(ActionEvent e) {
        this.setDrawAreaLineType();
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleCircleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ELLIPSE);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleSquareAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.RECTANGLE);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleTriangleAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.ISOSCELES);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleHexagonAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.HEXAGON);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handlePentagramAction(ActionEvent e) {
        drawarea.setDrawType(DrawingType.PENTAGRAM);
        drawarea.changeHandlers(HandlerType.FIGURE);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleSketchTypeAction(ActionEvent e) {
        this.setDrawAreaSketchType();
        drawarea.changeHandlers(HandlerType.SKETCH);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleTextAction(ActionEvent e) {
        drawarea.changeHandlers(HandlerType.TEXT);
        view.setEditing(KeyHandlerType.NONE);
    }

    @FXML
    private void handleDropperAction(ActionEvent e) {
        // not implemented
    }

    @FXML
    private void handleOpacityAction(ActionEvent e) {
        drawarea.setTransparency(outlineSelected.then(0).otherwise(100).intValue());
    }

    @FXML
    private void handleModeChange(ActionEvent e) {
        menubutton.fire();
    }

    /**
     * Quit app menu item
     * 
     * It proved necessary to emit a window close event rather than
     * invoking Platform.exit() which is too simplistic for fx-guice
     * 
     * @param e the {@link javafx.event.ActionEvent}
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
    private void handleMapInsertAction(ActionEvent e) {
        mapper.createMap();
        drawarea.changeHandlers(HandlerType.MAP);
        view.setEditing(KeyHandlerType.MAP);
        menubutton.fire();
    }

    @FXML
    private void handlePngSnapshotAction(ActionEvent e) {
        snapshotEnabled.setValue(true);
        share.snapshotPNG();
    }

    @FXML
    private void handleImgImportAction(ActionEvent e) {
        share.setImageFiles(share.chooseImages());
        share.readPictures();
        menubutton.fire();
    }

    @FXML
    private void handleHorizontalAction(ActionEvent e) {
        setLineButtonGraphic(false);
        setLineTypeProperty(false);
        linebutton.setSelected(true);
        setDrawAreaLineType();
        drawarea.changeHandlers(HandlerType.FIGURE);
        tabbutton.fire();
    }

    @FXML
    private void handleVerticalAction(ActionEvent e) {
        setLineButtonGraphic(true);
        setLineTypeProperty(true);
        linebutton.setSelected(true);
        setDrawAreaLineType();
        drawarea.changeHandlers(HandlerType.FIGURE);
        tabbutton.fire();
    }

    @FXML
    private void handleSketchAction(ActionEvent e) {
        setSketchButtonGraphic(true);
        setSketchTypeProperty(true);
        sketchbutton.setSelected(true);
        setDrawAreaSketchType();
        drawarea.changeHandlers(HandlerType.SKETCH);
        tabbutton_2.fire();
    }

    @FXML
    private void handlePolygonAction(ActionEvent e) {
        setSketchButtonGraphic(false);
        setSketchTypeProperty(false);
        sketchbutton.setSelected(true);
        setDrawAreaSketchType();
        drawarea.changeHandlers(HandlerType.SKETCH);
        tabbutton_2.fire();
    }

    @FXML
    private void handleAboutBoxAction(ActionEvent e) {
        menubutton.fire();
        if (aboutBox == null) {
            aboutBox = new AboutBox();
            Stage stage = application.getStage();
            aboutBox.initOwner(stage);
        }
        aboutBox.showAndWait();
    }

    /**
     * Set the button graphic for line button
     * 
     * @param vertical is vertical line active
     */
    private void setLineButtonGraphic(Boolean vertical) {
        Tooltip tooltip;
        SVGPath path = new SVGPath();
        if (vertical) {
            path.setContent(SVG_VERTICAL);
            tooltip = new Tooltip("Vertical");
        } else {
            path.setContent(SVG_HORIZONTAL);
            tooltip = new Tooltip("Horizontal");
        }
        path.getStyleClass().add("svgPath");
        linebutton.setGraphic(path);
        linebutton.setTooltip(tooltip);
    }

    /**
     * Set the line type horizontal or vertical
     */
    private void setDrawAreaLineType() {
        if (lineType.getValue()) {
            drawarea.setDrawType(DrawingType.VERTICAL);
        } else {
            drawarea.setDrawType(DrawingType.HORIZONTAL);
        }
    }

    /**
     * Set the line type
     * Choice is horizontal or vertical
     * 
     * @param type horizontal false vertical true
     */
    public void setLineTypeProperty(Boolean type) {
        lineType.setValue(type);
    }

    /**
     * Get the line type
     * Choice is horizontal or vertical
     * 
     * @return horizontal false vertical true
     */
    public BooleanProperty getLineTypeProperty() {
        return lineType;
    }

    /**
     * Set the button graphic for sketch button
     * 
     * @param sketch is sketch active
     */
    private void setSketchButtonGraphic(Boolean sketch) {
        Tooltip tooltip;
        SVGPath path = new SVGPath();
        if (sketch) {
            path.setContent(SVG_SKETCH);
            tooltip = new Tooltip("Sketch");
        } else {
            path.setContent(SVG_POLYGON);
            tooltip = new Tooltip("Polygon");
        }
        path.getStyleClass().add("svgPath");
        path.setFill(Color.TRANSPARENT);
        sketchbutton.setGraphic(path);
        sketchbutton.setTooltip(tooltip);
    }

    /**
     * Set the sketch type polygon or sketch
     */
    private void setDrawAreaSketchType() {
        if (sketchType.getValue()) {
            drawarea.setDrawType(DrawingType.SKETCH);
        } else {
            drawarea.setDrawType(DrawingType.POLYGON);
        }
    }

    /**
     * Set the sketch type
     * Choice is sketch or polyogon
     * 
     * @param type polygon false sketch true
     */
    public void setSketchTypeProperty(Boolean type) {
        sketchType.setValue(type);
    }

    /**
     * Get the sketch type
     * Choice is sketch or polygon
     * 
     * @param type polygon false sketch true
     */
    public BooleanProperty getSketchTypeProperty() {
        return sketchType;
    }

    /**
     * Snapshot button enabled property
     * 
     * @return snapshot button active
     */
    public BooleanProperty getSnapshotProperty() {
        return snapshotEnabled;
    }

    /**
     * Binary choice from outline button state
     * 
     * @return outline selected property binding
     */
    public When getOutlineWhen() {
        return outlineSelected;
    }

    /**
     * Dropper button enabled property
     * 
     * @return dropper is enabled
     */
    public BooleanProperty getDropperEnabledProperty() {
        return dropperEnabled;
    }

    /**
     * Get dropper button disabled
     * 
     * @return dropper is disabled
     */
    public Boolean getDropperDisabled() {
        return !dropperEnabled.getValue();
    }

    /**
     * Get 1:1 button selected
     * 
     * @return oneToOne selected
     */
    public Boolean getOneToOneEnabled() {
        return oneToOneEnabled.getValue();
    }

    /**
     * Progress bar visible property
     * 
     * @return progress visible property
     */
    public BooleanProperty getProgressVisibleProperty() {
        return progressBarVisible;
    }

    /**
     * Stroke type property
     * 
     * @return stroke type property
     */
    public ReadOnlyStringWrapper getStrokeTypeProperty() {
        return strokeTypeProperty;
    }

    /**
     * Stroke style property
     * 
     * @return stroke style property
     */
    public ReadOnlyStringWrapper getStrokeStyleProperty() {
        return strokeStyleProperty;
    }

    /**
     * Stroke colour property
     * 
     * @return stroke colour property
     */
    public ReadOnlyObjectWrapper<Color> getColorProperty() {
        return colorProperty;
    }

    /**
     * Fill colour property
     * 
     * @return fill colour property
     */
    public ReadOnlyObjectWrapper<Color> getFillColorProperty() {
        return fillColorProperty;
    }

    /**
     * Set stroke via combo box
     * 
     * @param fontFamily the font family
     */
    public void setFontFamily(String fontFamily) {
        fontcombobox.getSelectionModel().select(fontFamily);
    }

    /**
     * Set font size via combo box
     * 
     * @param fontSize the font size
     */
    public void setFontSize(int fontSize) {
        fontsizecombobox.getSelectionModel().select(String.valueOf(fontSize));
    }

    /**
     * Get font family property
     * 
     * @return font family property
     */
    public SimpleStringProperty getFontFamilyProperty() {
        return fontFamilyProperty;
    }

    /**
     * Get font size property
     * 
     * @return font size property
     */
    public SimpleStringProperty getFontSizeProperty() {
        return fontSizeProperty;
    }

    /**
     * Increment font size
     */
    public void incrementFontSize() {
        String size = getFontSizeProperty().getValue();
        int index = fontSize.indexOf(size) + 1;
        if (index >= fontSize.size()) {
            index = fontSize.size() - 1;
        }
        this.setFontSize(Integer.parseInt(fontSize.get(index)));
    }

    /**
     * Decrement font size
     */
    public void decrementFontSize() {
        String size = getFontSizeProperty().getValue();
        int index = fontSize.indexOf(size) - 1;
        if (index < 0) {
            index = 0;
        }
        this.setFontSize(Integer.parseInt(fontSize.get(index)));
    }

    /**
     * Bold button active property
     * 
     * @return bold selected
     */
    public BooleanProperty getBoldProperty() {
        return boldProperty;
    }

    /**
     * Italic button active property
     * 
     * @return italic selected
     */
    public BooleanProperty getItalicProperty() {
        return italicProperty;
    }

    /**
     * Underline button active property
     * 
     * @return underlined selected
     */
    public BooleanProperty getUnderlinedProperty() {
        return underlinedProperty;
    }

    /**
     * Progress bar progress property
     * 
     * @return progress property
     */
    public DoubleProperty getProgressProperty() {
        return progressbar.progressProperty();
    }

    /**
     * Set progress bar indeterminate
     */
    public void setProgressIndeterminate() {
        progressbar.setProgress(-1);
    }

    /**
     * Set status text to message
     * 
     * @param message the status text
     */
    public void setStatusMessage(String message) {
        statusTransition.stop();
        statusbar.setText(message);
        statusTransition.play();
    }

    /**
     * Get the theme dark or light
     * 
     * @return is dark mode
     */
    public BooleanProperty getThemeProperty() {        
        return this.themeProperty;
    }

    /**
     * Set theme via combo box
     * 
     * @param themeType the theme
     */
    public void setThemeType(String themeType) {
        comboTheme.getSelectionModel().select(themeType);
    }

    /**
     * Get the theme
     * 
     * @return themeProperty the theme
     */
    public SimpleStringProperty getComboThemeProperty() {        
        return this.comboThemeProperty;
    }

    /**
     * Adjust theme fill colour by providing an offset
     * 
     * @param c the web fill color
     */
    public void adjustThemeFillColor(String c) {
        themeFillColor.setValue(getThemeColor(c));
    }

    /**
     * Get theme fill colour
     * 
     * @return fillColor the web fill colour
     */
    public String getThemeFillColor() {
        return themeFillColor.getValue();
    }

    /**
     * Get theme background colour
     * 
     * @return background background web colour
     */
    public String getThemeBackgroundColor() {
        return themeBackgroundColor.getValue();
    }

    /**
     * Get theme accent colour
     * 
     * @return accent accent web colour
     */
    public String getThemeAccentColor() {
        return themeAccentColor.getValue();
    }

    /**
     * Set canvas background colour
     * 
     * @param c canvas background web colour
     */
    public void setCanvasBackgroundColor(String c) {
        canvasBackgroundColor.setValue(c);
    }

    /**
     * Get canvas background colour
     * 
     * @return canvas background web colour
     */
    public String getCanvasBackgroundColor() {
        return canvasBackgroundColor.getValue();
    }


    /**
     * Set colour of background colour picker
     * 
     * @param c web colour
     */
    public void setBackgroundPickerColor(String c) {
        backgroundcolorpicker.setValue(Color.web(c));
    }

    /**
     * Set stroke via combo box
     * 
     * @param strokeId the stroke ID
     */
    public void setStrokeCombo(int strokeId) {
        strokecombobox.getSelectionModel().select(strokeId);
    }

    /**
     * Set style via combo box
     * 
     * @param styleId the style
     */
    public void setStyleCombo(String styleId) {
        stylecombobox.getSelectionModel().select(styleId);
    }

    /**
     * Set the colour
     * 
     * @param color colour
     */
    public void setColor(Color color) {
        colorpicker.setValue(color);
        colorpicker.setStyle(backgroundStyle(color));
        pickerColorProperty.setValue(color);
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor fill colour
     */
    public void setFillColor(Color fillcolor) {
        fillcolorpicker.setValue(fillcolor);
        fillcolorpicker.setStyle(backgroundStyle(fillcolor));
        pickerFillColorProperty.setValue(fillcolor);
    }

    /**
     * Set selection mode
     */
    public void setSelectionMode() {
        // selection mode by default
        this.selectbutton.setSelected(true);
        drawarea.changeHandlers(HandlerType.SELECTION);
    }

    /**
     * Cell factory for theme colour stroke combo items
     * 
     * @param theme is dark mode
     * @return the list cell {@link javafx.util.Callback}
     */
    private Callback<ListView<String>, ListCell<String>> getCellFactory(Boolean theme) {
        return (ListView<String> p) -> new ListCell<String>() {

            private final ImageView cellImage;

            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                cellImage = new ImageView(new Image("images/stroke1.png"));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new ImageView(new Image("images/" + item + (theme ? "-alt" : "" ) + ".png")));
                }
            }
        };
    }

    /**
     * Set the theme
     * 
     * @param isDarkMode is dark mode
     */
    public void setAppStyles(Boolean isDarkMode) {
        // set theme colours
        if (isDarkMode) {
            // themeFillColor.setValue("#1d1d1d");
            themeBackgroundColor.setValue("#3a3a3a");
            themeAccentColor.setValue("#c0c481");
        } else {
            // themeFillColor.setValue("lightgray");
            themeBackgroundColor.setValue("white");
            themeAccentColor.setValue("black");
        }
        // alter draw area colour settings
        adjustThemeFillColor(themeBackgroundColor.getValue());
        setCanvasBackgroundColor(themeBackgroundColor.getValue());
        setBackgroundPickerColor(themeBackgroundColor.getValue());
        drawarea.setDarkModeEnabled(isDarkMode);
        drawarea.setTheme();
        drawarea.redrawGrid();
        // reset combo boxes
        String stroke = strokecombobox.getSelectionModel().getSelectedItem();
        Callback<ListView<String>, ListCell<String>> strokeCellFactory = getCellFactory(isDarkMode);
        strokecombobox.setButtonCell(strokeCellFactory.call(null));
        strokecombobox.setCellFactory(strokeCellFactory);
        strokecombobox.getSelectionModel().select(stroke);
        String style = stylecombobox.getSelectionModel().getSelectedItem();
        Callback<ListView<String>, ListCell<String>> styleCellFactory = getCellFactory(isDarkMode);
        stylecombobox.setButtonCell(styleCellFactory.call(null));
        stylecombobox.setCellFactory(styleCellFactory);
        stylecombobox.getSelectionModel().select(style);
    }

    /**
     * Initialise the controller
     * 
     * @param url the url
     * @param rb resource
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // set the theme to light
        this.themeFillColor = new SimpleStringProperty("lightgray");
        this.themeBackgroundColor = new SimpleStringProperty("white");
        this.themeAccentColor = new SimpleStringProperty("black");
        this.canvasBackgroundColor = new SimpleStringProperty("white");;
        this.prepareDarkModeOptions();
        this.themeProperty = new SimpleBooleanProperty();
        this.themeProperty.setValue(false);
        this.comboThemeProperty = new SimpleStringProperty();
        this.comboThemeProperty.setValue("Light");
        comboTheme.setOnAction((var event) -> {
            String s = comboTheme.getValue();
            comboThemeProperty.setValue(s);
            if (s.equals("System") && Gesticulate.MM_SYSTEM_THEME) {
                application.setSystemTheme();
            } else {
                themeProperty.setValue(s.equals("Dark"));
                if (Gesticulate.MM_SYSTEM_THEME) {
                    application.deregisterThemeListener();
                }
            }
        });
        /**
         * Property change handler to set/reset night mode on demand
         */
        this.themeProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setAppStyles(newValue);
            // reset application stylesheets
            application.resetStylesheets(newValue);
        });
        this.gridProperty = new SimpleBooleanProperty();
        this.gridProperty.bindBidirectional(this.checkGrid.selectedProperty());
        this.gridProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            application.drawGrid(newValue);
        });
        this.guideProperty = new SimpleBooleanProperty();
        this.guideProperty.bindBidirectional(this.checkGuide.selectedProperty());
        this.guideProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            drawarea.setGuideEnabled(newValue);
        });

        // Initialize the sliding application menu
        appmenu.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        this.prepareSlideMenuAnimation();

        // bind a property to the snapshot button disable state
        this.snapshotEnabled = new SimpleBooleanProperty();
        this.snapshotEnabled.bindBidirectional(snapshotbutton.disableProperty());

        // bind a property to the outline button selected state
        this.outlineSelected = Bindings.when(outlinebutton.selectedProperty());

        // bind a property to the dropper button selected state
        this.dropperEnabled = new SimpleBooleanProperty();
        this.dropperEnabled.bindBidirectional(dropperbutton.selectedProperty());

        // bind a property to the one-to-one button selected state
        this.oneToOneEnabled = new SimpleBooleanProperty();
        this.oneToOneEnabled.bindBidirectional(onetoonebutton.selectedProperty());

        // bind a property to the progress bar visible property
        this.progressBarVisible = new SimpleBooleanProperty();
        this.progressBarVisible.bindBidirectional(progressbar.visibleProperty());

        // selection mode by default
        this.selectbutton.setSelected(true);
        // correct toggle group select exactly once, see https://stackoverflow.com/a/50667161
        this.toolToggles.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if (newValue == null)
                oldValue.setSelected(true);
        });

        // instantiate a cellfactory for the stroke and style comboboxes
        Callback<ListView<String>, ListCell<String>> strokeCellFactory = getCellFactory(false);
        strokecombobox.setButtonCell(strokeCellFactory.call(null));
        strokecombobox.setCellFactory(strokeCellFactory);
        strokecombobox.getSelectionModel().select("stroke7");
        this.strokeTypeProperty = new ReadOnlyStringWrapper();
        this.strokeTypeProperty.bindBidirectional(strokecombobox.valueProperty());
        // setup stroke style combo box
        Callback<ListView<String>, ListCell<String>> styleCellFactory = getCellFactory(false);
        stylecombobox.setButtonCell(styleCellFactory.call(null));
        stylecombobox.setCellFactory(styleCellFactory);
        stylecombobox.getSelectionModel().selectFirst();
        this.strokeStyleProperty = new ReadOnlyStringWrapper();
        this.strokeStyleProperty.bindBidirectional(stylecombobox.valueProperty());
        
        // setup stroke color picker
        Color color = Color.web("#4860E0");
        colorpicker.getStyleClass().add("button");
        colorpicker.setValue(color);
        colorpicker.setStyle(backgroundStyle(color));
        colorpicker.getCustomColors().add(color);
        pickerColorProperty = new SimpleObjectProperty<>();
        pickerColorProperty.setValue(color);
        colorpicker.setOnAction((ActionEvent event) -> {
            Color c = colorpicker.getValue();
            colorpicker.setStyle(backgroundStyle(c));
            pickerColorProperty.setValue(c);
        });
        this.colorProperty = new ReadOnlyObjectWrapper<>();
        this.colorProperty.bindBidirectional(pickerColorProperty);
        // setup fill color picker
        Color fillColor = Color.web("#4860E0");
        fillcolorpicker.getStyleClass().add("button");
        fillcolorpicker.setValue(fillColor);
        fillcolorpicker.setStyle(backgroundStyle(fillColor));
        fillcolorpicker.getCustomColors().add(fillColor);
        pickerFillColorProperty = new SimpleObjectProperty<>();
        pickerFillColorProperty.setValue(fillColor);
        fillcolorpicker.setOnAction((ActionEvent event) -> {
            Color c = fillcolorpicker.getValue();
            fillcolorpicker.setStyle(backgroundStyle(c));
            pickerFillColorProperty.setValue(c);
        });
        this.fillColorProperty = new ReadOnlyObjectWrapper<>();
        this.fillColorProperty.bindBidirectional(pickerFillColorProperty);

        // background colour picker
        backgroundcolorpicker.setValue(Color.web(themeBackgroundColor.getValue()));
        backgroundcolorpicker.setOnAction((ActionEvent event) -> {
            Color c = backgroundcolorpicker.getValue();
            if (c != null) {
                canvasBackgroundColor.setValue(toRGBCode(c));
                adjustThemeFillColor(toRGBCode(c));
            }
            drawarea.setTheme();
            drawarea.redrawGrid();
        });

        // setup font style combo boxes
        fontcombobox.getItems().clear();
        fontcombobox.getItems().addAll(fontFamily);
        fontcombobox.getSelectionModel().select("Serif");
        comboFontProperty = new SimpleStringProperty();
        comboFontProperty.setValue("Serif");
        fontcombobox.setOnAction((ActionEvent event) -> {
            String s = fontcombobox.getValue();
            comboFontProperty.setValue(s);
        });
        this.fontFamilyProperty = new ReadOnlyStringWrapper();
        this.fontFamilyProperty.bindBidirectional(comboFontProperty);
        fontsizecombobox.getItems().clear();
        fontsizecombobox.getItems().addAll(fontSize);
        fontsizecombobox.getSelectionModel().select("12");
        comboFontSizeProperty = new SimpleStringProperty();
        comboFontSizeProperty.setValue("12");
        fontsizecombobox.setOnAction((ActionEvent event) -> {
            String s = fontsizecombobox.getValue();
            comboFontSizeProperty.setValue(s);
        });
        this.fontSizeProperty = new ReadOnlyStringWrapper();
        this.fontSizeProperty.bindBidirectional(comboFontSizeProperty);

        // bind a property to the bold button selected state
        this.boldProperty = new SimpleBooleanProperty();
        this.boldProperty.bindBidirectional(boldbutton.selectedProperty());
        // bind a property to the italic button selected state
        this.italicProperty = new SimpleBooleanProperty();
        this.italicProperty.bindBidirectional(italicbutton.selectedProperty());
        // bind a property to the underline button selected state
        this.underlinedProperty = new SimpleBooleanProperty();
        this.underlinedProperty.bindBidirectional(underlinebutton.selectedProperty());

        // set up the status message fade transition
        this.setupStatusTransition();
        // animate line button panel
        this.prepareSlideTabButtonsAnimation();
        // animate sketch button panel
        this.prepareSlideTabButtonsAnimation_2();
        // attach affordance button
        this.affordtab.setOnAction((ActionEvent event) -> tabbutton.fire());
        // attach affordance button
        this.affordtab_2.setOnAction((ActionEvent event) -> tabbutton_2.fire());
        // special line button handler
        this.lineType = new SimpleBooleanProperty();
        this.lineType.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (linebutton.isSelected()) {
                setDrawAreaLineType();
                drawarea.changeHandlers(HandlerType.FIGURE);
            }
        });
        // special sketch button handler
        this.sketchType = new SimpleBooleanProperty();
        setSketchTypeProperty(Boolean.TRUE);
        this.sketchType.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (sketchbutton.isSelected()) {
                setDrawAreaSketchType();
                drawarea.changeHandlers(HandlerType.SKETCH);
            }
        });
        this.prepareAboutBoxMenu();
    }

    /**
     * Set the slide menu transition
     */
    private void prepareSlideMenuAnimation() {
        TranslateTransition openNav = new TranslateTransition(new Duration(350), appmenu);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), appmenu);
        menubutton.setOnAction((ActionEvent event) -> {
            if (appmenu.getTranslateX() != 0) {
                openNav.play();
            } else {
                closeNav.setToX(-(appmenu.getWidth()));
                closeNav.play();
            }
        });
    }

    /**
     * Set the line button panel slider transition
     */
    private void prepareSlideTabButtonsAnimation() {
        TranslateTransition openTab = new TranslateTransition(new Duration(200), linepanel);
        double panelWidth = linepanel.getPrefTileWidth() + 2 * linepanel.getHgap() + 1;
        openTab.setToX(panelWidth);
        TranslateTransition closeTab = new TranslateTransition(new Duration(200), linepanel);
        tabbutton.setOnAction((ActionEvent event) -> {
            if (linepanel.getTranslateX() < panelWidth) {
                openTab.play();
            } else {
                closeTab.setToX(-panelWidth);
                closeTab.play();
            }
        });
    }

    /**
     * Set the sketch button panel slider transition
     */
    private void prepareSlideTabButtonsAnimation_2() {
        TranslateTransition openTab = new TranslateTransition(new Duration(200), sketchpanel);
        double panelWidth = sketchpanel.getPrefTileWidth() + 2 * sketchpanel.getHgap() + 1;
        openTab.setToX(panelWidth);
        TranslateTransition closeTab = new TranslateTransition(new Duration(200), sketchpanel);
        tabbutton_2.setOnAction((ActionEvent event) -> {
            if (sketchpanel.getTranslateX() < panelWidth) {
                openTab.play();
            } else {
                closeTab.setToX(-panelWidth);
                closeTab.play();
            }
        });
    }

    /**
     * Set the status text transition
     */
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

    /**
     * Add dark mode option UI
     */
    private void prepareDarkModeOptions() {
        SVGPath icon = getThemeGlyph();
        Button palette = new Button();
        palette.setAlignment(Pos.CENTER);
        palette.setFocusTraversable(false);
        palette.setMnemonicParsing(false);
        palette.getStyleClass().add("menuicon");
        palette.setPrefHeight(20.0);
        palette.setGraphic(icon);
        comboTheme = new ComboBox<>();
        comboTheme.setPrefHeight(20.0);
        comboTheme.setPrefWidth(120.0);
        comboTheme.setFocusTraversable(false);
        if (Gesticulate.MM_SYSTEM_THEME) {
            comboTheme.getItems().addAll("Light", "Dark", "System");
        } else {
            comboTheme.getItems().addAll("Light", "Dark");
        }
        comboTheme.getSelectionModel().select("Light");
        appmenu.getRowConstraints().add(getRow());
        appmenu.addRow(9, palette, comboTheme);
        GridPane.setConstraints(palette, 0, 9, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(comboTheme, 1, 9, 1, 1, HPos.LEFT, VPos.BASELINE);
    }

    /**
     * Add the palette box to slider menu and configure
     */
    private void prepareAboutBoxMenu() {
        int lastrow = 10;   // first empty row
        int hboxes = 11;    // empty row count
        
        for (int i=0; i<hboxes; i++) {
            appmenu.getRowConstraints().add(getRow());
            appmenu.addRow(lastrow+i, new HBox(), new HBox());
        }
        /**
         * branding text and app icon
         */
        final ImageView image = new ImageView("images/gesticulate-48.png");
        image.setFitWidth(26);
        image.setFitHeight(26);
        Label appname = new Label("Gesticulate FX");
        appname.setStyle("-fx-font-size: 18px;");
        appmenu.getRowConstraints().add(getRow());
        appmenu.addRow(lastrow + hboxes + 1, image, appname);
        GridPane.setConstraints(image, 0, lastrow + hboxes + 1, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(appname, 1, lastrow + hboxes + 1, 1, 1, HPos.LEFT, VPos.BASELINE);
        /**
         * information glyph
         */
        Group icon = getInfoGlyph();
        Button about = new Button();
        about.setAlignment(Pos.CENTER);
        about.setFocusTraversable(false);
        about.setMnemonicParsing(false);
        about.getStyleClass().add("menuicon");
        about.setPrefHeight(20.0);
        about.setGraphic(icon);
        about.setOnAction(this::handleAboutBoxAction);
        /**
         * menu item
         */
        Button aboutmenu = new Button();
        aboutmenu.setAlignment(Pos.CENTER_LEFT);
        aboutmenu.setFocusTraversable(false);
        aboutmenu.setMnemonicParsing(false);
        aboutmenu.getStyleClass().add("menuitem");
        aboutmenu.setPrefWidth(150.0);
        aboutmenu.setPrefHeight(20.0);
        aboutmenu.setText("About...");
        aboutmenu.setOnAction(this::handleAboutBoxAction);
        appmenu.getRowConstraints().add(getRow());
        appmenu.addRow(lastrow + hboxes + 2, about, aboutmenu);
    }

    /**
     * Get one row constraints row
     * 
     * @return the row constraint
     */
    private RowConstraints getRow() {
        RowConstraints con = new RowConstraints();
        con.setPrefHeight(35.0);
        return con;        
    }

    /**
     * Define the palette menu info icon
     * 
     * @return the {@link javafx.scene.Group}
     */
    private Group getInfoGlyph() {
        SVGPath path_a = new SVGPath();
        path_a.setContent(SVG_INFO_A);
        path_a.setFillRule(FillRule.EVEN_ODD);
        path_a.getStyleClass().add("svgFill");
        SVGPath path_b = new SVGPath();
        path_b.setContent(SVG_INFO_B);
        path_b.getStyleClass().add("svgPath");
        path_b.setFill(Color.TRANSPARENT);
        Group glyph = new Group();
        glyph.getChildren().addAll(path_a, path_b);
        return glyph;
    }

    /**
     * Define the theme selector icon
     * 
     * @return the {@link javafx.scene.shape.SVGPath}
     */
    private SVGPath getThemeGlyph() {
        SVGPath path_a = new SVGPath();
        path_a.setContent(SVG_PAL);
        path_a.getStyleClass().add("svgFill");
        return path_a;
    }

    /**
     * Provide a web colour for picker background CSS
     * 
     * @param c the {@link javafx.scene.paint.Color}
     * @return the web colour
     */
    private String backgroundStyle(Color c) {
        return "-fx-background-color: " + toRGBCode(c) + ";";
    }

    /**
     * Offset a given color used by adjust theme colour
     * 
     * @param color a web colour
     * @return a new web colour
     */
    private String getThemeColor(String color) {
        Color newColor;
        Color c = Color.web(color);
        int r = (int) (c.getRed() * 255);
        int g = (int) (c.getGreen() * 255);
        int b = (int) (c.getBlue() * 255);
        int rgb = (r << 16) + (g << 8) + b;
        if (rgb < 0x808080) {
            /**
             * The colour for dark theme.
             */
            newColor = new Color(((r >= 0x1d) ? r - 0x1d : 0) / 255.0, ((g >= 0x1d) ? g - 0x1d : 0) / 255.0, ((b >= 0x1d) ? b - 0x1d : 0) / 255.0, 1d);
        } else {
            /**
             * The colour for light theme.
             */
            newColor = new Color(((r >= 0x2c) ? r - 0x2c : 0) / 255.0, ((g >= 0x2c) ? g - 0x2c : 0) / 255.0, ((b >= 0x2c) ? b - 0x2c : 0) / 255.0, 1d);
        }
        return toRGBCode(newColor);
    }

    /**
     * Convert {@link javafx.scene.paint.Color} colour to web colour
     * 
     * @see <a href="https://stackoverflow.com/a/18803814">How to get hex web String from Color?</a>
     * 
     * @param color the {@link javafx.scene.paint.Color}
     * @return web colour
     */
    public String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }

    /**
     * Parse colour as HEX string
     * 
     * @param colorStr e.g. "#FFFFFF"
     * @return the {@link java.awt.Color}
     */
    public Color fromRGBCode(String colorStr) {
    return new Color(
            Integer.parseInt(colorStr.substring(1, 3), 16) / 255,
            Integer.parseInt(colorStr.substring(3, 5), 16) / 255,
            Integer.parseInt(colorStr.substring(5, 7), 16) / 255,
            1d);
    }

    /** Creates a new instance of <code>ApplicationController</code> */
    public ApplicationController() {}

    @FXML
    private GridPane appmenu;
    @FXML
    private ToggleButton selectbutton;
    @FXML
    private ToggleGroup toolToggles;
    // @FXML
    private ComboBox<String> comboTheme;
    @FXML
    private CheckBox checkGrid;
    @FXML
    private CheckBox checkGuide;
    @FXML
    private ColorPicker colorpicker;
    @FXML
    private ColorPicker fillcolorpicker;
    @FXML
    private ColorPicker backgroundcolorpicker;
    @FXML
    private Button menubutton;
    @FXML
    private Button snapshotbutton;
    @FXML
    private ToggleButton dropperbutton;
    @FXML
    private ToggleButton onetoonebutton;
    @FXML
    private ToggleButton outlinebutton;
    @FXML
    private ToggleButton boldbutton;
    @FXML
    private ToggleButton italicbutton;
    @FXML
    private ToggleButton underlinebutton;
    @FXML
    private Label statusbar;
    @FXML
    private ProgressBar progressbar;
    @FXML
    private ComboBox<String> strokecombobox;
    @FXML
    private ComboBox<String> stylecombobox;
    @FXML
    private ComboBox<String> fontcombobox;
    @FXML
    private ComboBox<String> fontsizecombobox;
    @FXML
    private ToggleButton linebutton;
    @FXML
    private TilePane linepanel;
    @FXML
    private ToggleButton tabbutton;
    @FXML
    private Button affordtab;
    @FXML
    private ToggleButton sketchbutton;
    @FXML
    private TilePane sketchpanel;
    @FXML
    private ToggleButton tabbutton_2;
    @FXML
    private Button affordtab_2;

}
