package org.dllearner.algorithms.ParCEL;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.dllearner.algorithms.ParCEL.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.util.List;
import java.util.Map;

/**
 * Refinement operator pool
 *
 * @author An C. Tran
 */
public class ParCELRefinementOperatorPool extends GenericObjectPool<LengthLimitedRefinementOperator> {

	/**
	 * Create refinement operator pool given max number of idle object without splitter
	 *
	 * @param reasoner
	 * @param classHierarchy
	 * @param startclass
	 * @param maxIdle
	 */
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										OWLClassExpression startclass, int maxIdle) {
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass));
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
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										OWLClassExpression startclass,
										Map<OWLDataProperty, List<OWLLiteral>> splits, int maxIdle) {
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits));
		setMaxIdle(maxIdle);
	}


	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										OWLClassExpression startclass,
										ParCELDoubleSplitterAbstract splitter, int maxIdle) {
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter));
		setMaxIdle(maxIdle);
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
										OWLClassExpression startclass, int maxIdle, int maxIdleCapacity) {
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass));
		setMaxIdle(maxIdle);
		setMinIdle(maxIdleCapacity);
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
		setMinIdle(maxIdleCapacity);
	}


	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
										OWLClassExpression startclass, Map<OWLDataProperty, List<OWLLiteral>> splits,
										int maxIdle, int maxIdleCapacity) {
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits));
		setMaxIdle(maxIdle);
		setMinIdle(maxIdleCapacity);
	}


	public ParCELRefinementOperatorFactory getFactory() {
		return (ParCELRefinementOperatorFactory) super.getFactory();
	}

}
