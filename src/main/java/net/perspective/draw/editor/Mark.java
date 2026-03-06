/**
 * Mark.java
 *
 * Created on 1 Mar 2026 11:55:56
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
import java.util.stream.Collectors;

/**
 * An immutable inline decoration that can be applied to text content within a document.
 * Marks represent formatting such as bold, italic, links, and other inline styles.
 *
 * <p>Mark sets are represented as sorted lists ordered by schema-defined rank,
 * guaranteeing a canonical representation: two mark sets with the same marks
 * always produce the same list.</p>
 *
 * @author ctipper
 */

public final class Mark implements HasAttrs {

    private final MarkType type;
    private final Map<String, Object> attrs;

    /**
     * Constructs a mark with the given type and attributes.
     *
     * @param type  the mark type
     * @param attrs the attribute map, or {@code null} for no attributes
     */
    Mark(MarkType type, Map<String, Object> attrs) {
        this.type = type;
        this.attrs = attrs != null ? Collections.unmodifiableMap(attrs) : Map.of();
    }

    // ---- Core properties ----

    /**
     * Returns the type of this mark.
     *
     * @return the mark type
     */
    public MarkType type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> attrs() {
        return attrs;
    }

    // ---- Equality ----

    /**
     * Two marks are equal if they have the same type and attributes. Since marks are immutable
     * values, this is purely structural.
     *
     * @param other the mark to compare with
     * @return {@code true} if both marks have the same type and attributes
     */
    public boolean eq(Mark other) {
        if (this == other) {
            return true;
        }
        if (type != other.type) {
            return false;
        }
        return attrs.equals(other.attrs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mark other)) {
            return false;
        }
        return eq(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, attrs);
    }

    // ---- Set operations ----
    // Mark sets are represented as sorted lists. The sort order is
    // determined by the schema — marks appear in the order their
    // types are defined. This guarantees a canonical representation:
    // two mark sets with the same marks always produce the same list.

    /**
     * Test whether two mark sets contain the same marks.
     *
     * @param a the first mark set
     * @param b the second mark set
     * @return {@code true} if both sets contain equal marks in the same order
     */
    public static boolean sameSet(List<Mark> a, List<Mark> b) {
        if (a == b) {
            return true;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).eq(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a mark to a set, replacing any existing mark of the same type. Returns a new list in
     * schema-defined order.
     *
     * @param set  the existing mark set
     * @param mark the mark to add
     * @return the updated mark set, or the original set if unchanged
     */
    public static List<Mark> addToSet(List<Mark> set, Mark mark) {
        List<Mark> result = new ArrayList<>(set.size() + 1);
        boolean added = false;

        for (Mark existing : set) {
            if (existing.type == mark.type) {
                // Same type — replace if different attrs, skip if identical
                if (mark.eq(existing)) {
                    return set;
                }
                // Insert the new mark at this position
                if (!added) {
                    result.add(mark);
                    added = true;
                }
                continue;
            }

            // Insert new mark before any type that comes after it in schema order
            if (!added && mark.type.rank() < existing.type.rank()) {
                result.add(mark);
                added = true;
            }

            result.add(existing);
        }

        if (!added) {
            result.add(mark);
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Remove a mark of the given type from a set. Returns the same list if the mark type wasn't
     * present.
     *
     * @param set  the mark set to remove from
     * @param type the mark type to remove
     * @return the updated mark set, or the original set if the type was not present
     */
    public static List<Mark> removeFromSet(List<Mark> set, MarkType type) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).type == type) {
                List<Mark> result = new ArrayList<>(set.size() - 1);
                for (int j = 0; j < set.size(); j++) {
                    if (j != i) {
                        result.add(set.get(j));
                    }
                }
                return Collections.unmodifiableList(result);
            }
        }
        return set; // type not found, no change
    }

    /**
     * Remove a specific mark instance from a set.
     *
     * @param set  the mark set to remove from
     * @param mark the mark instance to remove
     * @return the updated mark set, or the original set if the mark was not found
     */
    public static List<Mark> removeFromSet(List<Mark> set, Mark mark) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).eq(mark)) {
                List<Mark> result = new ArrayList<>(set.size() - 1);
                for (int j = 0; j < set.size(); j++) {
                    if (j != i) {
                        result.add(set.get(j));
                    }
                }
                return Collections.unmodifiableList(result);
            }
        }
        return set;
    }

    /**
     * Check whether a mark of the given type is in the set.
     *
     * @param set  the mark set to search
     * @param type the mark type to look for
     * @return {@code true} if a mark of the given type exists in the set
     */
    public static boolean hasMarkInSet(List<Mark> set, MarkType type) {
        for (Mark mark : set) {
            if (mark.type == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a mark of the given type in the set, or null.
     *
     * @param set  the mark set to search
     * @param type the mark type to look for
     * @return the matching mark, or {@code null} if not found
     */
    public static Mark findInSet(List<Mark> set, MarkType type) {
        for (Mark mark : set) {
            if (mark.type == type) {
                return mark;
            }
        }
        return null;
    }

    /**
     * Compute the union of two mark sets.
     *
     * @param a the first mark set
     * @param b the second mark set
     * @return a mark set containing all marks from both sets
     */
    public static List<Mark> union(List<Mark> a, List<Mark> b) {
        if (a == b || b.isEmpty()) {
            return a;
        }
        if (a.isEmpty()) {
            return b;
        }

        List<Mark> result = new ArrayList<>(a);
        boolean changed = false;
        for (Mark mark : b) {
            if (!hasMarkInSet(result, mark.type)) {
                if (!changed) {
                    result = new ArrayList<>(a);
                    changed = true;
                }
                result = new ArrayList<>(addToSet(result, mark));
            }
        }
        return changed ? Collections.unmodifiableList(result) : a;
    }

    /**
     * Compute the intersection of two mark sets — marks present in both.
     *
     * @param a the first mark set
     * @param b the second mark set
     * @return a mark set containing only marks present in both sets
     */
    public static List<Mark> intersect(List<Mark> a, List<Mark> b) {
        if (a == b) {
            return a;
        }

        List<Mark> result = new ArrayList<>();
        for (Mark mark : a) {
            Mark found = findInSet(b, mark.type);
            if (found != null && found.eq(mark)) {
                result.add(mark);
            }
        }

        if (result.size() == a.size()) {
            return a;
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(result);
    }

    // ---- Exclusion ----

    /**
     * Test whether this mark is present in the given set.
     *
     * @param set the mark set to search
     * @return {@code true} if an equal mark exists in the set
     */
    public boolean isInSet(List<Mark> set) {
        for (Mark mark : set) {
            if (mark.eq(this)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test whether this mark's type excludes the given mark type, or vice versa.
     *
     * @param other the mark to test exclusion against
     * @return {@code true} if the marks are mutually exclusive
     */
    public boolean excludes(Mark other) {
        return type.excludes(other.type) || other.type.excludes(type);
    }

    /**
     * Filter a mark set, removing any marks that are excluded by this mark or that exclude this
     * mark.
     *
     * @param set the mark set to filter
     * @return the filtered mark set, or the original set if no marks were excluded
     */
    public List<Mark> removeExcluded(List<Mark> set) {
        List<Mark> result = null;
        for (int i = 0; i < set.size(); i++) {
            if (excludes(set.get(i))) {
                if (result == null) {
                    result = new ArrayList<>(set.subList(0, i));
                }
            } else if (result != null) {
                result.add(set.get(i));
            }
        }
        return result != null ? Collections.unmodifiableList(result) : set;
    }

    // ---- Schema compatibility ----

    /**
     * Test whether this mark is allowed at the given node type.
     *
     * @param nodeType the node type to check against
     * @return {@code true} if this mark's type is permitted on the node type
     */
    public boolean isAllowedIn(NodeType nodeType) {
        return nodeType.allowsMark(type);
    }

    /**
     * Filter a mark set to only include marks allowed on the given node type.
     *
     * @param set      the mark set to filter
     * @param nodeType the node type to check against
     * @return the filtered mark set, or the original set if all marks are allowed
     */
    public static List<Mark> allowedIn(List<Mark> set, NodeType nodeType) {
        if (set.isEmpty()) {
            return set;
        }

        List<Mark> result = null;
        for (int i = 0; i < set.size(); i++) {
            if (!set.get(i).isAllowedIn(nodeType)) {
                if (result == null) {
                    result = new ArrayList<>(set.subList(0, i));
                }
            } else if (result != null) {
                result.add(set.get(i));
            }
        }

        if (result == null) {
            return set;
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(result);
    }

    // ---- Serialization ----

    /**
     * Serializes this mark to a JSON-compatible map.
     *
     * @return a map containing the mark type name and attributes
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", type.name());
        if (!attrs.isEmpty()) {
            json.put("attrs", new LinkedHashMap<>(attrs));
        }
        return json;
    }

    /**
     * Deserializes a mark from a JSON-compatible map.
     *
     * @param schema the schema to resolve the mark type from
     * @param json   the map containing mark type name and attributes
     * @return the deserialized mark
     */
    @SuppressWarnings("unchecked")
    public static Mark fromJSON(Schema schema, Map<String, Object> json) {
        String typeName = (String) json.get("type");
        MarkType markType = schema.markType(typeName);
        Map<String, Object> attrs = (Map<String, Object>) json.get("attrs");
        return markType.create(attrs);
    }

    @Override
    public String toString() {
        if (attrs.isEmpty()) {
            return type.name();
        }
        return type.name() + "("
            + attrs.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "))
            + ")";
    }

}
