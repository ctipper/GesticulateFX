/**
 * KeyHandler.java
 * 
 * Created on 21 Oct 2022 12:14:34
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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
package net.perspective.draw.event.keyboard;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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
    private String keychar;
    private KeyCode keycode;
    private boolean isAltDown;
    private boolean isControlDown;
    private boolean isMetaDown;
    private boolean isShiftDown;
    private boolean isShortcutDown;

    /**
     * Creates a new instance of <code>KeyHandler</code> 
     */
    @Inject
    public KeyListener() {
    }

    public void setEventHandler(KeyHandler handler) {
        this.handler = handler;
    }

    public void initializeHandlers(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            keyPressed(event);
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            keyReleased(event);
        });
        scene.addEventHandler(KeyEvent.KEY_TYPED, (KeyEvent event) -> {
            keyTyped(event);
        });
    }

    protected void keyPressed(KeyEvent event) {
        this.keycode = event.getCode();
        this.isAltDown = event.isAltDown();
        this.isControlDown = event.isControlDown();
        this.isMetaDown = event.isMetaDown();
        this.isShiftDown = event.isShiftDown();
        this.isShortcutDown = event.isShortcutDown();
        handler.keyPressed();
    }

    protected void keyReleased(KeyEvent event) {
        this.keycode = event.getCode();
        this.isAltDown = event.isAltDown();
        this.isControlDown = event.isControlDown();
        this.isMetaDown = event.isMetaDown();
        this.isShiftDown = event.isShiftDown();
        this.isShortcutDown = event.isShortcutDown();
        handler.keyReleased();
    }

    protected void keyTyped(KeyEvent event) {
        this.keychar = event.getCharacter();
        this.isAltDown = event.isAltDown();
        this.isControlDown = event.isControlDown();
        this.isMetaDown = event.isMetaDown();
        this.isShiftDown = event.isShiftDown();
        this.isShortcutDown = event.isShortcutDown();
        handler.keyTyped();
    }

    /**
     * @return the keychar
     */
    public String getKeyChar() {
        return keychar;
    }

    /**
     * @return the keycode
     */
    public KeyCode getKeyCode() {
        return keycode;
    }

    /**
     * @return the isAltDown
     */
    public boolean isIsAltDown() {
        return isAltDown;
    }

    /**
     * @return the isControlDown
     */
    public boolean isIsControlDown() {
        return isControlDown;
    }

    /**
     * @return the isMetaDown
     */
    public boolean isIsMetaDown() {
        return isMetaDown;
    }

    /**
     * @return the isShiftDown
     */
    public boolean isIsShiftDown() {
        return isShiftDown;
    }

    /**
     * @return the isShortcutDown
     */
    public boolean isIsShortcutDown() {
        return isShortcutDown;
    }

}
