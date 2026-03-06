/**
 * AttrSpec.java
 *
 * Created on 1 Mar 2026 12:44:32
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

import java.util.Objects;
import java.util.Set;

/**
 * Specification for an attribute that can be attached to a node or mark. Defines whether the
 * attribute is required or has a default value, and includes optional validation.
 *
 * @author ctipper
 */

public final class AttrSpec {

    private final Object defaultValue;
    private final boolean hasDefault;
    private final AttrValidator validator;

    private AttrSpec(Object defaultValue, boolean hasDefault, AttrValidator validator) {
        this.defaultValue = defaultValue;
        this.hasDefault = hasDefault;
        this.validator = validator;
    }

    // ---- Factories ----
    /**
     * An attribute that must be provided at node/mark creation time. Nodes with required attributes
     * cannot be auto-generated as filler content by the schema.
     */
    public static AttrSpec required() {
        return new AttrSpec(null, false, null);
    }

    /**
     * An attribute with a default value. If not provided at creation time, the default is used.
     * Nodes where all attributes have defaults can be auto-generated.
     */
    public static AttrSpec withDefault(Object value) {
        return new AttrSpec(value, true, null);
    }

    /**
     * A required attribute with validation.
     */
    public static AttrSpec required(AttrValidator validator) {
        return new AttrSpec(null, false, validator);
    }

    /**
     * An attribute with a default and validation.
     */
    public static AttrSpec withDefault(Object value, AttrValidator validator) {
        return new AttrSpec(value, true, validator);
    }

    // ---- Accessors ----
    /**
     * Get the default value for this attribute, or null if no default is set.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Check whether this attribute has a default value.
     */
    public boolean hasDefault() {
        return hasDefault;
    }

    /**
     * Get the validator for this attribute, or null if no validator is configured.
     */
    public AttrValidator getValidator() {
        return validator;
    }

    /**
     * Resolve the value for this attribute: use the given value if present, fall back to default,
     * or throw if required and missing.
     *
     * @param attrName the attribute name (for error messages)
     * @param typeName the type name (for error messages)
     * @param given the provided value, or null
     * @return the resolved value
     * @throws SchemaError if the attribute is required but no value is provided
     */
    public Object resolve(String attrName, String typeName, Object given) {
        if (given != null) {
            validate(attrName, typeName, given);
            return given;
        }
        if (hasDefault) {
            return defaultValue;
        }
        throw new SchemaError(
            "No value for required attribute '" + attrName + "' on " + typeName
        );
    }

    /**
     * Run validation if a validator is configured.
     *
     * @param attrName the attribute name (for error messages)
     * @param typeName the type name (for error messages)
     * @param value the value to validate
     * @throws SchemaError if validation fails
     */
    public void validate(String attrName, String typeName, Object value) {
        if (validator != null && !validator.isValid(value)) {
            throw new SchemaError(
                "Invalid value for attribute '" + attrName + "' on " + typeName
                + ": " + value
            );
        }
    }

    /**
     * Check whether a given value matches the default. Useful for JSON serialization — skip default
     * values to keep output compact.
     *
     * @param value the value to check
     * @return true if the value equals the default
     */
    public boolean isDefault(Object value) {
        if (!hasDefault) {
            return false;
        }
        return Objects.equals(value, defaultValue);
    }

    /**
     * Get a string representation of this attribute specification.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        if (hasDefault) {
            return "AttrSpec(default=" + defaultValue + ")";
        }
        return "AttrSpec(required)";
    }

    // ---- Validator ----
    /**
     * A validator function that checks whether a value satisfies certain constraints.
     */
    @FunctionalInterface
    public interface AttrValidator {

        /**
         * Check whether a value is valid.
         *
         * @param value the value to validate
         * @return true if the value is valid
         */
        boolean isValid(Object value);
    }

    // ---- Common validators ----
    /**
     * Validator that accepts positive integers.
     */
    public static final AttrValidator POSITIVE_INT = value
        -> value instanceof Integer i && i > 0;

    /**
     * Validator that accepts non-empty strings.
     */
    public static final AttrValidator NON_EMPTY_STRING = value
        -> value instanceof String s && !s.isEmpty();

    /**
     * Validator that accepts booleans.
     */
    public static final AttrValidator BOOLEAN = value
        -> value instanceof Boolean;

    /**
     * Create a validator that accepts one of a set of allowed values.
     *
     * @param allowed the allowed values
     * @return a validator for the given set
     */
    public static AttrValidator oneOf(Object... allowed) {
        Set<Object> set = Set.of(allowed);
        return set::contains;
    }

    /**
     * Create a validator that accepts integers within a given range (inclusive).
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a validator for the given range
     */
    public static AttrValidator range(int min, int max) {
        return value -> value instanceof Integer i && i >= min && i <= max;
    }

}
