/*
 * FigureTransferable.java
 * 
 * Created on Nov 16, 2013 3:56:35 PM
 * 
 */
package net.perspective.draw;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import net.perspective.draw.geom.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
public class FigureTransferable implements Transferable {

    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.Figure";
    DataFlavor dataFlavor;
    private final ByteArrayOutputStream out;

    private static final Logger logger = LoggerFactory.getLogger(FigureTransferable.class.getName());

    public FigureTransferable(Figure fig) {
        out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream item = new ObjectOutputStream(out);
            item.writeObject(fig);
        } catch (IOException e) {
            logger.warn("I/O Exception " + e.getMessage());
        }

        //Try to create a DataFlavor for Figures
        try {
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            logger.warn("mimeType failed in FigureTransferable");
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
            Figure item = (Figure) in.readObject();
            return item;
        } catch (IOException e) {
            logger.warn("I/O Exception");
        } catch (ClassNotFoundException e) {
            logger.warn("ClassNotFound");
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
