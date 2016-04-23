/*
 * DrawingArea.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.*;

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
    private SubScene canvas;
    private Group root;
    private Handler handler;

    private DrawingType drawtype;
    private Stroke stroke;
    private Color color, fillcolor;
    private int transparency;
    private double startX, startY;
    private double tempX, tempY;
    
    private Transferable clipboard;
    @Inject private CanvasTransferHandler transferhandler;

    private ContextMenu contextmenu;
    private EventHandler<ContextMenuEvent> contextlistener;
    private EventHandler<TouchEvent> popuplistener;
    private EventHandler<InputEvent> arealistener;

    /**
     * Creates a new instance of <code>DrawingArea</code>
     */
    @Inject
    public DrawingArea() {
    }

    void init(double width, double height) {
        root = new Group();
        canvas = new SubScene(root, width, height);
        canvas.setFill(Color.WHITE);
        contextmenu = new ContextMenu();
        contextlistener = null;
        view.setDrawingListener();
        this.prepareDrawing();
        this.setDrawType(DrawingType.SKETCH);
        this.changeHandlers(HandlerType.SKETCH);
        this.initializeHandlers();
    }

    public void prepareDrawing() {
        this.stroke = new BasicStroke(6.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        this.color = Color.web("#4860E0");
        this.fillcolor = Color.web("#4860E0");
        this.transparency = controller.getWireframe().then(0).otherwise(100).intValue();
        view.clearView();
        this.clear();
    }
    
    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
    }

    public void changeHandlers(HandlerType h) {
        this.resetContextHandlers();
        switch (h) {
            case SELECTION:
                this.handler = injector.getInstance(SelectionHandler.class);
                this.setContextHandlers();
                break;
            case FIGURE:
                this.handler = injector.getInstance(FigureHandler.class);
                break;
            case ROTATION:
                this.handler = injector.getInstance(RotationHandler.class);
                this.setContextHandlers();
                break;
            case SKETCH:
                this.handler = injector.getInstance(SketchHandler.class);
                break;
            default:
                break;
        }
        view.setSelected(-1);
        view.setDrawing(false);
    }

    void resetContextHandlers() {
        canvas.setOnContextMenuRequested(null);
        canvas.setOnTouchStationary(null);
        canvas.setOnMousePressed(null);
        canvas.setOnTouchPressed(null);
    }
    
    void setContextHandlers() {
        canvas.setOnContextMenuRequested(contextlistener);
        canvas.setOnTouchStationary(popuplistener);
        canvas.setOnMousePressed(arealistener);
        canvas.setOnTouchPressed(arealistener);
    }

    void initializeHandlers() {
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            mouseUp(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            mouseDown(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            mouseMoved(event);
        });
        canvas.addEventHandler(TouchEvent.TOUCH_RELEASED, (TouchEvent event) -> {
            touchEnd(event);
        });
        canvas.addEventHandler(TouchEvent.TOUCH_PRESSED, (TouchEvent event) -> {
            touchStart(event);
        });
        canvas.addEventHandler(TouchEvent.TOUCH_MOVED, (TouchEvent event) -> {
            touchMoved(event);
        });
        addContextMenu();
        this.changeHandlers(HandlerType.SKETCH);
    }

    public void mouseUp(MouseEvent event) {
        handler.upEvent();
    }

    public void mouseDown(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        handler.downEvent();
    }

    public void mouseMoved(MouseEvent event) {
        tempX = event.getX();
        tempY = event.getY();
        handler.dragEvent();
    }

    public void touchEnd(TouchEvent event) {
        handler.upEvent();
    }
    
    public void touchStart(TouchEvent event) {
        TouchPoint touch = event.getTouchPoints().get(0);
        startX = touch.getX();
        startY = touch.getY();
        handler.downEvent();
    }

    public void touchMoved(TouchEvent event) {
        TouchPoint touch = event.getTouchPoints().get(0);
        tempX = touch.getX();
        tempY = touch.getY();
        handler.dragEvent();
    }

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

    public SubScene getScene() {
        return canvas;
    }
    
    public Group getCanvas() {
        return root;
    }

    public CanvasView getView() {
        return view;
    }

    public void setDrawType(DrawingType s) {
        drawtype = s;
    }

    public DrawingType getDrawType() {
        return drawtype;
    }

    public void setStartX(double x) {
        this.startX = x;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartY(double y) {
        this.startY = y;
    }

    public double getStartY() {
        return startY;
    }

    public void setTempX(double x) {
        this.tempX = x;
    }

    public double getTempX() {
        return tempX;
    }

    public void setTempY(double y) {
        this.tempY = y;
    }

    public double getTempY() {
        return tempY;
    }
    
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        view.updateSelectedItem();
}

    public Stroke getStroke() {
        return this.stroke;
    }

    public void setColor(Color color) {
        this.color = color;
        view.updateSelectedItem();
    }

    public Color getColor() {
        return this.color;
    }

    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
        view.updateSelectedItem();
    }

    public Color getFillColor() {
        return this.fillcolor;
    }
    
    public void setTransparency(int transparency) {
        this.transparency = transparency;
        view.updateSelectedItem();
    }

    public int getTransparency() {
        return this.transparency;
    }

}
