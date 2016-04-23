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
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */
public class FigurePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    @SuppressWarnings("deprecation")
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Figure f = (Figure) oldInstance;
        return new Expression(oldInstance, f.getClass(), "new", new Object[]{
                f.getType(),
                f.getStart(),
                f.getEnd(),
                f.getPoints(),
                f.getAngle(),
                f.getTransparency(),
                f.getStroke(),
                f.getAwtColor(),
                f.getAwtFillColor()
            });
    }
}
