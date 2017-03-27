/**
 * BasicStrokePersistenceDelegate.java
 * 
 * Created on Apr 26, 2011, 9:46:14 PM
 * 
 */
package net.perspective.draw.serialise;

import java.awt.BasicStroke;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;

/**
 * 
 * @author ctipper
 */

public class BasicStrokePersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(final Object oldInstance, final Encoder out) {
        BasicStroke stroke = (BasicStroke) oldInstance;
        return new Expression(stroke, stroke.getClass(), "new", new Object[]{
                    stroke.getLineWidth(),
                    stroke.getEndCap(),
                    stroke.getLineJoin(),
                    stroke.getMiterLimit(),
                    stroke.getDashArray(),
                    stroke.getDashPhase()
                });
    }

}
