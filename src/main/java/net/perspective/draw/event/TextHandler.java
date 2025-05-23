/**
 * TextHandler.java
 * 
 * Created on 27 May 2021 14:26:48
 * 
 */

/**
 * Copyright (c) 2025 Christopher Tipper
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
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.scene.Cursor;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.TextController;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.TextItemBehaviour;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class TextHandler implements Handler {

    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    @Inject private BehaviourContext context;
    @Inject private TextController textController;

    private static final Logger logger = LoggerFactory.getLogger(TextHandler.class);

    /**
     * Creates a new instance of <code>TextHandler</code> 
     */
    @Inject
    public TextHandler() {
    }

    @Override
    public void upEvent() {
        // noop
    }

    @Override
    public void downEvent() {
        if (view.isEditing()) {
            // Text isEditing code here
            if (!listener.getRightClick()) {
                DrawItem item = view.getDrawings().get(view.getSelected());
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                context.select(item, 0);
            }
        }
    }

    @Override
    public void clickEvent() {
        if (!view.isEditing()) {
            Text item = new Text(listener.getTempX(), listener.getTempY());
            item = textController.initializeItem(item);
            item.updateProperties(drawarea);
            final javafx.scene.text.TextFlow flow = item.tf;
            flow.setFocusTraversable(true);
            flow.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        if (flow.getScene() != null && flow.getScene().getWindow() != null && flow.getScene().getWindow().isShowing()) {
                            flow.requestFocus();
                        }
                    });
                }
            });
            view.setNewItem(item);
            view.resetNewItem();
            int i = view.getDrawings().size() - 1;
            view.setSelected(i);
            view.setEditing(KeyHandlerType.TEXT);
            view.setTextHighlight(i);
            Platform.runLater(() -> {
                logger.debug("TextFlow in draw() focused: {}", flow.isFocused());
                if (flow.getScene() != null) {
                    logger.debug("Scene focus owner: {}", flow.getScene().getFocusOwner());
                } else {
                    logger.debug("TextFlow in draw() is not in a scene yet.");
                }
            });
        } else if (view.getSelected() != -1) {
            view.updateSelectedItem();
            view.moveSelection(view.getSelected());
        }
    }

    @Override
    public void hoverEvent() {
        if (view.getSelected() != -1) {
            DrawItem item = view.getDrawings().get(view.getSelected());
            if (item instanceof Text) {
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                context.hover(item);
            }
        } else {
            drawarea.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void dragEvent() {
        if (view.isEditing()) {
            // Text isSelecting code here
            if (view.getSelected() != -1) {
                DrawItem item = view.getDrawings().get(view.getSelected());
                context.setBehaviour(injector.getInstance(TextItemBehaviour.class));
                context.alter(item, 0, 0);
            }
        }
    }

    @Override
    public void zoomEvent() {
        // noop
    }

}
