/**
 * InstantPersistenceDelegate.java
 * 
 * Created on 02-Oct-2018 16:17:28
 * 
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
