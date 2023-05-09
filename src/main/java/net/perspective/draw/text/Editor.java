/*
 * Editor.java
 * 
 * Specifies operations for inserting and removing text from Text items.
 * 
 * Created on May 7, 2013 1:02:02 PM
 * 
 */
package net.perspective.draw.text;

import net.perspective.draw.geom.Text;

/**
 * 
 * @author ctipper
 */

public interface Editor {

    /**
     * Load Text content into editor
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    void editText(Text item);

    /**
     * Serialise editor content to Text
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    void commitText(Text item);

    /**
     * Return unformatted text to client
     * 
     * @return text Some textual data
     */
    String readPlainText();

    /**
     * Return the length of text content
     * 
     * @return length 
     */
    int getLength();

    /**
     * clipboard cut action
     */
    void cutText();

    /**
     * clipboard copy action
     */
    void copyText();

    /**
     * clipboard paste action
     */
    void pasteText();

    /**
     * keyboard backspace action
     */
    void backSpace();

    /**
     * keyboard delete action
     */
    void deleteChar();

    /**
     * keyboard type action
     * 
     * @param c The character to insert
     */
    void insertChar(char c);

    /**
     * insert text action
     * 
     * @param string text
     */
    void insertText(String string);

    /**
     * Set the starting caret position
     * 
     * @param s A text index
     */
    void setCaretStart(int s);

    /**
     * Get the starting caret position
     * 
     * @return The start index
     */
    int getCaretStart();

    /**
     * Set the ending caret position
     * 
     * @param e A text index
     */
    void setCaretEnd(int e);

    /**
     * Get the ending caret position
     * 
     * @return The end index
     */
    int getCaretEnd();

    /**
     * Set the clipboard text
     * 
     * @param s Some textual data
     */
    void setClipboard(String s);

    /**
     * Get the clipboard text
     * 
     * @return Some textual data
     */
    String getClipboard();
}
