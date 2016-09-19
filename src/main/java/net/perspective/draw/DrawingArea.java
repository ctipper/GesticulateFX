/*
 * DrawingArea.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

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

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;

import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.event.*;

/**
 *
 * @author ctipper
 */

@Singleton
public class DrawingArea {

    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private DrawAreaListener listener;
    private SubScene canvas;
    private Group root;

    private DrawingType drawtype;
    private Stroke stroke;
    private Color color, fillcolor;
    private int transparency;

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
        listener.initializeHandlers(canvas);
        listener.changeHandlers(HandlerType.SELECTION);
    }

    public void prepareDrawing() {
        this.stroke = new BasicStroke(6.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        this.color = Color.web("#4860E0");
        this.fillcolor = Color.web("#4860E0");
        this.transparency = controller.getWireframeWhen().then(0).otherwise(100).intValue();
        view.clearView();
        this.clear();
    }

    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
    }

    public void resetContextHandlers() {
        canvas.setOnContextMenuRequested(null);
        canvas.setOnTouchStationary(null);
        canvas.setOnMousePressed(null);
        canvas.setOnTouchPressed(null);
    }

    public void setContextHandlers() {
        canvas.setOnContextMenuRequested(contextlistener);
        canvas.setOnTouchStationary(popuplistener);
        canvas.setOnMousePressed(arealistener);
        canvas.setOnTouchPressed(arealistener);
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
