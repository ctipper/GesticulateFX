/**
 * ArrowLinePersistenceDelegate.java
 *
 * Created on 27-Aug-2018 19:15:29
 *
 */

/**
 * Copyright (c) 2022 e-conomist
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
import java.beans.Statement;
import net.perspective.draw.geom.ArrowLine;

/**
 *
 * @author ctipper
 */

public class ArrowLinePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {

        ArrowLine line = (ArrowLine) oldInstance;
        return new Expression(line, line.getClass(), "new", new Object[]{
            line.getLine(),
            line.getArrowType()
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void initialize(Class<?> type, Object oldInstance,
            Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);

        ArrowLine line = (ArrowLine) oldInstance;
        out.writeStatement(
                new Statement(line,
                        "setColor",
                        new Object[]{line.getAwtColor()}));
        out.writeStatement(
                new Statement(line,
                        "setFillColor",
                        new Object[]{line.getAwtFillColor()}));
    }

}
