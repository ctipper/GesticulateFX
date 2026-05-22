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
import net.perspective.draw.ApplicationController;
import net.perspective.draw.editor.*;
import net.perspective.draw.geom.Text;

/**
 * Rich-text editor implementing the {@link Editor} and {@link Styler}
 * interfaces. Manages a ProseMirror-style document model with inline marks
 * (bold, italic, underline) and caret/selection.
 *
 * @author ctipper
 */

public class RichTextEditor implements Editor, Styler {

    private final ApplicationController controller;
    private Clipboard clipboard;
    private int caretstart, caretend;
    private boolean coalesced;
    private Schema schema;
    private Node doc;
    private HTMLReader reader;
    private HTMLWriter writer;
    private String lastSetContent = null;

    /**
     * Creates a new instance of <code>RichTextEditor</code>
     *
     * @param controller the application controller
     */
    @Inject
    public RichTextEditor(ApplicationController controller) {
        super();
        this.controller = controller;
        this.initbuilder();
        caretstart = caretend = 0;
        Platform.runLater(() -> {
            clipboard = Clipboard.getSystemClipboard();
        });
    }

    /** Initialise the document schema, HTML reader, and HTML writer. */
    private void initbuilder() {
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
        return doc.textLength() + Math.max(0, doc.childCount() - 1);
    }

    /**
     * clipboard cut action
     */
    @Override
    public void cutText() {
        setClipboard(doc.content().textBetween(docPos(caretstart), docPos(caretend), "\n", null));
        this.removeText();
        caretend = caretstart;
    }

    /**
     * clipboard copy action
     */
    @Override
    public void copyText() {
        setClipboard(doc.content().textBetween(docPos(caretstart), docPos(caretend), "\n", null));
    }

