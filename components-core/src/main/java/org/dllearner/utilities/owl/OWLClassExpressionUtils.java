/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.MaximumModalDepthFinder;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;

/**
 * A utility class for OWL class expressions.
 * 
 * @author Lorenz Buehmann
 */
public class OWLClassExpressionUtils {
	
	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl();
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

	public static int getLength(OWLClassExpression ce, OWLClassExpressionLengthMetric metric) {
		OWLClassExpressionLengthCalculator calculator = new OWLClassExpressionLengthCalculator(metric);
		return calculator.getLength(ce);
	}

	/**
	 * Returns the depth of a class expression. 
	 * @param ce the class expression
	 * @return the depth of the class expression
	 */
	public static synchronized int getDepth(OWLClassExpression ce){
		return ce.accept(DEPTH_FINDER);
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
	 * Determine whether a named class occurs on the outermost level of a class expression, i.e. property depth 0
	 * (it can still be at higher depth, e.g. if intersections are nested in unions)
	 * @param description the class expression
	 * @param cls the named class
	 * @return whether the named class occurs on the outermost level of the class expression
	 */
	public static boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
		return description.containsConjunct(cls);
	}

	/**
	 * Replace the filler of an object property restriction.
	 *
	 * @param restriction the object property restriction
	 * @param newFiller   the filler to replace the old one
	 * @return the new object property restriction with a replaced filler
	 */
	public static OWLQuantifiedObjectRestriction replaceFiller(OWLQuantifiedObjectRestriction restriction,
															   OWLClassExpression newFiller) {
		OWLQuantifiedObjectRestriction newRestriction = null;
		OWLObjectPropertyExpression property = restriction.getProperty();
		if (restriction instanceof OWLObjectSomeValuesFrom) {
			newRestriction = dataFactory.getOWLObjectSomeValuesFrom(property, newFiller);
		} else if (restriction instanceof OWLObjectAllValuesFrom) {
			newRestriction = dataFactory.getOWLObjectAllValuesFrom(property, newFiller);
		} else if (restriction instanceof OWLObjectCardinalityRestriction) {
			int cardinality = ((OWLObjectCardinalityRestriction) restriction).getCardinality();

			if (restriction instanceof OWLObjectMinCardinality) {
				newRestriction = dataFactory.getOWLObjectMinCardinality(cardinality, property, newFiller);
			} else if (restriction instanceof OWLObjectMaxCardinality) {
				newRestriction = dataFactory.getOWLObjectMaxCardinality(cardinality, property, newFiller);
			} else if (restriction instanceof OWLObjectExactCardinality) {
				newRestriction = dataFactory.getOWLObjectExactCardinality(cardinality, property, newFiller);
			}
		} else {
			throw new IllegalArgumentException("unsupported restriction type for filler replacement: "
					+ restriction.getClassExpressionType().getName());
		}
		return newRestriction;
	}

}
