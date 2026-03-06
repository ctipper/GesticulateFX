/**
 * ResolvedPos.java
 *
 * Created on 1 Mar 2026 12:54:09
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author ctipper
 */

/**
 * Represents a resolved position in a document. A resolved position provides detailed information
 * about where a position sits in the document tree: which nodes contain it, at what offsets, and
 * what the surrounding context looks like.
 *
 * A resolved position stores a "path" of ancestor nodes from the document root down to the
 * innermost node containing the position. Each level in the path records the node, the position of
 * that node's start in the document, and the offset within that node's content.
 */

public final class ResolvedPos {

    private final int pos;
    private final List<Level> path;  // root (depth 0) to innermost
    private final int parentOffset;  // offset into innermost node's content

    private ResolvedPos(int pos, List<Level> path, int parentOffset) {
        this.pos = pos;
        this.path = path;
        this.parentOffset = parentOffset;
    }

    /**
     * One level in the resolution path.
     */
    private record Level(
        Node node, // the ancestor node
        int start, // absolute document position of this node's content start
        int offset // offset within this node's content where the path continues
        ) {

    }

    // ---- Factory ----
    /**
     * Resolve a position within the given top-level node.
     * @param doc the document node
     * @param pos the absolute position to resolve
     * @return a resolved position
     */
    public static ResolvedPos resolve(Node doc, int pos) {
        if (pos < 0 || pos > doc.content().size()) {
            throw new IndexOutOfBoundsException(
                "Position " + pos + " out of range for document of size "
                + doc.content().size()
            );
        }

        List<Level> path = new ArrayList<>();
        resolveInner(doc, pos, 0, path);

        Level innermost = path.get(path.size() - 1);
        int parentOffset = pos - innermost.start;

        return new ResolvedPos(pos, Collections.unmodifiableList(path), parentOffset);
    }

    private static void resolveInner(Node node, int pos, int start,
        List<Level> path) {
        int offset = pos - start;
        path.add(new Level(node, start, offset));

        // Walk children to find which one contains this offset
        int childStart = 0;
        for (int i = 0; i < node.content().childCount(); i++) {
            Node child = node.content().child(i);
            int childEnd = childStart + child.nodeSize();

            if (childEnd > offset) {
                // Text and leaf nodes don't have interior positions to resolve into
                if (child.isText() || child.isLeaf()) {
                    return;
                }
                // Descend into this child. +1 to skip the opening token.
                resolveInner(child, pos, start + childStart + 1, path);
                return;
            }

            childStart = childEnd;
        }
        // Position is at the end of node's content — stay at this level
    }

    // ---- Core properties ----
    /**
     * The absolute document position.
     * @return the position
     */
    public int pos() {
        return pos;
    }

    /**
     * The nesting depth (0 = document root).
     * @return the depth
     */
    public int depth() {
        return path.size() - 1;
    }

    /**
     * The offset into the innermost parent node's content.
     * @return the parent offset
     */
    public int parentOffset() {
        return parentOffset;
    }

    // ---- Ancestor access ----
    /**
     * The node at the given depth (0 = document root).
     * @param depth the ancestor depth
     * @return the node at that depth
     */
    public Node node(int depth) {
        return path.get(depth).node;
    }

    /**
     * The node at the given depth, or null if depth is out of range.
     * @param depth the ancestor depth
     * @return the node at that depth, or null
     */
    public Node nodeOrNull(int depth) {
        if (depth < 0 || depth >= path.size()) {
            return null;
        }
        return path.get(depth).node;
    }

    /**
     * The innermost parent node (the node directly containing the position).
     * @return the parent node
     */
    public Node parent() {
        return node(depth());
    }

    /**
     * The document root node.
     * @return the root node
     */
    public Node doc() {
        return node(0);
    }

    // ---- Position access ----
    /**
     * The absolute position of the start of the node at the given depth. This is the position right
     * after the opening token.
     * @param depth the ancestor depth
     * @return the start position
     */
    public int start(int depth) {
        return path.get(depth).start;
    }

    /**
     * The absolute position of the end of the node at the given depth. This is the position right
     * before the closing token.
     * @param depth the ancestor depth
     * @return the end position
     */
    public int end(int depth) {
        return path.get(depth).start + path.get(depth).node.content().size();
    }

    /**
     * The absolute position right before the node at the given depth. For the root (depth 0), this
     * is 0.
     * @param depth the ancestor depth
     * @return the position before the node
     */
    public int before(int depth) {
        if (depth == 0) {
            throw new IllegalArgumentException("Cannot call before on depth 0");
        }
        return path.get(depth).start - 1;
    }

