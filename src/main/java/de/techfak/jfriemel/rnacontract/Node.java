package de.techfak.jfriemel.rnacontract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Node<T> {
    T key;
    final List<Node<T>> children;
    Node<T> parent;
    public Node(T item) {
        key = item;
        children = new LinkedList<>();
    }

    /**
     * Adds a child to the node. Sets itself as the child's parent.
     *
     * @param child Child node.
     */
    public void addChild(Node<T> child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * Traverses the tree and returns a list with all of its nodes in preorder.
     *
     * @return List of all of the tree's nodes.
     */
    public List<Node<T>> getAllSubChildren() {
        List<Node<T>> children = new ArrayList<>();
        generateAllSubChildren(children, this);
        return children;
    }

    /**
     * Helper method that recursively traverses the tree and builds the child list.
     *
     * @param children List of children.
     * @param root     Root node for the traversal.
     */
    private void generateAllSubChildren(final List<Node<T>> children, final Node<T> root) {
        children.add(root);
        for (final Node<T> child : root.children) {
            generateAllSubChildren(children, child);
        }
    }

    /**
     * Traverses the tree with itself as the root in preorder.
     *
     * @return Preorder traversal of the tree, only node keys.
     */
    public List<T> getPreorder() {
        final List<T> traversal = new ArrayList<>();
        generatePreorder(traversal, this);
        return traversal;
    }

    /**
     * Helper method that recursively traverses the tree and builds the preorder list.
     *
     * @param traversal Preorder list.
     * @param root      Root node for the traversal.
     */
    private void generatePreorder(final List<T> traversal, final Node<T> root) {
        traversal.add(root.key);
        for (final Node<T> child : root.children) {
            generatePreorder(traversal, child);
        }
    }

    /**
     * Traverses the tree with itself as the root in preorder.
     *
     * @return List of all tree nodes in preorder.
     */
    public List<Node<T>> getPreorderNodes() {
        final List<Node<T>> traversal = new ArrayList<>();
        generatePreorderNodes(traversal, this);
        return traversal;
    }

    /**
     * Helper method that recursively traverses the tree and builds the preorder list.
     *
     * @param traversal Preorder list.
     * @param root      Root node for the traversal.
     */
    private void generatePreorderNodes(final List<Node<T>> traversal, final Node<T> root) {
        traversal.add(root);
        for (final Node<T> child : root.children) {
            generatePreorderNodes(traversal, child);
        }
    }

    /**
     * Traverses the tree with itself as the root in preorder, but only returns leaves.
     *
     * @return List of all leaves in preorder (from left to right).
     */
    public List<Node<T>> getPreorderLeaves() {
        final List<Node<T>> traversal = new ArrayList<>();
        generatePreorderLeaves(traversal, this);
        return traversal;
    }

    /**
     * Helper method that recursively traverses the tree and builds the preorder leaf list.
     *
     * @param traversal Preorder leaf list.
     * @param root      Root node for the traversal.
     */
    private void generatePreorderLeaves(final List<Node<T>> traversal, final Node<T> root) {
        if (root.children.size() == 0) {
            traversal.add(root);
        } else {
            for (final Node<T> child : root.children) {
                generatePreorderLeaves(traversal, child);
            }
        }
    }

    /**
     * Creates a balanced parentheses representation of the tree structure.
     *
     * @return Balanced parentheses representation of the tree structure.
     */
    public String getBrackets() {
        StringBuilder buffer = new StringBuilder();
        bracketRecursion(buffer, this, true);
        return buffer.toString();
    }

    /**
     * Creates an imbalanced parentheses representation of the tree structure, where all redundant brackets are left out.
     *
     * @return Imbalanced parentheses representation of the tree structure.
     */
    public String getImbalancedBrackets() {
        StringBuilder buffer = new StringBuilder();
        bracketRecursion(buffer, this, false);
        return buffer.toString();
    }

    /**
     * Counts the number of nodes in the tree.
     *
     * @return Number of nodes in the tree.
     */
    public int size() {
        return getPreorder().size();
    }

    /**
     * Recursively builds the balanced parentheses representation starting with a root node.
     *
     * @param buffer Builder for the tree representation.
     * @param root   Root node.
     */
    private void bracketRecursion(final StringBuilder buffer, final Node<T> root, final boolean balanced) {
        buffer.append('(');
        for (final Node<T> child : root.children) {
            bracketRecursion(buffer, child, balanced);
        }
        if (balanced || root.children.size() != 2) {
            buffer.append(')');
        }
    }

    /**
     * Creates a humanly-readable tree visualisation that can be printed.
     * Only makes sense when T is printable.
     * Mainly used for debugging.
     *
     * @author VasiliNovikov
     * Source: https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
     *
     * @return Tree representation.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        print(buffer, "", "");
        return buffer.toString();
    }

    /**
     * Internal function that is used for building the tree representation returned in toString().
     *
     * @author VasiliNovikov
     * Source: https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
     *
     * @param buffer         StringBuilder object that is used to build the tree representation.
     * @param prefix         Prefix of the current node.
     * @param childrenPrefix Prefix of the current node's children.
     */
    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(key);
        buffer.append('\n');
        for (Iterator<Node<T>> it = children.iterator(); it.hasNext();) {
            Node<T> next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "|-- ", childrenPrefix + "|   ");
            } else {
                next.print(buffer, childrenPrefix + "|-- ", childrenPrefix + "    ");
            }
        }
    }
}