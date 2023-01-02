/**
 * DrawAreaMouseListener.java
 * 
 * Created on 19-Sep-2016 11:20:35
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
package net.perspective.draw.event;

import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.input.ZoomEvent;
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
    private double wheel;
    private boolean snapEnabled;

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
        canvas.addEventHandler(ScrollEvent.SCROLL, (ScrollEvent event) -> {
            scrollEvent(event);
            event.consume(); // prevent event percolating to parent
        });
        canvas.addEventHandler(ZoomEvent.ZOOM, (ZoomEvent event) -> {
            zoomEvent(event);
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
        handler.clickEvent();
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

    protected void scrollEvent(ScrollEvent se) {
        tempX = se.getX();
        tempY = se.getY();
        this.wheel = se.getDeltaY() > 1 ? .1 : se.getDeltaY() < -1 ? -.1 : 0;
        handler.zoomEvent();
    }

    protected void zoomEvent(ZoomEvent ze) {
        tempX = ze.getX();
        tempY = ze.getY();
        this.wheel = ze.getZoomFactor() - 1;
        handler.zoomEvent();
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

    public double getWheel() {
        return this.wheel;
    }

    /**
     * Snap to guides is enabled
     * 
     * @param snapEnabled 
     */
    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
    }

    /**
     * Get snap to guides is enabled
     * 
     * @return 
     */
    public boolean isSnapEnabled() {
        return snapEnabled;
    }

}
