/*
 * TextEditor.java
 * 
 * Created on May 7, 2013 12:50:35 PM
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
package net.perspective.draw.text;

import net.perspective.draw.util.Messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.inject.Inject;
import net.perspective.draw.ApplicationController;
import net.perspective.draw.geom.Text;

/**
 * Plain-text editor implementing the {@link Editor} interface. Manages a
 * line-oriented text model with caret/selection and clipboard operations.
 *
 * @author ctipper
 */

public class TextEditor implements Editor {

    private final ApplicationController controller;
    private String[] text;
    private Clipboard clipboard;
    private int caretstart, caretend;
    private String lastSetContent = null;

    /**
     * Creates a new instance of <code>TextEditor</code>
     *
     * @param controller the application controller
     */
    @Inject
    public TextEditor(ApplicationController controller) {
        super();
        this.controller = controller;
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
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(item.getText());
        if (matcher.find()) {
            String content = matcher.group(2);
            content = content.replaceAll("(?i)</p>", "\n");
            content = content.replaceAll("(?s)<.*?>", "");
            content = content.replaceAll("&amp;", "&");
            content = content.replaceAll("&lt;", "<");
            content = content.replaceAll("&gt;", ">");
            text = content.split("\n", -1); // -1 keeps empty strings
        } else {
            text = item.getText().split("\n", -1);
        }
    }

    /**
     * Serialise editor content to Text
     * 
     * @param item the {@link net.perspective.draw.geom.Text}
     */
    @Override
    public void commitText(Text item) {
        item.setText(joinTexts(text));
    }

    /**
     * Return unformatted text to client
     * 
     * @return text Some textual data
     */
    @Override
    public String readPlainText() {
        return joinTexts(text);
    }

    /**
     * Return the length of text content
     * 
     * @return length
     */
    @Override
    public int getLength() {
        int length = 0;
        for (String line : text) {
            length += line.length();
        }
        return length + Math.max(0, text.length - 1);
    }

    /**
     * clipboard cut action
     */
    @Override
    public void cutText() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        setClipboard(copySelection(startLine, startOffset, endLine, endOffset));

        String newLine = text[startLine].substring(0, startOffset) + text[endLine].substring(endOffset);

