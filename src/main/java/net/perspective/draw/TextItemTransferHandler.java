/**
 * CanvasTransferHandler.java
 * 
 * Created on May 22, 2010, 2:02:19 PM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

/**
 * 
 * @author ctipper
 */

public class TextItemTransferHandler {

    @Inject private Injector injector;

    public static int COPY = 1;
    public static int MOVE = 2;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CanvasView.class.getName());

    @Inject
    public TextItemTransferHandler() {
    }

    public boolean canImport(DataFlavor[] flavors) {
        return hasStringFlavor(flavors);
    }

    public boolean importData(Transferable t) {

        if (!canImport(t.getTransferDataFlavors())) {
            return false;
        }

        if (hasStringFlavor(t.getTransferDataFlavors())) {
            try {
                String str = (String) t.getTransferData(DataFlavor.stringFlavor);
                injector.getInstance(TextController.class).setClipboard(str);
                injector.getInstance(TextController.class).pasteSelectedText();
                logger.debug("Pasted text");
                return true;
            } catch (UnsupportedFlavorException e) {
                logger.warn("importData: Unsupported data flavor.");
            } catch (IOException e) {
                logger.warn("importData: I/O exception.");
            }
        }
        logger.trace("ImportData");
        return false;
    }

    protected Transferable createTransferable() {
        injector.getInstance(TextController.class).copySelectedText();
        String data = injector.getInstance(TextController.class).getClipboard();
        logger.trace("Transfer data: {}", data);
        return new StringSelection(data);
    }

    protected void exportDone(Transferable data, int action) {
        if (action == MOVE) {
            injector.getInstance(TextController.class).cutSelectedText();
            logger.debug("Removed selected text");
        } else {
            logger.debug("Copied selected text");
        }
        logger.trace("ExportDone");
    }

    protected boolean hasStringFlavor(DataFlavor[] flavors) {
        for (DataFlavor f : flavors) {
            if (DataFlavor.stringFlavor.equals(f)) {
                return true;
            }
        }
        return false;
    }

}
