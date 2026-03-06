/**
 * Contentxpression.java
 *
 * Created on 1 Mar 2026 13:09:17
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
 *
 * @author ctipper
 */

/**
 * A compiled content expression that validates and matches sequences of child nodes against a
 * schema constraint like "paragraph+", "(heading | paragraph)*", or "caption? block+".
 *
 * Content expressions are essentially a small regex engine over node types. They are compiled from
 * the string content specs in the schema and used to: - Validate whether a fragment is legal
 * content for a node type - Generate default/filler content when needed - Determine what node types
 * are allowed at a given position
 *
 * The implementation uses a state machine (NFA converted to DFA) where each state represents a
 * point in the matching process and transitions are node types.
 */

public final class ContentExpression {

    private final NodeType owner;
    private final List<State> states;
    private final State startState;
    private final boolean leaf;        // no content allowed
    private final boolean inlineOnly;  // only inline content

    private ContentExpression(NodeType owner, List<State> states,
        State startState, boolean leaf, boolean inlineOnly) {
        this.owner = owner;
        this.states = states;
        this.startState = startState;
        this.leaf = leaf;
        this.inlineOnly = inlineOnly;
    }

    // ---- A state in the matching automaton ----
    private static final class State {

        final int id;
        final List<Transition> transitions;
        final boolean accepting;

        State(int id, boolean accepting) {
            this.id = id;
            this.transitions = new ArrayList<>();
            this.accepting = accepting;
        }

        void addTransition(NodeType type, State target) {
            transitions.add(new Transition(type, target));
        }

        State match(NodeType type) {
            for (Transition t : transitions) {
                if (t.type == type) {
                    return t.target;
                }
            }
            return null;
        }

        /**
         * All node types that are valid next transitions from this state.
         */
        Set<NodeType> validNext() {
            Set<NodeType> result = new LinkedHashSet<>();
            for (Transition t : transitions) {
                result.add(t.type);
            }
            return result;
        }
    }

    private record Transition(NodeType type, State target) {

    }

    // ---- Properties ----
    public boolean isLeaf() {
        return leaf;
    }

    public boolean inlineContent() {
        return inlineOnly;
    }

    // ---- Matching ----
    /**
     * Check whether the given fragment is valid content.
     */
    public boolean matches(Fragment content) {
        State state = matchFragment(content, 0, content.childCount());
        return state != null && state.accepting;
    }

    /**
     * Run the automaton over a range of children in a fragment. Returns the final state if
     * successful, null if no match.
     */
    private State matchFragment(Fragment fragment, int from, int to) {
        State state = startState;
        for (int i = from; i < to; i++) {
            Node child = fragment.child(i);
            state = state.match(child.type());
            if (state == null) {
                return null;
            }
        }
        return state;
    }

    /**
     * Create a Match cursor starting at the beginning of this expression. Used for incremental
     * matching as children are added.
     */
    public Match startMatch() {
        return new Match(startState);
    }

    // ---- Match cursor ----
    /**
     * A cursor into the content expression automaton. Allows incremental matching — feed it nodes
     * one at a time and check validity.
     */
    public static final class Match {

        private State state;

        private Match(State state) {
            this.state = state;
        }

        /**
         * Try to match a node type. Returns a new Match if successful, null if this type is not
         * allowed here.
         */
        public Match matchType(NodeType type) {
            State next = state.match(type);
            return next != null ? new Match(next) : null;
        }

        /**
         * Try to match a fragment of nodes. Returns a new Match positioned after the last matched
         * node, or null on failure.
         */
        public Match matchFragment(Fragment fragment) {
            return matchFragment(fragment, 0, fragment.childCount());
        }

        public Match matchFragment(Fragment fragment, int from, int to) {
            Match cur = this;
            for (int i = from; i < to; i++) {
                cur = cur.matchType(fragment.child(i).type());
                if (cur == null) {
                    return null;
                }
            }
            return cur;
        }

        /**
         * Whether the current state is accepting (content so far is valid as a complete sequence).
         */
        public boolean isValid() {
            return state.accepting;
        }

        /**
         * The set of node types that would be valid as the next child.
         */
        public Set<NodeType> validNext() {
            return state.validNext();
        }

        /**
         * Try to fill in default content from the current state to reach an accepting state.
         * Returns the list of filler nodes needed, or null if impossible.
         */
        public List<Node> fillDefaults() {
            return fillPath(state, new ArrayList<>(), new HashSet<>());
        }

