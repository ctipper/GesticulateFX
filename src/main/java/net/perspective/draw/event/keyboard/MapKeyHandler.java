/**
 * MapKeyHandler.java
 * 
 * Created on 28 Oct 2022 10:46:51
 * 
 */

/**
 * Copyright (c) 2022 Christopher Tipper
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
