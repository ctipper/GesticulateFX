/**
 * ArrowLinePersistenceDelegate.java
 *
 * Created on 27-Aug-2018 19:15:29
 *
 */
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import net.perspective.draw.geom.ArrowLine;

/**
 *
 * @author Christopher G D Tipper
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
