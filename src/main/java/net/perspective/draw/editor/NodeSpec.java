/**
 * NodeSpec.java
 *
 * Created on 1 Mar 2026 12:12:07
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ctipper
 */

public final class NodeSpec {

    private final String content;    // content expression, e.g. "block+", "text*"
    private final String group;      // group name, e.g. "block"
    private final boolean inline;
    private final Boolean atom;      // null means infer from content
    private final String marks;      // allowed marks: null/"_" = all, "" = none
    private final Map<String, AttrSpec> attrs;
    private final boolean selectable;
    private final boolean draggable;
    private final boolean code;      // treat content as code (preserve whitespace)
    private final boolean definingAsContext;
    private final boolean definingForContent;
    private final boolean isolating;
    private final ToDOMFunction toDOM;
    private final List<ParseRule> parseDOM;

    /**
     * Minimal constructor for simple node types.
     */
    public NodeSpec(String content, String group, boolean inline,
        Map<String, AttrSpec> attrs, ToDOMFunction toDOM) {
        this(content, group, inline, null, null, attrs,
            true, false, false, false, false, false,
            toDOM, null);
    }

    /**
     * Full constructor.
     */
    public NodeSpec(String content, String group, boolean inline,
        Boolean atom, String marks, Map<String, AttrSpec> attrs,
        boolean selectable, boolean draggable, boolean code,
        boolean definingAsContext, boolean definingForContent,
        boolean isolating,
        ToDOMFunction toDOM, List<ParseRule> parseDOM) {
        this.content = content;
        this.group = group;
        this.inline = inline;
        this.atom = atom;
        this.marks = marks;
        this.attrs = attrs != null ? Collections.unmodifiableMap(attrs) : Map.of();
        this.selectable = selectable;
        this.draggable = draggable;
        this.code = code;
        this.definingAsContext = definingAsContext;
        this.definingForContent = definingForContent;
        this.isolating = isolating;
        this.toDOM = toDOM;
        this.parseDOM = parseDOM;
    }

    public String getContent() {
        return content;
    }

    public String getGroup() {
        return group;
    }

    public boolean isInline() {
        return inline;
    }

    public Boolean isAtom() {
        return atom;
    }

    public String getMarks() {
        return marks;
    }

    public Map<String, AttrSpec> getAttrs() {
        return attrs;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isCode() {
        return code;
    }

    public boolean isDefiningAsContext() {
        return definingAsContext;
    }

    public boolean isDefiningForContent() {
        return definingForContent;
    }

    public boolean isIsolating() {
        return isolating;
    }

    public ToDOMFunction getToDOM() {
        return toDOM;
    }

    public List<ParseRule> getParseDOM() {
        return parseDOM;
    }

}