    /**
     * clipboard paste action
     */
    @Override
    public void pasteText() {
        String clipboardText = getClipboard();
        if (clipboardText.isEmpty()) {
            return;
        }

        this.removeText();
        caretend = caretstart;
        decoalesceText();
        List<Mark> marks = activeMarks();

        String[] lines = clipboardText.split("\n", -1);

        if (lines.length == 1) {
            Slice slice = new Slice(Fragment.from(schema.text(clipboardText, marks)), 0, 0);
            doc = doc.replace(docPos(caretstart), docPos(caretstart), slice);
            caretstart = caretstart + clipboardText.length();
            caretend = caretstart;
            storedMarks = null;
            return;
        }

        List<Node> newParagraphs = new ArrayList<>();
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                newParagraphs.addAll(splitParagraphAt(para, caretstart - textPos, lines, marks));
                for (int j = i + 1; j < doc.childCount(); j++) {
                    newParagraphs.add(doc.child(j));
                }
                break;
            }
            newParagraphs.add(para);
            textPos += paraLen + 1; // +1 for the paragraph separator in the caret model
        }

        doc = schema.nodeType("doc").create(null, Fragment.from(newParagraphs), null);
        caretstart = caretstart + clipboardText.length();
        caretend = caretstart;
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
     * @param string the text to insert
     */
    @Override
    public void insertText(String string) {
        if (string.isEmpty()) {
            return;
        }

        this.removeText();
        caretend = caretstart;
        decoalesceText();
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
     * insert new line action
     */
    @Override
    public void insertNewline() {
        this.removeText();
        caretend = caretstart;
        decoalesceText();

        List<Node> newParagraphs = new ArrayList<>();
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                newParagraphs.addAll(splitParagraphAt(para, caretstart - textPos));
                for (int j = i + 1; j < doc.childCount(); j++) {
                    newParagraphs.add(doc.child(j));
                }
                break;
            }
            newParagraphs.add(para);
            textPos += paraLen + 1; // +1 for the paragraph separator in the caret model
        }

        doc = schema.nodeType("doc").create(null, Fragment.from(newParagraphs), null);
        caretstart += 1;
        caretend = caretstart;
        storedMarks = null;
    }

    /**
     * move caret up action
     */
    @Override
    public void moveCaretUp() {
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                if (i > 0) {
                    int offset = caretstart - textPos;
                    int prevLen = doc.child(i - 1).textLength();
                    caretstart = textPos - prevLen - 1 + Math.min(prevLen, offset);
                    caretend = caretstart;
                }
                return;
            }
            textPos += paraLen + 1;
        }
    }

    /**
     * move caret down action
     */
    @Override
    public void moveCaretDown() {
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                if (i < doc.childCount() - 1) {
                    int offset = caretstart - textPos;
                    int nextLen = doc.child(i + 1).textLength();
                    caretstart = textPos + paraLen + 1 + Math.min(nextLen, offset);
                    caretend = caretstart;
                }
                return;
            }
            textPos += paraLen + 1;
        }
    }

    /**
     * move caret to start action
     */
    @Override
    public void moveCaretStart() {
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                caretstart = textPos;
                caretend = caretstart;
                return;
            }
            textPos += paraLen + 1;
        }
    }

    /**
     * move caret to end action
     */
    @Override
    public void moveCaretEnd() {
        int textPos = 0;
        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            if (caretstart >= textPos && caretstart <= textPos + paraLen) {
                caretstart = textPos + paraLen;
                caretend = caretstart;
                return;
            }
            textPos += paraLen + 1;
        }
    }

    /**
     * Remove text between caretStart and caretEnd positions. Positions are document-wide token
     * offsets that count paragraph separators as 1 each.
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

        List<Node> newParagraphs = new ArrayList<>();
        int textPos = 0;
        Fragment pendingBefore = null;
        Node pendingTemplate = null;

        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();
            int paraEnd = textPos + paraLen;

            boolean startsHere = caretstart >= textPos && caretstart <= paraEnd;
            boolean endsHere   = caretend   >= textPos && caretend   <= paraEnd;

            if (startsHere && endsHere) {
                int from = caretstart - textPos;
                int to   = caretend   - textPos;
                newParagraphs.add(para.copy(
                    para.content().cut(0, from).append(para.content().cut(to))
                ));
            } else if (startsHere) {
                pendingBefore   = para.content().cut(0, caretstart - textPos);
                pendingTemplate = para;
            } else if (endsHere && pendingBefore != null && pendingTemplate != null) {
                Fragment after = para.content().cut(caretend - textPos);
                newParagraphs.add(pendingTemplate.copy(pendingBefore.append(after)));
                pendingBefore   = null;
                pendingTemplate = null;
            } else if (pendingBefore == null) {
                newParagraphs.add(para); // before the removal range
            }
            // paragraphs fully inside the range are dropped

            textPos += paraLen + 1;
        }

        doc = schema.nodeType("doc").create(null, Fragment.from(newParagraphs), null);
        this.coalesceText();
    }

    /**
     * Split a paragraph at the given offset, returning two paragraphs with no inserted content.
     *
     * @param para   the paragraph node to split
     * @param offset the character offset within {@code para} at which to split
     * @return a list of two paragraph nodes: content before and content after the split point
     */
    private List<Node> splitParagraphAt(Node para, int offset) {
        return splitParagraphAt(para, offset, new String[]{"", ""}, List.of());
    }

    /**
     * Split a paragraph at the given offset and splice in multi-line clipboard content.
     * <p>
     * The first element of {@code lines} is appended to the content before the split; the last
     * element is prepended to the content after the split; any middle elements become new
     * paragraphs between them.
     *
     * @param para   the paragraph node to split
     * @param offset the character offset within {@code para} at which to split
     * @param lines  clipboard text already split on {@code \n}; must have at least two elements
     * @param marks  marks to apply to the inserted text
     * @return replacement paragraph nodes to substitute for {@code para} in the document
     */
    private List<Node> splitParagraphAt(Node para, int offset, String[] lines, List<Mark> marks) {
        Fragment before = para.content().cut(0, offset);
        Fragment after = para.content().cut(offset);
        NodeType paraType = schema.nodeType("paragraph");
        List<Node> result = new ArrayList<>();

        Fragment firstLine = lines[0].isEmpty()
            ? Fragment.EMPTY : Fragment.from(schema.text(lines[0], marks));
        result.add(para.copy(before.append(firstLine)));

        for (int j = 1; j < lines.length - 1; j++) {
            Fragment middle = lines[j].isEmpty()
                ? Fragment.EMPTY : Fragment.from(schema.text(lines[j], marks));
            result.add(paraType.create(null, middle, null));
        }

        Fragment lastLine = lines[lines.length - 1].isEmpty()
            ? Fragment.EMPTY : Fragment.from(schema.text(lines[lines.length - 1], marks));
        result.add(para.copy(lastLine.append(after)));

        return result;
    }

    /**
     * Insert a placeholder space when the document is empty so layout always
     * has at least one text node to measure.
     */
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

    /** Remove the coalesced placeholder space before inserting real content. */
    private void decoalesceText() {
        if (coalesced) {
            doc = doc.replace(docPos(0), docPos(1), Slice.EMPTY);
            caretstart = 0;
            coalesced = false;
        }
    }

    /**
     * Convert a caretstart/caretend offset to a ProseMirror document position.
     * Paragraph breaks count as 1 in the caret model but occupy 2 tokens in the
     * document (closing of one paragraph + opening of the next), so each paragraph
     * boundary crossed adds 1 extra position beyond the raw text offset.
     */
    private int docPos(int textOffset) {
        int pos = 1; // start inside the first paragraph, after its opening token
        int remaining = textOffset;

        for (int i = 0; i < doc.childCount(); i++) {
            Node para = doc.child(i);
            int paraLen = para.textLength();

            if (remaining <= paraLen) {
                return pos + remaining;
            }

            remaining -= paraLen + 1; // +1 for the paragraph separator in caretstart
            pos += para.nodeSize();   // advance to inside the next paragraph
        }

        return pos; // textOffset out of range — caller's responsibility
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
                controller.setStatusMessage("Text content too large");
                return "";
            }
            str = gathered;
        }

        return str;
    }

    /**
     * Return the set of style names active at the cursor or within the selection.
     *
     * @return style names — {@code "b"}, {@code "i"}, or {@code "u"}
     */
    @Override
    public Set<String> detectStyles() {
        Set<String> styles = new LinkedHashSet<>();
        if (caretstart == caretend) {
            for (Mark mark : activeMarks()) {
                styles.add(mark.type().name());
            }
        } else {
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

    /**
     * Clear the stored marks
     */
    @Override
    public void clearStoredMarks() {
        storedMarks = null;
    }

    /** Return the marks to apply to the next insertion: stored marks if set, otherwise position marks. */
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
