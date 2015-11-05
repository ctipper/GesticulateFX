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
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;

/**
 *
 * @author ctipper
 */
@Singleton
public class DrawingArea {

    @Inject Injector injector;
    @Inject private CanvasView view;
    private SubScene canvas;
    private Group root;
    private Handler handler;

    private DrawingType drawtype;
    private Stroke stroke;
    private String color, fillcolor;
    private int transparency;
    private double startX, startY;
    private double tempX, tempY;
    
    private Transferable clipboard;
    private CanvasTransferHandler transferhandler;

    private ContextMenu contextmenu;
    private EventHandler<ContextMenuEvent> contextlistener;
    private EventHandler<TouchEvent> popuplistener;
    private List<TouchPoint> starters, tempers = null;

    private static final Logger logger = LoggerFactory.getLogger(DrawingArea.class.getName());

    /**
     * Creates a new instance of <code>DrawingCanvas</code>
     */
    @Inject
    public DrawingArea() {
    }

    void init(double width, double height) {
        root = new Group();
        canvas = new SubScene(root, width, height);
        canvas.setFill(Color.WHITE);
        transferhandler = new CanvasTransferHandler(this);
        contextmenu = new ContextMenu();
        contextlistener = null;
        view.setDrawingListener();
        prepareDrawing();
        setHandlers();
    }

    public void prepareDrawing() {
        setDrawType(DrawingType.SKETCH);
        this.stroke = new BasicStroke(6.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        this.color = "#4860E0";
        this.fillcolor = "#4860E0";
        this.transparency = 100;
        view.clearView();
        this.clear();
        changeHandler(HandlerType.SKETCH);
    }
    
    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
    }

    public void changeHandler(HandlerType h) {
        canvas.setOnContextMenuRequested(null);
        canvas.setOnTouchStationary(null);
        switch (h) {
            case SELECTION:
                this.handler = injector.getInstance(SelectionHandler.class);
                canvas.setOnContextMenuRequested(contextlistener);
                canvas.setOnTouchStationary(popuplistener);
                break;
            case FIGURE:
                this.handler = injector.getInstance(FigureHandler.class);
                break;
            case ROTATION:
                this.handler = injector.getInstance(RotationHandler.class);
                canvas.setOnContextMenuRequested(contextlistener);
                canvas.setOnTouchStationary(popuplistener);
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

    void setHandlers() {
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mouseUp(event);
                }
            });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mouseDown(event);
                }
            });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mouseMoved(event);
                }
            });
        canvas.addEventHandler(TouchEvent.TOUCH_RELEASED,
            new EventHandler<TouchEvent>() {
                @Override
                public void handle(TouchEvent event) {
                    touchEnd(event);
                }
            });
        canvas.addEventHandler(TouchEvent.TOUCH_PRESSED,
            new EventHandler<TouchEvent>() {
                @Override
                public void handle(TouchEvent event) {
                    touchStart(event);
                }
            });
        canvas.addEventHandler(TouchEvent.TOUCH_MOVED,
            new EventHandler<TouchEvent>() {
                @Override
                public void handle(TouchEvent event) {
                    touchMoved(event);
                }
            });
        addContextMenu();
        this.changeHandler(HandlerType.SKETCH);
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
        this.setStartTouches(null);
        this.setTempTouches(null);
    }
    
    public void touchStart(TouchEvent event) {
        this.setStartTouches(event.getTouchPoints());
        TouchPoint touch = event.getTouchPoints().get(0);
        startX = touch.getX();
        startY = touch.getY();
        handler.downEvent();
    }

    public void touchMoved(TouchEvent event) {
        this.setTempTouches(event.getTouchPoints());
        TouchPoint touch = event.getTouchPoints().get(0);
        tempX = touch.getX();
        tempY = touch.getY();
        handler.dragEvent();
    }

    public void addContextMenu() {
        MenuItem cxtCutMenu = new MenuItem("Cut");
        cxtCutMenu.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
        	    if (view.getSelected() != -1) {
                    clipboard = transferhandler.createTransferable();
                    transferhandler.exportDone(clipboard, MOVE);
                    view.setSelected(-1);
                }
            }
        });
        MenuItem cxtCopyMenu = new MenuItem("Copy");
        cxtCopyMenu.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
        	    if (view.getSelected() != -1) {
                    clipboard = transferhandler.createTransferable();
                    transferhandler.exportDone(clipboard, COPY);
                }
            }
        });
        MenuItem cxtPasteMenu = new MenuItem("Paste");
        cxtPasteMenu.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (clipboard != null) {
                    transferhandler.importData(clipboard);
                    view.setSelected(-1);
                }
            }
        });
        contextmenu.getItems().addAll(cxtCutMenu, cxtCopyMenu, cxtPasteMenu);
        contextlistener = new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextmenu.show(canvas, event.getScreenX(), event.getScreenY());
            }
        };
        popuplistener = new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                TouchPoint touch = event.getTouchPoints().get(0);
                contextmenu.show(canvas, touch.getScreenX(), touch.getScreenY());
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
    
    public void setStartTouches(List<TouchPoint> touches) {
        this.starters = touches;
    }
    
    public List<TouchPoint> getStartTouches() {
        return this.starters;
    }

    public void setTempTouches(List<TouchPoint> touches) {
        this.tempers = touches;
    }
    
    public List<TouchPoint> getTempTouches() {
        return this.tempers;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        view.updateSelectedItem();
}

    public Stroke getStroke() {
        return this.stroke;
    }

    public void setColor(String color) {
        this.color = color;
        view.updateSelectedItem();
    }

    public String getColor() {
        return this.color;
    }

    public void setFillColor(String fillcolor) {
        this.fillcolor = fillcolor;
        view.updateSelectedItem();
    }

    public String getFillColor() {
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
