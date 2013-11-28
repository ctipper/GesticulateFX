/*
 * Handler.java
 * 
 * Created on Oct 19, 2013 8:18:21 PM
 * 
 */
package net.perspective.draw.event;

import net.perspective.draw.DrawingArea;

/**
 *
 * @author ctipper
 */

public abstract class Handler {
    
    DrawingArea canvas;
    
    public Handler(DrawingArea c) {
        this.canvas = c;
    }

    public abstract void upEvent();
    
    public abstract void downEvent();

    public abstract void dragEvent();
}
