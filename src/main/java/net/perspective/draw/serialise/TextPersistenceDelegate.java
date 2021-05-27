/**
 * TextPersistenceDelegate.java
 * 
 * Created on 27 May 2021 20:48:07
 * 
 */
package net.perspective.draw.serialise;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import net.perspective.draw.geom.Text;

/**
 *
 * @author ctipper
 */

public class TextPersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    @SuppressWarnings("deprecation")
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance,  newInstance, out);

        Text text = (Text) oldInstance;
        out.writeStatement(
                new Statement(text,
                        "setColor",
                        new Object[]{text.getAwtColor()}));
    }
}
