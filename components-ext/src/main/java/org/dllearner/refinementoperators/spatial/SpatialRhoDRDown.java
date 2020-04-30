package org.dllearner.refinementoperators.spatial;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.spatial.SpatialReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Set;

public class SpatialRhoDRDown extends RhoDRDown {
    private SpatialReasoner reasoner;
    private OWLClassExpressionLengthMetric lengthMetric =
            OWLClassExpressionLengthMetric.getDefaultMetric();

    // <getter/setter>
    public void setReasoner(SpatialReasoner reasoner) {
        // The spatial reasoner needs to be set here, since RhoDRDown won't be
        // able to get the domains/ranges of the virtual spatial properties
        // otherwise.
        super.setReasoner((AbstractReasonerComponent) reasoner);

        this.reasoner = reasoner;
    }
    // </getter/setter>

    // <interface methods>
    @Override
    public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
        Set<OWLClassExpression> refinements = super.refine(description, maxLength);

        return refinements;
    }

    @Override
    public void init() throws ComponentInitException {
        super.init();
    }
    // </interface methods>
}
