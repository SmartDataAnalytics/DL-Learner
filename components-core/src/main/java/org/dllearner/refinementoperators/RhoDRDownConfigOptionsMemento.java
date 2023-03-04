package org.dllearner.refinementoperators;

import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.*;

import java.util.*;

// does not store the reasoner used by the originator
public class RhoDRDownConfigOptionsMemento {

    private final boolean applyAllFilter;
    private final boolean applyExistsFilter;

    private final boolean useAllConstructor;
    private final boolean useExistsConstructor;

    private final boolean useNegation;
    private final boolean useDisjunction;
    private final boolean useSomeOnly;
    private final boolean useObjectValueNegation;

    private final int maxCardinalityLimit;

    public RhoDRDownConfigOptionsMemento(RhoDRDown originator) {
        applyAllFilter = originator.isApplyAllFilter();
        applyExistsFilter = originator.isApplyExistsFilter();

        useAllConstructor = originator.isUseAllConstructor();
        useExistsConstructor = originator.isUseExistsConstructor();

        useNegation = originator.isUseNegation();
        useDisjunction = originator.isUseDisjunction();
        useSomeOnly = originator.isUseSomeOnly();
        useObjectValueNegation = originator.isUseObjectValueNegation();

        maxCardinalityLimit = originator.getMaxCardinalityLimit();
    }

    public void restore(RhoDRDown originator) {
        originator.setApplyAllFilter(applyAllFilter);
        originator.setApplyExistsFilter(applyExistsFilter);

        originator.setUseAllConstructor(useAllConstructor);
        originator.setUseExistsConstructor(useExistsConstructor);

        originator.setUseNegation(useNegation);
        originator.setUseDisjunction(useDisjunction);
        originator.setUseSomeOnly(useSomeOnly);
        originator.setUseObjectValueNegation(useObjectValueNegation);

        originator.setMaxCardinalityLimit(maxCardinalityLimit);
    }
}
