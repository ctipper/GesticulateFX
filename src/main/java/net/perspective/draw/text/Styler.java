/*
 * Styler.java
 * 
 * Created on Jul 17, 2013 3:00:59 PM
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
package net.perspective.draw.text;

import java.util.Set;

/**
 * 
 * @author ctipper
 */

public interface Styler {

    /**
     * return a list of styles at cursor
     * 
     * @return the list of styles in fragment
     */
    Set<String> detectStyles();

    /**
     * insert style between carets
     * 
     * @param style the style
     */
    void createStyle(String style);

    /**
     * remove the style between carets
     *
     * @param style the style
     */
    void removeStyle(String style);

}
