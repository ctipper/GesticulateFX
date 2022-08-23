/*
 * CanvasTransferHandler.java
 * 
 * Created on Nov 16, 2013 5:38:34 PM
 * 
 */

/**
 * Copyright (c) 2022 Christopher Tipper
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
import net.perspective.draw.geom.StreetMap;
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
                if (t.getTransferData(dataFlavor) instanceof DrawItem drawItem) {
                    item = drawItem;
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
        if (drawing instanceof Picture && !(drawing instanceof StreetMap)) {
            DrawItem item = injector.getInstance(Picture.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        } else if (drawing instanceof StreetMap) {
            DrawItem item = injector.getInstance(StreetMap.class);
            try {
                BeanUtils.copyProperties(item, drawing);
                drawing = item;
                ((StreetMap) drawing).filterHandlers();
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.trace(ex.getMessage());
            }
        }
        
        if (drawing instanceof Grouped grouped) {
            DrawItem item = new Grouped();
            for (DrawItem shape : grouped.getShapes()) {
                ((Grouped) item).addShape(checkDrawings(shape));
            }
            drawing = item;
        }

        return drawing;
    }

}
