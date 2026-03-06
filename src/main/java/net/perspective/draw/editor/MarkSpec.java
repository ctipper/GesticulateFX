/**
 * MarkSpec.java
 *
 * Created on 1 Mar 2026 11:57:52
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

public final class MarkSpec {

    private final Map<String, AttrSpec> attrs;
    private final String excludes;     // null = default (self), "" = none, "_" = all
    private final String group;        // mark group name, if any
    private final boolean spanning;    // true if mark spans across nodes (default true)
    private final ToDOMFunction toDOM;
    private final List<ParseRule> parseDOM;

    public MarkSpec() {
        this(null, null, null, true, null, null);
    }

    public MarkSpec(ToDOMFunction toDOM) {
        this(null, null, null, true, toDOM, null);
    }

    public MarkSpec(Map<String, AttrSpec> attrs, String excludes, String group,
        boolean spanning, ToDOMFunction toDOM, List<ParseRule> parseDOM) {
        this.attrs = attrs != null ? Collections.unmodifiableMap(attrs) : Map.of();
        this.excludes = excludes;
        this.group = group;
        this.spanning = spanning;
        this.toDOM = toDOM;
        this.parseDOM = parseDOM;
    }

    public Map<String, AttrSpec> getAttrs() {
        return attrs;
    }

    public String getExcludes() {
        return excludes;
    }

    public String getGroup() {
        return group;
    }

    public boolean isSpanning() {
        return spanning;
    }

    public ToDOMFunction getToDOM() {
        return toDOM;
    }

    public List<ParseRule> getParseDOM() {
        return parseDOM;
    }

}
