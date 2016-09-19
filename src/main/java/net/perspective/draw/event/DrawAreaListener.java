/**
 * DrawAreaMouseListener.java
 * 
 * Created on 19-Sep-2016 11:20:35
 * 
 */
package net.perspective.draw.event;

import com.google.inject.Injector;
import javafx.scene.SubScene;
import javafx.scene.input.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.HandlerType;

/**
 *
 * @author ctipper
 */

@Singleton
public class DrawAreaListener {

    @Inject private Injector injector;
    @Inject private CanvasView view;
    @Inject private DrawingArea drawarea;
    private Handler handler;
    public double startX, startY;   // Hold co-ordinates of user's last mousePressed event.
    public double tempX, tempY;     // Hold co-ordinates of current mouseDragged event.
    private boolean leftbutton, rightbutton;
    private boolean doubleclick;

    /** Creates a new instance of <code>DrawAreaMouseListener</code> */
    @Inject
    public DrawAreaListener() {
    }

    public void changeHandlers(HandlerType h) {
        drawarea.resetContextHandlers();
        switch (h) {
            case SELECTION:
                this.handler = injector.getInstance(SelectionHandler.class);
                drawarea.setContextHandlers();
                break;
            case FIGURE:
                this.handler = injector.getInstance(FigureHandler.class);
                break;
            case ROTATION:
                this.handler = injector.getInstance(RotationHandler.class);
                drawarea.setContextHandlers();
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

    public void mouseUp(MouseEvent me) {
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        handler.upEvent();
    }

    public void mouseDown(MouseEvent me) {
        startX = me.getX();
        startY = me.getY();
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        handler.downEvent();
    }

    public void mouseClicked(MouseEvent me) {
        tempX = me.getX();
        tempY = me.getY();
        this.leftbutton = MouseButton.PRIMARY == me.getButton();
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        this.doubleclick = me.getClickCount() > 1 && MouseButton.PRIMARY == me.getButton();
        // click handler here
    }

    public void mouseDragged(MouseEvent me) {
        tempX = me.getX();
        tempY = me.getY();
        handler.dragEvent();
    }

    public void touchEnd(TouchEvent te) {
        handler.upEvent();
    }

    public void touchStart(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
        startX = touch.getX();
        startY = touch.getY();
        handler.downEvent();
    }

    public void touchStationary(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
        tempX = touch.getX();
        tempY = touch.getY();
        // click handler here
    }

    public void touchMoved(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
        tempX = touch.getX();
        tempY = touch.getY();
        handler.dragEvent();
    }

    public void initializeHandlers(SubScene canvas) {
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            mouseUp(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            mouseDown(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            mouseDragged(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            mouseClicked(event);
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
        canvas.addEventHandler(TouchEvent.TOUCH_STATIONARY, (TouchEvent event) -> {
            touchStationary(event);
        });
        drawarea.addContextMenu();
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

    public boolean getLeftClick() {
        return this.leftbutton;
    }

    public boolean getRightClick() {
        return this.rightbutton;
    }

    public boolean doubleClicked() {
        return this.doubleclick;
    }

}
