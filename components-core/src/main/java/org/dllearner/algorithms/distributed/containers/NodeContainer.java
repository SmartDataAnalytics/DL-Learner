package org.dllearner.algorithms.distributed.containers;

import org.dllearner.algorithms.celoe.OENode;

public class NodeContainer implements MessageContainer {
    private static final long serialVersionUID = 4537193881385473071L;
    private final OENode node;

    public NodeContainer(OENode node) {
        this.node = node;
    }

    public OENode getNode() {
        return this.node;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