    /**
     * The absolute position right after the node at the given depth.
     * @param depth the ancestor depth
     * @return the position after the node
     */
    public int after(int depth) {
        if (depth == 0) {
            throw new IllegalArgumentException("Cannot call after on depth 0");
        }
        return path.get(depth).start + path.get(depth).node.content().size() + 1;
    }

    /**
     * Start of the innermost parent's content.
     * @return the start position
     */
    public int start() {
        return start(depth());
    }

    /**
     * End of the innermost parent's content.
     * @return the end position
     */
    public int end() {
        return end(depth());
    }

    // ---- Child/sibling access ----
    /**
     * The index of the child at the position within the parent at the given depth. If the position
     * is at the end of the node's content, returns childCount.
     * @param depth the ancestor depth
     * @return the child index
     */
    public int index(int depth) {
        Level level = path.get(depth);
        Node node = level.node;
        int offset = level.offset;

        if (offset == 0) {
            return 0;
        }
        if (offset == node.content().size()) {
            return node.childCount();
        }

        Fragment.IndexResult result = node.content().findIndex(offset);
        return result.index();
    }

    /**
     * Index within the innermost parent.
     * @return the child index
     */
    public int index() {
        return index(depth());
    }

    /**
     * The index after the position (relevant when the position is between two nodes — index gives
     * the one before, indexAfter gives the one after).
     * @param depth the ancestor depth
     * @return the index after the position
     */
    public int indexAfter(int depth) {
        int idx = index(depth);
        Level level = path.get(depth);
        if (level.offset == level.node.content().size()) {
            return idx;
        }
        // If we're inside a child node (not between children), return same index + 1
        return idx + 1;
    }

    /**
     * The index after the position within the innermost parent.
     * @return the index after the position
     */
    public int indexAfter() {
        return indexAfter(depth());
    }

    /**
     * The node directly after the position at the given depth, or null if at the end.
     * @return the node after this position, or null
     */
    public Node nodeAfter() {
        Node par = parent();
        int idx = index(depth());
        if (idx == par.childCount()) {
            return null;
        }

        int offset = parentOffset;
        Node child = par.content().child(idx);

        // If we're partway through a text node, there's no nodeAfter
        Fragment.IndexResult result = par.content().findIndex(offset);
        if (result.offset() < offset) {
            return null;
        }

        return child;
    }

    /**
     * The node directly before the position, or null if at the start.
     * @return the node before this position, or null
     */
    public Node nodeBefore() {
        int offset = parentOffset;
        if (offset == 0) {
            return null;
        }

        Node par = parent();
        Fragment.IndexResult result = par.content().findIndex(offset);

        // If offset lands exactly at a child boundary, return the child before
        if (result.offset() == offset && result.index() > 0) {
            return par.content().child(result.index() - 1);
        }

        // If we're inside a text node, no discrete node before
        return null;
    }

    // ---- Text offset ----
    /**
     * If the position is inside a text node, returns the offset within that text node. Otherwise
     * returns 0.
     * @return the text offset within the text node
     */
    public int textOffset() {
        Node par = parent();
        if (parentOffset == 0) {
            return 0;
        }

        Fragment.IndexResult result = par.content().findIndex(parentOffset);
        Node child = result.node();
        if (child != null && child.isText()) {
            return parentOffset - result.offset();
        }
        return 0;
    }

    // ---- Mark queries ----
    /**
     * The marks active at this position. If the position is at the start of a text node, returns
     * that node's marks. If between nodes, returns the marks of the adjacent text content.
     * @return the active marks
     */
    public List<Mark> marks() {
        Node par = parent();
        if (par.isTextblock() && par.childCount() == 0) {
            return List.of();
        }

        if (parentOffset == par.content().size()) {
            Node last = par.lastChild();
            return last != null ? last.marks() : List.of();
        }

        Fragment.IndexResult result = par.content().findIndex(parentOffset);
        Node child = result.node();
        if (child == null) {
            return List.of();
        }

        if (result.offset() == parentOffset && result.index() > 0) {
            Node before = par.content().child(result.index() - 1);
            if (before.isText()) {
                return before.marks();
            }
        }

        return child.marks();
    }

