/*
 * TextController.java
 * 
 * Created on Mar 9, 2016 10:50:54 AM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.geom.Text;
import net.perspective.draw.text.*;

/**
 * 
 * @author ctipper
 */

@Singleton
public class TextController {

    @Inject private Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private ApplicationController controller;
    private Editor editor;

    public static final int FONT_BOLD = 1;
    public static final int FONT_ITALIC = 2;
    public static final int FONT_UNDERLINED = 4;

    /** 
     * Creates a new instance of <code>TextController</code>
     */
    @Inject
    public TextController() {
    }

    /**
     * Define the editor
     * 
     * @param isRichText enable formatted text
     */
    public void enableText(boolean isRichText) {
        if (!view.isEditing()) {
            this.editor = injector.getInstance(TextEditor.class);
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
//        item.setDimensions(drawarea);
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
//        item.setDimensions(drawarea);
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
//        item.setDimensions(drawarea);
    }

    /**
     * Cut the selected text
     */
    public void cutSelectedText() {
        if (view.isEditing()) {
            Text item = (Text) view.getDrawings().get(view.getSelected());
            editor.editText(item);
            editor.cutText();
            editor.commitText(item);
//            item.setDimensions(drawarea);
        }
    }

    /**
     * Copy the selected text
     */
    public void copySelectedText() {
        if (view.isEditing()) {
            Text item = (Text) view.getDrawings().get(view.getSelected());
            editor.editText(item);
            editor.copyText();
            editor.commitText(item);
        }
    }

    /**
     * Paste the selected text
     */
    public void pasteSelectedText() {
        if (view.isEditing()) {
            Text item = (Text) view.getDrawings().get(view.getSelected());
            editor.editText(item);
            editor.pasteText();
            editor.commitText(item);
//            item.setDimensions(drawarea);
        }
//        drawarea.repaint();
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
//        drawarea.repaint();
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
                    drawarea.updateFontStyle(drawarea.getFontStyle() ^ FONT_BOLD);
                } else {
                    controller.getBoldProperty().setValue(Boolean.TRUE);
                    drawarea.updateFontStyle(drawarea.getFontStyle() | FONT_BOLD);
                }
            }
            case FONT_ITALIC -> {
                if ((drawarea.getFontStyle() & FONT_ITALIC) == FONT_ITALIC) {
                    controller.getItalicProperty().setValue(Boolean.FALSE);
                    drawarea.updateFontStyle(drawarea.getFontStyle() ^ FONT_ITALIC);
                } else {
                    controller.getItalicProperty().setValue(Boolean.TRUE);
                    drawarea.updateFontStyle(drawarea.getFontStyle() | FONT_ITALIC);
                }
            }
            case FONT_UNDERLINED -> {
                if ((drawarea.getFontStyle() & FONT_UNDERLINED) == FONT_UNDERLINED) {
                    controller.getUnderlinedProperty().setValue(Boolean.FALSE);
                    drawarea.updateFontStyle(drawarea.getFontStyle() ^ FONT_UNDERLINED);
                } else {
                    controller.getUnderlinedProperty().setValue(Boolean.TRUE);
                    drawarea.updateFontStyle(drawarea.getFontStyle() | FONT_UNDERLINED);
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
