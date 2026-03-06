/**
 * Slice.java
 *
 * Created on 1 Mar 2026 12:56:08
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
 * A Slice represents a contiguous section of a document, along with metadata about how deeply the
 * slice is "open" on its start and end sides. This is used to track whether nodes at the boundaries
 * of a slice have been cut off mid-way through their content.
 *
 * @author ctipper
 */

public final class Slice {

    public static final Slice EMPTY = new Slice(Fragment.EMPTY, 0, 0);

    private final Fragment content;
    private final int openStart;
    private final int openEnd;

    /**
     * Create a new slice.
     *
     * @param content the fragment containing the slice's content
     * @param openStart the number of open levels at the start of the slice
     * @param openEnd the number of open levels at the end of the slice
     */
    public Slice(Fragment content, int openStart, int openEnd) {
        this.content = content;
        this.openStart = openStart;
        this.openEnd = openEnd;
    }

    /**
     * Get the content of this slice.
     */
    public Fragment content() {
        return content;
    }

    /**
     * Get the number of open levels at the start of this slice.
     */
    public int openStart() {
        return openStart;
    }

    /**
     * Get the number of open levels at the end of this slice.
     */
    public int openEnd() {
        return openEnd;
    }

    /**
     * The total size of this slice's content.
     *
     * @return the size of the content
     */
    public int size() {
        return content.size() - openStart - openEnd;
    }

    /**
     * Tests whether this slice has no content.
     *
     * @return true if the slice has no content
     */
    public boolean isEmpty() {
        return content.size() == 0;
    }

    /**
     * Create a new slice by cutting the content between the given positions, with open levels
     * reset to zero (producing a closed slice).
     *
     * @param from the start position
     * @param to the end position
     * @return a new closed slice with content from the given range
     */
    public Slice removeBetween(int from, int to) {
        return new Slice(content.cut(from, to), 0, 0);
    }

    /**
     * Create a slice from a fragment where the open sides extend as deeply as possible into the
     * nested node structure.
     *
     * @param fragment the fragment to create a slice from
     * @return a slice with maximally open sides
     */
    public static Slice maxOpen(Fragment fragment) {
        int openStart = 0;
        int openEnd = 0;

        // Walk down the start side
        Node node = fragment.firstChild();
        while (node != null && !node.isLeaf() && node.content().childCount() > 0) {
            openStart++;
            node = node.content().firstChild();
        }

        // Walk down the end side
        node = fragment.lastChild();
        while (node != null && !node.isLeaf() && node.content().childCount() > 0) {
            openEnd++;
            node = node.content().lastChild();
        }

        return new Slice(fragment, openStart, openEnd);
    }

    // ---- Serialization ----
    /**
     * Convert this slice to its JSON representation.
     *
     * @return a map representing the slice, or null if the slice is empty
     */
    public Map<String, Object> toJSON() {
        if (isEmpty()) {
            return null;
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("content", content.toJSON());
        if (openStart > 0) {
            json.put("openStart", openStart);
        }
        if (openEnd > 0) {
            json.put("openEnd", openEnd);
        }
        return json;
    }

    /**
     * Deserialize a slice from its JSON representation.
     *
     * @param schema the schema to use for fragment deserialization
     * @param json the JSON representation, or null for an empty slice
     * @return the deserialized slice
     */
    @SuppressWarnings("unchecked")
    public static Slice fromJSON(Schema schema, Map<String, Object> json) {
        if (json == null) {
            return EMPTY;
        }

        List<Map<String, Object>> contentJson
            = (List<Map<String, Object>>) json.get("content");
        Fragment content = Fragment.fromJSON(schema, contentJson);

        int openStart = json.containsKey("openStart")
            ? ((Number) json.get("openStart")).intValue() : 0;
        int openEnd = json.containsKey("openEnd")
            ? ((Number) json.get("openEnd")).intValue() : 0;

        return new Slice(content, openStart, openEnd);
    }

    /**
     * Tests whether this slice is equal to another object.
     *
     * @param obj the object to compare to
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Slice other)) {
            return false;
        }
        return openStart == other.openStart
            && openEnd == other.openEnd
            && content.eq(other.content);
    }

    /**
     * Get the hash code for this slice.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(content, openStart, openEnd);
    }

    /**
     * Get a string representation of this slice.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "Slice(" + content + ", " + openStart + ", " + openEnd + ")";
    }

}
