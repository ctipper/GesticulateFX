/**
 * TextHandler.java
 * 
 * Created on 27 May 2021 14:26:48
 * 
 */

/**
 * Copyright (c) 2022 e-conomist
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
