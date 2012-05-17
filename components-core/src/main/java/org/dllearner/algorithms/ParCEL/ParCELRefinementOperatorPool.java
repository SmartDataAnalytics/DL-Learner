package org.dllearner.algorithms.ParCEL;

import java.util.List;
import java.util.Map;

import org.apache.commons.pool.impl.StackObjectPool;
import org.dllearner.algorithms.ParCEL.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.refinementoperators.RefinementOperator;

/**
 * Refinement operator pool
 * 
 * @author An C. Tran
 *
 */
public class ParCELRefinementOperatorPool extends StackObjectPool<RefinementOperator> {

	/**
	 * Create refinement operator pool given max number of idle object without splitter 
	 * 
	 * @param reasoner
	 * @param classHierarchy
	 * @param startclass
	 * @param maxIdle
	 */
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, int maxIdle) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass), maxIdle);		
	}
	
	/**
	 * Create refinement operator pool given max number of idle object and splitter
	 * 
	 * @param reasoner
	 * @param classHierarchy
	 * @param startclass
	 * @param splitter Splitter used to calculate the splits
	 * @param maxIdle
	 */	
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, Description startclass, 
			Map<DatatypeProperty, List<Double>> splits, int maxIdle) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits), maxIdle);		
	}
	
	
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, Description startclass, 
			ParCELDoubleSplitterAbstract splitter, int maxIdle) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter), maxIdle);		
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
			Description startclass, int maxIdle, int maxIdleCapacity) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass), maxIdle, maxIdleCapacity);		
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
			Description startclass, ParCELDoubleSplitterAbstract splitter, int maxIdle, int maxIdleCapacity) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter), maxIdle, maxIdleCapacity);		
	}
	
	
	public ParCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, Map<DatatypeProperty, List<Double>> splits, int maxIdle, int maxIdleCapacity) 
	{
		super(new ParCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits), maxIdle, maxIdleCapacity);		
	}
	
	
	public ParCELRefinementOperatorFactory getFactory() {
		return (ParCELRefinementOperatorFactory)super.getFactory();
	}
	
}
