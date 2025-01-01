/**
 * InstantPersistenceDelegate.java
 * 
 * Created on 02-Oct-2018 16:17:28
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
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.time.Instant;

/**
 *
 * @author ctipper
 */

public class InstantPersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Instant t = (Instant) oldInstance;
        return new Expression(oldInstance, t.getClass(), "ofEpochMilli", new Object[] {
            t.toEpochMilli()
        });
    }

}
