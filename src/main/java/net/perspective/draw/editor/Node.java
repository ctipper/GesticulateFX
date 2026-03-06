/**
 * Node.java
 *
 * Created on 1 Mar 2026 11:46:06
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A node represents a single element in the document tree. Each node has a type, optional content
 * (for non-leaf nodes), attributes defined by its type, and a list of marks applied to it.
 *
 * @author ctipper
 */

public final class Node implements HasAttrs {

    private final NodeType type;
    private final Fragment content;
    private final Map<String, Object> attrs;
    private final List<Mark> marks;
    private final String text; // non-null only for text nodes

    // Package-private constructor — nodes are created through Schema/NodeType
    Node(NodeType type, Fragment content, Map<String, Object> attrs,
        List<Mark> marks, String text) {
        this.type = type;
        this.content = content != null ? content : Fragment.EMPTY;
        this.attrs = attrs != null ? Collections.unmodifiableMap(attrs) : Map.of();
        this.marks = marks != null ? Collections.unmodifiableList(marks) : List.of();
        this.text = text;
    }

    // ---- Core properties ----
    public NodeType type() {
        return type;
    }

    public Fragment content() {
        return content;
    }

    @Override
    public Map<String, Object> attrs() {
        return attrs;
    }

    public List<Mark> marks() {
        return marks;
    }

    public Node firstChild() {
        return content.firstChild();
    }

    public Node lastChild() {
        return content.lastChild();
    }

    public String text() {
        if (text != null) {
            return text;
        }
        // For non-text nodes, concatenate all descendant text
        StringBuilder sb = new StringBuilder();
        content.forEachNode(node -> {
            if (node.isText()) {
                sb.append(node.text);
            }
        });
        return sb.toString();
    }

    // ---- Node classification ----
    public boolean isText() {
        return text != null;
    }

    public boolean isBlock() {
        return !type.spec().isInline();
    }

    public boolean isInline() {
        return type.spec().isInline();
    }

    public boolean isTextblock() {
        return isBlock() && type.inlineContent();
    }

    public boolean isLeaf() {
        return type.isLeaf();
    }

    public boolean isAtom() {
        return type.isAtom();
    }

    // ---- Size and indexing ----
    /**
     * The total size of this node in the document's token sequence. For text nodes: length of text.
     * For leaf blocks: 1. For non-leaf nodes: content size + 2 (opening + closing tokens).
     */
    public int nodeSize() {
        if (isText()) {
            return text.length();
        }
        if (isLeaf()) {
            return 1;
        }
        return content.size() + 2;
    }

    // ---- Child access ----
    public int childCount() {
        return content.childCount();
    }

    public Node child(int index) {
        return content.child(index);
    }

    /**
     * Call the callback for every child node, passing the node, its offset within this node, and
     * its index.
     * @param callback the callback to invoke for each child node
     */
    public void forEach(NodeIterator callback) {
        content.forEach(callback);
    }

    @FunctionalInterface
    public interface NodeIterator {

        void accept(Node child, int offset, int index);
    }

    // ---- Mark queries ----
    public boolean hasMarkup(NodeType type, Map<String, Object> attrs, List<Mark> marks) {
        return this.type == type
            && this.attrs.equals(attrs != null ? attrs : Map.of())
            && Mark.sameSet(this.marks, marks != null ? marks : List.of());
    }

