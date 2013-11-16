/*
 * CanvasTransferHandler.java
 * 
 * Created on Nov 16, 2013 5:38:34 PM
 * 
 */
package net.perspective.draw;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import net.perspective.draw.geom.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class CanvasTransferHandler {

    DrawingCanvas canvas;
    String mimeType = DataFlavor.javaSerializedObjectMimeType
            + ";class=net.perspective.draw.geom.Figure";
    DataFlavor dataFlavor;
    private double shift;
    
    public static int COPY = 1;
    public static int MOVE = 2;
    
    private static final Logger logger = LoggerFactory.getLogger(CanvasTransferHandler.class.getName());
    
    public CanvasTransferHandler(DrawingCanvas c) {
        canvas = c;
        //Try to create a DataFlavor for drawItems.
        try {
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            logger.warn("mimeType failed in CanvasTransferHandler");
        }
        shift = 10.0;
    }

    public boolean importData(Transferable t) {
        Figure item;

        if (hasDrawItemFlavor(t.getTransferDataFlavors())) {
            try {
                if (t.getTransferData(dataFlavor) instanceof Figure) {
                    item = (Figure) t.getTransferData(dataFlavor);
                    // add item to Canvas
                    item.moveFigure(shift, shift);
                    canvas.getView().appendItemToCanvas(item);
                } 
                shift = shift + 20.0;
                logger.debug("Figure added to canvas");
                return true;
            } catch (UnsupportedFlavorException e) {
                logger.warn("importData: unsupported data flavor");
            } catch (IOException e) {
                logger.warn("importData: I/O exception");
            }
        }
        logger.debug("ImportData");
        return false;
    }

    protected Transferable createTransferable() {
        int selected = canvas.getView().getSelected();
        if (selected == -1) {
            return null;
        }
        Figure data = canvas.getView().getDrawings().get(selected);
        logger.debug("Created Transferable");
        return new FigureTransferable(data);
    }

    protected void exportDone(Transferable data, int action) {
        if (action == MOVE) {
            canvas.getView().deleteSelectedItem();
            logger.debug("Deleted selected item");
            shift = 0;
        }
        if (action == COPY) {
            shift = 20.0;
        }
        logger.debug("ExportDone");
    }

    protected boolean hasDrawItemFlavor(DataFlavor[] flavors) {
        if (dataFlavor == null) {
            return false;
        }

        for (DataFlavor f : flavors) {
            if (dataFlavor.equals(f)) {
                return true;
            }
        }
        return false;
    }
}
