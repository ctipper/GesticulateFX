/*
 * TextKeyHandler.java
 * 
 * Created on 15 May 2023 17:28:45
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
package net.perspective.draw.event.keyboard;

import javax.inject.Inject;
import javax.inject.Provider;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.TextController;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;

/**
 *
 * @author ctipper
 */

public class TextKeyHandler implements KeyHandler {

    private final DrawingArea drawarea;
    private final CanvasView view;
    private final ApplicationController controller;
    @Inject KeyListener keylistener;
    @Inject Provider<TextController> textControllerProvider;
    private int selection = -1;
    private boolean selectToLeft = false;
    private DrawingType drawingtype;
    private HandlerType handlerType;
    private boolean isEditing = false;
    private boolean pressed = false;

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    /**
     * Create a new instance of <code>TextKeyHandler</code>
     */
    @Inject
    public TextKeyHandler(DrawingArea drawarea, CanvasView view, ApplicationController controller) {
        this.drawarea = drawarea;
        this.view = view;
        this.controller = controller;
        handlerType = HandlerType.SELECTION;
    }

    /**
     * Pressed event
     */
    @Override
    public void keyPressed() {
        Editor editor = textControllerProvider.get().getEditor();
        selection = view.getSelected();
        if ((selection != -1) && (view.isEditing())) {
            DrawItem drawitem = view.getDrawings().get(selection);
            if (drawitem instanceof Text item) {
                // edit
                int textlength = editor.getLength();
                switch (keylistener.getKeyCode()) {
                    case A -> {
                        // Select all
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            editor.setCaretStart(0);
                            editor.setCaretEnd(textlength);
                        }
                    }
                    case B -> {
                        // Bold
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            textControllerProvider.get().formatSelectedText(TextController.FONT_BOLD);
                        }
                    }
                    case I -> {
                        // Italic
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            textControllerProvider.get().formatSelectedText(TextController.FONT_ITALIC);
                        }
                    }
                    case U -> {
                        // Underline
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            textControllerProvider.get().formatSelectedText(TextController.FONT_UNDERLINED);
                        }
                    }
                    case X -> {
                        // cut selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            view.cutTextItem();
                        }
                    }
                    case C -> {
                        // copy selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            view.copyTextItem();
                        }
                    }
                    case V -> {
                        // paste selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            view.pasteTextItem();
                        }
                    }
                    case PLUS, ADD, EQUALS -> {
                        // zoom font up
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            controller.incrementFontSize();
                        }
                    }
                    case MINUS, SUBTRACT -> {
                        // zoom font down
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            controller.decrementFontSize();
                        }
                    }
                    case SEMICOLON -> {
                        // Insert date
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            view.insertDateAndTime(item);
                        }
                    }
                    case BACK_SPACE -> {
                        // edit
                        editor.backSpace();
                        editor.commitText(item);
                    }
                    case KP_LEFT, LEFT -> {
                        if (keylistener.isIsShiftDown()) {
                            if (editor.getCaretStart() == editor.getCaretEnd()) {
                                selectToLeft = true;
                            }
                            if (selectToLeft) {
                                if (editor.getCaretStart() > 0) {
                                    editor.setCaretStart(editor.getCaretStart() - 1);
                                }
                            } else if (editor.getCaretEnd() > 0) {
                                editor.setCaretEnd(editor.getCaretEnd() - 1);
                            }
                        } else {
                            if (editor.getCaretStart() == editor.getCaretEnd()) {
                                if (editor.getCaretStart() > 0) {
                                    editor.setCaretStart(editor.getCaretStart() - 1);
                                }
                            }
                            editor.setCaretEnd(editor.getCaretStart());
                        }
                    }
                    case KP_RIGHT, RIGHT -> {
                        if (keylistener.isIsShiftDown()) {
                            if (editor.getCaretStart() == editor.getCaretEnd()) {
                                selectToLeft = false;
                            }
                            if (selectToLeft) {
                                if (editor.getCaretStart() < textlength) {
                                    editor.setCaretStart(editor.getCaretStart() + 1);
                                }
                            } else if (editor.getCaretEnd() < textlength) {
                                editor.setCaretEnd(editor.getCaretEnd() + 1);
                            }
                        } else if (editor.getCaretStart() == editor.getCaretEnd()) {
                            if (editor.getCaretStart() < textlength) {
                                editor.setCaretStart(editor.getCaretStart() + 1);
                            }
                            editor.setCaretEnd(editor.getCaretStart());
                        } else {
                            editor.setCaretStart(editor.getCaretEnd());
                        }
                    }
                    case DELETE -> {
                        // edit
                        editor.deleteChar();
                        editor.commitText(item);
                    }
                    case HOME -> {
                        editor.setCaretStart(0);
                        if (!keylistener.isIsShiftDown()) {
                            editor.setCaretEnd(0);
                        }
                    }
                    case END -> {
                        if (!keylistener.isIsShiftDown()) {
                            editor.setCaretStart(textlength);
                        }
                        editor.setCaretEnd(textlength);
                    }
                    case ENTER -> {
                        // commit
                        editor.commitText(item);
                        view.updateSelectedItem();
                        view.setSelected(-1);
                        view.setEditing(KeyHandlerType.MOVE);
                        editor.setCaretStart(0);
                        editor.setCaretEnd(0);
                    }
                    default -> {
                    }
                }
                item.setDimensions();
                view.updateSelectedItem();
                view.moveSelection(view.getSelected());
            }
        }
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                if (!pressed) {
                    drawingtype = drawarea.getDrawType().orElse(null);
                    handlerType = drawarea.getHandlerType();
                    selection = view.getSelected();
                    drawarea.setDrawType(null);
                    drawarea.changeHandlers(HandlerType.SELECTION);
                    isEditing = view.isEditing();
                    view.setEditing(false);
                    pressed = true;
                }
                drawarea.setMultiSelectEnabled(true);
            }
            default -> {
            }
        }
    }

    /**
     * Released event
     */
    @Override
    public void keyReleased() {
        switch (keylistener.getKeyCode()) {
            case ALT, ALT_GRAPH -> {
                drawarea.setDrawType(drawingtype);
                drawarea.changeHandlers(handlerType);
                drawarea.setMultiSelectEnabled(false);
                view.setEditing(isEditing);
                view.setSelected(selection);
                pressed = false;
            }
            default -> {
            }
        }
    }

    /**
     * Typed event
     */
    @Override
    public void keyTyped() {
        Editor editor = textControllerProvider.get().getEditor();
        selection = view.getSelected();
        if ((selection != -1) && (view.isEditing())) {
            DrawItem drawitem = view.getDrawings().get(selection);
            if (drawitem instanceof Text item) {
                String keyChar = keylistener.getKeyChar();
                /**
                 * On Windows 11 event.keyCode() will likely return &lt;DEL&gt; as a valid character
                 * and this needs to be rejected before inserting into the jdom used to sanitise
                 * input
                 *
                 * @see {@link net.perspective.draw.geom.TextFormatter#readFxText}
                 */
                if (!keyChar.isEmpty()) {
                    boolean illegalChar = false;
                    for (Integer t : keyChar.codePoints().toArray()) {
                        if (Character.getType(t) == Character.CONTROL) {
                            illegalChar = true;
                        }
                    }
                    if (!illegalChar && !keylistener.isIsControlDown() && !keylistener.isIsMetaDown()) {
                        // edit
                        editor.insertText(keyChar);
                        editor.commitText(item);
                    }
                    item.setDimensions();
                    view.updateSelectedItem();
                    view.moveSelection(view.getSelected());
                }
            }
        }
    }

}