        private List<Node> fillPath(State state, List<Node> path,
            Set<Integer> visited) {
            if (state.accepting) {
                return path;
            }
            if (visited.contains(state.id)) {
                return null; // cycle
            }
            visited.add(state.id);

            for (Transition t : state.transitions) {
                // Can only auto-generate if the type has no required attrs
                if (t.type.hasRequiredAttrs()) {
                    continue;
                }

                Node filler = t.type.createAndFill();
                if (filler == null) {
                    continue;
                }

                List<Node> newPath = new ArrayList<>(path);
                newPath.add(filler);

                List<Node> result = fillPath(t.target, newPath, visited);
                if (result != null) {
                    return result;
                }
            }

            visited.remove(state.id);
            return null;
        }
    }

    // ---- Default content generation ----
    /**
     * Fill a fragment with default content to make it valid. Appends filler nodes as needed to
     * satisfy the expression. Returns null if the constraints can't be satisfied.
     */
    public Fragment fillDefaults(Fragment content) {
        Match match = startMatch();

        // Match existing content
        for (int i = 0; i < content.childCount(); i++) {
            match = match.matchType(content.child(i).type());
            if (match == null) {
                return null;
            }
        }

        if (match.isValid()) {
            return content;
        }

        // Try to fill remaining required positions
        List<Node> fillers = match.fillDefaults();
        if (fillers == null) {
            return null;
        }

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < content.childCount(); i++) {
            nodes.add(content.child(i));
        }
        nodes.addAll(fillers);
        return Fragment.from(nodes);
    }

    /**
     * Generate the minimum valid content for this expression.
     */
    public Fragment createDefaultContent() {
        return fillDefaults(Fragment.EMPTY);
    }

    // ---- Queries ----
    /**
     * Check whether a node type is allowed anywhere in this expression.
     */
    public boolean allowsType(NodeType type) {
        for (State state : states) {
            if (state.match(type) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given node type is allowed at the given child index, given the existing
     * content.
     */
    public boolean allowsAt(Fragment content, int index, NodeType type) {
        Match match = startMatch();
        for (int i = 0; i < index && i < content.childCount(); i++) {
            match = match.matchType(content.child(i).type());
            if (match == null) {
                return false;
            }
        }
        return match.matchType(type) != null;
    }

    /**
     * What node types are allowed at the given index?
     */
    public Set<NodeType> allowedAt(Fragment content, int index) {
        Match match = startMatch();
        for (int i = 0; i < index && i < content.childCount(); i++) {
            match = match.matchType(content.child(i).type());
            if (match == null) {
                return Set.of();
            }
        }
        return match.validNext();
    }

    /**
     * Check if content can be appended with more content and still be valid.
     */
    public boolean compatible(Fragment content, ContentExpression other) {
        Match match = startMatch().matchFragment(content);
        if (match == null) {
            return false;
        }

        for (NodeType type : match.validNext()) {
            if (other.allowsType(type)) {
                return true;
            }
        }
        return false;
    }

    // ---- Factory ----
    /**
     * Empty content expression — accepts only empty content.
     */
    public static ContentExpression empty(NodeType owner) {
        State start = new State(0, true);
        return new ContentExpression(owner, List.of(start), start, true, false);
    }

    // ---- Parsing ----
    /**
     * Parse a content expression string like "paragraph+", "(heading | paragraph)* image?",
     * "block+", "text*"
     */
    public static ContentExpression parse(NodeType owner, String expr,
        Schema schema) {
        expr = expr.trim();
        if (expr.isEmpty()) {
            return empty(owner);
        }

        Parser parser = new Parser(expr, schema);
        List<SequenceItem> sequence = parser.parseSequence();

        // Build the state machine
        return compile(owner, sequence, schema);
    }

    // ---- Expression parser ----
    /**
     * Parses content expression syntax into an intermediate representation.
     *
     * Grammar: sequence = term (' ' term)* term = atom quantifier? atom = name | '(' choice ')'
     * choice = sequence ('|' sequence)* quantifier = '+' | '*' | '?' | '{' range '}' range = num |
     * num ',' | num ',' num
     */
    private static final class Parser {

        private final String expr;
        private final Schema schema;
        private int pos;

        Parser(String expr, Schema schema) {
            this.expr = expr;
            this.schema = schema;
            this.pos = 0;
        }

        List<SequenceItem> parseSequence() {
            List<SequenceItem> items = new ArrayList<>();
            while (pos < expr.length()) {
                skipWhitespace();
                if (pos >= expr.length()) {
                    break;
                }

                char c = expr.charAt(pos);
                if (c == ')' || c == '|') {
                    break;
                }

                items.add(parseTerm());
            }
            return items;
        }

        private SequenceItem parseTerm() {
            skipWhitespace();
            SequenceItem item;

            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++; // skip '('
                item = parseChoice();
                expect(')');
            } else {
                String name = parseName();
                List<NodeType> types = resolveTypes(name);
                item = new SequenceItem(types, null, 1, 1);
            }

            // Parse quantifier
            if (pos < expr.length()) {
                char c = expr.charAt(pos);
                switch (c) {
                    case '+' -> {
                        pos++;
                        item = item.withRange(1, Integer.MAX_VALUE);
                    }
                    case '*' -> {
                        pos++;
                        item = item.withRange(0, Integer.MAX_VALUE);
                    }
                    case '?' -> {
                        pos++;
                        item = item.withRange(0, 1);
                    }
                    case '{' -> {
                        pos++; // skip '{'
                        int[] range = parseRange();
                        expect('}');
                        item = item.withRange(range[0], range[1]);
                    }
                }
            }

            return item;
        }

        private SequenceItem parseChoice() {
            List<List<SequenceItem>> alternatives = new ArrayList<>();
            alternatives.add(parseSequence());

            while (pos < expr.length() && expr.charAt(pos) == '|') {
                pos++; // skip '|'
                alternatives.add(parseSequence());
            }

            if (alternatives.size() == 1) {
                // Single alternative — just a grouping, not a real choice
                return new SequenceItem(null, alternatives.get(0), 1, 1);
            }

            return new SequenceItem(null, alternatives, 1, 1);
        }

        private String parseName() {
            int start = pos;
            while (pos < expr.length() && isNameChar(expr.charAt(pos))) {
                pos++;
            }
            if (pos == start) {
                throw new SchemaError(
                    "Expected node type name at position " + pos
                    + " in content expression: " + expr
                );
            }
            return expr.substring(start, pos);
        }

        private int[] parseRange() {
            int min = parseNumber();
            int max = min;
            skipWhitespace();
            if (pos < expr.length() && expr.charAt(pos) == ',') {
                pos++;
                skipWhitespace();
                if (pos < expr.length() && Character.isDigit(expr.charAt(pos))) {
                    max = parseNumber();
                } else {
                    max = Integer.MAX_VALUE;
                }
            }
            return new int[] { min, max };
        }

        private int parseNumber() {
            int start = pos;
            while (pos < expr.length() && Character.isDigit(expr.charAt(pos))) {
                pos++;
            }
            if (pos == start) {
                throw new SchemaError(
                    "Expected number at position " + pos
                    + " in content expression: " + expr
                );
            }
            return Integer.parseInt(expr.substring(start, pos));
        }

        private void expect(char c) {
            skipWhitespace();
            if (pos >= expr.length() || expr.charAt(pos) != c) {
                throw new SchemaError(
                    "Expected '" + c + "' at position " + pos
                    + " in content expression: " + expr
                );
            }
            pos++;
        }

        private void skipWhitespace() {
            while (pos < expr.length() && expr.charAt(pos) == ' ') {
                pos++;
            }
        }

        private boolean isNameChar(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '-';
        }

        private List<NodeType> resolveTypes(String name) {
            return schema.resolveNodeTypes(name);
        }
    }

    // ---- Intermediate representation ----
    /**
     * One item in a parsed content expression sequence.
     */
    private static final class SequenceItem {

        final List<NodeType> types;       // leaf: matched node types
        final Object alternatives;        // group: nested sequences
        final int min;
        final int max;

        /**
         * Leaf item — matches specific node types.
         */
        SequenceItem(List<NodeType> types, Object alternatives, int min, int max) {
            this.types = types;
            this.alternatives = alternatives;
            this.min = min;
            this.max = max;
        }

        SequenceItem withRange(int min, int max) {
            return new SequenceItem(types, alternatives, min, max);
        }

        boolean isLeaf() {
            return types != null;
        }
    }

    // ---- Compilation to state machine ----
    /**
     * Compile parsed items into a state machine.
     */
    private static ContentExpression compile(NodeType owner,
        List<SequenceItem> sequence,
        Schema schema) {
        List<State> allStates = new ArrayList<>();
        int[] nextId = { 0 };

        State start = newState(allStates, nextId, false);
        State end = buildSequence(sequence, start, allStates, nextId);
        end = makeAccepting(end, allStates, nextId);

        // Determine if this is inline-only content
        boolean inlineOnly = detectInlineOnly(allStates);
        boolean leaf = sequence.isEmpty();

        return new ContentExpression(
            owner,
            Collections.unmodifiableList(allStates),
            start, leaf, inlineOnly
        );
    }

    /**
     * Build states for a sequence of items. Returns the final state.
     */
    private static State buildSequence(List<SequenceItem> items, State from,
        List<State> allStates, int[] nextId) {
        State current = from;
        for (SequenceItem item : items) {
            current = buildItem(item, current, allStates, nextId);
        }
        return current;
    }

    /**
     * Build states for a single item with its quantifier.
     */
    private static State buildItem(SequenceItem item, State from,
        List<State> allStates, int[] nextId) {
        if (item.min == 1 && item.max == 1) {
            // Exactly one
            return buildAtom(item, from, allStates, nextId);
        }

        if (item.min == 0 && item.max == 1) {
            // Optional (?)
            State end = buildAtom(item, from, allStates, nextId);
            // Epsilon transition: can skip
            copyTransitions(end, from);
            return end;
        }

        if (item.min == 0 && item.max == Integer.MAX_VALUE) {
            // Zero or more (*)
            State end = buildAtom(item, from, allStates, nextId);
            // Loop back
            copyTransitions(from, end);
            // Can skip entirely — from is also valid
            copyTransitions(end, from);
            return end;
        }

        if (item.min == 1 && item.max == Integer.MAX_VALUE) {
            // One or more (+)
            State end = buildAtom(item, from, allStates, nextId);
            // Loop back for repetition
            copyTransitions(from, end);
            return end;
        }

        // General {min, max}
        State current = from;

        // Required repetitions
        for (int i = 0; i < item.min; i++) {
            current = buildAtom(item, current, allStates, nextId);
        }

        // Optional repetitions
        if (item.max == Integer.MAX_VALUE) {
            // min required, then unlimited
            copyTransitions(current, current);  // self-loop
            State afterOpt = buildAtom(item, current, allStates, nextId);
            copyTransitions(current, afterOpt);
            return afterOpt;
        }

        State end = current;
        for (int i = item.min; i < item.max; i++) {
            State next = buildAtom(item, current, allStates, nextId);
            // Can skip each optional repetition
            copyTransitions(next, current);
            current = next;
            end = current;
        }

        return end;
    }

    /**
     * Build states for the core atom (ignoring quantifier).
     */
    @SuppressWarnings("unchecked")
    private static State buildAtom(SequenceItem item, State from,
        List<State> allStates, int[] nextId) {
        if (item.isLeaf()) {
            // Direct node type match
            State to = newState(allStates, nextId, false);
            for (NodeType type : item.types) {
                from.addTransition(type, to);
            }
            return to;
        }

        // Group or choice
        Object alt = item.alternatives;
        if (alt instanceof List<?> list) {
            if (!list.isEmpty() && list.get(0) instanceof List<?>) {
                // Choice: list of alternative sequences
                List<List<SequenceItem>> choices = (List<List<SequenceItem>>) alt;
                State end = newState(allStates, nextId, false);
                for (List<SequenceItem> choice : choices) {
                    State choiceEnd = buildSequence(choice, from, allStates, nextId);
                    copyTransitions(end, choiceEnd);
                    // Also copy accepting to end
                    for (Transition t : choiceEnd.transitions) {
                        if (!end.transitions.contains(t)) {
                            end.transitions.add(t);
                        }
                    }
                }
                return end;
            } else {
                // Simple group: single sequence
                List<SequenceItem> seq = (List<SequenceItem>) alt;
                return buildSequence(seq, from, allStates, nextId);
            }
        }

        throw new SchemaError("Invalid content expression structure");
    }

    // ---- State helpers ----
    private static State newState(List<State> allStates, int[] nextId,
        boolean accepting) {
        State state = new State(nextId[0]++, accepting);
        allStates.add(state);
        return state;
    }

    private static State makeAccepting(State state, List<State> allStates,
        int[] nextId) {
        if (state.accepting) {
            return state;
        }
        // Create a new accepting state and copy transitions
        State accepting = newState(allStates, nextId, true);
        copyTransitions(accepting, state);
        return state; // the original state now has paths to accepting
    }

    private static void copyTransitions(State to, State from) {
        for (Transition t : from.transitions) {
            boolean exists = false;
            for (Transition existing : to.transitions) {
                if (existing.type == t.type && existing.target == t.target) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                to.transitions.add(t);
            }
        }
    }

    private static boolean detectInlineOnly(List<State> states) {
        for (State state : states) {
            for (Transition t : state.transitions) {
                if (!t.type.isInline()) {
                    return false;
                }
            }
        }
        // If there are transitions and all are inline, it's inline content
        boolean hasTransitions = false;
        for (State state : states) {
            if (!state.transitions.isEmpty()) {
                hasTransitions = true;
                break;
            }
        }
        return hasTransitions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContentExpr(");
        sb.append(states.size()).append(" states, ");
        sb.append(leaf ? "leaf" : inlineOnly ? "inline" : "block");
        sb.append(")");
        return sb.toString();
    }

}
