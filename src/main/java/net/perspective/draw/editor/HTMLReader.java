/**
 * HTMLReader.java
 *
 * Created on 1 Mar 2026 19:32:34
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

/**
 *
 * @author ctipper
 */

public class HTMLReader {

    private final Schema schema;

    public HTMLReader(Schema schema) {
        this.schema = schema;
    }

    public Node parse(String content) {
        String html = normalizeText(content);
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        List<Node> paragraphs = new ArrayList<>();

        for (org.jsoup.nodes.Element el : jsoupDoc.body().children()) {
            if (el.tagName().equals("p")) {
                Node para = parseParagraph(el);
                if (para != null) {
                    paragraphs.add(para);
                }
            }
        }

        if (paragraphs.isEmpty()) {
            // Handle bare text (no <p> tags) — parse the body itself as inline content
            List<Node> inline = new ArrayList<>();
            parseInline(jsoupDoc.body(), inline, List.of());
            paragraphs.add(schema.nodeType("paragraph").create(null, Fragment.from(inline), null));
        }

        return schema.nodeType("doc").create(null, Fragment.from(paragraphs), null);
    }

    private Node parseParagraph(org.jsoup.nodes.Element el) {
        List<Node> inline = new ArrayList<>();
        parseInline(el, inline, List.of());
        return schema.nodeType("paragraph").create(null, Fragment.from(inline), null);
    }

    private void parseInline(org.jsoup.nodes.Element el, List<Node> into,
        List<Mark> marks) {
        for (org.jsoup.nodes.Node child : el.childNodes()) {
            switch (child) {
                case org.jsoup.nodes.TextNode textNode -> {
                    String text = textNode.getWholeText();
                    if (!text.isEmpty()) {
                        into.add(schema.text(text, marks));
                    }
                }
                case org.jsoup.nodes.Element childEl -> {
                    MarkType markType = tagToMark(childEl.tagName());
                    if (markType != null) {
                        List<Mark> newMarks = Mark.addToSet(marks, markType.create());
                        parseInline(childEl, into, newMarks);
                    } else {
                        parseInline(childEl, into, marks);
                    }
                }
                default -> {
                }
            }
        }
    }

    private MarkType tagToMark(String tag) {
        return switch (tag) {
            case "b", "strong" -> schema.markType("b");
            case "i", "em" -> schema.markType("i");
            case "u" -> schema.markType("u");
            default -> null;
        };
    }

    private String normalizeText(String content) {
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(content);
        if (!matcher.find()) {
            // Decode any pre-existing entities before re-escaping
            content = Parser.unescapeEntities(content, false);
            content = content.replaceAll("&", "&amp;");
            content = content.replaceAll("<", "&lt;");
            content = content.replaceAll(">", "&gt;");
            content = "<p>" + content + "</p>";
        }
        return content;
    }

}
