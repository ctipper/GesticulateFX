/*
 * DrawingCanvas.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;
import net.perspective.draw.event.FigureHandler;
import net.perspective.draw.event.HandlerAdapter;
import net.perspective.draw.event.HandlerType;
import net.perspective.draw.event.RotationHandler;
import net.perspective.draw.event.SelectionHandler;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
public class DrawingCanvas {

    private final DocView view;
    private final Canvas canvas;
    private final GraphicsContext context;
    private HandlerAdapter handler;
    private boolean mousedown, touchdown = false;
    private double startX, startY;
    private double tempX, tempY;
    private boolean smoothed;

    private static final Logger logger = getLogger(DrawingCanvas.class.getName());

    /**
     * Creates a new instance of <code>DrawingCanvas</code>
     *
     * @param width
     * @param height
     */
    public DrawingCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        context = canvas.getGraphicsContext2D();
        view = new DocView();
        canvas.setFocusTraversable(false);
    }

    void initCanvas() {
        view.initView();
        this.clear();
    }

    Canvas getCanvas() {
        return canvas;
    }

    public DocView getView() {
        return view;
    }

    public void clear() {
        this.clear(context);
    }

    private void clear(GraphicsContext cxt) {
        // Store the current transformation matrix
        cxt.save();

        cxt.setFill(Color.WHITE);
        cxt.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Restore the transform
        cxt.restore();
    }

    public void doUpdate() {
        // update the buffer canvas
        this.clear(context);
        if (smoothed) {
            for (Figure item : view.getDrawings()) {
                item.draw(context);
            }
            if (view.getSelected() != -1) {
                view.getDrawings().get(view.getSelected()).drawAnchors(context);
            }
            if (view.isDrawing()) {
                view.getNewItem().draw(context);
            }
        } else {
            for (Figure item : view.getDrawings()) {
                item.sketch(context);
            }
            if (view.getSelected() != -1) {
                view.getDrawings().get(view.getSelected()).drawAnchors(context);
            }
            if (view.isDrawing()) {
                view.getNewItem().sketch(context);
            }
        }
    }

    public void setSmoothed(boolean s) {
        smoothed = s;
    }

    public void changeHandler(HandlerType h) {
        switch (h) {
            case SELECTION:
                this.handler = new SelectionHandler(this);
                break;
            case FIGURE:
                this.handler = new FigureHandler(this);
                break;
            case ROTATION:
                this.handler = new RotationHandler(this);
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
                    mouseMove(event);
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
                    touchMove(event);
                }
            });
        this.changeHandler(HandlerType.FIGURE);
    }

    public void mouseUp(MouseEvent event) {
        handler.upEvent();
        mousedown = false;
        view.setDrawing(false);
    }

    public void mouseDown(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        handler.downEvent();
        mousedown = true;
    }

    public void mouseMove(MouseEvent event) {
        if (mousedown) {
            tempX = event.getX();
            tempY = event.getY();
            handler.moveEvent();
        }
    }

    public void touchEnd(TouchEvent event) {
        handler.upEvent();
        touchdown = false;
        view.setDrawing(false);
    }

    public void touchStart(TouchEvent event) {
        TouchPoint touch = event.getTouchPoints().get(0);
        startX = touch.getX();
        startY = touch.getY();
        handler.downEvent();
        touchdown = true;
    }

    public void touchMove(TouchEvent event) {
        if (touchdown) {
            TouchPoint touch = event.getTouchPoints().get(0);
            tempX = touch.getX();
            tempY = touch.getY();
            handler.moveEvent();
        }
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
}
