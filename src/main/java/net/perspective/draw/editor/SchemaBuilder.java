/**
 * SchemaBuilder.java
 * 
 * Created on 1 Mar 2026 12:13:15
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ctipper
 */

public final class SchemaBuilder {

    private final Map<String, NodeSpec> nodes = new LinkedHashMap<>();
    private final Map<String, MarkSpec> marks = new LinkedHashMap<>();

    public SchemaBuilder addNode(String name, NodeSpecBuilder spec) {
        nodes.put(name, spec.build());
        return this;
    }

    public SchemaBuilder addMark(String name, MarkSpecBuilder spec) {
        marks.put(name, spec.build());
        return this;
    }

    public Schema build() {
        return new Schema(nodes, marks);
    }

    // ---- Node spec builder ----

    public static NodeSpecBuilder node() {
        return new NodeSpecBuilder();
    }

    public static final class NodeSpecBuilder {
        private String content;
        private String group;
        private boolean inline;
        private Boolean atom;
        private String marks;
        private Map<String, AttrSpec> attrs = new LinkedHashMap<>();
        private boolean selectable = true;
        private boolean draggable = false;
        private boolean code = false;
        private boolean definingAsContext = false;
        private boolean definingForContent = false;
        private boolean isolating = false;
        private ToDOMFunction toDOM;
        private List<ParseRule> parseDOM;

        public NodeSpecBuilder content(String expr) { this.content = expr; return this; }
        public NodeSpecBuilder group(String g) { this.group = g; return this; }
        public NodeSpecBuilder inline() { this.inline = true; return this; }
        public NodeSpecBuilder atom() { this.atom = true; return this; }
        public NodeSpecBuilder marks(String m) { this.marks = m; return this; }
        public NodeSpecBuilder selectable(boolean s) { this.selectable = s; return this; }
        public NodeSpecBuilder draggable(boolean d) { this.draggable = d; return this; }
        public NodeSpecBuilder code() { this.code = true; return this; }
        public NodeSpecBuilder definingAsContext() { this.definingAsContext = true; return this; }
        public NodeSpecBuilder definingForContent() { this.definingForContent = true; return this; }
        public NodeSpecBuilder isolating() { this.isolating = true; return this; }
        public NodeSpecBuilder toDOM(ToDOMFunction fn) { this.toDOM = fn; return this; }
        public NodeSpecBuilder parseDOM(List<ParseRule> rules) { this.parseDOM = rules; return this; }

        public NodeSpecBuilder attr(String name, Object defaultValue) {
            attrs.put(name, AttrSpec.withDefault(defaultValue));
            return this;
        }

        public NodeSpecBuilder requiredAttr(String name) {
            attrs.put(name, AttrSpec.required());
            return this;
        }

        NodeSpec build() {
            return new NodeSpec(content, group, inline, atom, marks, attrs,
                selectable, draggable, code, definingAsContext,
                definingForContent, isolating, toDOM, parseDOM);
        }
    }

    // ---- Mark spec builder ----

    public static MarkSpecBuilder mark() {
        return new MarkSpecBuilder();
    }

    public static final class MarkSpecBuilder {
        private Map<String, AttrSpec> attrs = new LinkedHashMap<>();
        private String excludes;
        private String group;
        private boolean spanning = true;
        private ToDOMFunction toDOM;
        private List<ParseRule> parseDOM;

        public MarkSpecBuilder excludes(String e) { this.excludes = e; return this; }
        public MarkSpecBuilder group(String g) { this.group = g; return this; }
        public MarkSpecBuilder spanning(boolean s) { this.spanning = s; return this; }
        public MarkSpecBuilder toDOM(ToDOMFunction fn) { this.toDOM = fn; return this; }
        public MarkSpecBuilder parseDOM(List<ParseRule> rules) { this.parseDOM = rules; return this; }

        public MarkSpecBuilder attr(String name, Object defaultValue) {
            attrs.put(name, AttrSpec.withDefault(defaultValue));
            return this;
        }

        public MarkSpecBuilder requiredAttr(String name) {
            attrs.put(name, AttrSpec.required());
            return this;
        }

        MarkSpec build() {
            return new MarkSpec(attrs, excludes, group, spanning, toDOM, parseDOM);
        }
    }

}