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

import com.google.inject.Injector;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.TextController;
import net.perspective.draw.enums.KeyHandlerType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;

/**
 * 
 * @author ctipper
 */

public class TextKeyHandler implements KeyHandler {

    @Inject private Injector injector;
    @Inject private CanvasView view;
    @Inject private KeyListener keylistener;
    @Inject private TextController controller;
    private int selection = -1;
    private boolean selectToLeft = false;
    private int composedTextStartIndex = -1;
    private int composedTextEndIndex = -1;

    private static final boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    /**
     * Create a new instance of <code>TextKeyHandler</code>
     */
    @Inject
    public TextKeyHandler() {
    }

    /**
     * Pressed event
     * 
     * @param e a key event
     */
    @Override
    public void keyPressed() {
        Editor editor = controller.getEditor();
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
                            injector.getInstance(TextController.class).formatSelectedText(TextController.FONT_BOLD);
                        }
                    }
                    case I -> {
                        // Italic
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            injector.getInstance(TextController.class).formatSelectedText(TextController.FONT_ITALIC);
                        }
                    }
                    case U -> {
                        // Underline
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            injector.getInstance(TextController.class).formatSelectedText(TextController.FONT_UNDERLINED);
                        }
                    }
                    case SEMICOLON -> {
                        // Insert date
                        if (!MAC_OS_X && keylistener.isIsControlDown() || MAC_OS_X && keylistener.isIsMetaDown()) {
                            // view.insertDateAndTime((Text) item);
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
                view.moveSelection(view.getSelected());
            }
        }
    }

    /**
     * Released event
     * 
     * @param e a key event
     */
    @Override
    public void keyReleased() {
    }

    /**
     * Typed event
     * 
     * @param e a key event
     */
    @Override
    public void keyTyped() {
        Editor editor = controller.getEditor();
        selection = view.getSelected();
        if ((selection != -1) && (view.isEditing())) {
            DrawItem item = view.getDrawings().get(selection);
            if (item instanceof Text) {
                String c = keylistener.getKeyChar();
                if (!keylistener.isIsControlDown() || !keylistener.isIsMetaDown()) {
                    // edit
                    editor.insertChar(c);
                    editor.commitText((Text) item);
                }
                ((Text) item).setDimensions();
                view.moveSelection(view.getSelected());
            }
        }
    }

    /**
     * Input method handler inspired by 
     * javax.swing.text.JTextComponent.replaceInputMethodText()
     * 
     * @param e an input event
     */
//    @Override
//    public void inputMethodTextChanged(InputMethodEvent e) {
//        int commitCount = e.getCommittedCharacterCount();
//        AttributedCharacterIterator text = e.getText();
//        this.editor = controller.getEditor();
//        if ((view.getSelected() != -1) && (view.isEditing())) {
//            DrawItem item = view.getDrawings().get(view.getSelected());
//            if (item instanceof Text) {
//                // old composed text deletion
//                if (composedTextExists()) {
//                    for (int i = 0; i < composedTextEndIndex; i++) {
//                        // edit
//                        editor.backSpace();
//                        editor.commitText((Text) item);
//                    }
//                    ((Text) item).setDimensions();
//                    view.moveSelection(view.getSelected());
//                }
//
//                if (text != null) {
//                    text.first();
//                    int committedTextStartIndex = 0;
//                    int committedTextEndIndex = 0;
//
//                    // committed text insertion
//                    if (commitCount > 0) {
//                        for (char c = text.current(); commitCount > 0;
//                            c = text.next(), commitCount--) {
//                            // Remember latest committed text end index
//                            committedTextEndIndex++;
//                        }
//                    }
//
//                    // new composed text insertion
//                    composedTextEndIndex = 0;
//                    for (char c = text.first(); c != CharacterIterator.DONE; c = text.next(), composedTextEndIndex++) {
//                        if (Character.getType(c) != Character.CONTROL) {
//                            // edit
//                            editor.insertChar(c);
//                            editor.commitText((Text) item);
//                        }
//                        ((Text) item).setDimensions();
//                        view.moveSelection(view.getSelected());
//                    }
//
//                    // Save the latest committed text information
//                    if (committedTextStartIndex != committedTextEndIndex) {
//                        composedTextStartIndex = -1;
//                    } else {
//                        composedTextStartIndex = 0;
//                    }
//                }
//            }
//        }
//    }

    /**
     * Caret position changed
     * 
     * @param e in input method event
     */
//    @Override
//    public void caretPositionChanged(InputMethodEvent e) {
//    }

//    private boolean composedTextExists() {
//	return (composedTextStartIndex != -1);
//    }

}
