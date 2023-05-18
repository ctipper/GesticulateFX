/*
 * KeyBoardHandler.java
 * 
 * Created on March 2008
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
package net.perspective.draw.event.keyboard;

import java.awt.datatransfer.Transferable;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.CanvasView;
import net.perspective.draw.TextController;
import net.perspective.draw.TextItemTransferHandler;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;

/**
 * 
 * @author ctipper
 */

public class TextKeyHandler implements KeyHandler {

    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    @Inject private KeyListener keylistener;
    @Inject private TextController textController;
    @Inject private TextItemTransferHandler transferhandler;
    private Transferable clipboard;
    private int selection = -1;
    private boolean selectToLeft = false;

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    /**
     * Create a new instance of <code>TextKeyHandler</code>
     */
    @Inject
    public TextKeyHandler() {
    }

    /**
     * Pressed event
     */
    @Override
    public void keyPressed() {
        Editor editor = textController.getEditor();
        selection = view.getSelected();
        if ((selection != -1) && (view.isEditing())) {
            DrawItem item = view.getDrawings().get(selection);
            if (item instanceof Text) {
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
                            textController.formatSelectedText(TextController.FONT_BOLD);
                        }
                    }
                    case I -> {
                        // Italic
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            textController.formatSelectedText(TextController.FONT_ITALIC);
                        }
                    }
                    case U -> {
                        // Underline
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            textController.formatSelectedText(TextController.FONT_UNDERLINED);
                        }
                    }
                    case X -> {
                        // cut selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            clipboard = transferhandler.createTransferable();
                            transferhandler.exportDone(clipboard, MOVE);
                        }
                    }
                    case C -> {
                        // copy selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            clipboard = transferhandler.createTransferable();
                            transferhandler.exportDone(clipboard, COPY);
                        }
                    }
                    case V -> {
                        // paste selected
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            if (clipboard != null) transferhandler.importData(clipboard);
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
                            view.insertDateAndTime((Text) item);
                        }
                    }
                    case BACK_SPACE -> {
                        // edit
                        editor.backSpace();
                        editor.commitText((Text) item);
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
                        editor.commitText(((Text) item));
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
                        editor.commitText(((Text) item));
                        view.updateSelectedItem();
                        view.setSelected(-1);
                        view.setEditing(KeyHandlerType.MOVE);
                        editor.setCaretStart(0);
                        editor.setCaretEnd(0);
                    }
                    default -> {
                    }
                }
                ((Text) item).setDimensions();
                view.updateSelectedItem();
                view.moveSelection(view.getSelected());
            }
        }
    }

    /**
     * Released event
     */
    @Override
    public void keyReleased() {
    }

    /**
     * Typed event
     */
    @Override
    public void keyTyped() {
        Editor editor = textController.getEditor();
        selection = view.getSelected();
        if ((selection != -1) && (view.isEditing())) {
            DrawItem item = view.getDrawings().get(selection);
            if (item instanceof Text) {
                String c = keylistener.getKeyChar();
                if (!c.isEmpty() && !keylistener.isIsControlDown() && !keylistener.isIsMetaDown()) {
                    // edit
                    editor.insertChar(c);
                    editor.commitText((Text) item);
                }
                ((Text) item).setDimensions();
                view.updateSelectedItem();
                view.moveSelection(view.getSelected());
            }
        }
    }

}
