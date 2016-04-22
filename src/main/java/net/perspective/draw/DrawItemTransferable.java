/*
 * DrawItemTransferable.java
 * 
 * Created on Nov 16, 2013 3:56:35 PM
 * 
 */
package net.perspective.draw;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
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

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(out.toByteArray());
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            DrawItem item = (DrawItem) in.readObject();
            return item;
        } catch (IOException e) {
            logger.warn("I/O Exception");
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

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return dataFlavor.equals(flavor);
    }
}
