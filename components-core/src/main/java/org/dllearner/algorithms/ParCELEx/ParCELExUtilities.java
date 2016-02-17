package org.dllearner.algorithms.ParCELEx;

import org.dllearner.algorithms.ParCEL.ParCELExtraNode;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;
import java.util.TreeSet;


/**
 * This class contains some utility functions for ParCELEx algorithm such as group the partial definitions, 
 * 	calculate intersection between a description and a set of counter partial definitions, etc.  
 *  
 * @author An C. Tran
 *
 */
public class ParCELExUtilities {

	private static OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	/**
	 * Group the counter partial definitions in a partial definition using ConceptComparator
	 * so that the "relevant" counter partial definitions will ordered near each other for readability
	 * 
	 * @param definition A definition (description)
	 * 
	 * @return Description with the member counter partial definitions are grouped 
	 */
	public static OWLClassExpression groupDefinition(OWLClassExpression definition) {
		
		Set<OWLClassExpression> children = OWLClassExpressionUtils.getChildren(definition);
		
		Set<OWLClassExpression> counterPartialDefinitions = new TreeSet<>();
		Set<OWLClassExpression> partialDefinitions = new TreeSet<>();
		for (OWLClassExpression def : children) {
			if (def.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF)
				counterPartialDefinitions.add(def);
			else
				partialDefinitions.add(def);
		}
		
		for (OWLClassExpression cpd : counterPartialDefinitions)
			partialDefinitions.add(cpd);
		
		OWLClassExpression result = dataFactory.getOWLObjectIntersectionOf(partialDefinitions);
		
		return result;
	}
	
	
	/**
	 * Create an Intersection object given a description and a set of descriptions
	 *  
	 * @param description A description
	 * @param counterDefinitions Set of descriptions
	 * 
	 * @return An Intersection object of the given description and the set of descriptions
	 */
	public static OWLClassExpression createIntersection(OWLClassExpression description, Set<ParCELExtraNode> counterDefinitions, boolean setUsed) {
		Set<OWLClassExpression> descriptions = new TreeSet<>();
		
		descriptions.add(description);
		for (ParCELExtraNode node : counterDefinitions) {
			descriptions.add(node.getDescription());
			if (setUsed)
				node.setType(ParCELExNodeTypes.COUNTER_PARTIAL_DEFINITION_USED);
		}
		
		return dataFactory.getOWLObjectIntersectionOf(descriptions);
	}

}
