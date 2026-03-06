/**
 * NodeToAttributedString.java
 *
 * Created on 1 Mar 2026 12:01:53
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

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.List;

/**
 *
 * @author ctipper
 */

public class NodeToAttributedString {

    private static final Font BASE_FONT = new Font("Serif", Font.PLAIN, 16);

    public AttributedString convert(Node doc) {
        // First pass: collect all text to build the AttributedString
        String plainText = extractText(doc);
        if (plainText.isEmpty()) {
            return new AttributedString("");
        }

        AttributedString result = new AttributedString(plainText);

        // Second pass: walk the tree and apply attributes at correct offsets
        int[] offset = { 0 };
        applyAttributes(doc, result, offset);

        return result;
    }

    private String extractText(Node node) {
        StringBuilder sb = new StringBuilder();
        collectText(node, sb);
        return sb.toString();
    }

    private void collectText(Node node, StringBuilder sb) {
        if (node.isText()) {
            sb.append(node.text());
            return;
        }
        for (int i = 0; i < node.content().childCount(); i++) {
            Node child = node.content().child(i);
            collectText(child, sb);
            // Add newline after block-level children (except last)
            if (child.isBlock() && i < node.content().childCount() - 1) {
                sb.append("\n");
            }
        }
    }

    private void applyAttributes(Node node, AttributedString result, int[] offset) {
        if (node.isText()) {
            int start = offset[0];
            int end = start + node.text().length();

            Font font = deriveFont(node.marks());
            result.addAttribute(TextAttribute.FONT, font, start, end);

            // Underline is a separate attribute, not a font style
            if (hasMark(node.marks(), "u")) {
                result.addAttribute(TextAttribute.UNDERLINE,
                    TextAttribute.UNDERLINE_ON, start, end);
            }

            offset[0] = end;
            return;
        }

        for (int i = 0; i < node.content().childCount(); i++) {
            Node child = node.content().child(i);
            applyAttributes(child, result, offset);

            if (child.isBlock() && i < node.content().childCount() - 1) {
                offset[0]++; // skip the \n we inserted
            }
        }
    }

    private Font deriveFont(List<Mark> marks) {
        boolean bold = hasMark(marks, "b");
        boolean italic = hasMark(marks, "i");

        int style = Font.PLAIN;
        if (bold && italic) {
            style = Font.BOLD | Font.ITALIC;
        } else if (bold) {
            style = Font.BOLD;
        } else if (italic) {
            style = Font.ITALIC;
        }

        return BASE_FONT.deriveFont(style);
    }

    private boolean hasMark(List<Mark> marks, String name) {
        for (Mark mark : marks) {
            if (mark.type().name().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
