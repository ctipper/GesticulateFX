/**
 * MoveKeyHandler.java
 * 
 * Created on 21 Oct 2022 12:39:45
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

import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.ItemId;
import net.perspective.draw.ItemStore;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.geom.DrawItem;

/**
 *
 * @author ctipper
 */

public class MoveKeyHandler implements KeyHandler {

    private final DrawingArea drawarea;
    private final CanvasView view;
    @Inject ItemStore store;
    @Inject KeyListener keylistener;
    private DrawingType drawingtype;
    private HandlerType handlertype;
    private boolean pressed = false;

    /**
     * Creates a new instance of <code>MoveKeyHandler</code>
     */
    @Inject
    public MoveKeyHandler(DrawingArea drawarea, CanvasView view) {
        this.drawarea = drawarea;
        this.view = view;
        handlertype = HandlerType.SELECTION;
    }

    @Override
    public void keyPressed() {
        /**
         * Keyboard movement semantics
         */
        ItemId id = view.getSelectedId();
        DrawItem item = store.lookupId(id);
        if (item != null && !view.isEditing()) {
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
                    if (keylistener.isIsAltDown()) {
                        drawarea.setRotationMode(true);
                        drawarea.rotateLeft(item);
                    } else {
                        drawarea.snapLeft(item);
                    }
                }
                case KP_RIGHT, RIGHT -> {
                    if (keylistener.isIsAltDown()) {
                        drawarea.setRotationMode(true);
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
            /*
             * The snap and rotate cases above edit the item in place, so it has to be republished
             * or the change never reaches the canvas. After DELETE the slot is gone and both calls
             * no-op on the stale id, which is what stops the item being resurrected.
             */
            view.updateCanvasItem(id, item);
            view.refreshSelection();
        }
        /**
         * Keyboard paste operation semantics
         */
        if (!view.isEditing()) {
            switch (keylistener.getKeyCode()) {
                case X -> {
                    // cut selected
                    if (keylistener.isIsShortcutDown()) {
                        drawarea.cutSelectedItem();
                    }
                }
                case C -> {
                    // copy selected
                    if (keylistener.isIsShortcutDown()) {
                        drawarea.copySelectedItem();
                    }
                }
                case V -> {
                    // paste selected
                    if (keylistener.isIsShortcutDown()) {
                        drawarea.pasteSelectedItem();
                    }
                }
                default -> {
                }
            }
        }
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                if (!pressed) {
                    drawingtype = drawarea.getDrawType().orElse(null);
                    handlertype = drawarea.getHandlerType();
                    drawarea.setDrawType(null);
                    drawarea.changeHandlers(HandlerType.SELECTION);
                    pressed = true;
                }
                drawarea.setMultiSelectEnabled(true);
            }
            default -> {
            }
        }
    }

    @Override
    public void keyReleased() {
        // changeHandlers() below resets rotation mode, so capture it first.
        boolean wasRotating = drawarea.isRotationMode();
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                // Only unwind an interlude this handler actually opened: a release can arrive
                // with no matching press, when the handler was swapped while the key was held
                // or the platform took the press for its menu bar.
                if (pressed) {
                    /*
                     * Restoring the tool drops the selection with it: every changeHandlers() case
                     * but SELECTION clears it, and a group marquee'd under Alt is not meant to
                     * outlive the interlude that made it.
                     */
                    drawarea.setDrawType(drawingtype);
                    drawarea.changeHandlers(handlertype);
                    drawarea.setMultiSelectEnabled(false);
                    pressed = false;
                }
            }
            default -> {
            }
        }
        // Only leave rotation mode once Alt is released.
        if (!keylistener.isIsAltDown() && wasRotating) {
            if (!HandlerType.ROTATION.equals(drawarea.getHandlerType())) {
                drawarea.setRotationMode(false);
            }
            view.refreshSelection();
        }
    }

    @Override
    public void keyTyped() {
        
    }

}
