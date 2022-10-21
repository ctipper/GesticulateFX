/**
 * DummyKeyHandler.java
 * 
 * Created on 21 Oct 2022 12:38:32
 * 
 */
package net.perspective.draw.event.keyboard;

import javax.inject.Inject;

/**
 *
 * @author ctipper
 */

public class DummyKeyHandler implements KeyHandler {

    @Inject KeyListener keylistener;

    /** Creates a new instance of <code>DummyKeyHandler</code> */
    @Inject
    public DummyKeyHandler() {
    }

    @Override
    public void keyPressed() {
        
    }

    @Override
    public void keyReleased() {
        
    }

    @Override
    public void keyTyped() {
        
    }

}
