package org.dllearner.algorithms.qtl.util.filters;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.core.AbstractReasonerComponent;

/**
 * Tree filter that removes redundant rdf:type edges according to subsumption by using the
 * given reasoner.
 *
 * @author Lorenz Buehmann
 */
public class MostSpecificTypesFilter extends  AbstractTreeFilter<RDFResourceTree>{

    private final AbstractReasonerComponent reasoner;

    public MostSpecificTypesFilter(AbstractReasonerComponent reasoner) {
        this.reasoner = reasoner;
    }


    @Override
    public RDFResourceTree apply(RDFResourceTree tree) {
        RDFResourceTree copy = new RDFResourceTree(tree);

        QueryTreeUtils.keepMostSpecificTypes(copy, reasoner);

        return copy;
    }


}
