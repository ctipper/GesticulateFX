/*
 * TextEditor.java
 * 
 * Created on May 7, 2013 12:50:35 PM
 * 
 */

/**
 * Copyright (c) 2024 Christopher Tipper
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
package net.perspective.draw.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.inject.Inject;
import net.perspective.draw.geom.Text;

/**
 * 
 * @author ctipper
 */

public class TextEditor implements Editor {

    private String text;
    private Clipboard clipboard;
    private int caretstart, caretend;

    /**
     * Creates a new instance of <code>TextEditor</code>
     */
    @Inject
    public TextEditor() {
        caretstart = caretend = 0;
        Platform.runLater(() -> {
            clipboard = Clipboard.getSystemClipboard();
        });
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
        this.setClipboard(text.substring(caretstart, caretend));
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
        this.setClipboard(text.substring(caretstart, caretend));
    }

    /**
     * clipboard paste action
     */
    @Override
    public void pasteText() {
        String startText = text.substring(0, caretstart);
        String endText = text.substring(caretend);
        String clip = this.getClipboard();
        String newText = startText + clip + endText;
        if (newText.length() == 0) {
            text = " ";
        } else {
            text = newText;
        }
        caretend = caretstart + clip.length();
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
     * @param str Some textual data
     */
    @Override
    public void setClipboard(String str) {
        // update system clipboard
        ClipboardContent content = new ClipboardContent();
        content.putString(str);
        clipboard.setContent(content);
    }

    /**
     * Get the clipboard text
     * 
     * @return Some textual data
     */
    @Override
    public String getClipboard() {
        String str;
        if (clipboard.hasString()) {
            str = clipboard.getString();
        } else {
            str = "";
        }
        return str;
    }

}