/*
 * XMLEncoder.java
 * 
 * Created on 16-May-2011, 14:41:08
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
