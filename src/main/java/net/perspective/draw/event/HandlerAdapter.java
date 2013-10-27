/*
 * HandlerAdapter.java
 * 
 * Created on Oct 19, 2013 8:18:21 PM
 * 
 */
package net.perspective.draw.event;

import net.perspective.draw.DrawingCanvas;

/**
 *
 * @author ctipper
 */

public abstract class HandlerAdapter {

    protected DrawingCanvas c;

    /**
     *
     * @param c
     */
    public HandlerAdapter(DrawingCanvas c) {
        this.c = c;
    }

    public abstract void upEvent();
    
    public abstract void downEvent();

    public abstract void moveEvent();
}
