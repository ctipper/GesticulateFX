/**
 * MoveKeyHandler.java
 * 
 * Created on 21 Oct 2022 12:39:45
 * 
 */
package net.perspective.draw.event.keyboard;

import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.DrawItem;

/**
 *
 * @author ctipper
 */

public class MoveKeyHandler implements KeyHandler {

    @Inject DrawingArea drawarea;
    @Inject CanvasView view;
    @Inject KeyListener keylistener;

    /** Creates a new instance of <code>MoveKeyHandler</code> */
    @Inject
    public MoveKeyHandler() {
    }

    @Override
    public void keyPressed() {
        if ((view.getSelected() != -1) && (!view.isEditing())) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            switch (keylistener.getKeycode()) {
                case KP_UP, UP -> {
                }
                case KP_DOWN, DOWN -> {
                }
                case KP_LEFT, LEFT -> {
                }
                case KP_RIGHT, RIGHT -> {
                }
                case DELETE -> {
                }
                default -> {
                }
            }
        }
    }

    @Override
    public void keyReleased() {
        
    }

    @Override
    public void keyTyped() {
        
    }

}
