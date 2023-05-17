/**
 * TextItemBehaviour.java
 * 
 * Created on 15 May 2023 17:28:45
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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
package net.perspective.draw.event.behaviours;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.text.HitInfo;
import javafx.scene.text.TextFlow;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.TextController;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;

/**
 *
 * @author ctipper
 */

public class TextItemBehaviour implements ItemBehaviours {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    @Inject private TextController textController;

    /** 
     * Creates a new instance of <code>TextItemBehaviour</code> 
     */
    @Inject
    public TextItemBehaviour() {
    }

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        HitInfo currentHit;

        Editor editor = textController.getEditor();
        TextFlow layout = drawarea.getTextLayout(item);

        // currently fix for vertical TextFlow is not known
        Point2D point = new Point2D(listener.getTempX() - item.getTop()[0].x, listener.getTempY() - item.getTop()[0].y);
        currentHit = layout.hitTest(point);

        editor.setCaretStart(currentHit.getInsertionIndex());
        editor.setCaretEnd(editor.getCaretStart());
        view.setTextHighlight(index);
        return true;
    }

    @Override
    public void editItem(BehaviourContext context, DrawItem item, int index) {
        HitInfo currentHit;

        TextFlow layout = drawarea.getTextLayout(item);

        // currently fix for vertical TextFlow is not known
        Point2D point = new Point2D(listener.getTempX() - item.getTop()[0].x, listener.getTempY() - item.getTop()[0].y);
        currentHit = layout.hitTest(point);

        textController.editItem((Text) item, currentHit.getInsertionIndex());
        view.setSelected(index);
        view.setEditing(KeyHandlerType.TEXT);
        view.setTextHighlight(index);
    }

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        if (item.contains(listener.getTempX(), listener.getTempY()) && view.isEditing()) {
            drawarea.getScene().setCursor(Cursor.TEXT);
        } else {
            drawarea.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        HitInfo currentHit;

        Editor editor = textController.getEditor();
        TextFlow layout = drawarea.getTextLayout(item);

        // currently fix for vertical TextFlow is not known
        Point2D point = new Point2D(listener.getTempX() - item.getTop()[0].x, listener.getTempY() - item.getTop()[0].y);
        currentHit = layout.hitTest(point);

        int select = currentHit.getInsertionIndex();
        if (select > editor.getCaretStart()) {
            editor.setCaretEnd(select);
        } else if (select < editor.getCaretEnd()) {
            editor.setCaretStart(select);
        }
        view.setTextHighlight(view.getSelected());
    }
}
