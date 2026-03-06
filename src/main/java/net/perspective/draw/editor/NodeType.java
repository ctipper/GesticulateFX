/**
 * NodeType.java
 *
 * Created on 1 Mar 2026 11:52:59
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
 * The type (schema) of a node. Defines what content a node can have, what marks can be applied to
 * it, and what attributes it must or may have. NodeTypes are stateless and can be shared between
 * multiple documents.
 *
 * @author ctipper
 */
public final class NodeType {

    private final String name;
    private final NodeSpec spec;
    private final Schema schema;

    private ContentExpression contentExpr; // compiled lazily after all types exist
    private Set<MarkType> allowedMarks;    // resolved lazily after all types exist

    NodeType(String name, NodeSpec spec, Schema schema) {
        this.name = name;
        this.spec = spec;
        this.schema = schema;
    }

    // ---- Core properties ----
    /**
     * Get the name of this node type.
     */
    public String name() { return name; }

    /**
     * Get the specification for this node type.
     */
    public NodeSpec spec() { return spec; }

    /**
     * Get the schema that owns this node type.
     */
    public Schema schema() { return schema; }

    /**
     * Get the compiled content expression for this node type, or null if not yet compiled.
     */
    public ContentExpression contentExpr() { return contentExpr; }

    // ---- Node classification ----
    /**
     * Check whether this node type is a block node (not inline).
     */
    public boolean isBlock() {
        return !spec.isInline();
    }

    /**
     * Check whether this node type is inline.
     */
    public boolean isInline() {
        return spec.isInline();
    }

    /**
     * Check if this node type has inline content (is a textblock).
     *
     * @return true if this node type allows inline content
     */
    public boolean inlineContent() {
        return contentExpr != null && contentExpr.inlineContent();
    }

    /**
     * Check if this is a block node with inline content.
     *
     * @return true if this node type is a textblock
     */
    public boolean isTextblock() {
        return isBlock() && inlineContent();
    }

    /**
     * Check if this node type allows no content.
     *
     * @return true if this node type is a leaf node
     */
    public boolean isLeaf() {
        return contentExpr != null && contentExpr.isLeaf();
    }

    /**
     * Check if this is a leaf node or an atom node (treated as a single unit).
     *
     * @return true if this node type is a leaf or atom
     */
    public boolean isAtom() {
        return isLeaf() || (spec.isAtom() != null && spec.isAtom());
    }

    // ---- Content validation ----

    /**
     * Check whether the given fragment is valid content for this type.
     *
     * @param content the fragment to validate
     * @return true if the content is valid
     */
    public boolean checkContent(Fragment content) {
        if (contentExpr == null) return true;
        return contentExpr.matches(content);
    }

    /**
     * Validate the given fragment, throwing if the content is invalid.
     *
     * @param content the fragment to validate
     * @throws SchemaError if the content is invalid
     */
    public void assertContent(Fragment content) {
        if (!checkContent(content)) {
            throw new SchemaError(
                "Invalid content for node " + name + ": " + content
            );
        }
    }

    // ---- Mark compatibility ----

    /**
     * Called during schema construction to resolve which marks are
     * allowed on children of this node type.
     */
    void resolveAllowedMarks() {
        String marksSpec = spec.getMarks();

        if (marksSpec == null || marksSpec.equals("_")) {
            // null or "_" means all marks allowed
            this.allowedMarks = null; // null = wildcard
            return;
        }

        if (marksSpec.isEmpty()) {
            // Empty string = no marks allowed
            this.allowedMarks = Set.of();
            return;
        }

        // Space-separated list of mark names or group names
        Set<MarkType> result = new LinkedHashSet<>();
        for (String token : marksSpec.split("\\s+")) {
            // Could be a mark name or a mark group
            MarkType direct = schema.marks().get(token);
            if (direct != null) {
                result.add(direct);
            } else {
                // Search for marks belonging to this group
                boolean found = false;
                for (MarkType markType : schema.marks().values()) {
                    String group = markType.spec().getGroup();
                    if (group != null && Set.of(group.split("\\s+")).contains(token)) {
                        result.add(markType);
                        found = true;
                    }
                }
                if (!found) {
                    throw new SchemaError(
                        "Unknown mark or mark group '" + token +
                        "' referenced in marks spec for " + name
                    );
                }
            }
        }

        this.allowedMarks = Collections.unmodifiableSet(result);
    }

    /**
     * Check whether a given mark type is allowed in children of this node type.
     *
     * @param markType the mark type to check
     * @return true if the mark is allowed
     */
    public boolean allowsMark(MarkType markType) {
        if (allowedMarks == null) return true; // wildcard
        return allowedMarks.contains(markType);
    }

    /**
     * Check whether a mark type name is allowed in children of this node type.
     *
     * @param markName the name of the mark type
     * @return true if the mark is allowed
     */
    public boolean allowsMarkType(String markName) {
        MarkType type = schema.marks().get(markName);
        return type != null && allowsMark(type);
    }

    // ---- Content expression compilation ----