    public boolean hasMark(MarkType markType) {
        for (Mark mark : marks) {
            if (mark.type() == markType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a copy of this node with a different set of marks.
     * @param marks the new list of marks
     * @return a new node with the specified marks
     */
    public Node mark(List<Mark> marks) {
        if (Mark.sameSet(this.marks, marks)) {
            return this;
        }
        return new Node(type, content, attrs, marks, text);
    }

    // ---- Tree traversal ----
    /**
     * Visit every descendant node. The callback receives the node, its absolute position in the
     * document, the parent node, and its index within the parent. Return false to skip descendants.
     * @param visitor the visitor to invoke for each descendant node
     */
    public void descendants(DescendantVisitor visitor) {
        descendantsInner(0, visitor);
    }

    private void descendantsInner(int startPos, DescendantVisitor visitor) {
        int pos = startPos;
        for (int i = 0; i < content.childCount(); i++) {
            Node child = content.child(i);
            boolean descend = visitor.visit(child, pos, this, i);
            if (descend && child.content.childCount() > 0) {
                child.descendantsInner(pos + 1, visitor);
            }
            pos += child.nodeSize();
        }
    }

    @FunctionalInterface
    public interface DescendantVisitor {

        boolean visit(Node node, int pos, Node parent, int index);
    }

    /**
     * Find the first matching node in a depth-first traversal.
     * @param predicate the predicate to test each node
     * @return the first node matching the predicate, or null if none found
     */
    public Node findFirst(Predicate<Node> predicate) {
        if (predicate.test(this)) {
            return this;
        }
        for (int i = 0; i < content.childCount(); i++) {
            Node found = content.child(i).findFirst(predicate);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // ---- Position resolution ----
    /**
     * Resolve a document position to a ResolvedPos, which provides information about the position's
     * context in the tree.
     * @param pos the document position to resolve
     * @return a ResolvedPos object representing the position in context
     */
    public ResolvedPos resolve(int pos) {
        return ResolvedPos.resolve(this, pos);
    }

    /**
     * Get the node at the given document position (the deepest node that contains that position).
     * @param pos the document position to search for
     * @return the node at the position, or null if outside bounds
     */
    public Node nodeAt(int pos) {
        for (int i = 0; i < content.childCount(); i++) {
            Node child = content.child(i);
            if (child.isText()) {
                if (pos < child.nodeSize()) {
                    return child;
                }
            } else if (child.isLeaf()) {
                if (pos == 0) {
                    return child;
                }
            } else {
                // pos 0 is the open token of this child
                if (pos == 0) {
                    return child;
                }
                // pos 1..content.size is inside the child
                if (pos >= 1 && pos <= child.content.size()) {
                    return child.nodeAt(pos - 1);
                }
            }
            pos -= child.nodeSize();
        }
        return null;
    }

    // ---- Slicing ----
    /**
     * Cut a slice from this node between the given positions.
     * @param from the starting position
     * @param to the ending position
     * @return a slice containing the content between the positions
     */
    public Slice slice(int from, int to) {
        return slice(from, to, false);
    }

    /**
     * Cut a slice from this node between the given positions, optionally including parent nodes.
     * @param from the starting position
     * @param to the ending position
     * @param includeParents whether to include parent nodes in the open boundaries
     * @return a slice containing the content between the positions
     */
    public Slice slice(int from, int to, boolean includeParents) {
        if (from == to) {
            return Slice.EMPTY;
        }

        ResolvedPos fromRes = resolve(from);
        ResolvedPos toRes = resolve(to);
        int depth = includeParents ? 0 : fromRes.sharedDepth(to);

        Node start = fromRes.node(depth);
        Fragment content = start.content().cut(
            fromRes.start(depth),
            toRes.start(depth)
        );

        int openStart = fromRes.depth() - depth;
        int openEnd = toRes.depth() - depth;

        return new Slice(content, openStart, openEnd);
    }

    // ---- Immutable updates ----
    /**
     * Create a copy with new content but same type, attrs, and marks.
     * @param newContent the new content for this node
     * @return a new node with the specified content
     */
    public Node copy(Fragment newContent) {
        if (newContent == content) {
            return this;
        }
        return new Node(type, newContent, attrs, marks, null);
    }

    /**
     * Replace the content between from and to with the given slice.
     * @param from the starting position
     * @param to the ending position
     * @param slice the slice to insert
     * @return a new node with the replaced content
     */
    public Node replace(int from, int to, Slice slice) {
        return Replace.replace(this, from, to, slice);
    }

    /**
     * Replace a child at the given index.
     * @param index the index of the child to replace
     * @param child the new child node
     * @return a new node with the replaced child
     */
    public Node replaceChild(int index, Node child) {
        return copy(content.replaceChild(index, child));
    }

    /**
     * Create a copy with different attributes.
     * @param newAttrs the new attributes for this node
     * @return a new node with the specified attributes
     */
    public Node withAttrs(Map<String, Object> newAttrs) {
        if (newAttrs.equals(attrs)) {
            return this;
        }
        return new Node(type, content, newAttrs, marks, text);
    }

    /**
     * Create a text node with different text but same marks.
     * @param newText the new text content
     * @return a new text node with the specified text
     * @throws IllegalStateException if this is not a text node
     */
    public Node withText(String newText) {
        if (!isText()) {
            throw new IllegalStateException("Not a text node");
        }
        if (newText.equals(text)) {
            return this;
        }
        return new Node(type, Fragment.EMPTY, attrs, marks, newText);
    }

    /**
     * Cut this text node to a substring.
     * @param from the starting index
     * @param to the ending index
     * @return a new text node with the substring
     * @throws IllegalStateException if this is not a text node
     */
    public Node cut(int from, int to) {
        if (!isText()) {
            throw new IllegalStateException("Not a text node");
        }
        return withText(text.substring(from, to));
    }

    // ---- Content validation ----
    /**
     * Check whether this node's content matches the schema.
     * @return true if the content is valid, false otherwise
     */
    public boolean check() {
        return type.checkContent(content);
    }

    // ---- Equality and identity ----
    /**
     * Test whether two nodes represent the same content.
     * @param other the node to compare with
     * @return true if the nodes have the same type, attributes, marks, and content
     */
    public boolean eq(Node other) {
        if (this == other) {
            return true;
        }
        if (type != other.type) {
            return false;
        }
        if (!attrs.equals(other.attrs)) {
            return false;
        }
        if (!Mark.sameSet(marks, other.marks)) {
            return false;
        }
        if (isText()) {
            return Objects.equals(text, other.text);
        }
        return content.eq(other.content);
    }

    /**
     * Get text content
     * 
     * @return 
     */
    public String textContent() {
        if (isText()) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.childCount(); i++) {
            sb.append(content.child(i).textContent());
        }
        return sb.toString();
    }

    /**
     * Get text content length
     * 
     * @return 
     */
    public int textLength() {
        if (isText()) {
            return text.length();
        }
        int length = 0;
        for (int i = 0; i < content.childCount(); i++) {
            length += content.child(i).textLength();
        }
        return length;
    }

    // ---- Serialization ----
    /**
     * Serialize to a JSON-compatible map.
     * @return a map representation of this node in JSON format
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", type.name());

        if (!attrs.isEmpty()) {
            json.put("attrs", new LinkedHashMap<>(attrs));
        }

        if (isText()) {
            json.put("text", text);
        } else if (content.childCount() > 0) {
            List<Map<String, Object>> contentJson = new ArrayList<>();
            for (int i = 0; i < content.childCount(); i++) {
                contentJson.add(content.child(i).toJSON());
            }
            json.put("content", contentJson);
        }

        if (!marks.isEmpty()) {
            List<Map<String, Object>> marksJson = new ArrayList<>();
            for (Mark mark : marks) {
                marksJson.add(mark.toJSON());
            }
            json.put("marks", marksJson);
        }

        return json;
    }

    /**
     * Deserialize from JSON through the schema.
     * @param schema the schema to use for type resolution
     * @param json the JSON object representing the node
     * @return a new node deserialized from the JSON
     */
    @SuppressWarnings("unchecked")
    public static Node fromJSON(Schema schema, Map<String, Object> json) {
        String typeName = (String) json.get("type");
        NodeType nodeType = schema.nodeType(typeName);

        Map<String, Object> attrs = (Map<String, Object>) json.get("attrs");

        List<Mark> marks = List.of();
        if (json.containsKey("marks")) {
            List<Map<String, Object>> marksJson = (List<Map<String, Object>>) json.get("marks");
            marks = new ArrayList<>();
            for (Map<String, Object> markJson : marksJson) {
                marks.add(Mark.fromJSON(schema, markJson));
            }
        }

        if (json.containsKey("text")) {
            return new Node(nodeType, Fragment.EMPTY, attrs, marks, (String) json.get("text"));
        }

        Fragment content = Fragment.EMPTY;
        if (json.containsKey("content")) {
            List<Map<String, Object>> contentJson = (List<Map<String, Object>>) json.get("content");
            List<Node> children = new ArrayList<>();
            for (Map<String, Object> childJson : contentJson) {
                children.add(Node.fromJSON(schema, childJson));
            }
            content = Fragment.from(children);
        }

        return nodeType.create(attrs, content, marks);
    }

    @Override
    public String toString() {
        if (isText()) {
            String marksStr = marks.isEmpty() ? ""
                : marks.stream().map(m -> m.type().name())
                    .collect(Collectors.joining(",", "(", ")"));
            return "\"" + text + "\"" + marksStr;
        }
        String contentStr = content.childCount() == 0 ? ""
            : "(" + IntStream.range(0, content.childCount())
                .mapToObj(i -> content.child(i).toString())
                .collect(Collectors.joining(", ")) + ")";
        return type.name() + contentStr;
    }

}
