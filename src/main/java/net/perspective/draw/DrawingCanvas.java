/*
 * DrawingCanvas.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.paint.Color;

import net.perspective.draw.event.*;
import net.perspective.draw.geom.Figure;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
@Singleton
public class DrawingCanvas {

    @Inject private CanvasView view;
    private final Canvas canvas;
    private final GraphicsContext context;
    private HandlerAdapter handler;
    private double startX, startY;
    private double tempX, tempY;

    private static final Logger logger = LoggerFactory.getLogger(DrawingCanvas.class.getName());

    /**
     * Creates a new instance of <code>DrawingCanvas</code>
     *
     * @param width
     * @param height
     */
    @Inject
    public DrawingCanvas(@Assisted("width") Double width, @Assisted("height") Double height) {
        canvas = new Canvas(width.doubleValue(), height.doubleValue());
        context = canvas.getGraphicsContext2D();
        canvas.setFocusTraversable(false);
    }

    void initCanvas() {
        view.initView();
        this.clear();
        setHandler(HandlerType.SKETCH);
    }

    Canvas getCanvas() {
        return canvas;
    }

    public CanvasView getView() {
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

    public void repaint() {
        // update the buffer canvas
        this.clear(context);
        for (Figure item : view.getDrawings()) {
            item.draw(context);
        }
        if (view.getSelected() != -1) {
            view.getDrawings().get(view.getSelected()).drawAnchors(context);
        }
        if (view.isDrawing()) {
            view.getNewItem().sketch(context);
        }
    }

    public void setHandler(HandlerType h) {
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
            case SKETCH:
                this.handler = new SketchHandler(this);
            default:
                break;
        }
        view.setSelected(-1);
        view.setDrawing(false);
    }

    void initHandlers() {
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
        this.setHandler(HandlerType.SKETCH);
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
