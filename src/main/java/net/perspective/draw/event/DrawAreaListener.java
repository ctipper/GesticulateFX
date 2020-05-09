/**
 * DrawAreaMouseListener.java
 * 
 * Created on 19-Sep-2016 11:20:35
 * 
 */
package net.perspective.draw.event;

import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 
 * @author ctipper
 */

@Singleton
public class DrawAreaListener {

    private Handler handler;
    private double startX, startY;   // Hold co-ordinates of user's last mousePressed event.
    private double tempX, tempY;     // Hold co-ordinates of current mouseDragged event.
    private boolean leftbutton, rightbutton;
    private boolean doubleclick;

    /** Creates a new instance of <code>DrawAreaMouseListener</code> */
    @Inject
    public DrawAreaListener() {
    }

    public void setEventHandler(Handler handler) {
        this.handler = handler;
    }

    public void initializeHandlers(SubScene canvas) {
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            mouseUp(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            mouseDown(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, (MouseEvent event) -> {
            mouseHover(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            mouseClicked(event);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            mouseDragged(event);
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
    }

    protected void mouseUp(MouseEvent me) {
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        handler.upEvent();
    }

    protected void mouseDown(MouseEvent me) {
        startX = me.getX();
        startY = me.getY();
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        handler.downEvent();
    }

    protected void mouseClicked(MouseEvent me) {
        tempX = me.getX();
        tempY = me.getY();
        this.leftbutton = MouseButton.PRIMARY == me.getButton();
        this.rightbutton = MouseButton.SECONDARY == me.getButton();
        this.doubleclick = me.getClickCount() > 1 && MouseButton.PRIMARY == me.getButton();
        // click handler here
    }

    protected void mouseHover(MouseEvent me) {
        tempX = me.getX();
        tempY = me.getY();
        handler.hoverEvent();
    }

    protected void mouseDragged(MouseEvent me) {
        tempX = me.getX();
        tempY = me.getY();
        handler.dragEvent();
    }

    protected void touchEnd(TouchEvent te) {
        handler.upEvent();
    }

    protected void touchStart(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
        startX = touch.getX();
        startY = touch.getY();
        handler.downEvent();
    }

    protected void touchStationary(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
        tempX = touch.getX();
        tempY = touch.getY();
        // click handler here
    }

    protected void touchMoved(TouchEvent te) {
        TouchPoint touch = te.getTouchPoints().get(0);
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
