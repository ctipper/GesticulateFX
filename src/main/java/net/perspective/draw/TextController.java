/*
 * TextController.java
 * 
 * Created on Mar 9, 2016 10:50:54 AM
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.Editor;
import net.perspective.draw.text.Styler;

/**
 * 
 * @author ctipper
 */

@Singleton
public class TextController {

    private final Provider<DrawingArea> drawareaProvider;
    private final Provider<CanvasView> viewProvider;
    private final Provider<Editor> plainTextEditorProvider;
    private final Provider<Editor> richTextEditorProvider;
    private Editor editor;
    private Styler styler;
    private boolean isRichText;

    public static final int FONT_BOLD = 1;
    public static final int FONT_ITALIC = 2;
    public static final int FONT_UNDERLINED = 4;

    /** 
     * Creates a new instance of <code>TextController</code>
     */
    @Inject
    public TextController(Provider<DrawingArea> drawareaProvider, Provider<CanvasView> viewProvider,
            @Named("plaintext") Provider<Editor> plainTextEditorProvider,
            @Named("richtext") Provider<Editor> richTextEditorProvider) {
        this.drawareaProvider = drawareaProvider;
        this.viewProvider = viewProvider;
        this.plainTextEditorProvider = plainTextEditorProvider;
        this.richTextEditorProvider = richTextEditorProvider;
        this.editor = plainTextEditorProvider.get();
    }

    /**
     * Define the editor
     * 
     * @param isRichText enable formatted text
     */
    public void enableRichText(boolean isRichText) {
        if (isRichText) {
            this.editor = richTextEditorProvider.get();
        } else {
            this.editor = plainTextEditorProvider.get();
        }
        if (editor instanceof Styler styler1) {
            this.styler = styler1;
        } else {
            this.styler = null;
        }
        this.isRichText = isRichText;
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
        item.setFont(drawareaProvider.get().getFontFamily());
        item.setStyle(drawareaProvider.get().getFontStyle());
        item.setSize(drawareaProvider.get().getFontSize());
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
        item.setFont(drawareaProvider.get().getFontFamily());
        item.setStyle(drawareaProvider.get().getFontStyle());
        item.setSize(drawareaProvider.get().getFontSize());
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
        item.setFont(drawareaProvider.get().getFontFamily());
        item.setStyle(drawareaProvider.get().getFontStyle());
        item.setSize(drawareaProvider.get().getFontSize());
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
        if (viewProvider.get().isEditing()) {
            Text item = (Text) viewProvider.get().getDrawings().get(viewProvider.get().getSelected());
            editor.editText(item);
            editor.cutText();
            editor.commitText(item);
            item.setDimensions();
        }
        viewProvider.get().moveSelection(viewProvider.get().getSelected());
    }

    /**
     * Copy the selected text
     */
    public void copySelectedText() {
        if (viewProvider.get().isEditing()) {
            Text item = (Text) viewProvider.get().getDrawings().get(viewProvider.get().getSelected());
            editor.editText(item);
            editor.copyText();
            editor.commitText(item);
        }
    }

    /**
     * Paste the selected text
     */
    public void pasteSelectedText() {
        if (viewProvider.get().isEditing()) {
            Text item = (Text) viewProvider.get().getDrawings().get(viewProvider.get().getSelected());
            editor.editText(item);
            editor.pasteText();
            editor.commitText(item);
            item.setDimensions();
        }
        viewProvider.get().moveSelection(viewProvider.get().getSelected());
    }

    /**
     * Format the selected text
     *
     * See {@link net.perspective.draw.text.RichTextEditor} and
     * {@link net.perspective.draw.text.TextEditor}
     *
     * @param format text format property
     */
    public void formatSelectedText(int format) {
        this.formatSelectedRichText(format);
        viewProvider.get().moveSelection(viewProvider.get().getSelected());
    }

    /**
     * Format selected text
     *
     * See {@link net.perspective.draw.text.RichTextEditor}
     *
     * @param format text format property
     */
    private void formatSelectedRichText(int format) {
        if (viewProvider.get().isEditing()) {
            Text item = (Text) viewProvider.get().getDrawings().get(viewProvider.get().getSelected());
            editor.editText(item);
            styler.clearStoredMarks();
            Set<String> styles = styler.detectStyles();
            switch (format) {
                case FONT_BOLD -> {
                    if (styles.contains("b")) {
                        styler.removeStyle("b");
                    } else {
                        styler.createStyle("b");
                    }
                }
                case FONT_ITALIC -> {
                    if (styles.contains("i")) {
                        styler.removeStyle("i");
                    } else {
                        styler.createStyle("i");
                    }
                }
                case FONT_UNDERLINED -> {
                    if (styles.contains("u")) {
                        styler.removeStyle("u");
                    } else {
                        styler.createStyle("u");
                    }
                }
                default -> {
                }
            }
            editor.commitText(item);
            item.setDimensions();
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

    /**
     * Formatted text is enabled
     *
     * @return formatted text is enabled
     */
    public boolean isRichText() {
        return isRichText;
    }

}
