/*
 * DrawingArea.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.event.FigureHandler;
import net.perspective.draw.event.RotationHandler;
import net.perspective.draw.event.MapHandler;
import net.perspective.draw.event.SelectionHandler;
import net.perspective.draw.event.SketchHandler;
import net.perspective.draw.event.TextHandler;
import net.perspective.draw.event.keyboard.DummyKeyHandler;
import net.perspective.draw.event.keyboard.MapKeyHandler;
import net.perspective.draw.event.keyboard.MoveKeyHandler;
import net.perspective.draw.event.keyboard.KeyListener;
import net.perspective.draw.geom.ArrowType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Edge;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.geom.Text;
import net.perspective.draw.geom.TextFormatter;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.G2;

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.MapItemBehaviour;
import net.perspective.draw.event.behaviours.TextItemBehaviour;
import net.perspective.draw.event.keyboard.TextKeyHandler;

/**
 * 
 * @author ctipper
 */

@Singleton
public class DrawingArea {

    @Inject private Injector injector;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private DrawAreaListener listener;
    @Inject private KeyListener keylistener;
    @Inject private CanvasTransferHandler transferhandler;
    @Inject private FigureFactory figurefactory;
    @Inject private Dropper dropper;
    @Inject private MapController mapper;
    @Inject private G2 g2;
    @Inject private BehaviourContext context;
    private SubScene canvas;
    private Group root;

    private Optional<DrawingType> drawtype;
    private java.awt.Stroke stroke, plainstroke;
    private Color color, fillcolor;
    private String fontfamily;
    private int fontstyle, fontsize;
    private int transparency;
    private ArrowType arrowtype;
    private DrawItem marquee;
    private Grouped guides;
    private boolean rotationMode;
    private boolean gridVisible;
    private boolean darkModeEnabled;
    private boolean multiSelectEnabled;
    private boolean isGuideEnabled;
    private Transferable clipboard;

