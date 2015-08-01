/**
 * 
 */
package org.dllearner.utilities.owl;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.util.MaximumModalDepthFinder;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * A utility class for OWL class expressions.
 * 
 * @author Lorenz Buehmann
 */
public class OWLClassExpressionUtils {
	
	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl(false, false);
	private static OWLObjectDuplicator duplicator = new OWLObjectDuplicator(dataFactory);
	private static final OWLClassExpressionLengthCalculator LENGTH_CALCULATOR= new OWLClassExpressionLengthCalculator();
	private static final MaximumModalDepthFinder DEPTH_FINDER = new MaximumModalDepthFinder();
	private static final OWLClassExpressionChildrenCollector CHILDREN_COLLECTOR = new OWLClassExpressionChildrenCollector();
	
	/**
	 * Returns the length of a given class expression. 
	 * @param ce the class expression
	 * @return the length of the class expression
	 */
	public static int getLength(OWLClassExpression ce){
		OWLClassExpressionLengthCalculator calculator = new OWLClassExpressionLengthCalculator();
		return calculator.getLength(ce);
	}
	
	/**
	 * Returns the depth of a class expression. 
	 * @param ce the class expression
	 * @return the depth of the class expression
	 */
	public static synchronized int getDepth(OWLClassExpression ce){
		int depth = ce.accept(DEPTH_FINDER);
		return depth;
	}
	
	/**
	 * Returns the arity of a class expression. 
	 * @param ce the class expression
	 * @return the arity of the class expression
	 */
	public static synchronized int getArity(OWLClassExpression ce){
		return getChildren(ce).size();
	}
	
	/**
	 * Returns all direct child expressions of a class expression.
	 * @param ce the class expression
	 * @return the direct child expressions
	 */
	public static Set<OWLClassExpression> getChildren(OWLClassExpression ce){
		return ce.accept(CHILDREN_COLLECTOR);
	}
	
	/**
	 * Returns a clone of the given class expression.
	 * @param ce the class expression
	 * @return a class expression clone
	 */
	public static OWLClassExpression clone(OWLClassExpression ce) {
		return duplicator.duplicateObject(ce);
	}
	
	/**
	 * Determine whether a named class occurs on the outermost level, i.e. property depth 0
	 * (it can still be at higher depth, e.g. if intersections are nested in unions)
	 * @param description
	 * @param cls
	 * @return
	 */
	public static boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
		return description.containsConjunct(cls);
	}
}
