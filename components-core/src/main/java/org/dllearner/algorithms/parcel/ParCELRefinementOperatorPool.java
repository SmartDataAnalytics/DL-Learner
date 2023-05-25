package org.dllearner.algorithms.parcel;

import java.util.List;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.dllearner.algorithms.parcel.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;

/**
 * Refinement operator pool
 *
 * @author An C. Tran
 */
public class ParCELRefinementOperatorPool extends GenericObjectPool<RefinementOperator> {

    /**
     * Create refinement operator pool given max number of idle object without splitter
     *
     * @param reasoner
     * @param classHierarchy
     * @param startclass
     * @param maxIdle
     */
    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                        OWLClassExpression startclass, int maxIdle, int maxNoOfSplits) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, maxNoOfSplits));
        setMaxIdle(maxIdle);
    }

    /**
     * Create refinement operator pool given max number of idle object and splitter
     *
     * @param reasoner
     * @param classHierarchy
     * @param startclass
     * @param splits       Splitter used to calculate the splits
     * @param maxIdle
     */
    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, OWLClassExpression startclass,
                                        Map<OWLDataProperty, List<Double>> splits, int maxIdle) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits));
        setMaxIdle(maxIdle);
    }


    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, OWLClassExpression startclass,
                                        ParCELDoubleSplitterAbstract splitter, int maxIdle) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter));
        setMaxIdle(maxIdle);
    }


    public ParCELRefinementOperatorPool(ParCELRefinementOperatorFactory parcelRefinementFactory) {
        super(parcelRefinementFactory);
    }

    /**
     * Create refinement operator pool given max number of idle object, max capacity without splitter
     *
     * @param reasoner
     * @param classHierarchy
     * @param startclass
     * @param maxIdle
     * @param maxIdleCapacity
     */
    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                        OWLClassExpression startclass, int maxIdle, int maxIdleCapacity,
                                        int maxNoOfSplits) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, maxNoOfSplits));
        setMaxIdle(maxIdle);
        setMaxTotal(maxIdleCapacity);
    }


    /**
     * Create refinement operator pool given max number of idle object, max capacity and splitter
     *
     * @param reasoner
     * @param classHierarchy
     * @param startclass
     * @param splitter
     * @param maxIdle
     * @param maxIdleCapacity
     */
    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                        OWLClassExpression startclass, ParCELDoubleSplitterAbstract splitter,
                                        int maxIdle, int maxIdleCapacity) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter));
        setMaxIdle(maxIdle);
        setMaxTotal(maxIdleCapacity);
    }


    public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                        OWLClassExpression startclass, Map<OWLDataProperty, List<Double>> splits,
                                        int maxIdle, int maxIdleCapacity) {
        super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits));
        setMaxIdle(maxIdle);
        setMaxTotal(maxIdleCapacity);
    }


    public ParCELRefinementOperatorFactory getFactory() {
        return (ParCELRefinementOperatorFactory) super.getFactory();
    }

}