    private HandlerType handlertype, oldhandlertype;
    private ContextMenu contextmenu;
    private EventHandler<ContextMenuEvent> contextlistener;
    private EventHandler<TouchEvent> popuplistener;
    private EventHandler<InputEvent> arealistener;

    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);
    java.util.List<String> strokeStrings = Arrays.asList("stroke1", "stroke2", "stroke3", "stroke4",
            "stroke5", "stroke6", "stroke7", "stroke8", "stroke9");

    final static double pib12 = Math.PI / 12;

    /**
     * Creates a new instance of <code>DrawingArea</code>
     */
    @Inject
    public DrawingArea() {
    }

    void init(double width, double height) {
        root = new Group();
        canvas = new SubScene(root, width, height);
        canvas.setFill(Color.web(controller.getThemeBackgroundColor()));
        contextmenu = new ContextMenu();
        contextlistener = null;
        view.setDrawingListener();
        view.enableRichText(false);
        this.prepareDrawing();
        this.setDrawType(DrawingType.SKETCH);
        this.arrowtype = ArrowType.NONE;
        listener.initializeHandlers(canvas);
        view.setEditing(KeyHandlerType.MOVE);
        this.addContextMenu();
        this.handlertype = HandlerType.SELECTION;
        this.changeHandlers(HandlerType.SELECTION);
        this.gridVisible = false;
        guides = new Grouped();
        figurefactory = injector.getInstance(FigureFactory.class);
        controller.getStrokeTypeProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            /**
             * Set stroke type
             */
            Integer strokeId = strokeStrings.indexOf(newValue);
            String strokeStyle = controller.getStrokeStyleProperty().getValue();
            setStrokeType(strokeId, strokeStyle);
        });
        controller.getStrokeStyleProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            /**
             * Set stroke style
             */
            String strokeType = controller.getStrokeTypeProperty().getValue();
            Integer strokeId = strokeStrings.indexOf(strokeType);
            setStrokeType(strokeId, newValue);
        });
        controller.getColorProperty().addListener((ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
            setColor(newValue);
        });
        controller.getFillColorProperty().addListener((ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
            setFillColor(newValue);
        });
        controller.getDropperEnabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            /**
             * Activate dropper tool
             */
            if (oldValue && !newValue) {
                if (oldhandlertype.equals(HandlerType.SELECTION)) {
                    changeHandlers(handlertype);
                } else {
                    changeHandlers(oldhandlertype);
                }
                canvas.setCursor(Cursor.DEFAULT);
                controller.setStatusMessage("Dropper tool off");
            } else if (!oldValue && newValue) {
                changeHandlers(HandlerType.SELECTION);
                canvas.setCursor(Cursor.HAND);
                controller.setStatusMessage("Dropper tool selected");
            }
        });
        controller.getFontFamilyProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            this.setFontFamily(newValue);
            view.moveSelection(view.getSelected());
        });
        controller.getFontSizeProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            this.setFontSize(Integer.parseInt(newValue));
            view.moveSelection(view.getSelected());
        });
        controller.getBoldProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                this.updateFontStyle(this.getFontStyle() | TextFormatter.FONT_BOLD);
            } else {
                this.updateFontStyle(this.getFontStyle() ^ TextFormatter.FONT_BOLD);
            }
            view.moveSelection(view.getSelected());
            });
        controller.getItalicProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                this.updateFontStyle(this.getFontStyle() | TextFormatter.FONT_ITALIC);
            } else {
                this.updateFontStyle(this.getFontStyle() ^ TextFormatter.FONT_ITALIC);
            }
            view.moveSelection(view.getSelected());
        });
        controller.getUnderlinedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                this.updateFontStyle(this.getFontStyle() | TextFormatter.FONT_UNDERLINED);
            } else {
                this.updateFontStyle(this.getFontStyle() ^ TextFormatter.FONT_UNDERLINED);
            }
            view.moveSelection(view.getSelected());
        });
    }

    /**
     * Clear the document and prepare the View
     */
    public void prepareDrawing() {
        Integer strokeId = strokeStrings.indexOf(controller.getStrokeTypeProperty().getValue());
        String strokeStyle = controller.getStrokeStyleProperty().getValue();
        setStrokeType(strokeId, strokeStyle);
        color = controller.getColorProperty().getValue();
        fillcolor = controller.getFillColorProperty().getValue();
        fontfamily = "Serif";
        fontsize = 14;
        fontstyle = java.awt.Font.PLAIN;
        transparency = controller.getOutlineWhen().then(0).otherwise(100).intValue();
        view.clearView();
        this.clear();
    }

    /**
     * Set the canvas colour
     */
    public void setTheme(){
        canvas.setFill(Color.web(controller.getCanvasBackgroundColor()));
        view.moveSelection(view.getSelected());
    }

    /**
     * Get the theme fill colour
     * 
     * @return web colour
     */
    public String getThemeFillColor() {
        return controller.getThemeFillColor();
    }

    /**
     * Get the theme background colour
     * 
     * @return web colour
     */
    public String getThemeBackgroundColor() {
        return controller.getThemeBackgroundColor();
    }

    /**
     * Get the theme accent colour
     * 
     * @return web colour
     */
    public String getThemeAccentColor() {
        return controller.getThemeAccentColor();
    }

    /**
     * Get the canvas background colour
     * 
     * @return web colour
     */
    public String getCanvasBackgroundColor() {
        return controller.getCanvasBackgroundColor();
    }

    /**
     * Get the scene
     * 
     * @return the {@link javafx.scene.SubScene}
     */
    public SubScene getScene() {
        return canvas;
    }

    /**
     * Get the canvas root
     * 
     * @return the root node
     */
    public Group getCanvas() {
        return root;
    }

    /**
     * Clear the canvas
     */
    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
        redrawGrid();
    }

    /**
     * Set mouse and touch handlers
     * 
     * @param handler the {@link net.perspective.draw.enums.HandlerType}
     */
    public void changeHandlers(HandlerType handler) {
        this.oldhandlertype = this.handlertype;
        this.handlertype = handler;
        this.resetContextHandlers();
        switch (handler) {
            case SELECTION -> {
                listener.setEventHandler(injector.getInstance(SelectionHandler.class));
                this.setRotationMode(false);
                this.setContextHandlers();
                mapper.finaliseMap();
            }
            case FIGURE -> {
                listener.setEventHandler(injector.getInstance(FigureHandler.class));
                this.setRotationMode(false);
                mapper.finaliseMap();
                view.setSelected(-1);
            }
            case ROTATION -> {
                listener.setEventHandler(injector.getInstance(RotationHandler.class));
                this.setRotationMode(true);
                this.setContextHandlers();
                mapper.finaliseMap();
                view.setSelected(-1);
            }
            case SKETCH -> {
                listener.setEventHandler(injector.getInstance(SketchHandler.class));
                this.setRotationMode(false);
                mapper.finaliseMap();
                view.setSelected(-1);
            }
            case TEXT -> {
                listener.setEventHandler(injector.getInstance(TextHandler.class));
                this.setRotationMode(false);
                this.setContextHandlers();
                mapper.finaliseMap();
                view.setSelected(-1);
            }
            case MAP -> listener.setEventHandler(injector.getInstance(MapHandler.class));
            default -> {
                listener.setEventHandler(injector.getInstance(SelectionHandler.class));
                this.setRotationMode(false);
                this.setContextHandlers();
                mapper.finaliseMap();
                view.setSelected(-1);
            }
        }
        view.setDrawing(false);
    }

    /**
     * Set keyboard handlers
     * 
     * @param handler the {@link net.perspective.draw.enums.KeyHandlerType}
     */
    public void setKeyboardHandler(KeyHandlerType handler) {
        switch (handler) {
            case TEXT -> keylistener.setEventHandler(injector.getInstance(TextKeyHandler.class));
            case MOVE -> keylistener.setEventHandler(injector.getInstance(MoveKeyHandler.class));
            case MAP -> keylistener.setEventHandler(injector.getInstance(MapKeyHandler.class));
            default -> keylistener.setEventHandler(injector.getInstance(DummyKeyHandler.class));
        }
    }

    /**
     * Return the mouse handler type
     * 
     * @return the {@link net.perspective.draw.enums.HandlerType}
     */
    public HandlerType getHandlerType() {
        return handlertype;
    }

    /**
     * Reset the listeners
     */
    public void resetContextHandlers() {
        canvas.setOnContextMenuRequested(null);
        canvas.setOnTouchStationary(null);
        canvas.setOnMousePressed(null);
        canvas.setOnTouchPressed(null);
    }

    /**
     * Set the listeners
     */
    public void setContextHandlers() {
        canvas.setOnContextMenuRequested(contextlistener);
        canvas.setOnTouchStationary(popuplistener);
        canvas.setOnMousePressed(arealistener);
        canvas.setOnTouchPressed(arealistener);
    }

    /**
     * Set up the context menu
     */
    public void addContextMenu() {
        MenuItem menuCut = new MenuItem("Cut");
        menuCut.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                clipboard = transferhandler.createTransferable();
                transferhandler.exportDone(clipboard, MOVE);
                view.setSelected(-1);
            }
        });
        MenuItem menuCopy = new MenuItem("Copy");
        menuCopy.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                clipboard = transferhandler.createTransferable();
                transferhandler.exportDone(clipboard, COPY);
            }
        });
        MenuItem menuPaste = new MenuItem("Paste");
        menuPaste.setOnAction((ActionEvent e) -> {
            if (clipboard != null) {
                transferhandler.importData(clipboard);
                view.setSelected(-1);
            }
        });
        MenuItem menuTextCut = new MenuItem("Cut");
        menuTextCut.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                DrawItem item = view.getDrawings().get(view.getSelected());
                if (item instanceof Text) {
                    view.cutTextItem();
                    ((Text) item).setDimensions();
                    view.updateSelectedItem();
                    view.moveSelection(view.getSelected());
                }
            }
        });
        MenuItem menuTextCopy = new MenuItem("Copy");
        menuTextCopy.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                DrawItem item = view.getDrawings().get(view.getSelected());
                if (item instanceof Text) {
                    view.copyTextItem();
                    ((Text) item).setDimensions();
                    view.updateSelectedItem();
                    view.moveSelection(view.getSelected());
                }
            }
        });
        MenuItem menuTextPaste = new MenuItem("Paste");
        menuTextPaste.setOnAction((ActionEvent e) -> {
                DrawItem item = view.getDrawings().get(view.getSelected());
                if (item instanceof Text) {
                    view.pasteTextItem();
                    ((Text) item).setDimensions();
                    view.updateSelectedItem();
                    view.moveSelection(view.getSelected());
                }
        });
        // contextmenu.getItems().addAll(menuCut, menuCopy, menuPaste);
        SeparatorMenuItem groupSeparator = new SeparatorMenuItem();
        MenuItem menuGroup = new MenuItem("Group Selection");
        menuGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.groupSelection();
            }
        });
        MenuItem menuUnGroup = new MenuItem("Ungroup Selection");
        menuUnGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.ungroupSelection();
            }
        });
        // contextmenu.getItems().addAll(groupSeparator, menuGroup, menuUnGroup);
        SeparatorMenuItem sendSeparator = new SeparatorMenuItem();
        MenuItem menuSBItem = new MenuItem("Send Backwards");
        menuSBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.sendBackwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuBFItem = new MenuItem("Bring Forwards");
        menuBFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.bringForwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuSTBItem = new MenuItem("Send to Back");
        menuSTBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.sendToBack();
                view.setSelected(-1);
            }
        });
        MenuItem menuBTFItem = new MenuItem("Bring to Front");
        menuBTFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                view.bringToFront();
                view.setSelected(-1);
            }
        });
        // contextmenu.getItems().addAll(sendSeparator, menuSBItem, menuBFItem, menuSTBItem, menuBTFItem);
        MenuItem menuEditMapItem = new MenuItem("Edit");
        menuEditMapItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                editMapItem();
            }
        });
        MenuItem menuEditTextItem = new MenuItem("Edit");
        menuEditTextItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                editTextItem();
            }
        });
        // contextmenu.getItems().addAll(menuEditMapItem, editSeparator);
        SeparatorMenuItem editSeparator = new SeparatorMenuItem();
        contextlistener = (ContextMenuEvent event) -> {
            if (view.getSelected() != -1 && !view.isMapping() && !view.isEditing()) {
                if (view.getDrawings().get(view.getSelected()) instanceof StreetMap) {
                    contextmenu.getItems().clear();
                    contextmenu.getItems().addAll(menuEditMapItem, editSeparator);
                } else if (view.getDrawings().get(view.getSelected()) instanceof Text) {
                    contextmenu.getItems().clear();
                    contextmenu.getItems().addAll(menuEditTextItem, editSeparator);
                }
            } else {
                contextmenu.getItems().clear();
            }
            if (view.getSelected() != -1 && !view.isMapping() && view.isEditing()) {
                if (view.getDrawings().get(view.getSelected()) instanceof Text) {
                    contextmenu.getItems().addAll(menuTextCut, menuTextCopy, menuTextPaste);
                }
            } else {
                contextmenu.getItems().addAll(menuCut, menuCopy, menuPaste);
            }
            contextmenu.getItems().addAll(groupSeparator, menuGroup, menuUnGroup);
            contextmenu.getItems().addAll(sendSeparator, menuSBItem, menuBFItem, menuSTBItem, menuBTFItem);
            contextmenu.show(canvas, event.getScreenX(), event.getScreenY());
        };
        popuplistener = (TouchEvent event) -> {
            if (view.getSelected() != -1 && !view.isMapping() && view.getDrawings().get(view.getSelected()) instanceof StreetMap) {
                contextmenu.getItems().clear();
                contextmenu.getItems().addAll(menuEditMapItem, editSeparator);
            } else {
                contextmenu.getItems().clear();
            }
            contextmenu.getItems().addAll(menuCut, menuCopy, menuPaste);
            contextmenu.getItems().addAll(groupSeparator, menuGroup, menuUnGroup);
            contextmenu.getItems().addAll(sendSeparator, menuSBItem, menuBFItem, menuSTBItem, menuBTFItem);
            TouchPoint touch = event.getTouchPoints().get(0);
            contextmenu.show(canvas, touch.getScreenX(), touch.getScreenY());
        };
        arealistener = (InputEvent event) -> {
            if (contextmenu.isShowing()) {
                contextmenu.hide();
            }
        };
    }

    /**
     * Initiate map edit mode
     */
    protected void editMapItem() {
        if (view.getSelected() != -1) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            if (item instanceof StreetMap) {
                context.setBehaviour(injector.getInstance(MapItemBehaviour.class));
                context.edit(item, view.getSelected());
            }
        }
    }
 
    /**
     * Initiate text edit mode
     */
    protected void editTextItem() {
        if (view.getSelected() != -1) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            if (item instanceof Text) {
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                context.edit(item, view.getSelected());
                ((Text) item).setDimensions();
                view.updateSelectedItem();
                view.moveSelection(view.getSelected());
            }
        }
    }

    private void setStrokeType(Integer strokeId, String strokeStyle) {
        setStroke(dropper.selectStroke(strokeId, strokeStyle));
        switch (strokeStyle) {
            case "style6" -> // Arrow at start
                setArrow(ArrowType.END);
            case "style7" -> // Arrow at start
                setArrow(ArrowType.END);
            case "style8" -> // Arrow at both ends
                setArrow(ArrowType.BOTH);
            case "style9" -> // Arrow at both ends
                setArrow(ArrowType.BOTH);
            default -> resetArrow();
        }
        setPlainStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    /**
     * Get the item text
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @return the {@link java.awt.font.TextLayout}
     */
    public TextFlow getTextLayout(DrawItem item) {
        TextFlow layout = ((Text) item).getLayout();
        return layout;
    }

    /**
     * Up align shape to grid
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void snapUp(DrawItem item) {
        double y = item.getTop()[0].getY();
        double yinc = ((((int) ((y / 10) + 0.5)) - 1) * 10.0) - y;
        item.moveTo(0, yinc);
    }

    /**
     * Down align shape to grid
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void snapDown(DrawItem item) {
        double y = item.getTop()[0].getY();
        double yinc = ((((int) ((y / 10) + 0.5)) + 1) * 10.0) - y;
        item.moveTo(0, yinc);
    }

    /**
     * Left align shape to grid
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void snapLeft(DrawItem item) {
        double x = item.getTop()[0].getX();
        double xinc = ((((int) ((x / 10) + 0.5)) - 1) * 10.0) - x;
        item.moveTo(xinc, 0);
    }

    /**
     * Right align shape to grid
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void snapRight(DrawItem item) {
        double x = item.getTop()[0].getX();
        double xinc = ((((int) ((x / 10) + 0.5)) + 1) * 10.0) - x;
        item.moveTo(xinc, 0);
    }

    /**
     * Move shape and steer to grid increments
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @param xinc x increment
     * @param yinc y increment
     */
    public void moveToWithIncrements(DrawItem item, double xinc, double yinc) {
        /**
         * x adjustment
         */
        double x = item.getTop()[0].getX();
        // actual incremental offset
        double inc_xa = Math.round(xinc / 10) * 10.0;
        // corrected incremental offset
        double inc_xc = x - Math.round(x / 10) * 10.0;
        /**
         * y adjustment
         */
        double y = item.getTop()[0].getY();
        // actual incremental offset
        double inc_ya = Math.round(yinc / 10) * 10.0;
        // corrected incremental offset
        double inc_yc = y - Math.round(y / 10) * 10.0;
        item.moveTo(inc_xa - inc_xc, inc_ya - inc_yc);
    }

    /**
     * Rotate anti-clockwise by 15°
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void rotateLeft(DrawItem item) {
        double angle = item.getAngle();
        double theta = ((((int) (Math.signum(angle) * 0.5)) - 1) * pib12);
        double zeta = (Math.round(angle / pib12) * pib12) + theta;
        this.rotateTo(item, zeta - angle);
    }

    /**
     * Rotate clockwise by 15°
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     */
    public void rotateRight(DrawItem item) {
        double angle = item.getAngle();
        double theta = ((((int) (Math.signum(angle) * 0.5)) + 1) * pib12);
        double zeta = (Math.round(angle / pib12) * pib12) + theta;
        this.rotateTo(item, zeta - angle);
    }

    /**
     * Rotate a given item by a forced increment
     * 
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @param theta angle increment
     */
    public void rotateWithIncrements(DrawItem item, double theta) {
        double angle = item.getAngle();
        // actual incremental offset
        double inc_th = Math.round(theta / pib12) * pib12;
        // corrected incremental offset
        double zeta = angle - Math.round(angle / pib12) * pib12;
        this.rotateTo(item, inc_th - zeta);
    }
    
    /**
     * Rotate given item by angle theta increment
     *
     * @param item the {@link net.perspective.draw.geom.DrawItem}
     * @param theta angle increment
     */
    public void rotateTo(DrawItem item, double theta) {
        if (((item instanceof Figure) && (!((Figure) item).getType().equals(FigureType.LINE)))
                || (item instanceof Text)
                || (item instanceof Grouped)
                || (item instanceof Picture)) {
            double angle = item.getAngle();
            double zeta = angle + theta;
            // normalise angle
            if (zeta > Math.PI) {
                item.setAngle(zeta - 2 * Math.PI);
            } else if (zeta < -Math.PI) {
                item.setAngle(zeta + 2 * Math.PI);
            } else {
                item.setAngle(zeta);
            }
        } else if (item instanceof Figure && ((Figure) item).getType().equals(FigureType.LINE)) {
            // manipulate lines directly
            CanvasPoint s = this.rotatePoint((Figure) item, item.getStart(), theta);
            CanvasPoint e = this.rotatePoint((Figure) item, item.getEnd(), theta);
            item.setStart(s.x, s.y);
            item.setEnd(e.x, e.y);
            ((Figure) item).setPoints(DrawingType.LINE);
            ((Figure) item).setPath();
        }
    }

    /**
     * Rotate a point around figure axis by an angle
     *
     * @param figure the {@link net.perspective.draw.geom.Figure}
     * @param p the {@link net.perspective.draw.util.CanvasPoint}
     * @param angle the angle in radians
     * @return the {@link net.perspective.draw.util.CanvasPoint}
     */
    protected CanvasPoint rotatePoint(Figure figure, CanvasPoint p, double angle) {
        CanvasPoint centre = figure.rotationCentre();
        CanvasPoint point = new CanvasPoint(p.x, p.y);
        point.translate(-centre.x, -centre.y);
        if (angle != 0) {
            // rotate point about centroid
            point.rotate(angle);
        }
        point.translate(centre.x, centre.y);
        return point;
    }

    /**
     * Set the drawing mode
     * 
     * @param type the {@link net.perspective.draw.enums.DrawingType}
     */
    public void setDrawType(DrawingType type) {
        this.drawtype = Optional.ofNullable(type);
    }

    /**
     * Return the canvas drawing type and also allows a 
     * correction if isometric drawing is enabled
     * 
     * @return optional of {@link net.perspective.draw.enums.DrawingType}
     */
    public Optional<DrawingType> getDrawType() {
        if (!drawtype.isPresent()) {
            // certain handlers set null for no draw
            return Optional.empty();
        }
        DrawingType type = drawtype.get();
        if (controller.getOneToOneEnabled()) {
            switch (type) {
                case CIRCLE -> {
                }
                case ELLIPSE -> type = DrawingType.CIRCLE;
                case SQUARE -> {
                }
                case RECTANGLE -> type = DrawingType.SQUARE;
                case TRIANGLE -> {
                }
                case ISOSCELES -> type = DrawingType.TRIANGLE;
                case HEXAGON -> type = DrawingType.ISOHEX;
                case ISOHEX -> {
                }
                default -> {
                }
            }
        }
        return Optional.of(type);
    }

    /**
     * Set the stroke type
     * 
     * @param stroke the {@link java.awt.Stroke}
     */
    public void setStroke(java.awt.Stroke stroke) {
        this.stroke = stroke;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the stroke type
     * 
     * @return the {@link java.awt.Stroke}
     */
    public java.awt.Stroke getStroke() {
        return this.stroke;
    }

    /**
     * Set the basic stroke type
     * This is used when drawing, usually un-tinted
     * 
     * @param stroke the {@link java.awt.Stroke}
     */
    public void setPlainStroke(java.awt.Stroke stroke) {
        this.plainstroke = stroke;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the basic stroke type
     * 
     * @return the {@link java.awt.Stroke}
     */
    public java.awt.Stroke getPlainStroke() {
        return plainstroke;
    }

    /**
     * Set the stroke colour
     * 
     * @param color the {@link javafx.scene.paint.Color}
     */
    public void setColor(Color color) {
        this.color = color;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the stroke colour
     * 
     * @return the {@link javafx.scene.paint.Color}
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor the {@link javafx.scene.paint.Color}
     */
    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the fill colour
     * 
     * @return the {@link javafx.scene.paint.Color}
     */
    public Color getFillColor() {
        return this.fillcolor;
    }

    /**
     * Set transparency 0-100
     * 
     * @param transparency transparency 0 (clear) - 100 (opaque)
     */
    public void setTransparency(int transparency) {
        this.transparency = transparency;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the transparency
     * 
     * @return transparency 0 (clear) - 100 (opaque)
     */
    public int getTransparency() {
        return this.transparency;
    }

    /**
     * Set the text font
     * 
     * @param fontfamily
     */
    public void setFontFamily(String fontfamily) {
        this.fontfamily = fontfamily;
        if (controller.getDropperDisabled()) {
            view.updateSelectedItem();
        }
    }

    /**
     * Get the text font
     * 
     * @return font family
     */
    public String getFontFamily() {
        return fontfamily;
    }

    /**
     * Update the font style
     * 
     * @param fontstyle font style Id
     */
    public void updateFontStyle(int fontstyle) {
        this.fontstyle = fontstyle;
        if (controller.getDropperDisabled()) {
            view.updateSelectedItem();
        }
    }

    /**
     * Set the font style
     * 
     * @param fontstyle font style Id
     */
    public void setFontStyle(int fontstyle) {
        this.fontstyle = fontstyle;
    }

    /**
     * Get the font style
     * 
     * @return font style Id
     */
    public int getFontStyle() {
        return fontstyle;
    }

    /**
     * Set the font size
     * 
     * @param fontsize font size
     */
    public void setFontSize(int fontsize) {
        this.fontsize = fontsize;
        if (controller.getDropperDisabled()) {
            view.updateSelectedItem();
        }
    }

    /**
     * Get the font size
     * 
     * @return font size
     */
    public int getFontSize() {
        return fontsize;
    }

    /**
     * Set the arrow type
     * 
     * @param arrowtype the {@link net.perspective.draw.geom.ArrowType}
     */
    public void setArrow(ArrowType arrowtype) {
        this.arrowtype = arrowtype;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }
    
    /**
     * Get the arrow type
     * 
     * @return the {@link net.perspective.draw.geom.ArrowType}
     */
    public ArrowType getArrow() {
        return arrowtype;
    }

    /**
     * Set arrow to NONE
     */
    public void resetArrow() {
        arrowtype = ArrowType.NONE;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Show drawing grid
     * 
     * @param gridVisible is grid visible
     */
    public void setGrid(boolean gridVisible) {
        this.gridVisible = gridVisible;
    }

    /**
     * Grid is visible
     * 
     * @return is grid visible
     */
    public boolean isGridVisible() {
        return gridVisible;
    }

    /**
     * Enable snap to guides
     * 
     * @param gridVisible is grid active
     */
    public void setSnapTo(boolean gridVisible) {
        listener.setSnapEnabled(gridVisible);
    }

    /**
     * Draw the grid
     */
    public void redrawGrid() {
        ObservableList<Node> nodes = getCanvas().getChildren();
        List<Node> removal = new ArrayList<>();
        for (Node node : nodes) {
            String prop = node.idProperty().get();
            if (prop != null && prop.startsWith("grid")) {
                removal.add(node);
            }
        }
        for (Node node : removal) {
            nodes.remove(node);
        }
        if (isGridVisible()) {
            CanvasPoint bounds = new CanvasPoint(getScene().getWidth(), getScene().getHeight());
            Node gridrea = g2.drawGridLayout(bounds);
            nodes.add(0, gridrea);
        }
    }

    /**
     * Add a guide to canvas
     * 
     * @param xy false is x true is y
     * @param p an xy value
     */
    public void addGuide(boolean xy, Double p) {
        if (xy) {
            // horizontal rule
            Edge guide = (Edge) figurefactory.createFigure(DrawingType.LINE);
            guide.setStart(0, p);
            guide.setEnd(getScene().getWidth(), p);
            guide.setPoints(DrawingType.LINE);
            guide.setEndPoints();
            guide.setPath();
            guide.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            guide.setColor(Color.rgb(204, 102, 255));
            guide.setFillColor(Color.rgb(48, 96, 255));
            guide.setTransparency(25);
            guides.addShape(guide);
        } else {
            // vertical rule
            Edge guide = (Edge) figurefactory.createFigure(DrawingType.LINE);
            guide.setStart(p, 0);
            guide.setEnd(p, getScene().getHeight());
            guide.setPoints(DrawingType.LINE);
            guide.setEndPoints();
            guide.setPath();
            guide.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            guide.setColor(Color.rgb(204, 102, 255));
            guide.setFillColor(Color.rgb(48, 96, 255));
            guide.setTransparency(25);
            guides.addShape(guide);
        }
    }

    /**
     * Get the guides
     * 
     * @return a {@link net.perspective.draw.geom.Grouped}
     */
    public Grouped getGuides() {
        return guides;
    }

    /**
     * Clear the list of guides
     */
    public void resetGuides() {
        guides = new Grouped();
    }

    /**
     * Set dark mode enabled
     * 
     * @param darkModeEnabled dark theme
     */
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    /**
     * Is dark mode enabled
     * 
     * @return dark theme
     */
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    /**
     * Activate alignment guides
     * 
     * @param isGuideEnabled 
     */
    public void setGuideEnabled(boolean isGuideEnabled) {
        this.isGuideEnabled = isGuideEnabled;
    }

    /**
     * Alignment guides are enabled
     * 
     * @return isGuideEnabled guides are activated
     */
    public boolean isGuideEnabled() {
        return isGuideEnabled;
    }

    /**
     * Define the drawing loup
     * 
     * @param marquee the loup
     */
    public void setMarquee(DrawItem marquee) {
        this.marquee = marquee;
    }

    /**
     * Get the drawing loup
     * 
     * @return the loup
     */
    public DrawItem getMarquee() {
        return marquee;
    }

    /**
     * Set rotation mode
     * 
     * @param rotationMode is rotating
     */
    public void setRotationMode(boolean rotationMode) {
        this.rotationMode = rotationMode;
    }

    /**
     * Is rotation mode enabled
     * 
     * @return is rotating
     */
    public boolean isRotationMode() {
        return rotationMode;
    }

    /**
     * Set multiple selection mode
     * 
     * @param multiSelectEnabled is multi-select
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        this.multiSelectEnabled = multiSelectEnabled;
    }

    /**
     * Get multiple selection mode
     * 
     * @return is multi-select
     */
    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

}
