/*
 * HandlerAdapter.java
 * 
 * Created on Oct 19, 2013 8:18:21 PM
 * 
 */
package net.perspective.draw.event;

/**
 *
 * @author ctipper
 */

public interface HandlerAdapter {

    public abstract void upEvent();
    
    public abstract void downEvent();

    public abstract void dragEvent();
}
