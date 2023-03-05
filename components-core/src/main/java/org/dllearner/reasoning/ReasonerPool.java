package org.dllearner.reasoning;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.dllearner.core.AbstractReasonerComponent;

public class ReasonerPool extends GenericObjectPool<AbstractReasonerComponent> {

    public ReasonerPool(AbstractReasonerComponent reasonerPrototype, int maxIdle) {
        super(new ReasonerFactory(reasonerPrototype));
        setMaxIdle(maxIdle);
    }
}
