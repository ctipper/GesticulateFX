/**
 * DOMParser.java
 * 
 * Created on 1 Mar 2026 12:31:08
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
import org.jsoup.Jsoup;

/**
 *
 * @author ctipper
 */

/**
 * Parses DOM/HTML content into nodes using the parseDOM rules defined in the schema's node and mark
 * specs.
 */

public class DOMParser {

    private final Schema schema;
    private final List<NodeParseRule> nodeRules;
    private final List<MarkParseRule> markRules;

    private DOMParser(Schema schema, List<NodeParseRule> nodeRules,
                      List<MarkParseRule> markRules) {
        this.schema = schema;
        this.nodeRules = nodeRules;
        this.markRules = markRules;
    }

    /**
     * Build a parser from the parseDOM annotations in the schema.
     */
    public static DOMParser fromSchema(Schema schema) {
        List<NodeParseRule> nodeRules = new ArrayList<>();
        List<MarkParseRule> markRules = new ArrayList<>();

        // Collect node parse rules
        for (Map.Entry<String, NodeType> entry : schema.nodes().entrySet()) {
            NodeType type = entry.getValue();
            List<ParseRule> rules = type.spec().getParseDOM();
            if (rules == null) continue;
            for (ParseRule rule : rules) {
                nodeRules.add(new NodeParseRule(rule, type));
            }
        }

        // Collect mark parse rules
        for (Map.Entry<String, MarkType> entry : schema.marks().entrySet()) {
            MarkType type = entry.getValue();
            List<ParseRule> rules = type.spec().getParseDOM();
            if (rules == null) continue;
            for (ParseRule rule : rules) {
                markRules.add(new MarkParseRule(rule, type));
            }
        }

        // Sort by priority — lower number = higher priority
        nodeRules.sort(Comparator.comparingInt(r -> r.rule().getPriority()));
        markRules.sort(Comparator.comparingInt(r -> r.rule().getPriority()));

        return new DOMParser(schema, nodeRules, markRules);
    }

    // ---- Parsing from HTML string ----

