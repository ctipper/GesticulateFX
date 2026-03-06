/**
 * RichTextEditor.java
 * 
 * Created on 1 Mar 2026 16:06:58
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

import java.util.*;
import javax.inject.Inject;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import net.perspective.draw.editor.*;
import net.perspective.draw.geom.Text;

/**
 *
 * @author ctipper
 */

public class RichTextEditor implements Editor, Styler {

    private Clipboard clipboard;
    private int caretstart, caretend;
    private boolean coalesced;
    private Schema schema;
    private Node doc;
    private HTMLReader reader;
    private HTMLWriter writer;

    /** 
     * Creates a new instance of <code>RichTextEditor</code> 
     */
    @Inject
    public RichTextEditor() {
        super();
        this.initbuilder();
        caretstart = caretend = 0;
        Platform.runLater(() -> {
            clipboard = Clipboard.getSystemClipboard();
        });
    }

    private void initbuilder() {
        // Build the schema
        schema = new Schema(
            new LinkedHashMap<>(Map.of(
                "doc",       new NodeSpec("paragraph+", null, false, Map.of(), null),
                "paragraph", new NodeSpec("text*", "block", false, Map.of(), null),
                "text",      new NodeSpec(null, null, true, Map.of(), null)
            )),
            new LinkedHashMap<>(Map.of(
                "b", new MarkSpec(),
                "i", new MarkSpec(),
                "u", new MarkSpec()
            ))
        );
        reader = new HTMLReader(schema);
        writer = new HTMLWriter();
    }

    /**
     * Load Text content into editor
     * 
     * @param item A Text item
     */
    @Override
    public void editText(Text item) {
        doc = reader.parse(item.getText());
        coalesced = false;
        // Don't clear storedMarks here - they're meant to persist for next input
    }

    /**
     * Serialise editor content to Text
     * 
     * @param item A Text item
     */
    @Override
    public void commitText(Text item) {
        item.setText(writer.serialize(doc));
    }

    /**
     * Return unformatted text to client
     * 
     * @return Some textual data
     */
    @Override
    public String readPlainText() {
        return doc.textContent();
    }

    /**
     * Return the length of text content
     * 
     * @return length
     */
    @Override
    public int getLength() {
        return doc.textLength();
    }

    /**
     * clipboard cut action
     */
    @Override
    public void cutText() {
        this.setClipboard(doc.content().textBetween(docPos(caretstart), docPos(caretend), null, null));
        this.removeText();
        caretend = caretstart;
    }

    /**
     * clipboard copy action
     */
    @Override
    public void copyText() {
        this.setClipboard(doc.content().textBetween(docPos(caretstart), docPos(caretend), null, null));
    }

    /**
     * clipboard paste action
     */
    @Override
    public void pasteText() {
        String clip = this.getClipboard();
        // Guard against empty clipboard
        if (clip.isEmpty()) {
            return;
        }

        // remove highlighted text
        this.removeText();
        caretend = caretstart;
        decoalesceText();
        // Use stored marks if set, otherwise resolve from position
        List<Mark> marks = activeMarks();
        Slice slice = new Slice(
            Fragment.from(schema.text(clip, marks)), 0, 0
        );
        doc = doc.replace(docPos(caretstart), docPos(caretstart), slice);

        caretstart = caretstart + clip.length();
        caretend = caretstart;

        // Clear stored marks — they've been consumed
        storedMarks = null;
    }

    /**
     * keyboard backspace action
     */
    @Override
    public void backSpace() {
        if (caretstart == caretend) {
            if (caretstart > 0) {
                caretstart = caretstart - 1;
                removeText();
            }
        } else {
            removeText();
        }
        caretend = caretstart;
        storedMarks = null; // movement cancels stored marks
        this.coalesceText();
    }

    /**
     * keyboard delete action
     */
    @Override
    public void deleteChar() {
        if (caretend < this.getLength()) {
            caretend += 1;
            removeText();
        } else {
            removeText();
        }
        caretend = caretstart;
        storedMarks = null; // movement cancels stored marks
        this.coalesceText();
    }

    /**
     * insert string action
     * 
     * @param string text
     */
    @Override
    public void insertText(String string) {
        if (string.isEmpty()) {
            return;
        }

        // remove highlighted text
        this.removeText();
        caretend = caretstart;
        decoalesceText();
        // Use stored marks if set, otherwise resolve from position
        List<Mark> marks = activeMarks();
        Slice slice = new Slice(
            Fragment.from(schema.text(string, marks)), 0, 0
        );
        doc = doc.replace(docPos(caretstart), docPos(caretstart), slice);

        caretstart = caretstart + string.length();
        caretend = caretstart;

        // Clear stored marks — they've been consumed
        storedMarks = null;
    }

    /**
     * Remove text between caretStart and caretEnd positions. Positions are document-wide token
     * offsets.
     */
    private void removeText() {
        if (caretstart == caretend) {
            return;
        }
        if (caretstart > caretend) {
            int tmp = caretstart;
            caretstart = caretend;
            caretend = tmp;
        }
        doc = doc.replace(docPos(caretstart), docPos(caretend), Slice.EMPTY);
        this.coalesceText();
    }

