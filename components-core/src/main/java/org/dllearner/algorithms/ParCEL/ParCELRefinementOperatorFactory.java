package org.dllearner.algorithms.ParCEL;

/**
 * Refinement operator factory (RhoDRDown2008)
 * 
 * @author An C. Tran
 */

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.ParCEL.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.util.List;
import java.util.Map;

public class ParCELRefinementOperatorFactory extends BasePooledObjectFactory<LengthLimitedRefinementOperator> {
		
	private AbstractReasonerComponent reasoner;
	private ClassHierarchy classHierarchy;
	private OWLClassExpression startclass;
	private Map<OWLDataProperty, List<OWLLiteral>> splits;
	

	private boolean useNegation = true;
	private boolean useDisjunction = true;

	Logger logger = Logger.getLogger(this.getClass());
	
	public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, OWLClassExpression startclass) {
		this.reasoner = reasoner;
		this.classHierarchy = classHierarchy;
		this.startclass = startclass;
		this.splits = null;
	}
	
	
	public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										   OWLClassExpression startclass, ParCELDoubleSplitterAbstract splitter)
	{
		this.reasoner = reasoner;
		this.classHierarchy = classHierarchy;
		this.startclass = startclass;
		this.splits = splitter.computeSplits();
		
		if (logger.isDebugEnabled())
			logger.debug("Splits is calculated: " + splits);
	}
	
	
	public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										   OWLClassExpression startclass, Map<OWLDataProperty, List<OWLLiteral>> splits)
	{
		this.reasoner = reasoner;
		this.classHierarchy = classHierarchy;
		this.startclass = startclass;
		this.splits = splits;	
	}

	@Override
	public LengthLimitedRefinementOperator create() throws Exception {
		//clone a new class heirarchy to avoid the competition between refinement operators
		ClassHierarchy clonedClassHierarchy = this.classHierarchy.clone();

		if (logger.isDebugEnabled())
			logger.info("A new refinement operator had been made");

		//create a new RhoDRDown and return
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setClassHierarchy(clonedClassHierarchy);
		op.setStartClass(startclass);

		op.setUseNegation(this.useNegation);

		if (this.splits != null)
			op.setSplits(this.splits);

		//init the refinement operator;
		op.init();

		return op;
	}

	@Override
	public PooledObject<LengthLimitedRefinementOperator> wrap(
			LengthLimitedRefinementOperator lengthLimitedRefinementOperator) {
		return null;
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
}
