/**
 * DOMSerializer.java
 * 
 * Created on 1 Mar 2026 12:00:13
 * 
 */

/*
 * Copyright (c) 2026 Christopher Tipper
 * * This file is part of GesticulateFX, which is licensed under the 
 * GNU General Public License v3.0 (GPLv3).
 * * ---
 * * Portions of this code are transcribed or derived from the ProseMirror 
 * project (originally written in JavaScript).
 * * ProseMirror Original Copyright (C) 2015-2023 by Marijn Haverbeke 
 * <marijnh@gmail.com> and others.
 * * The ProseMirror portions are used under the terms of the MIT License.
 * A copy of the MIT License can be found in the root of this project 
 * or at https://opensource.org/licenses/MIT.
 */
package net.perspective.draw.editor;

import java.util.*;

/**
 *
 * @author ctipper
 */

public class DOMSerializer {

    private final Map<String, ToDOMFunction> nodes;
    private final Map<String, ToDOMFunction> marks;

    public DOMSerializer(Schema schema) {
        this.nodes = new LinkedHashMap<>();
        this.marks = new LinkedHashMap<>();

        for (Map.Entry<String, NodeType> entry : schema.nodes().entrySet()) {
            ToDOMFunction fn = entry.getValue().spec().getToDOM();
            if (fn != null) nodes.put(entry.getKey(), fn);
        }
        for (Map.Entry<String, MarkType> entry : schema.marks().entrySet()) {
            ToDOMFunction fn = entry.getValue().spec().getToDOM();
            if (fn != null) marks.put(entry.getKey(), fn);
        }
    }

    public String serializeToHTML(Node doc) {
        StringBuilder sb = new StringBuilder();
        serializeFragment(doc.content(), sb);
        return sb.toString();
    }

    private void serializeFragment(Fragment fragment, StringBuilder sb) {
        for (int i = 0; i < fragment.childCount(); i++) {
            Node child = fragment.child(i);
            if (child.isTextblock()) {
                serializeInlineContent(child, sb);
            } else {
                serializeNode(child, sb);
            }
        }
    }

    private void serializeNode(Node node, StringBuilder sb) {
        if (node.isText()) return; // handled by serializeInlineContent

        ToDOMFunction fn = nodes.get(node.type().name());
        if (fn == null) {
            serializeFragment(node.content(), sb);
            return;
        }

        DOMOutputSpec spec = fn.toDOM(node);
        renderSpec(spec, node, sb);
    }

    private void renderSpec(DOMOutputSpec spec, Node node, StringBuilder sb) {
        switch (spec) {
            case DOMOutputSpec.Element el -> {
                sb.append("<").append(el.tag());
                renderAttrs(el.attrs(), sb);

                if (isSelfClosing(el.tag()) && el.children().isEmpty()) {
                    sb.append(" />");
                    return;
                }

                sb.append(">");

                for (DOMOutputSpec child : el.children()) {
                    switch (child) {
                        case DOMOutputSpec.Hole hole -> {
                            if (node.isTextblock()) {
                                renderInlineContent(node.content(), sb);
                            } else {
                                serializeFragment(node.content(), sb);
                            }
                        }
                        default -> renderSpec(child, node, sb);
                    }
                }

                sb.append("</").append(el.tag()).append(">");
            }

            case DOMOutputSpec.Text text -> {
                sb.append(escapeHTML(text.value()));
            }

            case DOMOutputSpec.Hole hole -> {
                // Hole at top level — shouldn't happen but handle gracefully
                serializeFragment(node.content(), sb);
            }
        }
    }

    /**
     * Render inline content with optimal mark nesting.
     */
    private void serializeInlineContent(Node textblock, StringBuilder sb) {
        ToDOMFunction fn = nodes.get(textblock.type().name());
        if (fn == null) {
            renderInlineContent(textblock.content(), sb);
            return;
        }

        DOMOutputSpec spec = fn.toDOM(textblock);
        renderSpec(spec, textblock, sb);
    }

    private void renderInlineContent(Fragment fragment, StringBuilder sb) {
        List<Mark> active = new ArrayList<>();

        for (int i = 0; i < fragment.childCount(); i++) {
            Node child = fragment.child(i);
            List<Mark> nextMarks = child.isText() ? child.marks() : List.of();

            // Find how many leading marks match
            int keep = 0;
            while (keep < active.size() && keep < nextMarks.size()
                    && active.get(keep).eq(nextMarks.get(keep))) {
                keep++;
            }

            // Close marks no longer active
            for (int j = active.size() - 1; j >= keep; j--) {
                closeMarkTag(active.get(j), sb);
            }

            // Open new marks
            for (int j = keep; j < nextMarks.size(); j++) {
                openMarkTag(nextMarks.get(j), sb);
            }

            active = new ArrayList<>(nextMarks);

            // Render the node
            if (child.isText()) {
                sb.append(escapeHTML(child.text()));
            } else {
                // Inline non-text node (image, hard break, etc.)
                serializeNode(child, sb);
            }
        }

        // Close remaining marks
        for (int j = active.size() - 1; j >= 0; j--) {
            closeMarkTag(active.get(j), sb);
        }
    }

    private void openMarkTag(Mark mark, StringBuilder sb) {
        ToDOMFunction fn = marks.get(mark.type().name());
        if (fn == null) return;

        // Mark itself implements HasAttrs — pass it directly
        DOMOutputSpec spec = fn.toDOM(mark);

        if (spec instanceof DOMOutputSpec.Element el) {
            sb.append("<").append(el.tag());
            renderAttrs(el.attrs(), sb);
            sb.append(">");
        }
    }

    private void closeMarkTag(Mark mark, StringBuilder sb) {
        ToDOMFunction fn = marks.get(mark.type().name());
        if (fn == null) return;

        DOMOutputSpec spec = fn.toDOM(mark);

        if (spec instanceof DOMOutputSpec.Element el) {
            sb.append("</").append(el.tag()).append(">");
        }
    }

    private void renderAttrs(Map<String, String> attrs, StringBuilder sb) {
        if (attrs == null || attrs.isEmpty()) return;
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            String value = attr.getValue();
            if (value == null) continue;
            sb.append(" ").append(attr.getKey())
              .append("=\"").append(escapeAttr(value)).append("\"");
        }
    }

    private static final Set<String> SELF_CLOSING = Set.of(
        "br", "hr", "img", "input", "meta", "link",
        "area", "base", "col", "embed", "source", "track", "wbr"
    );

    private boolean isSelfClosing(String tag) {
        return SELF_CLOSING.contains(tag.toLowerCase());
    }

    private String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    private String escapeAttr(String value) {
        return value.replace("&", "&amp;")
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
    }

}