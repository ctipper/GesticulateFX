/**
 * MapKeyHandler.java
 * 
 * Created on 28 Oct 2022 10:46:51
 * 
 */
package net.perspective.draw.event.keyboard;

import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.MapController;

/**
 *
 * @author ctipper
 */

public class MapKeyHandler implements KeyHandler {

    @Inject CanvasView view;
    @Inject MapController mapper;
    @Inject KeyListener keylistener;

    /** Creates a new instance of <code>MapKeyHandler</code> */
    @Inject
    public MapKeyHandler() {
    }

    @Override
    public void keyPressed() {
        if ((view.getSelected() != -1) && (view.isMapping())) {
            switch (keylistener.getKeyCode()) {
                case KP_UP, UP -> mapper.moveMap(0, -20);
                case KP_DOWN, DOWN -> mapper.moveMap(0, 20);
                case KP_LEFT, LEFT -> mapper.moveMap(-20, 0);
                case KP_RIGHT, RIGHT -> mapper.moveMap(20, 0);
                case ESCAPE -> mapper.quitMapping();
            }
        }
    }

    @Override
    public void keyReleased() {

    }

    @Override
    public void keyTyped() {
        if ((view.getSelected() != -1) && (view.isMapping())) {
            String c = keylistener.getKeyChar();
            switch (c) {
                case "+" -> mapper.zoomIn();
                case "-" -> mapper.zoomOut();
            }
        }
    }
}
