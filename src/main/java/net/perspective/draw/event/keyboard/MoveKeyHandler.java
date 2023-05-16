/**
 * MoveKeyHandler.java
 * 
 * Created on 21 Oct 2022 12:39:45
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

    /**
     * Creates a new instance of <code>MoveKeyHandler</code>
     */
    @Inject
    public MoveKeyHandler() {
    }

    @Override
    public void keyPressed() {
        if ((view.getSelected() != -1) && (!view.isEditing())) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            switch (keylistener.getKeyCode()) {
                case KP_UP, UP -> {
                    if (!drawarea.isRotationMode()) {
                        drawarea.snapUp(item);
                    }
                }
                case KP_DOWN, DOWN -> {
                    if (!drawarea.isRotationMode()) {
                        drawarea.snapDown(item);
                    }
                }
                case KP_LEFT, LEFT -> {
                    if (drawarea.isRotationMode()) {
                        drawarea.rotateLeft(item);
                    } else {
                        drawarea.snapLeft(item);
                    }
                }
                case KP_RIGHT, RIGHT -> {
                    if (drawarea.isRotationMode()) {
                        drawarea.rotateRight(item);
                    } else {
                        drawarea.snapRight(item);
                    }
                }
                case DELETE -> {
                    view.deleteSelectedItem();
                }
                default -> {
                }
            }
            view.moveSelection(view.getSelected());
            view.updateCanvasItem(view.getSelected(), item);
        }
    }

    @Override
    public void keyReleased() {

    }

    @Override
    public void keyTyped() {
        
    }

}