    private void coalesceText() {
        if (doc.textLength() == 0) {
            ResolvedPos $pos = doc.resolve(docPos(caretstart));
            List<Mark> marks = $pos.marks();
            Slice slice = new Slice(
                Fragment.from(schema.text(String.valueOf(" "), marks)), 0, 0
            );
            doc = doc.replace(docPos(caretstart), docPos(caretstart), slice);
            coalesced = true;
        }
    }

    private void decoalesceText() {
        if (coalesced) {
            doc = doc.replace(docPos(0), docPos(1), Slice.EMPTY);
            caretstart = 0;
            coalesced = false;
        }
    }

    /**
     * Convert a text offset to a document position by finding the content start
     * of the first textblock in the document tree.
     */
    private int docPos(int textOffset) {
        Node node = doc;
        int offset = 0;
        while (!node.isTextblock() && node.childCount() > 0) {
            node = node.firstChild();
            offset++;
        }
        return offset + textOffset;
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

    /**
     * Implement Styler interface
     */

    /**
     * return a list of styles at cursor
     * 
     * @return a set of styles defined by "b" "i" or "u"
     */
    @Override
    public Set<String> detectStyles() {
        Set<String> styles = new LinkedHashSet<>();
        if (caretstart == caretend) {
            for (Mark mark : activeMarks()) {
                styles.add(mark.type().name());
            }
        } else {
            // Check all text nodes in the selection range
            int from = docPos(Math.min(caretstart, caretend));
            int to = docPos(Math.max(caretstart, caretend));
            int pos = 0;
            for (int i = 0; i < doc.childCount(); i++) {
                Node para = doc.child(i);
                int paraStart = pos + 1;
                para.content().forEach((child, offset, index) -> {
                    int childFrom = paraStart + offset;
                    int childTo = childFrom + child.nodeSize();
                    if (child.isText() && childTo > from && childFrom < to) {
                        for (Mark mark : child.marks()) {
                            styles.add(mark.type().name());
                        }
                    }
                });
                pos += para.nodeSize();
            }
        }
        return styles;
    }

    private List<Mark> storedMarks = null; // null means "use marks from position"

    private List<Mark> activeMarks() {
        if (storedMarks != null) return storedMarks;
        ResolvedPos $pos = doc.resolve(docPos(caretstart));
        return $pos.marks();
    }

    /**
     * insert style between carets
     * 
     * @param style defined by one of "b" "i" or "u"
     */
    @Override
    public void createStyle(String style) {
        MarkType markType = schema.markType(style);
        Mark mark = markType.create();

        if (caretstart == caretend) {
            // No selection — toggle stored marks
            List<Mark> current = activeMarks();
            if (Mark.hasMarkInSet(current, markType)) {
                storedMarks = Mark.removeFromSet(current, markType);
            } else {
                storedMarks = Mark.addToSet(current, mark);
            }
            return;
        }
    
        // Apply mark to the range — convert text offsets to document positions
        int from = docPos(Math.min(caretstart, caretend));
        int to = docPos(Math.max(caretstart, caretend));

        List<Node> newParagraphs = new ArrayList<>();
        int pos = 0;

        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraStart = pos + 1;
            int paraEnd = paraStart + para.content().size();

            if (paraEnd > from && paraStart < to) {
                int cutFrom = Math.max(0, from - paraStart);
                int cutTo = Math.min(para.content().size(), to - paraStart);

                Fragment before = para.content().cut(0, cutFrom);
                Fragment middle = para.content().cut(cutFrom, cutTo).addMark(mark);
                Fragment after = para.content().cut(cutTo);

                newParagraphs.add(para.copy(before.append(middle).append(after)));
            } else {
                newParagraphs.add(para);
            }

            pos += para.nodeSize();
        }

        doc = schema.nodeType("doc").create(null, Fragment.from(newParagraphs), null);
    }

    /**
     * remove the style between carets
     * 
     * @param style defined by one of "b" "i" or "u"
     */
    @Override
    public void removeStyle(String style) {
        MarkType markType = schema.markType(style);

        if (caretstart == caretend) {
            List<Mark> current = activeMarks();
            storedMarks = Mark.removeFromSet(current, markType);
            return;
        }

        int from = docPos(Math.min(caretstart, caretend));
        int to = docPos(Math.max(caretstart, caretend));

        List<Node> newParagraphs = new ArrayList<>();
        int pos = 0;

        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraStart = pos + 1;
            int paraEnd = paraStart + para.content().size();

            if (paraEnd > from && paraStart < to) {
                int cutFrom = Math.max(0, from - paraStart);
                int cutTo = Math.min(para.content().size(), to - paraStart);

                Fragment before = para.content().cut(0, cutFrom);
                Fragment middle = para.content().cut(cutFrom, cutTo).removeMark(markType);
                Fragment after = para.content().cut(cutTo);

                newParagraphs.add(para.copy(before.append(middle).append(after)));
            } else {
                newParagraphs.add(para);
            }

            pos += para.nodeSize();
        }

        doc = schema.nodeType("doc").create(null, Fragment.from(newParagraphs), null);
    }

}
