/**
 * DOMOutputSpec.java
 * 
 * Created on 1 Mar 2026 12:16:26
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

import java.util.List;
import java.util.Map;

/**
 *
 * @author ctipper
 */

public sealed interface DOMOutputSpec {

    /**
     * A DOM element with a tag, optional attributes, and children.
     */
    record Element(
        String tag,
        Map<String, String> attrs,
        List<DOMOutputSpec> children
    ) implements DOMOutputSpec {

        public Element(String tag) {
            this(tag, Map.of(), List.of());
        }

        public Element(String tag, Map<String, String> attrs) {
            this(tag, attrs, List.of());
        }
    }

    /**
     * The content hole — marks where the node's children
     * should be rendered.
     */
    record Hole() implements DOMOutputSpec {
        public static final Hole INSTANCE = new Hole();
    }

    /**
     * A raw text string (rarely used directly, but needed
     * for cases like whitespace or fixed text content).
     */
    record Text(String value) implements DOMOutputSpec {}


    // ---- Convenience factory methods ----

    /**
     * Element with content hole: ["p", 0]
     */
    static DOMOutputSpec el(String tag) {
        return new Element(tag, Map.of(), List.of(Hole.INSTANCE));
    }

    /**
     * Element with attributes and content hole: ["div", {"class": "c"}, 0]
     */
    static DOMOutputSpec el(String tag, Map<String, String> attrs) {
        return new Element(tag, attrs, List.of(Hole.INSTANCE));
    }

    /**
     * Leaf element with no hole: ["hr"] or ["img", {"src": "..."}]
     */
    static DOMOutputSpec leaf(String tag) {
        return new Element(tag, Map.of(), List.of());
    }

    static DOMOutputSpec leaf(String tag, Map<String, String> attrs) {
        return new Element(tag, attrs, List.of());
    }

    /**
     * Nested elements: ["pre", ["code", 0]]
     */
    static DOMOutputSpec nested(String outerTag, DOMOutputSpec inner) {
        return new Element(outerTag, Map.of(), List.of(inner));
    }

    static DOMOutputSpec nested(String outerTag, Map<String, String> attrs,
                                DOMOutputSpec inner) {
        return new Element(outerTag, attrs, List.of(inner));
    }

    /**
     * Text content.
     */
    static DOMOutputSpec text(String value) {
        return new Text(value);
    }

    /**
     * The content hole singleton.
     */
    static DOMOutputSpec hole() {
        return Hole.INSTANCE;
    }

}