/**
 * Schema.java
 *
 * Created on 1 Mar 2026 12:09:37
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
 * A Schema defines the structure of documents in the editor, including all valid node and mark
 * types and their allowed content and marks. Schemas are immutable and can be shared across multiple
 * documents and editors.
 *
 * @author ctipper
 */

public final class Schema {

    private final Map<String, NodeType> nodes;
    private final Map<String, MarkType> marks;
    private final NodeType topNodeType;

    // Cached for quick lookup
    private final Map<String, List<NodeType>> groups;

    public Schema(Map<String, NodeSpec> nodeSpecs, Map<String, MarkSpec> markSpecs) {
        // ---- Build mark types first (nodes reference them for allowed marks) ----
        this.marks = new LinkedHashMap<>();
        int markRank = 0;
        for (Map.Entry<String, MarkSpec> entry : markSpecs.entrySet()) {
            this.marks.put(entry.getKey(),
                new MarkType(entry.getKey(), entry.getValue(), this, markRank++));
        }

        // ---- Build node types ----
        this.nodes = new LinkedHashMap<>();
        for (Map.Entry<String, NodeSpec> entry : nodeSpecs.entrySet()) {
            this.nodes.put(entry.getKey(),
                new NodeType(entry.getKey(), entry.getValue(), this));
        }

        // ---- Resolve groups ----
        this.groups = new LinkedHashMap<>();
        for (NodeType type : this.nodes.values()) {
            String group = type.spec().getGroup();
            if (group != null && !group.isEmpty()) {
                for (String groupName : group.split("\\s+")) {
                    groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(type);
                }
            }
        }

        // ---- Compile content expressions now that all types and groups exist ----
        for (NodeType type : this.nodes.values()) {
            type.compileContentExpr();
        }

        // ---- Resolve allowed marks on each node type ----
        for (NodeType type : this.nodes.values()) {
            type.resolveAllowedMarks();
        }

        // ---- Identify top node ----
        String topNodeName = nodeSpecs.entrySet().stream()
            .filter(e -> {
                String name = e.getKey();
                return name.equals("doc"); // default top node
            })
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(nodeSpecs.keySet().iterator().next());

        this.topNodeType = this.nodes.get(topNodeName);

        // ---- Validate ----
        if (!this.nodes.containsKey("text")) {
            throw new SchemaError("Every schema must define a 'text' node type");
        }
    }

    // ---- Accessors ----
    /**
     * Get all node types defined in this schema.
     */
    public Map<String, NodeType> nodes() {
        return nodes;
    }

    /**
     * Get all mark types defined in this schema.
     */
    public Map<String, MarkType> marks() {
        return marks;
    }

    /**
     * Get the top-level node type for this schema.
     */
    public NodeType topNodeType() {
        return topNodeType;
    }

    /**
     * Get a node type by name.
     *
     * @param name the name of the node type
     * @return the node type
     * @throws SchemaError if the node type is not found
     */
    public NodeType nodeType(String name) {
        NodeType type = nodes.get(name);
        if (type == null) {
            throw new SchemaError("Unknown node type: " + name);
        }
        return type;
    }

    /**
     * Get a mark type by name.
     *
     * @param name the name of the mark type
     * @return the mark type
     * @throws SchemaError if the mark type is not found
     */
    public MarkType markType(String name) {
        MarkType type = marks.get(name);
        if (type == null) {
            throw new SchemaError("Unknown mark type: " + name);
        }
        return type;
    }

    /**
     * Get all node types belonging to a group.
     */
    public List<NodeType> groupTypes(String groupName) {
        List<NodeType> types = groups.get(groupName);
        return types != null ? Collections.unmodifiableList(types) : List.of();
    }

    /**
     * Resolve a name that could be either a node type name or a group name into a list of concrete
     * node types.
     */
    public List<NodeType> resolveNodeTypes(String nameOrGroup) {
        NodeType direct = nodes.get(nameOrGroup);
        if (direct != null) {
            return List.of(direct);
        }
        List<NodeType> group = groups.get(nameOrGroup);
        if (group != null) {
            return Collections.unmodifiableList(group);
        }
        throw new SchemaError(
            "'" + nameOrGroup + "' is neither a node type nor a group in this schema"
        );
    }

    // ---- Node creation convenience methods ----
    /**
     * Create a text node.
     *
     * @param text the text content
     * @return a new text node
     * @throws IllegalArgumentException if the text is empty
     */
    public Node text(String text) {
        return text(text, List.of());
    }

    /**
     * Create a text node with marks.
     *
     * @param text the text content
     * @param marks the marks to apply
     * @return a new text node
     * @throws IllegalArgumentException if the text is empty
     */
    public Node text(String text, List<Mark> marks) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Empty text nodes are not allowed");
        }
        NodeType textType = nodeType("text");
        return new Node(textType, Fragment.EMPTY, Map.of(), marks, text);
    }

    /**
     * Create a mark of the given type.
     *
     * @param markName the name of the mark type
     * @return a new mark
     * @throws SchemaError if the mark type is not found
     */
    public Mark mark(String markName) {
        return markType(markName).create();
    }

    /**
     * Create a mark of the given type with attributes.
     *
     * @param markName the name of the mark type
     * @param attrs the mark attributes
     * @return a new mark
     * @throws SchemaError if the mark type is not found
     */
    public Mark mark(String markName, Map<String, Object> attrs) {
        return markType(markName).create(attrs);
    }

    // ---- Deserialization ----
    /**
     * Deserialize a node from its JSON representation.
     */
    public Node nodeFromJSON(Map<String, Object> json) {
        return Node.fromJSON(this, json);
    }

    /**
     * Deserialize a mark from its JSON representation.
     */
    public Mark markFromJSON(Map<String, Object> json) {
        return Mark.fromJSON(this, json);
    }

    // ---- DOM parsing ----
    /**
     * Create a DOMParser configured for this schema, using the parseDOM rules defined in the node
     * and mark specs.
     */
    public DOMParser parser() {
        return DOMParser.fromSchema(this);
    }

    /**
     * Create a DOMSerializer configured for this schema, using the toDOM functions defined in the
     * node and mark specs.
     */
    public DOMSerializer serializer() {
        return new DOMSerializer(this);
    }

    @Override
    public String toString() {
        return "Schema(nodes: " + nodes.keySet() + ", marks: " + marks.keySet() + ")";
    }

}
