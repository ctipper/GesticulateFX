/**
 * ToDOMFunction.java
 *
 * Created on 1 Mar 2026 12:15:45
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
 *
 * @author ctipper
 */

/**
 * Describes how a node or mark should be rendered to DOM.
 *
 * The return value is a DOMOutputSpec — a nested structure describing the DOM element(s) to create:
 *
 * ["p", 0] — a
 * <p>
 * tag with content hole ["div", {"class": "note"}, 0] — tag with attributes, then hole ["img",
 * {"src": "..."}] — leaf element, no hole ["pre", ["code", 0]] — nested elements, hole in inner
 * ["a", {"href": "..."}, 0] — link wrapping content
 *
 * The "0" (hole) indicates where child content should be rendered. Marks always wrap content
 * directly, so their hole is implicit.
 */

@FunctionalInterface
public interface ToDOMFunction {

    DOMOutputSpec toDOM(HasAttrs source);
}