    /**
     * Parse an HTML string into a document node.
     */
    public Node parseHTML(String html) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        return parseElement(jsoupDoc.body(), schema.topNodeType());
    }

    /**
     * Parse an HTML string into a fragment (no wrapping doc node).
     */
    public Fragment parseHTMLFragment(String html) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        return parseChildren(jsoupDoc.body(), schema.topNodeType());
    }

    // ---- Core parsing logic ----

    /**
     * Parse the children of a DOM element into a fragment.
     */
    private Fragment parseChildren(org.jsoup.nodes.Element element, NodeType parentType) {
        List<Node> children = new ArrayList<>();
        List<Mark> activeMarks = new ArrayList<>();
        parseChildNodes(element, parentType, children, activeMarks);
        return Fragment.from(children);
    }

    /**
     * Parse a DOM element into a document node.
     */
    private Node parseElement(org.jsoup.nodes.Element element, NodeType parentType) {
        Fragment content = parseChildren(element, parentType);
        return schema.topNodeType().createAndFill(null, content, null);
    }

    private void parseChildNodes(org.jsoup.nodes.Element element,
                                 NodeType parentType,
                                 List<Node> into,
                                 List<Mark> activeMarks) {
        for (org.jsoup.nodes.Node child : element.childNodes()) {
            switch (child) {
                case org.jsoup.nodes.TextNode textNode -> parseText(textNode, parentType, into, activeMarks);
                case org.jsoup.nodes.Element childEl -> parseElementNode(childEl, parentType, into, activeMarks);
                default -> {
                }
            }
            // Ignore comments, CDATA, etc.
        }
    }

    private void parseText(org.jsoup.nodes.TextNode textNode,
                           NodeType parentType,
                           List<Node> into,
                           List<Mark> activeMarks) {
        String text = textNode.getWholeText();

        // Collapse whitespace unless inside a code block
        if (!parentType.spec().isCode()) {
            text = collapseWhitespace(text);
        }

        if (text.isEmpty()) return;

        List<Mark> marks = Mark.allowedIn(activeMarks, parentType);
        into.add(schema.text(text, marks));
    }

    private void parseElementNode(org.jsoup.nodes.Element element,
                                  NodeType parentType,
                                  List<Node> into,
                                  List<Mark> activeMarks) {
        // Try matching a mark rule first
        MarkParseRule markRule = matchMarkRule(element);
        if (markRule != null) {
            Mark mark = createMarkFromRule(markRule, element);
            List<Mark> newMarks = Mark.addToSet(activeMarks, mark);
            parseChildNodes(element, parentType, into, newMarks);
            return;
        }

        // Try matching a node rule
        NodeParseRule nodeRule = matchNodeRule(element);
        if (nodeRule != null) {
            NodeType nodeType = nodeRule.nodeType();

            // Parse attributes from the DOM element
            Map<String, Object> attrs = extractAttrs(nodeRule, element);

            if (nodeType.isLeaf()) {
                Node node = nodeType.create(attrs, Fragment.EMPTY,
                    nodeType.isInline() ? Mark.allowedIn(activeMarks, parentType) : null);
                into.add(node);
                return;
            }

            // Parse children recursively
            List<Node> children = new ArrayList<>();
            org.jsoup.nodes.Element contentElement = findContentElement(nodeRule, element);
            parseChildNodes(contentElement, nodeType, children, List.of());

            Fragment content = Fragment.from(children);
            Node node = nodeType.createAndFill(attrs, content,
                nodeType.isInline() ? Mark.allowedIn(activeMarks, parentType) : null);

            if (node != null) {
                into.add(node);
            }
            return;
        }

        // No rule matched — descend into children transparently
        parseChildNodes(element, parentType, into, activeMarks);
    }

    // ---- Rule matching ----

    private NodeParseRule matchNodeRule(org.jsoup.nodes.Element element) {
        for (NodeParseRule rule : nodeRules) {
            if (ruleMatches(rule.rule(), element)) {
                return rule;
            }
        }
        return null;
    }

    private MarkParseRule matchMarkRule(org.jsoup.nodes.Element element) {
        for (MarkParseRule rule : markRules) {
            if (ruleMatches(rule.rule(), element)) {
                return rule;
            }
        }
        return null;
    }

    private boolean ruleMatches(ParseRule rule, org.jsoup.nodes.Element element) {
        // Match by tag/CSS selector
        if (rule.getTag() != null) {
            return element.is(rule.getTag());
        }

        // Match by inline style
        if (rule.getStyle() != null) {
            return matchesStyle(rule.getStyle(), element);
        }

        return false;
    }

    private boolean matchesStyle(String styleSpec, org.jsoup.nodes.Element element) {
        String inlineStyle = element.attr("style");
        if (inlineStyle.isEmpty()) return false;

        // styleSpec is like "font-weight=bold" or "font-style"
        int eqIndex = styleSpec.indexOf('=');
        if (eqIndex >= 0) {
            String prop = styleSpec.substring(0, eqIndex).trim();
            String value = styleSpec.substring(eqIndex + 1).trim();
            return parseInlineStyle(inlineStyle, prop, value);
        } else {
            // Just check property existence
            return inlineStyle.contains(styleSpec.trim());
        }
    }

    private boolean parseInlineStyle(String style, String property, String value) {
        for (String declaration : style.split(";")) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2
                    && parts[0].trim().equalsIgnoreCase(property)
                    && parts[1].trim().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    // ---- Attribute extraction ----

    private Map<String, Object> extractAttrs(NodeParseRule rule,
                                             org.jsoup.nodes.Element element) {
        ParseRule parseRule = rule.rule();
        GetAttrsFunction getAttrs = parseRule.getGetAttrs();

        if (getAttrs != null) {
            return getAttrs.getAttrs(element);
        }

        // Auto-extract: map DOM attributes to node attributes by name
        Map<String, AttrSpec> attrSpecs = rule.nodeType().spec().getAttrs();
        if (attrSpecs == null || attrSpecs.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, AttrSpec> entry : attrSpecs.entrySet()) {
            String name = entry.getKey();
            if (element.hasAttr(name)) {
                result.put(name, element.attr(name));
            }
        }
        return result;
    }

    private Mark createMarkFromRule(MarkParseRule rule,
                                    org.jsoup.nodes.Element element) {
        GetAttrsFunction getAttrs = rule.rule().getGetAttrs();
        Map<String, Object> attrs = null;

        if (getAttrs != null) {
            attrs = getAttrs.getAttrs(element);
        } else {
            // Auto-extract for marks
            Map<String, AttrSpec> attrSpecs = rule.markType().spec().getAttrs();
            if (attrSpecs != null && !attrSpecs.isEmpty()) {
                attrs = new LinkedHashMap<>();
                for (String name : attrSpecs.keySet()) {
                    if (element.hasAttr(name)) {
                        attrs.put(name, element.attr(name));
                    }
                }
            }
        }

        return rule.markType().create(attrs);
    }

    /**
     * Find the element within which to parse content. Usually the
     * element itself, but a rule can specify a contentElement selector
     * to dig deeper (e.g. parse content from a wrapper div inside).
     */
    private org.jsoup.nodes.Element findContentElement(NodeParseRule rule,
                                                       org.jsoup.nodes.Element element) {
        String selector = rule.rule().getContentElement();
        if (selector != null) {
            org.jsoup.nodes.Element found = element.selectFirst(selector);
            if (found != null) return found;
        }
        return element;
    }

    // ---- Whitespace handling ----

    private String collapseWhitespace(String text) {
        // Collapse runs of whitespace to single space, matching browser behavior
        return text.replaceAll("\\s+", " ");
    }

    // ---- Records for rule + type pairs ----

    private record NodeParseRule(ParseRule rule, NodeType nodeType) {}
    private record MarkParseRule(ParseRule rule, MarkType markType) {}

}