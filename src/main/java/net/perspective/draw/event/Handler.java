/*
 * Handler.java
 * 
 * Created on Oct 19, 2013 8:18:21 PM
 * 
 */
package net.perspective.draw.event;

/**
 *
 * @author ctipper
 */

public interface Handler {
    
    void upEvent();
    
    void downEvent();

    void dragEvent();
}
