/*
 * CanvasTransferHandler.java
 * 
 * Created on Nov 16, 2013 5:38:34 PM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Grouped;
import net.perspective.draw.geom.Picture;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class CanvasTransferHandler {

    @Inject private Injector injector;
    String mimeType = DataFlavor.javaSerializedObjectMimeType
        + ";class=net.perspective.draw.geom.DrawItem";
    DataFlavor dataFlavor;
    @Inject private CanvasView view;
    private double shift;

    public static int COPY = 1;
    public static int MOVE = 2;

    private static final Logger logger = LoggerFactory.getLogger(CanvasTransferHandler.class.getName());

    @Inject
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
                if (t.getTransferData(dataFlavor) instanceof DrawItem) {
                    item = (DrawItem) t.getTransferData(dataFlavor);
                    // add item to Canvas
                    item.moveTo(shift, shift);
                    item = checkDrawings(item);
                    view.appendItemToCanvas(item);
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
        int selected = view.getSelected();
        if (selected == -1) {
            return null;
        }
        DrawItem data = view.getDrawings().get(selected);
        logger.trace("Item createTransferable");
        return new DrawItemTransferable(data);
    }

    protected void exportDone(Transferable data, int action) {
        if (action == MOVE) {
            view.deleteSelectedItem();
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

    private DrawItem checkDrawings(DrawItem drawing) {
        if (drawing instanceof Picture) {
            DrawItem item = injector.getInstance(Picture.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        }
        
        if (drawing instanceof Grouped) {
            DrawItem item = new Grouped();
            for (DrawItem shape : ((Grouped) drawing).getShapes()) {
                ((Grouped) item).addShape(checkDrawings(shape));
            }
            drawing = item;
        }

        return drawing;
    }

}
