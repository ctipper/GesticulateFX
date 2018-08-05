/**
 * EnumPersistenceDelegate.java
 * 
 * Created on Apr 30, 2011, 7:47:42 PM
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

public class ArrowTypePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        Enum<?> t = (Enum<?>) oldInstance;
        return new Expression(oldInstance, t.getDeclaringClass(), "valueOf", new Object[] {
            t.name()
        });
    }

}
