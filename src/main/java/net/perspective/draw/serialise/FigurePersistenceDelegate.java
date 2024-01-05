/**
 * FigurePersistenceDelegate.java
 * 
 * Created on 23-Apr-2016 12:06:14
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
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import net.perspective.draw.geom.Edge;
import net.perspective.draw.geom.Figure;

/**
 * 
 * @author ctipper
 */

public class FigurePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {

        if (oldInstance instanceof Edge edge) {
            return new Expression(edge, edge.getClass(), "new", new Object[]{
                edge.getType()
            });
        } else {
            Figure figure = (Figure) oldInstance;
            return new Expression(figure, figure.getClass(), "new", new Object[]{
                figure.getType()
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance,  newInstance, out);

        Figure figure = (Figure) oldInstance;
        out.writeStatement(
                new Statement(figure,
                        "setStart",
                        new Object[]{figure.getStart().getX(), figure.getStart().getY()}));
        out.writeStatement(
                new Statement(figure,
                        "setEnd",
                        new Object[]{figure.getEnd().getX(), figure.getEnd().getY()}));
        out.writeStatement(
                new Statement(figure,
                        "setColor",
                        new Object[]{figure.getAwtColor()}));
        out.writeStatement(
                new Statement(figure,
                        "setFillColor",
                        new Object[]{figure.getAwtFillColor()}));
    }

}
