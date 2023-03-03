package org.dllearner.reasoning;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.dllearner.core.AbstractReasonerComponent;

public class ReasonerFactory extends BasePooledObjectFactory<AbstractReasonerComponent> {

    private final AbstractReasonerComponent reasonerPrototype;

    public ReasonerFactory(AbstractReasonerComponent reasonerPrototype) {
        this.reasonerPrototype = reasonerPrototype.clone();
    }

    @Override
    public AbstractReasonerComponent create() throws Exception {
        if (!reasonerPrototype.isInitialized()) {
            reasonerPrototype.init();
        }

        AbstractReasonerComponent reasoner = reasonerPrototype.clone();
        reasoner.init();

        return reasoner;
    }

    @Override
    public PooledObject<AbstractReasonerComponent> wrap(AbstractReasonerComponent reasonerComponent) {
        return new DefaultPooledObject<>(reasonerComponent);
    }
}
