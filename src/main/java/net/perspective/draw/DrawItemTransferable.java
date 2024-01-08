/*
 * DrawItemTransferable.java
 * 
 * Created on Nov 16, 2013 3:56:35 PM
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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
package net.perspective.draw;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import net.perspective.draw.geom.DrawItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class DrawItemTransferable implements Transferable {

    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.DrawItem";
    DataFlavor dataFlavor;
    private final ByteArrayOutputStream out;

    private static final Logger logger = LoggerFactory.getLogger(DrawItemTransferable.class.getName());

    public DrawItemTransferable(DrawItem item) {
        out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream i = new ObjectOutputStream(out);
            i.writeObject(item);
        } catch (IOException e) {
            logger.warn("I/O Exception " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    logger.error(null, ex);
                }
            }
        }

        //Try to create a DataFlavor for Figures
        try {
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            logger.warn("mimeType failed in DrawItemTransferable");
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(out.toByteArray());
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            if (dataFlavor.equals(flavor)) {
                DrawItem item = (DrawItem) in.readObject();
                return item;
            } else if (DataFlavor.imageFlavor.equals(flavor)) {
                logger.debug(flavor.getMimeType());
                if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(flavor)) {
                    java.awt.Image image = (java.awt.Image) Toolkit.getDefaultToolkit().getSystemClipboard().getData(flavor);
                    return image;
                }
            }
        } catch (IOException e) {
            logger.warn("I/O Exception " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.warn("ClassNotFound");
        } finally {
            try {
                bin.close();
            } catch (IOException ex) {
                logger.error(null, ex);
            }
        }
        return null;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = {dataFlavor, DataFlavor.imageFlavor};
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (dataFlavor.equals(flavor)) {
            return true;
        } else if (DataFlavor.imageFlavor.equals(flavor)) {
            return true;
        }
        return false;
    }

}
