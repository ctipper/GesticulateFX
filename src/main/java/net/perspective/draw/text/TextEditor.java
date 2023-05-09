/*
 * TextEditor.java
 * 
 * Created on May 7, 2013 12:50:35 PM
 * 
 */
package net.perspective.draw.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.perspective.draw.geom.Text;

/**
 * 
 * @author ctipper
 */

public class TextEditor implements Editor {

    private String text, clipboard;
    private int caretstart, caretend;

    /**
     * Creates a new instance of <code>TextEditor</code>
     */
    public TextEditor() {
        caretstart = caretend = 0;
        clipboard = "";
    }

    /**
     * Load Text content into editor
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    @Override
    public void editText(Text item) {
        // Load Text content into editor
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(item.getText());
        if (matcher.find()) {
            String content = matcher.group(2);
            content = content.replaceAll("<(.|\n)*?>", "");
            content = content.replaceAll("&amp;", "&");
            content = content.replaceAll("&lt;", "<");
            content = content.replaceAll("&gt;", ">");
            text = content;
        } else {
            text = item.getText();
        }
    }

    /**
     * Serialise editor content to Text
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    @Override
    public void commitText(Text item) {
        // Serialise editor content to Text
        item.setText(text);
    }

    /**
     * Return unformatted text to client
     * 
     * @return text Some textual data
     */
    @Override
    public String readPlainText() {
        // Return unformatted text to client
        return text;
    }

    /**
     * Return the length of text content
     * 
     * @return length
     */
    @Override
    public int getLength() {
        // return length of text
        return text.length();
    }

    /**
     * clipboard cut action
     */
    @Override
    public void cutText() {
        String newText = text.substring(0, caretstart) + text.substring(caretend);
        clipboard = text.substring(caretstart, caretend);
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart;
    }

    /**
     * clipboard copy action
     */
    @Override
    public void copyText() {
        clipboard = text.substring(caretstart, caretend);
    }

    /**
     * clipboard paste action
     */
    @Override
    public void pasteText() {
        String startText = text.substring(0, caretstart);
        String endText = text.substring(caretend);
        String newText = startText + clipboard + endText;
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart + clipboard.length();
        caretstart = caretend;
    }

    /**
     * keyboard backspace action
     */
    @Override
    public void backSpace() {
        String newText;
        if (caretstart == caretend) {
            if (caretstart > 0) {
                newText = text.substring(0, caretstart - 1) + text.substring(caretend);
                caretstart = caretstart - 1;
            } else {
                newText = text.substring(0, caretstart) + text.substring(caretend);
            }
        } else {
            newText = text.substring(0, caretstart) + text.substring(caretend);
        }
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart;
    }

    /**
     * keyboard delete action
     */
    @Override
    public void deleteChar() {
        String newText;
        if (caretend < text.length()) {
            newText = text.substring(0, caretstart) + text.substring(caretend + 1);
        } else {
            newText = text.substring(0, caretstart) + text.substring(caretend);
        }
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart;
    }

    /**
     * keyboard type action
     * 
     * @param c The character to insert
     */
    @Override
    public void insertChar(char c) {
        String startText = text.substring(0, caretstart);
        String endText = text.substring(caretend);
        if (text.equals(" ")) {
            text = String.valueOf(c);
        } else {
            text = startText + String.valueOf(c) + endText;
        }
        caretstart = caretstart + 1;
        caretend = caretstart;
    }

    /**
     * insert text action
     * 
     * @param string text
     */
    @Override
    public void insertText(String string) {
        String startText = text.substring(0, caretstart);
        String endText = text.substring(caretend);
        String newText = startText + string + endText;
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart + string.length();
        caretstart = caretend;
    }

    /**
     * Set the starting caret position
     * 
     * @param s A text index
     */
    @Override
    public void setCaretStart(int s) {
        caretstart = s;
    }

    /**
     * Get the starting caret position
     * 
     * @return The start index
     */
    @Override
    public int getCaretStart() {
        return caretstart;
    }

    /**
     * Set the ending caret position
     * 
     * @param e A text index
     */
    @Override
    public void setCaretEnd(int e) {
        caretend = e;
    }

    /**
     * Get the ending caret position
     * 
     * @return The end index
     */
    @Override
    public int getCaretEnd() {
        return caretend;
    }

    /**
     * Set the clipboard text
     * 
     * @param s Some textual data
     */
    @Override
    public void setClipboard(String s) {
        clipboard = s;
    }

    /**
     * Get the clipboard text
     * 
     * @return Some textual data
     */
    @Override
    public String getClipboard() {
        return clipboard;
    }

}