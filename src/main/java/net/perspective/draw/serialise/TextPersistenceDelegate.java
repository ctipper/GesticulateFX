/**
 * TextPersistenceDelegate.java
 * 
 * Created on 27 May 2021 20:48:07
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

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import net.perspective.draw.geom.Text;

/**
 *
 * @author ctipper
 */

public class TextPersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    @SuppressWarnings("deprecation")
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance,  newInstance, out);

        Text text = (Text) oldInstance;
        out.writeStatement(
                new Statement(text,
                        "setColor",
                        new Object[]{text.getAwtColor()}));
    }
}
