package org.dllearner.algorithms.PADCEL;

import java.util.List;
import java.util.Map;

import org.apache.commons.pool.impl.StackObjectPool;
import org.dllearner.algorithms.PADCEL.split.PADCELDoubleSplitterAbstract;
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
public class PADCELRefinementOperatorPool extends StackObjectPool<RefinementOperator> {

	/**
	 * Create refinement operator pool given max number of idle object without splitter 
	 * 
	 * @param reasoner
	 * @param classHierarchy
	 * @param startclass
	 * @param maxIdle
	 */
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, int maxIdle) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass), maxIdle);		
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
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, Description startclass, 
			Map<DatatypeProperty, List<Double>> splits, int maxIdle) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits), maxIdle);		
	}
	
	
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, Description startclass, 
			PADCELDoubleSplitterAbstract splitter, int maxIdle) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter), maxIdle);		
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
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, int maxIdle, int maxIdleCapacity) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass), maxIdle, maxIdleCapacity);		
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
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, PADCELDoubleSplitterAbstract splitter, int maxIdle, int maxIdleCapacity) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splitter), maxIdle, maxIdleCapacity);		
	}
	
	
	public PADCELRefinementOperatorPool(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, 
			Description startclass, Map<DatatypeProperty, List<Double>> splits, int maxIdle, int maxIdleCapacity) 
	{
		super(new PADCELRefinementOperatorFactory(reasoner, classHierarchy, startclass, splits), maxIdle, maxIdleCapacity);		
	}
	
	
	public PADCELRefinementOperatorFactory getFactory() {
		return (PADCELRefinementOperatorFactory)super.getFactory();
	}
	
}
