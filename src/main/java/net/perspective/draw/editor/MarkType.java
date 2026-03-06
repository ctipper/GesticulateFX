/**
 * MarkType.java
 * 
 * Created on 1 Mar 2026 11:57:08
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

import java.util.*;

/**
 * The type (schema) of a mark. Defines what attributes a mark can have and how it interacts with
 * other marks (exclusions). MarkTypes are stateless and can be shared between multiple documents.
 *
 * @author ctipper
 */

public final class MarkType {

    private final String name;
    private final MarkSpec spec;
    private final Schema schema;
    private final int rank; // position in schema's mark ordering

    private final Set<String> excludes; // mark type names this excludes

    MarkType(String name, MarkSpec spec, Schema schema, int rank) {
        this.name = name;
        this.spec = spec;
        this.schema = schema;
        this.rank = rank;

        // Parse excludes spec: space-separated mark type names,
        // or "_" for "excludes all", or "" for "excludes nothing"
        // Default: excludes self (can't have two of the same mark)
        String excludeStr = spec.getExcludes();
        if (excludeStr == null) {
            this.excludes = Set.of(name); // default: exclude self
        } else if (excludeStr.equals("_")) {
            this.excludes = null; // sentinel: excludes everything
        } else if (excludeStr.isEmpty()) {
            this.excludes = Set.of(); // excludes nothing
        } else {
            this.excludes = Set.of(excludeStr.split("\\s+"));
        }
    }

    /**
     * Get the name of this mark type.
     */
    public String name() { return name; }

    /**
     * Get the specification for this mark type.
     */
    public MarkSpec spec() { return spec; }

    /**
     * Get the schema that owns this mark type.
     */
    public Schema schema() { return schema; }

    /**
     * Get the rank (position in mark ordering) of this mark type.
     */
    public int rank() { return rank; }

    /**
     * Create an instance of this mark type with the given attributes.
     *
     * @param attrs the mark attributes, or null for defaults
     * @return a new mark of this type
     */
    public Mark create(Map<String, Object> attrs) {
        Map<String, Object> resolved = resolveAttrs(attrs);
        return new Mark(this, resolved);
    }

    /**
     * Create an instance of this mark type with no attributes.
     *
     * @return a new mark of this type
     */
    public Mark create() {
        return create(null);
    }

    /**
     * Check whether this mark type excludes the given mark type. Marks that exclude each other
     * cannot coexist on the same text range.
     *
     * @param other the mark type to check
     * @return true if this mark excludes the other mark type
     */
    public boolean excludes(MarkType other) {
        if (excludes == null) return true; // "_" — excludes everything
        return excludes.contains(other.name);
    }

    /**
     * Internal: Resolve mark attributes from the given map, applying defaults and validation.
     */
    private Map<String, Object> resolveAttrs(Map<String, Object> given) {
        Map<String, AttrSpec> attrSpecs = spec.getAttrs();
        if (attrSpecs == null || attrSpecs.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, AttrSpec> entry : attrSpecs.entrySet()) {
            String attrName = entry.getKey();
            AttrSpec attrSpec = entry.getValue();

            if (given != null && given.containsKey(attrName)) {
                result.put(attrName, given.get(attrName));
            } else if (attrSpec.hasDefault()) {
                result.put(attrName, attrSpec.getDefaultValue());
            } else {
                throw new SchemaError(
                    "No value for required attribute '" + attrName + "' on mark " + name
                );
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Get a string representation of this mark type.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "MarkType(" + name + ")";
    }

}