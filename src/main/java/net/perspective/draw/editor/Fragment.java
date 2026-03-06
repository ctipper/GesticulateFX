/**
 * Fragment.java
 *
 * Created on 1 Mar 2026 11:50:24
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A fragment is a slice of a document's content. It contains a sequence of nodes that may not
 * form a complete document but can be used for editing operations like copying, pasting, and
 * slicing content.
 *
 * @author ctipper
 */

public final class Fragment {

    public static final Fragment EMPTY = new Fragment(List.of(), 0);

    private final List<Node> content;
    private final int size; // cached token size

    private Fragment(List<Node> content, int size) {
        this.content = content;
        this.size = size;
    }

    // ---- Factory methods ----
    public static Fragment from(List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return EMPTY;
        }
        List<Node> merged = mergeAdjacentText(nodes);
        int size = 0;
        for (Node node : merged) {
            size += node.nodeSize();
        }
        return new Fragment(Collections.unmodifiableList(merged), size);
    }

    public static Fragment from(Node node) {
        if (node == null) {
            return EMPTY;
        }
        return new Fragment(List.of(node), node.nodeSize());
    }

    /**
     * Adjacent text nodes with identical marks must be merged. Empty text nodes are dropped. This
     * maintains the invariant that a document has a single canonical representation.
     */
    private static List<Node> mergeAdjacentText(List<Node> nodes) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isText() && node.text().isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                Node last = result.get(result.size() - 1);
                if (last.isText() && node.isText()
                    && Mark.sameSet(last.marks(), node.marks())) {
                    result.set(result.size() - 1, last.withText(last.text() + node.text()));
                    continue;
                }
            }
            result.add(node);
        }
        return result;
    }

    // ---- Core properties ----
    /**
     * The total token size of this fragment's content.
     * @return the size in tokens
     */
    public int size() {
        return size;
    }

    public int childCount() {
        return content.size();
    }

    public Node child(int index) {
        return content.get(index);
    }

    public Node firstChild() {
        return content.isEmpty() ? null : content.get(0);
    }

    public Node lastChild() {
        return content.isEmpty() ? null : content.get(content.size() - 1);
    }

    // ---- Iteration ----
    /**
     * Call the callback for every child node with its offset and index.
     * @param callback the callback to invoke for each child node
     */
    public void forEach(Node.NodeIterator callback) {
        int offset = 0;
        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            callback.accept(child, offset, i);
            offset += child.nodeSize();
        }
    }

    /**
     * Simple iteration over nodes without offset tracking.
     * @param callback the callback to invoke for each node
     */
    public void forEachNode(Consumer<Node> callback) {
        for (Node node : content) {
            callback.accept(node);
        }
    }

    /**
     * Visit every descendant node in the fragment. The visitor receives the node, its position
     * relative to the start of this fragment, the parent, and the child index. Return false to skip
     * descending into that node's children.
     * @param visitor the visitor to invoke for each descendant node
     * @param parent the parent node of this fragment
     */
    public void descendants(Node.DescendantVisitor visitor, Node parent) {
        int pos = 0;
        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            final int childPos = pos;
            boolean descend = visitor.visit(child, childPos, parent, i);
            if (descend && child.content().childCount() > 0) {
                child.descendants((Node node, int nodePos, Node par, int index) -> visitor.visit(node, childPos + 1 + nodePos, par, index));
            }
            pos += child.nodeSize();
        }
    }

    // ---- Searching ----
    /**
     * Find the child node at a given token offset. Returns an IndexResult with the index, the
     * offset of that child, and the child node.
     * @param pos the token position to search for
     * @return an IndexResult containing the child index, offset, and node
     */
    public IndexResult findIndex(int pos) {
        if (pos == 0) {
            return new IndexResult(0, 0, firstChild());
        }
        if (pos == size) {
            return new IndexResult(content.size(), size, null);
        }
        if (pos < 0 || pos > size) {
            throw new IndexOutOfBoundsException(
                "Position " + pos + " outside fragment of size " + size
            );
        }
        int offset = 0;
        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            int end = offset + child.nodeSize();
            if (end > pos) {
                return new IndexResult(i, offset, child);
            }
            offset = end;
        }
        throw new IllegalStateException("Should not reach here");
    }

    public static final class IndexResult {

        private final int index;
        private final int offset;
        private final Node node;

        public IndexResult(int index, int offset, Node node) {
            this.index = index;
            this.offset = offset;
            this.node = node;
        }

        public int index() {
            return index;
        }

        public int offset() {
            return offset;
        }

        public Node node() {
            return node;
        }
    }

    /**
     * Call the callback for each node between the given positions. Positions are relative to the
     * start of this fragment.
     * 
     * @param from the starting token position
     * @param to the ending token position
     * @param visitor the visitor to invoke for each node in range
     * @param startPos the absolute position of the start of this fragment
     * @param parent the parent node of this fragment
     */
    public void nodesBetween(int from, int to, Node.DescendantVisitor visitor,
        int startPos, Node parent) {
        int pos = 0;
        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            int end = pos + child.nodeSize();

            if (end > from && pos < to) {
                boolean descend = visitor.visit(child, startPos + pos, parent, i);
                if (descend && child.content().childCount() > 0) {
                    int innerFrom = Math.max(0, from - pos - 1);
                    int innerTo = Math.min(child.content().size(), to - pos - 1);
                    if (innerFrom < innerTo) {
                        child.content().nodesBetween(
                            innerFrom, innerTo, visitor,
                            startPos + pos + 1, child
                        );
                    }
                }
            }

            pos = end;
            if (pos >= to) {
                break;
            }
        }
    }

    // ---- Immutable updates ----
    /**
     * Return a new fragment with the child at index replaced.
     * 
     * @param index the index of the child to replace
     * @param node the new node to insert at the index
     * @return a new fragment with the replaced child
     */
    public Fragment replaceChild(int index, Node node) {
        Node current = content.get(index);
        if (current == node) {
            return this;
        }
        List<Node> newContent = new ArrayList<>(content);
        newContent.set(index, node);
        return new Fragment(
            Collections.unmodifiableList(newContent),
            size - current.nodeSize() + node.nodeSize()
        );
    }

    /**
     * Insert a node at the given child index.
     * @param index the index at which to insert the node
     * @param node the node to insert
     * @return a new fragment with the inserted child
     */
    public Fragment insertChild(int index, Node node) {
        List<Node> newContent = new ArrayList<>(content);
        newContent.add(index, node);
        return Fragment.from(newContent); // from() handles merging
    }

    /**
     * Remove the child at the given index.
     * @param index the index of the child to remove
     * @return a new fragment with the child removed
     */
    public Fragment removeChild(int index) {
        List<Node> newContent = new ArrayList<>(content);
        newContent.remove(index);
        return Fragment.from(newContent);
    }

    /**
     * Append another fragment's content to this one.
     * @param other the fragment to append
     * @return a new fragment with the other fragment's content appended
     */
    public Fragment append(Fragment other) {
        if (other.content.isEmpty()) {
            return this;
        }
        if (content.isEmpty()) {
            return other;
        }

        List<Node> combined = new ArrayList<>(content.size() + other.content.size());
        combined.addAll(content);
        combined.addAll(other.content);
        return Fragment.from(combined); // from() merges adjacent text
    }

    /**
     * Cut the fragment to only include content between the given token positions (relative to the
     * start of this fragment).
     * @param from the starting token position
     * @param to the ending token position
     * @return a new fragment containing only the content in the specified range
     */
    public Fragment cut(int from, int to) {
        if (from == 0 && to == size) {
            return this;
        }
        if (from >= to) {
            return EMPTY;
        }

        List<Node> result = new ArrayList<>();
        int pos = 0;

        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            int end = pos + child.nodeSize();

            if (end <= from) {
                pos = end;
                continue;
            }
            if (pos >= to) {
                break;
            }

            if (child.isText()) {
                // Slice the text node
                int textFrom = Math.max(0, from - pos);
                int textTo = Math.min(child.text().length(), to - pos);
                result.add(child.cut(textFrom, textTo));
            } else if (child.isLeaf()) {
                // Leaf node — include it whole
                result.add(child);
            } else {
                // Non-leaf, non-text: may need to cut into the child's content
                int innerFrom = Math.max(0, from - pos - 1);
                int innerTo = Math.min(child.content().size(), to - pos - 1);

                if (innerFrom == 0 && innerTo == child.content().size()) {
                    // Include the whole child
                    result.add(child);
                } else {
                    // Cut into the child — create a new node with sliced content
                    result.add(child.copy(child.content().cut(innerFrom, innerTo)));
                }
            }

            pos = end;
        }

        return Fragment.from(result);
    }

    public Fragment cut(int from) {
        return cut(from, size);
    }

    // ---- Mark operations ----
    /**
     * Add a mark to every inline node in this fragment.
     * @param mark the mark to add
     * @return a new fragment with the mark added to all inline nodes
     */
    public Fragment addMark(Mark mark) {
        List<Node> result = new ArrayList<>(content.size());
        for (Node child : content) {
            if (child.isInline()) {
                result.add(child.mark(Mark.addToSet(child.marks(), mark)));
            } else {
                result.add(child.copy(child.content().addMark(mark)));
            }
        }
        return Fragment.from(result);
    }

    /**
     * Remove a mark type from every inline node in this fragment.
     * @param markType the type of mark to remove
     * @return a new fragment with the mark type removed from all inline nodes
     */
    public Fragment removeMark(MarkType markType) {
        List<Node> result = new ArrayList<>(content.size());
        for (Node child : content) {
            if (child.isInline()) {
                result.add(child.mark(Mark.removeFromSet(child.marks(), markType)));
            } else {
                result.add(child.copy(child.content().removeMark(markType)));
            }
        }
        return Fragment.from(result);
    }

    // ---- Text operations ----
    /**
     * Concatenate all text content in this fragment between the given positions.
     * @param from the starting position
     * @param to the ending position
     * @param blockSeparator the separator to insert between block nodes (may be null)
     * @param leafText the text to insert for leaf nodes (may be null)
     * @return the concatenated text content
     */
    public String textBetween(int from, int to, String blockSeparator,
        String leafText) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        int pos = 0;

        for (int i = 0; i < content.size(); i++) {
            Node child = content.get(i);
            int end = pos + child.nodeSize();

            if (end <= from) {
                pos = end;
                continue;
            }
            if (pos >= to) {
                break;
            }

            if (child.isText()) {
                int textFrom = Math.max(0, from - pos);
                int textTo = Math.min(child.text().length(), to - pos);
                sb.append(child.text(), textFrom, textTo);
            } else if (child.isLeaf()) {
                if (leafText != null) {
                    sb.append(leafText);
                }
            } else {
                if (blockSeparator != null && child.isBlock() && !first) {
                    sb.append(blockSeparator);
                }
                int innerFrom = Math.max(0, from - pos - 1);
                int innerTo = Math.min(child.content().size(), to - pos - 1);
                sb.append(child.content().textBetween(
                    innerFrom, innerTo, blockSeparator, leafText
                ));
            }

            first = false;
            pos = end;
        }

        return sb.toString();
    }

    // ---- Equality ----
    /**
     * Structural equality — same children in the same order.
     * @param other the fragment to compare with
     * @return true if this fragment has the same children in the same order as the other fragment
     */
    public boolean eq(Fragment other) {
        if (this == other) {
            return true;
        }
        if (content.size() != other.content.size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            if (!content.get(i).eq(other.content.get(i))) {
                return false;
            }
        }
        return true;
    }

    // ---- Serialization ----
    public List<Map<String, Object>> toJSON() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Node node : content) {
            result.add(node.toJSON());
        }
        return result;
    }

    public static Fragment fromJSON(Schema schema, List<Map<String, Object>> json) {
        if (json == null || json.isEmpty()) {
            return EMPTY;
        }
        List<Node> nodes = new ArrayList<>();
        for (Map<String, Object> nodeJson : json) {
            nodes.add(Node.fromJSON(schema, nodeJson));
        }
        return Fragment.from(nodes);
    }

    @Override
    public String toString() {
        return content.stream()
            .map(Node::toString)
            .collect(Collectors.joining(", ", "<", ">"));
    }

}
