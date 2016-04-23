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
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
public class FigurePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Figure figure = (Figure) oldInstance;
        return new Expression(figure, figure.getClass(), "new", new Object[]{
                figure.getType()
                });
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void initialize(Class type, Object oldInstance,
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
