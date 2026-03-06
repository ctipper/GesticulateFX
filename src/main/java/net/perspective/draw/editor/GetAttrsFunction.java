/**
 * GetAttrsFunction.java
 *
 * Created on 1 Mar 2026 12:34:27
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

import java.util.Map;

/**
 *
 * @author ctipper
 */

@FunctionalInterface
public interface GetAttrsFunction {

    /**
     * Extract node/mark attributes from a DOM element. Return null to indicate this rule doesn't
     * match after all (acts as an additional filter beyond the tag/style match).
     */
    Map<String, Object> getAttrs(org.jsoup.nodes.Element element);

}