    /**
     * Called during schema construction after all types and groups
     * are registered.
     */
    void compileContentExpr() {
        String contentStr = spec.getContent();
        if (contentStr == null || contentStr.isEmpty()) {
            this.contentExpr = ContentExpression.empty(this);
        } else {
            this.contentExpr = ContentExpression.parse(this, contentStr, schema);
        }
    }

    // ---- Node creation ----

    /**
     * Create a node of this type. Does NOT validate content.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the node content, or null for empty
     * @param marks the marks to apply, or null for none
     * @return a new node of this type
     * @throws SchemaError if this is a text node type
     */
    public Node create(Map<String, Object> attrs, Fragment content, List<Mark> marks) {
        if (isText()) {
            throw new SchemaError("Use schema.text() to create text nodes");
        }
        Map<String, Object> resolved = resolveAttrs(attrs);
        Fragment frag = content != null ? content : Fragment.EMPTY;
        List<Mark> resolvedMarks = marks != null ? marks : List.of();
        return new Node(this, frag, resolved, resolvedMarks, null);
    }

    /**
     * Create a node of this type from a list of child nodes.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the child nodes
     * @param marks the marks to apply, or null for none
     * @return a new node of this type
     */
    public Node create(Map<String, Object> attrs, List<Node> content, List<Mark> marks) {
        return create(attrs, Fragment.from(content), marks);
    }

    /**
     * Create a node of this type from a list of child nodes without marks.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the child nodes
     * @return a new node of this type
     */
    public Node create(Map<String, Object> attrs, List<Node> content) {
        return create(attrs, Fragment.from(content), null);
    }

    /**
     * Create a node of this type from varargs child nodes.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the child nodes
     * @return a new node of this type
     */
    public Node create(Map<String, Object> attrs, Node... content) {
        return create(attrs, Fragment.from(List.of(content)), null);
    }

    /**
     * Create a node of this type with no attributes or content.
     *
     * @return a new node of this type
     */
    public Node create() {
        return create(null, Fragment.EMPTY, null);
    }

    /**
     * Create a node and validate its content against the schema.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the node content, or null for empty
     * @param marks the marks to apply, or null for none
     * @return a new node with validated content
     * @throws SchemaError if the content is invalid for this node type
     */
    public Node createChecked(Map<String, Object> attrs, Fragment content,
                              List<Mark> marks) {
        Fragment frag = content != null ? content : Fragment.EMPTY;
        if (!checkContent(frag)) {
            throw new SchemaError("Invalid content for " + name + ": " + frag);
        }
        return create(attrs, frag, marks);
    }

    /**
     * Create a node, filling in required content with defaults where possible.
     *
     * @param attrs the node attributes, or null for defaults
     * @param content the node content, or null for empty
     * @param marks the marks to apply, or null for none
     * @return a new node with auto-filled content, or null if constraints can't be satisfied
     */
    public Node createAndFill(Map<String, Object> attrs, Fragment content,
                              List<Mark> marks) {
        Fragment frag = content != null ? content : Fragment.EMPTY;

        if (contentExpr == null) {
            return create(attrs, frag, marks);
        }

        Fragment filled = contentExpr.fillDefaults(frag);
        if (filled == null) return null;

        return create(attrs, filled, marks);
    }

    /**
     * Create a node with auto-filled default attributes.
     *
     * @param attrs the node attributes, or null for defaults
     * @return a new node with auto-filled content, or null if constraints can't be satisfied
     */
    public Node createAndFill(Map<String, Object> attrs) {
        return createAndFill(attrs, null, null);
    }

    /**
     * Create a default instance of this node type.
     *
     * @return a new default node, or null if constraints can't be satisfied
     */
    public Node createAndFill() {
        return createAndFill(null, null, null);
    }

    // ---- Default node generation ----

    /**
     * Create a default instance of this node type with minimal content that satisfies the schema.
     *
     * @return a new default node
     */
    public Node createDefaultNode() {
        return createAndFill();
    }

    // ---- Attribute resolution ----

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
                    "No value for required attribute '" + attrName +
                    "' on node type " + name
                );
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Check whether this node type has required attributes (attributes without defaults).
     *
     * @return true if at least one attribute is required
     */
    public boolean hasRequiredAttrs() {
        Map<String, AttrSpec> attrSpecs = spec.getAttrs();
        if (attrSpecs == null) return false;
        for (AttrSpec attrSpec : attrSpecs.values()) {
            if (!attrSpec.hasDefault()) return true;
        }
        return false;
    }

    // ---- Helpers ----
    /**
     * Check whether this is the special text node type.
     *
     * @return true if this node type is "text"
     */
    public boolean isText() {
        return name.equals("text");
    }

    /**
     * Get the default attributes for this node type — all defaults filled in. Only valid if
     * hasRequiredAttrs() is false.
     *
     * @return a map of default attribute values
     */
    public Map<String, Object> defaultAttrs() {
        return resolveAttrs(null);
    }

    /**
     * Get a string representation of this node type.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "NodeType(" + name + ")";
    }

}