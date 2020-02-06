package org.dllearner.algorithms.parcelex;

import java.util.*;

import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;


/**
 * This class contains some utility functions for ParCELEx algorithm such as group the partial definitions, 
 * 	calculate intersection between a description and a set of counter partial definitions, etc.  
 *  
 * @author An C. Tran
 *
 */
public class ParCELExUtilities {

	/**
	 * Group the counter partial definitions in a partial definition using ConceptComparator
	 * so that the "relevant" counter partial definitions will ordered near each other for readability
	 *
	 * @param definition A definition (description)
	 * @return Description with the member counter partial definitions are grouped
	 */
	public static org.semanticweb.owlapi.model.OWLClassExpression groupDefinition(OWLClassExpression definition) {

		// TODO check if we need to keep track of proper lists, i.e. D and D and D
		List<OWLClassExpression> children = new ArrayList<>(OWLClassExpressionUtils.getChildren(definition));

		List<OWLClassExpression> counterPartialDefinitions = new LinkedList<>();
		List<OWLClassExpression> partialDefinitions = new LinkedList<>();
		for (OWLClassExpression def : children) {
			if (def.toString().toLowerCase().contains("not "))
				counterPartialDefinitions.add(def);
			else
				partialDefinitions.add(def);
		}

		Collections.sort(counterPartialDefinitions);
		Collections.sort(partialDefinitions);

		partialDefinitions.addAll(counterPartialDefinitions);

		OWLClassExpression result = new OWLObjectIntersectionOfImplExt(partialDefinitions);

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
		LinkedList<OWLClassExpression> descriptionList = new LinkedList<>();
		
		descriptionList.add(description);
		for (ParCELExtraNode node : counterDefinitions) {
			descriptionList.add(node.getDescription());
			if (setUsed)
				node.setType(ParCELExNodeTypes.COUNTER_PARTIAL_DEFINITION_USED);
		}
		
		return new OWLObjectIntersectionOfImplExt(descriptionList);
	}

}
