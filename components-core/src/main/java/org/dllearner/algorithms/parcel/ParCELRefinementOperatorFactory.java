package org.dllearner.algorithms.parcel;

/**
 * Refinement operator factory (RhoDRDown2008)
 *
 * @author An C. Tran
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class ParCELRefinementOperatorFactory extends BasePooledObjectFactory<RefinementOperator> {

    private final AbstractReasonerComponent reasoner;
    private final ClassHierarchy classHierarchy;
    private final OWLClassExpression startclass;
    private final Map<OWLDataProperty, List<Double>> splits;
    private int maxNoOfSplits;


    private boolean useNegation = true;
    private boolean useDisjunction = true;
    private boolean useHasValue = true;


    final Logger logger = Logger.getLogger(this.getClass());

    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startclass, int maxNoOfSplits) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startclass = startclass;
        this.splits = null;
        this.maxNoOfSplits = maxNoOfSplits;
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startclass, ParCELDoubleSplitterAbstract splitter) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startclass = startclass;
        this.splits = splitter.computeSplits();

        if (logger.isDebugEnabled())
            logger.debug("Splits is calculated: " + splits);
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startclass, Map<OWLDataProperty, List<Double>> splits) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startclass = startclass;
        this.splits = splits;
    }

    @Override
    public RefinementOperator create() throws Exception {

        //clone a new class hierarchy to avoid the competition between refinement operators
        ClassHierarchy clonedClassHierarchy = this.classHierarchy.clone();

        if (logger.isDebugEnabled())
            logger.info("A new refinement operator had been created");

        //create a new RhoDRDown and return
        RhoDRDown refinementOperator = new RhoDRDown();
        refinementOperator.setReasoner(this.reasoner);
        refinementOperator.setClassHierarchy(clonedClassHierarchy);
        refinementOperator.setStartClass(this.startclass);

        refinementOperator.setUseDisjunction(this.useDisjunction);
        refinementOperator.setUseNegation(this.useNegation);
        refinementOperator.setUseDataHasValueConstructor(this.useHasValue);
        refinementOperator.setUseHasValueConstructor(this.useHasValue);
        refinementOperator.setMaxNrOfSplits(maxNoOfSplits);

        if (this.splits != null) {
            OWLDataFactory df = new OWLDataFactoryImpl();
            Map<OWLDataProperty, List<OWLLiteral>> splitsTransformed = splits.entrySet().stream().collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().stream().map(df::getOWLLiteral).collect(Collectors.toList()))
            );
            refinementOperator.setSplits(splitsTransformed);
        }

        //init the refinement operator;
        refinementOperator.init();

        return refinementOperator;
    }

	@Override
	public PooledObject<RefinementOperator> wrap(RefinementOperator refinementOperator) {
		return new DefaultPooledObject<>(refinementOperator);
	}


	public boolean isUseNegation() {
        return useNegation;
    }


    public void setUseNegation(boolean useNegation) {
        this.useNegation = useNegation;
    }


    public boolean isUseDisjunction() {
        return useDisjunction;
    }


    public void setUseDisjunction(boolean useDisjunction) {
        this.useDisjunction = useDisjunction;
    }

    public void setUseHasValue(boolean useHasValue) {
        this.useHasValue = useHasValue;
    }

    public boolean getUseHasValue() {
        return this.useHasValue;
    }

}
