package org.dllearner.utilities.graph;

import java.util.Objects;

import org.jgrapht.graph.DefaultEdge;

/**
 * A labeled edge in the graph.
 *
 * @param <T> the type of the label
 */
public class LabeledEdge<T> extends DefaultEdge {
    private T label;

    /**
     * Constructs a labeled edge
     *
     * @param label the label of the new edge.
     */
    public LabeledEdge(T label) {
        Objects.requireNonNull(label, "label must not be null");
        this.label = label;
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public T getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "(" + getSource() + " -> " + getTarget() + " : " + label + ")";
    }
}