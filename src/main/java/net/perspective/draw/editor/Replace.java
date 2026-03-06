/**
 * Replace.java
 *
 * Created on 1 Mar 2026 13:00:11
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
import java.util.List;

/**
 * Handles replacing a range in a document with a slice of content. This is the core primitive for
 * document mutation — transforms, copy-paste, drag-drop, and typing all ultimately go through
 * replace.
 *
 * The algorithm works by resolving the from and to positions, finding their shared ancestor, then
 * stitching together: 1. Content before the range 2. The slice content (accounting for open depths)
 * 3. Content after the range
 *
 * @author ctipper
 */

public final class Replace {

    private Replace() {
    } // static utility

    /**
     * Replace the content between from and to in the given node with the provided slice.
     *
     * @param doc the document node
     * @param from the start position
     * @param to the end position
     * @param slice the slice to insert
     * @return the document with the replaced content
     */
    public static Node replace(Node doc, int from, int to, Slice slice) {
        if (slice == Slice.EMPTY && from == to) {
            return doc;
        }

        ResolvedPos $from = doc.resolve(from);
        ResolvedPos $to = from == to ? $from : doc.resolve(to);

        return replaceRange($from, $to, slice);
    }

    /**
     * Internal: Replace the range at the resolved position level.
     */
    private static Node replaceRange(ResolvedPos $from, ResolvedPos $to,
        Slice slice) {
        if (slice.isEmpty()) {
            return close($from, $to);
        }

        int openStart = slice.openStart();
        int openEnd = slice.openEnd();

        // Find the shared depth between from and to
        int sharedDepth = $from.sharedDepth($to.pos());

        // Walk down the open start side of the slice to find where
        // the slice content actually begins
        Fragment sliceContent = slice.content();
        Node sliceStart = null;
        for (int d = 0; d < openStart; d++) {
            sliceStart = sliceContent.firstChild();
            sliceContent = sliceStart.content();
        }

        // Walk down the open end side
        Fragment sliceEndContent = slice.content();
        Node sliceEnd = null;
        for (int d = 0; d < openEnd; d++) {
            sliceEnd = sliceEndContent.lastChild();
            sliceEndContent = sliceEnd.content();
        }

        Node result = replaceOuter($from, $to, slice, sharedDepth);
        return rebuildAncestors($from, result, sharedDepth);
    }

    /**
     * Replace at the level of the shared ancestor.
     */
    private static Node replaceOuter(ResolvedPos $from, ResolvedPos $to,
        Slice slice, int depth) {
        int index = $from.index(depth);
        int endIndex = $to.indexAfter(depth);
        Node parent = $from.node(depth);

        // Build the new content for this level
        Fragment before = parent.content().cut(0, $from.start(depth) == $from.pos()
            ? $from.pos() - $from.start(depth) : fragmentOffset($from, depth));
        Fragment after = parent.content().cut(fragmentOffsetEnd($to, depth));

        Fragment middle = placeSlice($from, $to, slice, depth);

        Fragment newContent = before.append(middle).append(after);

        return parent.copy(newContent);
    }

    /**
     * Place slice content at the appropriate depth, handling open sides.
     */
    private static Fragment placeSlice(ResolvedPos $from, ResolvedPos $to,
        Slice slice, int depth) {
        int openStart = slice.openStart();
        int openEnd = slice.openEnd();

        if (openStart == 0 && openEnd == 0) {
            return slice.content();
        }

        // Descend into open start
        Fragment content = slice.content();
        if (openStart > 0) {
            Node first = content.firstChild();
            if (openStart == 1) {
                content = joinFragments(first.content(), content.cut(first.nodeSize()));
            } else {
                Fragment inner = placeSliceStart(first, openStart - 1, $from, depth + 1);
                List<Node> nodes = new ArrayList<>();
                if (inner.childCount() > 0) {
                    addNodes(inner, nodes);
                }
                for (int i = 1; i < content.childCount(); i++) {
                    nodes.add(content.child(i));
                }
                content = Fragment.from(nodes);
            }
        }

        if (openEnd > 0) {
            content = closeEnd(content, openEnd, $to, depth);
        }

        return content;
    }

