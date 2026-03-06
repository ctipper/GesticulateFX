/**
 * HasAttrs.java
 *
 * Created on 1 Mar 2026 12:21:21
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

/**
 * Read-only view of attributes, shared by both nodes and marks for use in toDOM functions.
 */
public interface HasAttrs {

    Map<String, Object> attrs();

    default Object attr(String name) {
        return attrs().get(name);
    }

    default String attrString(String name) {
        Object val = attrs().get(name);
        return val != null ? val.toString() : "";
    }

    default int attrInt(String name) {
        Object val = attrs().get(name);
        if (val instanceof Number n) {
            return n.intValue();
        }
        throw new IllegalStateException("Attribute '" + name + "' is not a number");
    }

}
