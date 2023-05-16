/*
 * Handler.java
 * 
 * Created on Oct 19, 2013 8:18:21 PM
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
package net.perspective.draw.event;

/**
 * 
 * @author ctipper
 */

public interface Handler {

    void upEvent();

    void downEvent();
    
    void clickEvent();

    void hoverEvent();

    void dragEvent();

    void zoomEvent();

}