    /**
     * Handle the open start side of a slice by fitting it into the existing structure at the from
     * position.
     */
    private static Fragment placeSliceStart(Node sliceNode, int openDepth,
        ResolvedPos $from, int depth) {
        if (openDepth == 0) {
            return sliceNode.content();
        }

        Node first = sliceNode.content().firstChild();
        Fragment rest = sliceNode.content().cut(first.nodeSize());
        Fragment inner = placeSliceStart(first, openDepth - 1, $from, depth + 1);

        List<Node> nodes = new ArrayList<>();
        addNodes(inner, nodes);
        addNodes(rest, nodes);
        return Fragment.from(nodes);
    }

    /**
     * Handle the open end side of a slice.
     */
    private static Fragment closeEnd(Fragment content, int openEnd,
        ResolvedPos $to, int depth) {
        if (openEnd == 0 || content.childCount() == 0) {
            return content;
        }

        Node last = content.lastChild();
        if (openEnd == 1) {
            Fragment before = content.cut(0, content.size() - last.nodeSize());
            return joinFragments(before, last.content());
        }

        Fragment inner = closeEnd(last.content(), openEnd - 1, $to, depth + 1);
        Node newLast = last.copy(inner);
        return content.replaceChild(content.childCount() - 1, newLast);
    }

    /**
     * Close a range with no inserted content — just stitch together the content before $from with
     * the content after $to.
     */
    private static Node close(ResolvedPos $from, ResolvedPos $to) {
        int sharedDepth = $from.sharedDepth($to.pos());
        Node result = closeRange($from, $to, sharedDepth);
        return rebuildAncestors($from, result, sharedDepth);
    }

    /**
     * Internal: Close a range at a specific depth.
     */
    private static Node closeRange(ResolvedPos $from, ResolvedPos $to, int depth) {
        Node parent = $from.node(depth);

        if (depth == $from.depth() && depth == $to.depth()) {
            // Same parent — just cut out the range
            Fragment before = parent.content().cut(0, $from.parentOffset());
            Fragment after = parent.content().cut($to.parentOffset());
            return parent.copy(before.append(after));
        }

        // Need to close nodes between $from and $to at this depth
        int fromIndex = $from.index(depth);
        int fromOffset = $from.pos() - $from.start(depth);
        int toIndex = $to.index(depth);
        int toOffset = $to.pos() - $to.start(depth);

        Fragment before = parent.content().cut(0, fromOffset);
        Fragment after = parent.content().cut(toOffset);

        // If from and to are in the same child, recurse into it
        if (fromIndex == toIndex && !parent.content().child(fromIndex).isLeaf()) {
            Node child = parent.content().child(fromIndex);
            Node closedChild = closeRange($from, $to, depth + 1);
            Fragment middle = Fragment.from(closedChild);
            return parent.copy(before.append(middle).append(after));
        }

        // Different children — close the from-child's end and to-child's start
        Fragment middle = Fragment.EMPTY;

        // Close the node at fromIndex (keep content before $from)
        if (fromIndex < parent.childCount()) {
            Node fromChild = parent.content().child(fromIndex);
            if (!fromChild.isLeaf() && $from.depth() > depth) {
                Node closedFrom = closeNodeEnd($from, depth + 1);
                middle = middle.append(Fragment.from(closedFrom));
            }
        }

        // Close the node at toIndex (keep content after $to)
        if (toIndex < parent.childCount() && toIndex != fromIndex) {
            Node toChild = parent.content().child(toIndex);
            if (!toChild.isLeaf() && $to.depth() > depth) {
                Node closedTo = closeNodeStart($to, depth + 1);
                middle = middle.append(Fragment.from(closedTo));
            }
        }

        return parent.copy(before.append(middle).append(after));
    }

    /**
     * Close a node by keeping only the content before the position.
     */
    private static Node closeNodeEnd(ResolvedPos $pos, int depth) {
        Node node = $pos.node(depth);
        int offset = $pos.pos() - $pos.start(depth);
        Fragment content = node.content().cut(0, offset);

        if ($pos.depth() > depth) {
            Node closedChild = closeNodeEnd($pos, depth + 1);
            content = content.replaceChild(content.childCount() - 1, closedChild);
        }

        return node.copy(content);
    }

