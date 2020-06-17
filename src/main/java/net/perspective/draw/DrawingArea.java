/*
 * DrawingArea.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.event.FigureHandler;
import net.perspective.draw.event.RotationHandler;
import net.perspective.draw.event.SelectionHandler;
import net.perspective.draw.event.SketchHandler;
import net.perspective.draw.geom.ArrowType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.util.CanvasPoint;

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;

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
    @Inject private CanvasTransferHandler transferhandler;
    @Inject private Dropper dropper;
    private SubScene canvas;
    private Group root;

    private DrawingType drawtype;
    private java.awt.Stroke stroke, plainstroke;
    private Color color, fillcolor;
    private int transparency;
    private ArrowType arrowtype;
    private DrawItem marquee;
    private boolean multiSelectEnabled;
    private Transferable clipboard;

    private HandlerType handlertype, oldhandlertype;
    private ContextMenu contextmenu;
    private EventHandler<ContextMenuEvent> contextlistener;
    private EventHandler<TouchEvent> popuplistener;
    private EventHandler<InputEvent> arealistener;

    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);
    java.util.List<String> strokeStrings = Arrays.asList("stroke1", "stroke2", "stroke3", "stroke4",
            "stroke5", "stroke6", "stroke7", "stroke8", "stroke9");

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
        this.prepareDrawing();
        this.setDrawType(DrawingType.SKETCH);
        this.arrowtype = ArrowType.NONE;
        listener.initializeHandlers(canvas);
        this.addContextMenu();
        this.handlertype = HandlerType.SELECTION;
        this.changeHandlers(HandlerType.SELECTION);
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
    }

    /**
     * Clear the document and prepare the View
     */
    public void prepareDrawing() {
        Integer strokeId = strokeStrings.indexOf(controller.getStrokeTypeProperty().getValue());
        String strokeStyle = controller.getStrokeStyleProperty().getValue();
        setStrokeType(strokeId, strokeStyle);
        this.color = controller.getColorProperty().getValue();
        this.fillcolor = controller.getFillColorProperty().getValue();
        this.transparency = controller.getOutlineWhen().then(0).otherwise(100).intValue();
        view.clearView();
        this.clear();
    }

    /**
     * Set the canvas colour
     */
    public void setTheme(){
        canvas.setFill(Color.web(controller.getThemeBackgroundColor()));
        view.setSelected(-1);
    }

    /**
     * Get the theme fill colour
     * 
     * @return
     */
    public String getThemeFillColor() {
        return controller.getThemeFillColor();
    }

    /**
     * Get the theme background colour
     * 
     * @return
     */
    public String getThemeBackgroundColor() {
        return controller.getThemeBackgroundColor();
    }

    /**
     * Get the theme accent colour
     * 
     * @return
     */
    public String getThemeAccentColor() {
        return controller.getThemeAccentColor();
    }

    /**
     * Get the scene
     * 
     * @return
     */
    public SubScene getScene() {
        return canvas;
    }

    /**
     * Get the canvas root
     * 
     * @return
     */
    public Group getCanvas() {
        return root;
    }

    /**
     * Clear the canvas
     */
    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
    }

    /**
     * Set mouse and touch handlers
     * 
     * @param handler  HandlerType
     */
    public void changeHandlers(HandlerType handler) {
        this.oldhandlertype = this.handlertype;
        this.handlertype = handler;
        this.resetContextHandlers();
        switch (handler) {
            case SELECTION:
                listener.setEventHandler(injector.getInstance(SelectionHandler.class));
                this.setContextHandlers();
                break;
            case FIGURE:
                listener.setEventHandler(injector.getInstance(FigureHandler.class));
                break;
            case ROTATION:
                listener.setEventHandler(injector.getInstance(RotationHandler.class));
                this.setContextHandlers();
                break;
            case SKETCH:
                listener.setEventHandler(injector.getInstance(SketchHandler.class));
                break;
            default:
                break;
        }
        view.setSelected(-1);
        view.setDrawing(false);
    }

    /**
     * Return the mouse handler type
     * 
     * @return  HandlerType
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
        contextmenu.getItems().addAll(menuCut, menuCopy, menuPaste);
        SeparatorMenuItem groupSeparator = new SeparatorMenuItem();
        MenuItem menuGroup = new MenuItem("Group Selection");
        menuGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.groupSelection();
            }
        });
        MenuItem menuUnGroup = new MenuItem("Ungroup Selection");
        menuUnGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.ungroupSelection();
            }
        });
        contextmenu.getItems().addAll(groupSeparator, menuGroup, menuUnGroup);
        SeparatorMenuItem sendSeparator = new SeparatorMenuItem();
        MenuItem menuSBItem = new MenuItem("Send Backwards");
        menuSBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.sendBackwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuBFItem = new MenuItem("Bring Forwards");
        menuBFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.bringForwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuSTBItem = new MenuItem("Send to Back");
        menuSTBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.sendToBack();
                view.setSelected(-1);
            }
        });
        MenuItem menuBTFItem = new MenuItem("Bring to Front");
        menuBTFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.bringToFront();
                view.setSelected(-1);
            }
        });
        contextmenu.getItems().addAll(sendSeparator, menuSBItem, menuBFItem, menuSTBItem, menuBTFItem);
        contextlistener = (ContextMenuEvent event) -> {
            contextmenu.show(canvas, event.getScreenX(), event.getScreenY());
        };
        popuplistener = (TouchEvent event) -> {
            TouchPoint touch = event.getTouchPoints().get(0);
            contextmenu.show(canvas, touch.getScreenX(), touch.getScreenY());
        };
        arealistener = (InputEvent event) -> {
            if (contextmenu.isShowing()) {
                contextmenu.hide();
            }
        };
    }

    private void setStrokeType(Integer strokeId, String strokeStyle) {
        setStroke(dropper.selectStroke(strokeId, strokeStyle));
        switch (strokeStyle) {
            case "style6": // Arrow at start
                setArrow(ArrowType.END);
                break;
            case "style7": // Arrow at start
                setArrow(ArrowType.END);
                break;
            case "style8": // Arrow at both ends
                setArrow(ArrowType.BOTH);
                break;
            case "style9": // Arrow at both ends
                setArrow(ArrowType.BOTH);
                break;
            default:
                resetArrow();
        }
        setPlainStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    /**
     * Rotate given item by angle theta increment
     *
     * @param item DrawItem
     * @param theta angle increment
     */
    public void rotateTo(DrawItem item, double theta) {
        if (((item instanceof Figure) && (!((Figure) item).getType().equals(FigureType.LINE)))
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
     * @param figure
     * @param p
     * @param angle
     * @return
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
     * @param type  DrawType
     */
    public void setDrawType(DrawingType type) {
        drawtype = type;
    }

    /**
     * Return the canvas drawing type and also allows a 
     * correction if proportional drawing is enabled
     * 
     * @return 
     */
    public DrawingType getDrawType() {
        DrawingType type = this.drawtype;
        if (controller.getOneToOneEnabled()) {
            switch (drawtype) {
                case CIRCLE:
                    break;
                case ELLIPSE:
                    type = DrawingType.CIRCLE;
                    break;
                case SQUARE:
                    break;
                case RECTANGLE:
                    type = DrawingType.SQUARE;
                    break;
                case TRIANGLE:
                    break;
                case ISOSCELES:
                    type = DrawingType.TRIANGLE;
                    break;
                case HEXAGON:
                    type = DrawingType.ISOHEX;
                    break;
                case ISOHEX:
                    break;
                default:
                    break;

            }
        }
        return type;
    }

    /**
     * Set the stroke type
     * 
     * @param stroke
     */
    public void setStroke(java.awt.Stroke stroke) {
        this.stroke = stroke;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the stroke type
     * 
     * @return
     */
    public java.awt.Stroke getStroke() {
        return this.stroke;
    }

    /**
     * Set the basic stroke type
     * This is used when drawing, usually un-tinted
     * 
     * @param stroke
     */
    public void setPlainStroke(java.awt.Stroke stroke) {
        this.plainstroke = stroke;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the basic stroke type
     * 
     * @return
     */
    public java.awt.Stroke getPlainStroke() {
        return plainstroke;
    }

    /**
     * Set the stroke colour
     * 
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the stroke colour
     * 
     * @return
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Set the fill colour
     * 
     * @param fillcolor
     */
    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the fill colour
     * 
     * @return
     */
    public Color getFillColor() {
        return this.fillcolor;
    }

    /**
     * Set transparency 0-100
     * 
     * @param transparency
     */
    public void setTransparency(int transparency) {
        this.transparency = transparency;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }

    /**
     * Get the transparency
     * 
     * @return
     */
    public int getTransparency() {
        return this.transparency;
    }

    /**
     * Set the arrow type
     * 
     * @param arrowtype
     */
    public void setArrow(ArrowType arrowtype) {
        this.arrowtype = arrowtype;
        if (controller.getDropperDisabled())
            view.updateSelectedItem();
    }
    
    /**
     * Get the arrow type
     * 
     * @return
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
     * Define the drawing loup
     * 
     * @param marquee
     */
    public void setMarquee(DrawItem marquee) {
        this.marquee = marquee;
    }

    /**
     * Get the drawing loup
     * 
     * @return
     */
    public DrawItem getMarquee() {
        return marquee;
    }

    /**
     * Set multiple selection mode
     * 
     * @param multiSelectEnabled
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        this.multiSelectEnabled = multiSelectEnabled;
    }

    /**
     * Get multiple selection mode
     * 
     * @return
     */
    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

}
