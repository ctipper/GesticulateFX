/**
 * ParseRule.java
 * 
 * Created on 1 Mar 2026 12:32:12
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

/**
 * Defines a rule for parsing DOM elements into document nodes or marks. Each rule specifies how to
 * match an HTML element (by tag selector or inline style) and how to interpret it during parsing.
 *
 * <p>
 * Rules are consumed by {@link DOMParser} and configured via {@link NodeSpec} and {@link MarkSpec}
 * parse rule lists.</p>
 *
 * <p>
 * Simple rules can be created with the {@link #tag(String)} and {@link #style(String)} factory
 * methods. For more complex cases use the {@link Builder}.</p>
 *
 * @author ctipper
 */

public final class ParseRule {

    private final String tag;             // CSS selector to match, e.g. "em", "div.note"
    private final String style;           // inline style to match, e.g. "font-weight=bold"
    private final int priority;           // lower = matched first (default 50)
    private final String contentElement;  // CSS selector for where content lives
    private final boolean skip;           // skip this element, parse children
    private final boolean ignore;         // ignore this element and its children
    private final GetAttrsFunction getAttrs;  // extract attributes from DOM element
    private final PreserveWhitespace preserveWhitespace;

    /**
     * Controls how whitespace is handled when parsing the content of a
     * matched element.
     */
    public enum PreserveWhitespace {
        /** Collapse whitespace according to normal HTML rules (default). */
        NONE,
        /** Keep whitespace as-is but still normalise newlines. */
        PRESERVE,
        /** Keep all whitespace including newlines verbatim. */
        FULL
    }

    /**
     * Creates a rule that matches elements by CSS tag selector.
     *
     * @param selector CSS selector to match, e.g. {@code "em"} or
     *                 {@code "div.note"}
     * @return a new parse rule with default priority and no attribute
     *         extraction
     */
    public static ParseRule tag(String selector) {
        return new ParseRule(selector, null, 50, null, false, false, null,
                             PreserveWhitespace.NONE);
    }

    /**
     * Creates a rule that matches elements by inline style.
     *
     * @param styleSpec style specification to match, e.g.
     *                  {@code "font-weight=bold"}
     * @return a new parse rule with default priority and no attribute
     *         extraction
     */
    public static ParseRule style(String styleSpec) {
        return new ParseRule(null, styleSpec, 50, null, false, false, null,
                             PreserveWhitespace.NONE);
    }

    /**
     * Constructs a parse rule with all options specified.
     *
     * @param tag                 CSS selector to match, or {@code null}
     * @param style               inline style to match, or {@code null}
     * @param priority            matching priority; lower values are tried
     *                            first (default 50)
     * @param contentElement      CSS selector for the child element that
     *                            holds the content, or {@code null}
     * @param skip                if {@code true} the matched element is
     *                            skipped but its children are still parsed
     * @param ignore              if {@code true} the matched element and
     *                            all its children are ignored
     * @param getAttrs            function to extract attributes from the
     *                            DOM element, or {@code null}
     * @param preserveWhitespace  whitespace handling mode
     */
    public ParseRule(String tag, String style, int priority,
                     String contentElement, boolean skip, boolean ignore,
                     GetAttrsFunction getAttrs,
                     PreserveWhitespace preserveWhitespace) {
        this.tag = tag;
        this.style = style;
        this.priority = priority;
        this.contentElement = contentElement;
        this.skip = skip;
        this.ignore = ignore;
        this.getAttrs = getAttrs;
        this.preserveWhitespace = preserveWhitespace;
    }

    public String getTag() { return tag; }
    public String getStyle() { return style; }
    public int getPriority() { return priority; }
    public String getContentElement() { return contentElement; }
    public boolean isSkip() { return skip; }
    public boolean isIgnore() { return ignore; }
    public GetAttrsFunction getGetAttrs() { return getAttrs; }
    public PreserveWhitespace getPreserveWhitespace() { return preserveWhitespace; }

    /**
     * Returns a new {@link Builder} for constructing complex parse rules.
     *
     * @return a blank builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A fluent builder for constructing {@link ParseRule} instances with
     * non-default options.
     */
    public static final class Builder {
        private String tag;
        private String style;
        private int priority = 50;
        private String contentElement;
        private boolean skip = false;
        private boolean ignore = false;
        private GetAttrsFunction getAttrs;
        private PreserveWhitespace preserveWhitespace = PreserveWhitespace.NONE;

        public Builder tag(String t) { this.tag = t; return this; }
        public Builder style(String s) { this.style = s; return this; }
        public Builder priority(int p) { this.priority = p; return this; }
        public Builder contentElement(String c) { this.contentElement = c; return this; }
        public Builder skip() { this.skip = true; return this; }
        public Builder ignore() { this.ignore = true; return this; }
        public Builder getAttrs(GetAttrsFunction fn) { this.getAttrs = fn; return this; }
        public Builder preserveWhitespace(PreserveWhitespace pw) {
            this.preserveWhitespace = pw; return this;
        }

        /**
         * Builds the {@link ParseRule} from the current builder state.
         *
         * @return a new parse rule
         */
        public ParseRule build() {
            return new ParseRule(tag, style, priority, contentElement,
                                skip, ignore, getAttrs, preserveWhitespace);
        }
    }

}