        if (startLine == endLine) {
            if (newLine.length() == 0 && text.length == 1) {
                text = new String[]{" "};
            } else {
                text[startLine] = newLine;
            }
        } else {
            replaceLines(startLine, endLine, newLine);
            if (text.length == 1 && text[0].length() == 0) {
                text = new String[]{" "};
            }
        }
        caretend = caretstart;
    }

    /**
     * clipboard copy action
     */
    @Override
    public void copyText() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        setClipboard(copySelection(startLine, startOffset, endLine, endOffset));
    }

    /**
     * clipboard paste action
     */
    @Override
    public void pasteText() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        String[] clipLines = getClipboard().split("\n", -1);

        if (clipLines.length == 1) {
            String newLine = text[startLine].substring(0, startOffset) + getClipboard() + text[endLine].substring(endOffset);
            if (startLine == endLine) {
                text[startLine] = newLine;
            } else {
                replaceLines(startLine, endLine, newLine);
            }
        } else {
            String[] insertLines = clipLines.clone();
            insertLines[0] = text[startLine].substring(0, startOffset) + clipLines[0];
            insertLines[clipLines.length - 1] = clipLines[clipLines.length - 1] + text[endLine].substring(endOffset);
            replaceLines(startLine, endLine, insertLines);
        }
        caretstart += getClipboard().length();
        caretend = caretstart;
    }

    /**
     * keyboard backspace action
     */
    @Override
    public void backSpace() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        if (caretstart == caretend) {
            if (startOffset > 0) {
                String newLine = text[startLine].substring(0, startOffset - 1) + text[startLine].substring(startOffset);
                if (newLine.length() == 0 && text.length == 1) {
                    text = new String[]{" "};
                } else {
                    text[startLine] = newLine;
                }
                caretstart--;
                caretend = caretstart;
            } else if (startLine > 0) {
                String newLine = text[startLine - 1] + text[startLine];
                replaceLines(startLine - 1, startLine, newLine);
                if (text.length == 1 && text[0].length() == 0) {
                    text = new String[]{" "};
                }
                caretstart--;
                caretend = caretstart;
            }
            return;
        }

        // Selection: delete range caretstart..caretend
        String newLine = text[startLine].substring(0, startOffset) + text[endLine].substring(endOffset);

        if (startLine == endLine) {
            if (newLine.length() == 0 && text.length == 1) {
                text = new String[]{" "};
            } else {
                text[startLine] = newLine;
            }
        } else {
            replaceLines(startLine, endLine, newLine);
            if (text.length == 1 && text[0].length() == 0) {
                text = new String[]{" "};
            }
        }
        caretend = caretstart;
    }

    /**
     * keyboard delete action
     */
    @Override
    public void deleteChar() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        if (caretstart == caretend) {
            if (endOffset < text[endLine].length()) {
                endOffset++;
            } else if (endLine < text.length - 1) {
                endLine++;
                endOffset = 0;
            } else {
                return;
            }
        }

        String newLine = text[startLine].substring(0, startOffset) + text[endLine].substring(endOffset);

        if (startLine == endLine) {
            if (newLine.length() == 0 && text.length == 1) {
                text = new String[]{" "};
            } else {
                text[startLine] = newLine;
            }
        } else {
            replaceLines(startLine, endLine, newLine);
            if (text.length == 1 && text[0].length() == 0) {
                text = new String[]{" "};
            }
        }
        caretend = caretstart;
    }

    /**
     * insert text action
     *
     * @param string the text to insert
     */
    @Override
    public void insertText(String string) {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        String newLine = text[startLine].substring(0, startOffset) + string + text[endLine].substring(endOffset);

        if (startLine == endLine) {
            text[startLine] = newLine;
        } else {
            replaceLines(startLine, endLine, newLine);
        }
        caretstart += string.length();
        caretend = caretstart;
    }

    /**
     * insert new line action
     */
    @Override
    public void insertNewline() {
        int startLine = 0, startOffset = caretstart;
        while (startLine < text.length - 1 && startOffset > text[startLine].length()) {
            startOffset -= (text[startLine].length() + 1);
            startLine++;
        }
        int endLine = 0, endOffset = caretend;
        while (endLine < text.length - 1 && endOffset > text[endLine].length()) {
            endOffset -= (text[endLine].length() + 1);
            endLine++;
        }

        replaceLines(startLine, endLine,
                text[startLine].substring(0, startOffset),
                text[endLine].substring(endOffset));
        caretstart += 1;
        caretend = caretstart;
    }

    /**
     * move caret up action
     */
    @Override
    public void moveCaretUp() {
        int line = 0, offset = caretstart;
        while (line < text.length - 1 && offset > text[line].length()) {
            offset -= (text[line].length() + 1);
            line++;
        }
        if (line > 0) {
            int prevLineLength = text[line - 1].length();
            int newOffset = Math.min(prevLineLength, offset);
            caretstart = (caretstart - offset) - (prevLineLength + 1) + newOffset;
            caretend = caretstart;
        }
    }

    /**
     * move caret down action
     */
    @Override
    public void moveCaretDown() {
        int line = 0, offset = caretstart;
        while (line < text.length - 1 && offset > text[line].length()) {
            offset -= (text[line].length() + 1);
            line++;
        }
        if (line < text.length - 1) {
            int nextLineLength = text[line + 1].length();
            int newOffset = Math.min(nextLineLength, offset);
            caretstart = caretstart - offset + text[line].length() + 1 + newOffset;
            caretend = caretstart;
        }
    }

    /**
     * move caret to start action
     */
    @Override
    public void moveCaretStart() {
        int line = 0, offset = caretstart;
        while (line < text.length - 1 && offset > text[line].length()) {
            offset -= (text[line].length() + 1);
            line++;
        }
        caretstart -= offset;
        caretend = caretstart;
    }

    /**
     * move caret to end action
     */
    @Override
    public void moveCaretEnd() {
        int line = 0, offset = caretstart;
        while (line < text.length - 1 && offset > text[line].length()) {
            offset -= (text[line].length() + 1);
            line++;
        }
        caretstart = caretstart - offset + text[line].length();
        caretend = caretstart;
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
        ClipboardContent content = new ClipboardContent();
        content.putString(str);
        clipboard.setContent(content);
        lastSetContent = str;
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
        if (!str.equals(lastSetContent)) {
            String gathered = new TextGatherer().textGatherer(str);
            if (gathered == null) {
                controller.setStatusMessage(Messages.get("status.textTooLarge"));
                return "";
            }
            str = gathered;
        }

        return str;
    }

    /**
     * Extract the selected region as a flat newline-delimited string, suitable
     * for assigning to the clipboard. Used by {@link #cutText()} and
     * {@link #copyText()}.
     */
    private String copySelection(int startLine, int startOffset, int endLine, int endOffset) {
        if (startLine == endLine) {
            return text[startLine].substring(startOffset, endOffset);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(text[startLine].substring(startOffset));
        for (int i = startLine + 1; i < endLine; i++) {
            sb.append('\n').append(text[i]);
        }
        sb.append('\n').append(text[endLine].substring(0, endOffset));
        return sb.toString();
    }

    /**
     * Replace lines {@code startLine} through {@code endLine} (inclusive) with
     * the given replacement lines, resizing the text array accordingly.
     */
    private void replaceLines(int startLine, int endLine, String... replacements) {
        String[] newText = new String[text.length - (endLine - startLine + 1) + replacements.length];
        System.arraycopy(text, 0, newText, 0, startLine);
        System.arraycopy(replacements, 0, newText, startLine, replacements.length);
        System.arraycopy(text, endLine + 1, newText, startLine + replacements.length, text.length - endLine - 1);
        text = newText;
    }

    /** Join lines into a single newline-delimited string. */
    private String joinTexts(String[] lines) {
        return String.join("\n", lines);
    }

}