    /**
     * The marks active at this position that are also allowed by the parent node type.
     * @param other the other resolved position
     * @return the intersected marks, or null if not in a textblock
     */
    public List<Mark> marksAcross(ResolvedPos other) {
        Node par = parent();
        if (!par.isTextblock()) {
            return null;
        }

        List<Mark> myMarks = marks();
        List<Mark> otherMarks = other.marks();

        // Intersect: keep marks present in both sets
        return Mark.intersect(myMarks, otherMarks);
    }

    // ---- Depth queries ----
    /**
     * Find the deepest shared ancestor depth between this position and another position.
     * @param otherPos the other absolute position
     * @return the shared ancestor depth
     */
    public int sharedDepth(int otherPos) {
        for (int d = depth(); d >= 0; d--) {
            if (start(d) <= otherPos && end(d) >= otherPos) {
                return d;
            }
        }
        return 0;
    }

    /**
     * Find the deepest depth at which this position and another are in the same parent node.
     * @param other the other resolved position
     * @return the shared ancestor depth
     */
    public int sharedDepth(ResolvedPos other) {
        return sharedDepth(other.pos);
    }

    /**
     * Find the closest ancestor at or above the given depth that matches the predicate.
     * @param predicate the predicate to test nodes against
     * @return the matching ancestor depth, or -1 if not found
     */
    public int findAncestor(Predicate<Node> predicate) {
        for (int d = depth(); d >= 0; d--) {
            if (predicate.test(node(d))) {
                return d;
            }
        }
        return -1;
    }

    // ---- Block boundary queries ----
    /**
     * The depth of the innermost block node containing this position.
     * @return the block depth
     */
    public int blockDepth() {
        for (int d = depth(); d >= 0; d--) {
            if (node(d).isBlock()) {
                return d;
            }
        }
        return 0;
    }

    /**
     * The innermost block ancestor.
     * @return the block parent node
     */
    public Node blockParent() {
        return node(blockDepth());
    }

    /**
     * Whether this position is at the start of its parent's content.
     * @return true if at the start
     */
    public boolean atStart() {
        return parentOffset == 0;
    }

    /**
     * Whether this position is at the end of its parent's content.
     * @return true if at the end
     */
    public boolean atEnd() {
        return parentOffset == parent().content().size();
    }

    /**
     * Whether this position is at the start of the node at the given depth.
     * @param depth the ancestor depth
     * @return true if at the start
     */
    public boolean atStart(int depth) {
        return pos == start(depth);
    }

    /**
     * Whether this position is at the end of the node at the given depth.
     * @param depth the ancestor depth
     * @return true if at the end
     */
    public boolean atEnd(int depth) {
        return pos == end(depth);
    }

    // ---- Comparison ----
    /**
     * Compare this position to another. Resolving the same position in the same document always
     * produces equivalent results.
     * @param other the other resolved position
     * @return true if positions are equal
     */
    public boolean eq(ResolvedPos other) {
        return pos == other.pos;
    }

    /**
     * The minimum of this and another resolved position.
     * @param other the other resolved position
     * @return the position with the smaller value
     */
    public ResolvedPos min(ResolvedPos other) {
        return pos <= other.pos ? this : other;
    }

    /**
     * The maximum of this and another resolved position.
     * @param other the other resolved position
     * @return the position with the larger value
     */
    public ResolvedPos max(ResolvedPos other) {
        return pos >= other.pos ? this : other;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResolvedPos other)) {
            return false;
        }
        return pos == other.pos;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(pos);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResolvedPos(").append(pos).append(", path: ");
        for (int d = 0; d < path.size(); d++) {
            Level level = path.get(d);
            if (d > 0) {
                sb.append(" > ");
            }
            sb.append(level.node.type().name());
            sb.append("[").append(level.start).append("+").append(level.offset).append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    // ---- Static helpers ----
    /**
     * Resolve a position within a specific node (not necessarily the document root). The startPos
     * is the absolute position of the node's content start.
     * @param node the node to resolve within
     * @param pos the absolute position to resolve
     * @param startPos the absolute position of the node's content start
     * @return a resolved position
     */
    public static ResolvedPos resolveInNode(Node node, int pos, int startPos) {
        if (pos < startPos || pos > startPos + node.content().size()) {
            throw new IndexOutOfBoundsException(
                "Position " + pos + " out of range [" + startPos + ", "
                + (startPos + node.content().size()) + "]"
            );
        }

        List<Level> path = new ArrayList<>();
        resolveInner(node, pos, startPos, path);

        Level innermost = path.get(path.size() - 1);
        int parentOffset = pos - innermost.start;

        return new ResolvedPos(pos, Collections.unmodifiableList(path), parentOffset);
    }

}
