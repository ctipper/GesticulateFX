/*
 * TextController.java
 * 
 * Created on Mar 9, 2016 10:50:54 AM
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
package net.perspective.draw;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;

/**
 * 
 * @author ctipper
 */

@Singleton
public class TextController {

    private final DrawingArea drawarea;
    private final Provider<CanvasView> view;
    private final ApplicationController controller;
    @Inject Editor editor;

    public static final int FONT_BOLD = 1;
    public static final int FONT_ITALIC = 2;
    public static final int FONT_UNDERLINED = 4;

    /** 
     * Creates a new instance of <code>TextController</code>
     */
    @Inject
    public TextController(DrawingArea drawarea, Provider<CanvasView> view,
            ApplicationController controller) {
        this.drawarea = drawarea;
        this.view = view;
        this.controller = controller;
    }

    /**
     * Define the editor
     * 
     * @param isRichText enable formatted text
     */
    public void enableRichText(boolean isRichText) {
        if (!view.get().isEditing()) {
            // this.editor = editorProvider.get();
        }
    }

    /**
     * Get the editor
     * 
     * @return the {@link net.perspective.draw.text.Editor}
     */
    public Editor getEditor() {
        return editor;
    }

    /**
     * Initialise a text item
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     * @return the {@link net.perspective.draw.geom.Text}
     */
    public Text initializeItem(Text item) {
        item.setFont(drawarea.getFontFamily());
        item.setStyle(drawarea.getFontStyle());
        item.setSize(drawarea.getFontSize());
        editor.editText(item);
        editor.commitText(item);
        editor.setCaretStart(0);
        editor.setCaretEnd(editor.readPlainText().length());
        item.setDimensions();
        return item;
    }

    /**
     * Edit a text item at caret
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     * @param start start position
     */
    public void editItem(Text item, int start) {
        item.setFont(drawarea.getFontFamily());
        item.setStyle(drawarea.getFontStyle());
        item.setSize(drawarea.getFontSize());
        editor.editText(item);
        editor.commitText(item);
        editor.setCaretStart(start);
        editor.setCaretEnd(start);
        item.setDimensions();
    }

    /**
     * Edit item and select all
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    public void editItem(Text item) {
        item.setFont(drawarea.getFontFamily());
        item.setStyle(drawarea.getFontStyle());
        item.setSize(drawarea.getFontSize());
        editor.editText(item);
        editor.commitText(item);
        editor.setCaretStart(0);
        editor.setCaretEnd(editor.readPlainText().length());
        item.setDimensions();
    }

    /**
     * Cut the selected text
     */
    public void cutSelectedText() {
        if (view.get().isEditing()) {
            Text item = (Text) view.get().getDrawings().get(view.get().getSelected());
            editor.editText(item);
            editor.cutText();
            editor.commitText(item);
            item.setDimensions();
        }
        view.get().moveSelection(view.get().getSelected());
    }

    /**
     * Copy the selected text
     */
    public void copySelectedText() {
        if (view.get().isEditing()) {
            Text item = (Text) view.get().getDrawings().get(view.get().getSelected());
            editor.editText(item);
            editor.copyText();
            editor.commitText(item);
        }
    }

    /**
     * Paste the selected text
     */
    public void pasteSelectedText() {
        if (view.get().isEditing()) {
            Text item = (Text) view.get().getDrawings().get(view.get().getSelected());
            editor.editText(item);
            editor.pasteText();
            editor.commitText(item);
            item.setDimensions();
        }
        view.get().moveSelection(view.get().getSelected());
    }

    /**
     * Format the selected text
     * 
     * see {@link net.perspective.draw.text.RichTextEditor} and 
     * {@link net.perspective.draw.text.TextEditor}
     * 
     * @param format text format property
     */
    public void formatSelectedText(int format) {
        this.formatSelected(format);
        view.get().moveSelection(view.get().getSelected());
    }

    /**
     * Format selected text item
     * 
     * @param format  text formate property
     */
    private void formatSelected(int format) {
        switch (format) {
            case FONT_BOLD -> {
                if ((drawarea.getFontStyle() & FONT_BOLD) == FONT_BOLD) {
                    controller.getBoldProperty().setValue(Boolean.FALSE);
                } else {
                    controller.getBoldProperty().setValue(Boolean.TRUE);
                }
            }
            case FONT_ITALIC -> {
                if ((drawarea.getFontStyle() & FONT_ITALIC) == FONT_ITALIC) {
                    controller.getItalicProperty().setValue(Boolean.FALSE);
                } else {
                    controller.getItalicProperty().setValue(Boolean.TRUE);
                }
            }
            case FONT_UNDERLINED -> {
                if ((drawarea.getFontStyle() & FONT_UNDERLINED) == FONT_UNDERLINED) {
                    controller.getUnderlinedProperty().setValue(Boolean.FALSE);
                } else {
                    controller.getUnderlinedProperty().setValue(Boolean.TRUE);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Get the clipboard text
     * 
     * @return the text
     */
    public String getClipboard() {
        return editor.getClipboard();
    }

    /**
     * Set the clipboard text
     * 
     * @param str the text
     */
    public void setClipboard(String str) {
        editor.setClipboard(str);
    }

}
