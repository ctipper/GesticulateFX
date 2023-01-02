/**
 * BasicStrokePersistenceDelegate.java
 * 
 * Created on Apr 26, 2011, 9:46:14 PM
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
package net.perspective.draw.serialise;

import java.awt.BasicStroke;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;

/**
 * 
 * @author ctipper
 */

public class BasicStrokePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        BasicStroke stroke = (BasicStroke) oldInstance;
        return new Expression(stroke, stroke.getClass(), "new", new Object[]{
                    stroke.getLineWidth(),
                    stroke.getEndCap(),
                    stroke.getLineJoin(),
                    stroke.getMiterLimit(),
                    stroke.getDashArray(),
                    stroke.getDashPhase()
                });
    }

    /** Creates a new instance of <code>BasicStrokePersistenceDelegate</code> */
    public BasicStrokePersistenceDelegate() {}

}
