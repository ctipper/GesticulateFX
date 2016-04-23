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
import javax.inject.Inject;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */
public class CanvasTransferHandler {

    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.DrawItem";
    DataFlavor dataFlavor;
    @Inject private DrawingArea drawarea;
    private double shift;

    public static int COPY = 1;
    public static int MOVE = 2;

    private static final Logger logger = LoggerFactory.getLogger(CanvasTransferHandler.class.getName());

    public CanvasTransferHandler() {
        //Try to create a DataFlavor for drawItems.
        try {
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            logger.warn("mimeType failed in CanvasTransferHandler");
        }
        shift = 10.0;
    }

    public boolean importData(Transferable t) {
        DrawItem item;

        if (hasDrawItemFlavor(t.getTransferDataFlavors())) {
            try {
                if (t.getTransferData(dataFlavor) instanceof Figure) {
                    item = (Figure) t.getTransferData(dataFlavor);
                    // add item to Canvas
                    item.moveShape(shift, shift);
                    drawarea.getView().appendItemToCanvas(item);
                }
                shift = shift + 20.0;
                logger.debug("Item added to canvas");
                return true;
            } catch (UnsupportedFlavorException e) {
                logger.warn("importData: unsupported data flavor");
            } catch (IOException e) {
                logger.warn("importData: I/O exception");
            }
        }
        logger.trace("ImportData");
        return false;
    }

    protected Transferable createTransferable() {
        int selected = drawarea.getView().getSelected();
        if (selected == -1) {
            return null;
        }
        DrawItem data = drawarea.getView().getDrawings().get(selected);
        logger.trace("Item createTransferable");
        return new DrawItemTransferable(data);
    }

    protected void exportDone(Transferable data, int action) {
        if (action == MOVE) {
            drawarea.getView().deleteSelectedItem();
            logger.debug("Removed selected item");
            shift = 0;
        } else {
            shift = 20.0;
            logger.debug("Copied selected item");
        }
        logger.trace("ExportDone");
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
