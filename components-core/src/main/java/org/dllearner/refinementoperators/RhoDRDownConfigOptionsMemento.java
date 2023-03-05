package org.dllearner.refinementoperators;

public class RhoDRDownConfigOptionsMemento {

    private final boolean useNegation;
    private final boolean useDisjunction;
    private final boolean useSomeOnly;
    private final boolean useObjectValueNegation;

    private final int maxCardinalityLimit;

    public RhoDRDownConfigOptionsMemento(RhoDRDown originator) {
        useNegation = originator.isUseNegation();
        useDisjunction = originator.isUseDisjunction();
        useSomeOnly = originator.isUseSomeOnly();
        useObjectValueNegation = originator.isUseObjectValueNegation();

        maxCardinalityLimit = originator.getMaxCardinalityLimit();
    }

    public void restore(RhoDRDown originator) {
        originator.setUseNegation(useNegation);
        originator.setUseDisjunction(useDisjunction);
        originator.setUseSomeOnly(useSomeOnly);
        originator.setUseObjectValueNegation(useObjectValueNegation);

        originator.setMaxCardinalityLimit(maxCardinalityLimit);
    }
}
