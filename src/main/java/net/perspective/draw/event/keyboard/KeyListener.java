/**
 * KeyHandler.java
 * 
 * Created on 21 Oct 2022 12:14:34
 * 
 */
package net.perspective.draw.event.keyboard;

import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author ctipper
 */

@Singleton
public class KeyListener {

    private KeyHandler handler;

    /** Creates a new instance of <code>KeyHandler</code> */
    @Inject
    public KeyListener() {
    }

    public void setEventHandler(KeyHandler handler) {
        this.handler = handler;
    }

    public void initializeHandlers(SubScene canvas) {
        canvas.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            keyPressed(event);
        });
        canvas.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            keyReleased(event);
        });
        canvas.addEventHandler(KeyEvent.KEY_TYPED, (KeyEvent event) -> {
            keyTyped(event);
        });
    }

    protected void keyPressed(KeyEvent event) {
        handler.keyPressed();
    }

    protected void keyReleased(KeyEvent event) {
        handler.keyReleased();
    }

    protected void keyTyped(KeyEvent event) {
        handler.keyTyped();
    }

}