    /**
     * Close a node by keeping only the content after the position.
     */
    private static Node closeNodeStart(ResolvedPos $pos, int depth) {
        Node node = $pos.node(depth);
        int offset = $pos.pos() - $pos.start(depth);
        Fragment content = node.content().cut(offset);

        if ($pos.depth() > depth) {
            Node closedChild = closeNodeStart($pos, depth + 1);
            content = content.replaceChild(0, closedChild);
        }

        return node.copy(content);
    }

    // ---- Helpers ----
    /**
     * Rebuild ancestor nodes from the shared depth back up to the document root.
     * The result node replaces the child at each ancestor level.
     */
    private static Node rebuildAncestors(ResolvedPos $pos, Node result, int sharedDepth) {
        for (int d = sharedDepth - 1; d >= 0; d--) {
            Node ancestor = $pos.node(d);
            int childIndex = $pos.index(d);
            result = ancestor.copy(ancestor.content().replaceChild(childIndex, result));
        }
        return result;
    }

    /**
     * Calculate the offset of a position relative to the start at a given depth.
     */
    private static int fragmentOffset(ResolvedPos $pos, int depth) {
        return $pos.pos() - $pos.start(depth);
    }

    /**
     * Calculate the offset of a position relative to the end at a given depth.
     */
    private static int fragmentOffsetEnd(ResolvedPos $pos, int depth) {
        return $pos.pos() - $pos.start(depth);
    }

    /**
     * Join two fragments, returning empty if both are empty.
     */
    private static Fragment joinFragments(Fragment a, Fragment b) {
        if (a.childCount() == 0) {
            return b;
        }
        if (b.childCount() == 0) {
            return a;
        }
        return a.append(b);
    }

    /**
     * Add all nodes from a fragment to a list.
     */
    private static void addNodes(Fragment fragment, List<Node> into) {
        for (int i = 0; i < fragment.childCount(); i++) {
            into.add(fragment.child(i));
        }
    }

    // ---- Public utilities ----
    /**
     * Replace a range with a slice, delegating to the core replace algorithm.
     *
     * @param doc the document node
     * @param from the start position
     * @param to the end position
     * @param slice the slice to insert
     * @return the document with the replaced content
     */
    public static Node replaceRange(Node doc, int from, int to, Slice slice) {
        return replace(doc, from, to, slice);
    }

    /**
     * Delete a range from the document.
     *
     * @param doc the document node
     * @param from the start position
     * @param to the end position
     * @return the document with the range deleted
     */
    public static Node deleteRange(Node doc, int from, int to) {
        return replace(doc, from, to, Slice.EMPTY);
    }

    /**
     * Insert a fragment at the given position.
     *
     * @param doc the document node
     * @param pos the insertion position
     * @param content the fragment to insert
     * @return the document with the inserted content
     */
    public static Node insertAt(Node doc, int pos, Fragment content) {
        return replace(doc, pos, pos, new Slice(content, 0, 0));
    }

    /**
     * Insert a single node at the given position.
     *
     * @param doc the document node
     * @param pos the insertion position
     * @param node the node to insert
     * @return the document with the inserted node
     */
    public static Node insertAt(Node doc, int pos, Node node) {
        return insertAt(doc, pos, Fragment.from(node));
    }

    // ---- Validation ----
    /**
     * Check whether replacing the range from-to with the given slice would produce valid content
     * according to the schema.
     *
     * @param doc the document node
     * @param from the start position
     * @param to the end position
     * @param slice the slice to insert
     * @return true if the replacement would be valid
     */
    public static boolean canReplace(Node doc, int from, int to, Slice slice) {
        try {
            Node result = replace(doc, from, to, slice);
            result.check();
            return true;
        } catch (SchemaError e) {
            return false;
        }
    }

    /**
     * Check whether a position is a valid insertion point for the given node type.
     *
     * @param doc the document node
     * @param pos the position to check
     * @param type the node type to check
     * @return true if insertion would be valid
     */
    public static boolean canInsert(Node doc, int pos, NodeType type) {
        ResolvedPos $pos = doc.resolve(pos);
        Node parent = $pos.parent();

        if (parent.type().contentExpr() == null) {
            return false;
        }

        // Check if inserting a node of this type at this index is valid
        Fragment before = parent.content().cut(0, $pos.parentOffset());
        Fragment after = parent.content().cut($pos.parentOffset());
        Fragment withNode = before
            .append(Fragment.from(type.createAndFill()))
            .append(after);

        return parent.type().checkContent(withNode);
    }

}
