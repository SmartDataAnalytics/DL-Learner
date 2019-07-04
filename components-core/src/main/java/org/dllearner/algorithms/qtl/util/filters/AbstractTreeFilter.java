package org.dllearner.algorithms.qtl.util.filters;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 */
public abstract class AbstractTreeFilter<T extends GenericTree> implements TreeFilter<T> {

    protected Set<Node> nodes2Keep = new HashSet<>();

    /**
     * @param nodes2Keep nodes that have to be kept available in any case during filtering
     */
    public void setNodes2Keep(Collection<Node> nodes2Keep) {
        this.nodes2Keep = Sets.newHashSet(nodes2Keep);
    }

    /**
     * @return  nodes that have to be kept available in any case during filtering
     */
    public Set<Node> getNodes2Keep() {
        return nodes2Keep;
    }
}
