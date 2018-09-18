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
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;

/**
 * 
 * @author ctipper
 */

@Singleton
public class ApplicationController implements Initializable {

    @Inject private DrawingArea drawarea;
    @Inject private Gesticulate application;
    @Inject private ShareUtils share;
    private BooleanProperty snapshotEnabled;
    private BooleanProperty progressBarVisible;
    private BooleanProperty themeProperty;
    private When wireframeSelected;
    private SequentialTransition statusTransition;
    private ReadOnlyStringWrapper strokeTypeProperty;
    private ReadOnlyStringWrapper strokeStyleProperty;
    private SimpleObjectProperty<Color> pickerColorProperty, pickerFillColorProperty;
    private ReadOnlyObjectWrapper<Color> colorProperty, fillColorProperty;
    private SimpleStringProperty themeBackgroundColor, themeFillColor, themeAccentColor;

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
     * Stroke type property
     * 
     * @return 
     */
    public ReadOnlyStringWrapper getStrokeTypeProperty() {
        return strokeTypeProperty;
    }

    /**
     * Stroke style property
     * 
     * @return 
     */
    public ReadOnlyStringWrapper getStrokeStyleProperty() {
        return strokeStyleProperty;
    }

    /**
     * Stroke color property
     * 
     * @return 
     */
    public ReadOnlyObjectWrapper<Color> getColorProperty() {
        return colorProperty;
    }

    /**
     * Fill color property
     * 
     * @return 
     */
    public ReadOnlyObjectWrapper<Color> getFillColorProperty() {
        return fillColorProperty;
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

    /**
     * Get the theme
     * 
     * @return themeProperty
     */
    public BooleanProperty getThemeProperty() {
        return this.themeProperty;
    }

    /**
     * Get theme fill colour
     * 
     * @return fillColor
     */
    public String getThemeFillColor() {
        return themeFillColor.getValue();
    }

    /**
     * Get them border colour
     * 
     * @return borderColor
     */
    public String getThemeBackgroundColor() {
        return themeBackgroundColor.getValue();
    }

    /**
     * Get theme fill colour
     * 
     * @return fillColor
     */
    public String getThemeAccentColor() {
        return themeAccentColor.getValue();
    }

    private Callback<ListView<String>, ListCell<String>> getCellFactory(Boolean alternate) {
        return new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> p) {
                return new ListCell<String>() {

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
                            setGraphic(new ImageView(new Image("images/" + item + (alternate ? "-alt" : "" ) + ".png")));
                        }
                    }
                };
            }
        };
    }

    public void setAppStyles(Boolean isNightMode) {
        // set theme colours
        if (isNightMode) {
            themeFillColor.setValue("#1d1d1d");
            themeBackgroundColor.setValue("#3a3a3a");
            themeAccentColor.setValue("#c0c481");
        } else {
            themeFillColor.setValue("lightgray");
            themeBackgroundColor.setValue("white");
            themeAccentColor.setValue("black");
        }
        // alter draw area colour settings
        drawarea.setTheme();
        // reset combo boxes
        String stroke = strokecombobox.getSelectionModel().getSelectedItem();
        Callback<ListView<String>, ListCell<String>> strokeCellFactory = getCellFactory(isNightMode);
        strokecombobox.setButtonCell(strokeCellFactory.call(null));
        strokecombobox.setCellFactory(strokeCellFactory);
        strokecombobox.getSelectionModel().select(stroke);
        String style = stylecombobox.getSelectionModel().getSelectedItem();
        Callback<ListView<String>, ListCell<String>> styleCellFactory = getCellFactory(isNightMode);
        stylecombobox.setButtonCell(styleCellFactory.call(null));
        stylecombobox.setCellFactory(styleCellFactory);
        stylecombobox.getSelectionModel().select(style);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // set the theme to light
        this.themeFillColor = new SimpleStringProperty("lightgray");
        this.themeBackgroundColor = new SimpleStringProperty("white");
        this.themeAccentColor = new SimpleStringProperty("black");
        this.checkTheme.selectedProperty().bindBidirectional(toggleTheme.selectedProperty());
        this.themeProperty = new ReadOnlyBooleanWrapper();
        this.themeProperty.bindBidirectional(this.checkTheme.selectedProperty());
        /**
         * Property change handler to set/reset night mode on demand
         */
        this.themeProperty.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                setAppStyles(newValue);
                // reset application stylesheets
                application.resetStylesheets(newValue);
                menubutton.fire();
            }
        });
        
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
        colorpicker.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Color c = colorpicker.getValue();
                colorpicker.setStyle(backgroundStyle(c));
                pickerColorProperty.setValue(c);
            }
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
        fillcolorpicker.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Color c = fillcolorpicker.getValue();
                fillcolorpicker.setStyle(backgroundStyle(c));
                pickerFillColorProperty.setValue(c);
            }
        });
        this.fillColorProperty = new ReadOnlyObjectWrapper<>();
        this.fillColorProperty.bindBidirectional(pickerFillColorProperty);

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

    private String backgroundStyle(Color c) {
        return "-fx-background-color: " + toRGBCode(c) + ";";
    }

    // https://stackoverflow.com/a/18803814
    private String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }

    @FXML
    private GridPane appmenu;
    @FXML
    private CheckBox checkTheme;
    @FXML
    private ToggleButton toggleTheme;
    @FXML
    private ColorPicker colorpicker;
    @FXML
    private ColorPicker fillcolorpicker;
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
    @FXML
    private ComboBox<String> strokecombobox;
    @FXML
    private ComboBox<String> stylecombobox;

}
