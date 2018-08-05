/**
 * FigurePersistenceDelegate.java
 * 
 * Created on 23-Apr-2016 12:06:14
 * 
 */
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.Edge;
import net.perspective.draw.geom.Figure;

/**
 * 
 * @author ctipper
 */

public class FigurePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {

        if (oldInstance instanceof ArrowLine) {
            ArrowLine line = (ArrowLine) oldInstance;
            return new Expression(line, line.getClass(), "new", new Object[]{
                line.getLine(),
                line.getArrowType()
            });
        } else if (oldInstance instanceof Edge) {
            Edge edge = (Edge) oldInstance;
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
                        "setColor",
                        new Object[]{figure.getAwtColor()}));
        out.writeStatement(
                new Statement(figure,
                        "setFillColor",
                        new Object[]{figure.getAwtFillColor()}));
    }

}
