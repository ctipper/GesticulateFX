/**
 * DummyKeyHandler.java
 * 
 * Created on 21 Oct 2022 12:38:32
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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

/**
 *
 * @author ctipper
 */

public class DummyKeyHandler implements KeyHandler {

    @Inject KeyListener keylistener;

    /**
     * Creates a new instance of <code>DummyKeyHandler</code> 
     */
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
