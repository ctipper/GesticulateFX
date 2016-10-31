/*
 * FigureTypePersistenceDelegate.java
 * 
 * Created on Mar 28, 2012, 2:01:47 PM
 * 
 */
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;

/**
 *
 * @author ctipper
 */

public class FigureTypePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Enum<?> t = (Enum<?>) oldInstance;
        return new Expression(oldInstance, t.getDeclaringClass(), "valueOf", new Object[] {
            t.name()
        });
    }
}
