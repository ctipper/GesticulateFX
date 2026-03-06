/**
 * SchemaError.java
 *
 * Created on 1 Mar 2026 12:28:31
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

public class SchemaError extends RuntimeException {

    public SchemaError(String message) {
        super(message);
    }

    public SchemaError(String message, Throwable cause) {
        super(message, cause);
    }

}
