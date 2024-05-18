/**
 * MapKeyHandler.java
 * 
 * Created on 28 Oct 2022 10:46:51
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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
import net.perspective.draw.MapController;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;

/**
 *
 * @author ctipper
 */

public class MapKeyHandler implements KeyHandler {

    @Inject DrawingArea drawarea;
    @Inject CanvasView view;
    @Inject MapController mapper;
    @Inject KeyListener keylistener;
    private int selection = -1;
    private DrawingType drawingtype;
    private HandlerType handlerType;
    private boolean isMapping = false;
    private boolean pressed = false;

    /**
     * Creates a new instance of <code>MapKeyHandler</code> 
     */
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
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                if (!pressed) {
                    drawingtype = drawarea.getDrawType().orElse(null);
                    handlerType = drawarea.getHandlerType();
                    selection = view.getSelected();
                    drawarea.setDrawType(null);
                    drawarea.changeHandlers(HandlerType.SELECTION);
                    isMapping = view.isMapping();
                    view.setMapping(false);
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
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                drawarea.setDrawType(drawingtype);
                drawarea.changeHandlers(handlerType);
                drawarea.setMultiSelectEnabled(false);
                view.setMapping(isMapping);
                view.setSelected(selection);
                mapper.initMap();
                pressed = false;

            }
            default -> {
            }
        }
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
