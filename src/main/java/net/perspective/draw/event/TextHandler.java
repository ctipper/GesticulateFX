/**
 * TextHandler.java
 * 
 * Created on 27 May 2021 14:26:48
 * 
 */
package net.perspective.draw.event;

import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.geom.Text;

/**
 *
 * @author ctipper
 */

public class TextHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;

    /** Creates a new instance of <code>TextHandler</code> */
    public TextHandler() {
    }

    @Override
    public void upEvent() {
    }

    @Override
    public void downEvent() {
    }

    @Override
    public void clickEvent() {
        if (!view.isEditing()) {
            Text item = new Text(listener.getTempX(), listener.getTempY());
            // item = view.getTextController().initializeItem(item);
            item.updateProperties(drawarea);
            view.setNewItem(item);
            view.resetNewItem();
            int i = view.getDrawings().size() - 1;
            view.setSelected(i);
            // view.setEditing(KeyboardHandlerType.TEXT);
        } else if (view.getSelected() != -1) {
            view.updateSelectedItem();
            view.moveSelection(view.getSelected());
        }
    }

    @Override
    public void hoverEvent() {
    }

    @Override
    public void dragEvent() {
    }

}
