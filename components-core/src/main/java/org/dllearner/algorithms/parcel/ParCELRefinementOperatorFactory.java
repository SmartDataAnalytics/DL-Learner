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
    private final OWLClassExpression startClass;
    private Map<OWLDataProperty, List<OWLLiteral>> splits;
    private int maxNoOfSplits;

    private int cardinalityLimit = 5;
    private boolean useNegation = true;
    private boolean useDisjunction = true;
    private boolean useHasValue = true;
    private boolean useHasData = false;
    private boolean useCardinalityRestrictions = true;

    private RhoDRDown operatorPrototype = null;

    final Logger logger = Logger.getLogger(this.getClass());

    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startClass, int maxNoOfSplits) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startClass = startClass;
        this.splits = null;
        this.maxNoOfSplits = maxNoOfSplits;
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startClass, ParCELDoubleSplitterAbstract splitter) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startClass = startClass;

        OWLDataFactory df = new OWLDataFactoryImpl();
        this.splits = splitter.computeSplits().entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().map(df::getOWLLiteral).collect(Collectors.toList()))
        );

        if (logger.isDebugEnabled())
            logger.debug("Splits is calculated: " + splits);
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startClass, Map<OWLDataProperty, List<Double>> splits) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startClass = startClass;

        OWLDataFactory df = new OWLDataFactoryImpl();
        this.splits = splits.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().map(df::getOWLLiteral).collect(Collectors.toList()))
        );
    }

    public ParCELRefinementOperatorFactory(RhoDRDown operatorPrototype) {
        reasoner = null;
        classHierarchy = null;
        startClass = null;
        this.operatorPrototype = operatorPrototype.clone();
    }

    public ParCELRefinementOperatorFactory(RhoDRDown operatorPrototype, Map<OWLDataProperty, List<Double>> splits) {
        reasoner = null;
        classHierarchy = null;
        startClass = null;

        OWLDataFactory df = new OWLDataFactoryImpl();
        this.operatorPrototype = operatorPrototype.clone();
        this.operatorPrototype.setSplits(
            splits.entrySet().stream().collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(df::getOWLLiteral).collect(Collectors.toList()))
            )
        );
    }

    @Override
    public RefinementOperator create() throws Exception {
        if (operatorPrototype == null) {
            //clone a new class hierarchy to avoid the competition between refinement operators
            ClassHierarchy clonedClassHierarchy = classHierarchy.clone();

            if (logger.isDebugEnabled())
                logger.info("A new refinement operator had been created");

            //create a new RhoDRDown and return
            operatorPrototype = new RhoDRDown();
            operatorPrototype.setReasoner(reasoner);
            operatorPrototype.setClassHierarchy(clonedClassHierarchy);
            operatorPrototype.setStartClass(startClass);
            operatorPrototype.setUseDisjunction(useDisjunction);
            operatorPrototype.setUseNegation(useNegation);
            operatorPrototype.setUseDataHasValueConstructor(useHasData); // TODO: MY set default back to true
            operatorPrototype.setUseHasValueConstructor(useHasValue);
            operatorPrototype.setCardinalityLimit(cardinalityLimit);
            operatorPrototype.setMaxNrOfSplits(maxNoOfSplits);
            operatorPrototype.setCardinalityLimit(cardinalityLimit);
            operatorPrototype.setUseCardinalityRestrictions(useCardinalityRestrictions);

            if (this.splits != null) {
                operatorPrototype.setSplits(splits);
            }
        }

        if (!operatorPrototype.isInitialized()) {
            operatorPrototype.init();
        }

        RhoDRDown refinementOperator = operatorPrototype.clone();

        // init the refinement operator;
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

    public void setUseHasData(boolean useData) {
        this.useHasData = useData;
    }

    public boolean getUseHasData() {
        return this.useHasData;
    }

    public int getCardinalityLimit() {
        return this.cardinalityLimit;
    }

    public void setCardinalityLimit(int cardinality) {
        this.cardinalityLimit = cardinality;
    }

    public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
        this.useCardinalityRestrictions = useCardinalityRestrictions;
    }

    public boolean getUseCardinalityRestrictions() {
        return this.useCardinalityRestrictions;
    }
}
