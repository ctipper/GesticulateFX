/**
 * HTMLWriter.java
 *
 * Created on 2 Mar 2026 11:28:52
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ctipper
 */

public class HTMLWriter {

    public String serialize(Node doc) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < doc.childCount(); i++) {
            Node paragraph = doc.child(i);
            sb.append("<p>");
            serializeInline(paragraph, sb);
            sb.append("</p>");
        }
        return sb.toString();
    }

    private void serializeInline(Node parent, StringBuilder sb) {
        List<Mark> active = new ArrayList<>();

        for (int i = 0; i < parent.childCount(); i++) {
            Node child = parent.child(i);
            List<Mark> marks = child.isText() ? child.marks() : List.of();

            int keep = 0;
            while (keep < active.size() && keep < marks.size()
                && active.get(keep).eq(marks.get(keep))) {
                keep++;
            }

            for (int j = active.size() - 1; j >= keep; j--) {
                sb.append(closeTag(active.get(j)));
            }
            for (int j = keep; j < marks.size(); j++) {
                sb.append(openTag(marks.get(j)));
            }

            active = new ArrayList<>(marks);

            if (child.isText()) {
                sb.append(escape(child.text()));
            }
        }

        for (int j = active.size() - 1; j >= 0; j--) {
            sb.append(closeTag(active.get(j)));
        }
    }

    private String openTag(Mark mark) {
        return "<" + markTag(mark) + ">";
    }

    private String closeTag(Mark mark) {
        return "</" + markTag(mark) + ">";
    }

    private String markTag(Mark mark) {
        return mark.type().name(); // "b", "i", "u" — already valid HTML tags
    }

    private String escape(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

}
