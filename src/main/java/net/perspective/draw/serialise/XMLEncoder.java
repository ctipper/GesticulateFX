/*
 * XMLEncoder.java
 * 
 * Created on 16-May-2011, 14:41:08
 * 
 */
package net.perspective.draw.serialise;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author ctipper
 */

public class XMLEncoder extends java.beans.XMLEncoder {

    OutputStream os;

    public XMLEncoder(OutputStream out) {
        super(out);
        os = out;
    }

    /**
     * Alternative to close() which closes the XML node and also the
     * OutputStream. Sometimes we need to close the XML without closing the
     * OutputStream. For example, entries in a ZipOutputStream.
     *
     * @throws IOException
     */
    public void finished() throws IOException {
        flush();
        // borrowed from XMLEncoder's close()
        os.write("</java> \n".getBytes());
    }
}
