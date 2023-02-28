package org.dllearner.algorithms.parcelex;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELNode;
import org.semanticweb.owlapi.model.OWLIndividual;


/**
 * 
 * This class provide utility functions for combination of description and counter partial definitions.
 * 
 * 	NOTE: This should be move to the utility class? 
 * 
 * @author An C. Tran
 *
 */

public class ParCELExCombineCounterPartialDefinition {

	/**
	 * Combine a description and a give set of counter partial definition into a partial definition
	 * if possible<br>
	 * Steps:
	 * <ul>
	 * While the covered negative examples of the description is not empty and there exists
	 * definitions in the list"</li>
	 * <ol>
	 * <li>Choose the <i>best</i> definition in the list</li>
	 * <li>Remove all negative examples covered by the description that are also covered by the
	 * <i>best</i> definition</li>
	 * <li>Remove the definition out of the list of definitions</li>
	 * </ol>
	 * </ul>
	 * 
	 * @param description
	 *            Description which will be combined (if possible)
	 * @param counterPartialDefinitions
	 *            Set of counter partial definitions
	 * 
	 * @return A set combinable partial definitions if exists, <code>null</code> otherwise
	 */
	public static Set<ParCELExtraNode> getCombinable(ParCELNode description,
			SortedSet<ParCELExtraNode> counterPartialDefinitions) {

		if (counterPartialDefinitions == null || counterPartialDefinitions.isEmpty())
			return null;

		Set<ParCELExtraNode> combinableCounterPartialDefinitions = new HashSet<>();

		// this set holds the set of negative examples covered by the description
		// all operations on the set of covered negative examples will be performed on this set to
		// avoid changing the description
		HashSet<OWLIndividual> coveredNegativeExamples = new HashSet<>(description.getCoveredNegativeExamples());

		
		Object[] partialDefinitionsArr;
		synchronized (counterPartialDefinitions) {
			partialDefinitionsArr = counterPartialDefinitions.toArray();
		}

		// for each loop, choose the best counter partial definition which can reduce the remaining
		// covered negative examples of the description
		int i = 0;
		for (i = 0; i < partialDefinitionsArr.length; i++) {

			ParCELExtraNode currentNode = (ParCELExtraNode) partialDefinitionsArr[i];

			// get the number of common covered negative examples between description and the
			// partial definition i
			int maxIntersection = countIntersection(coveredNegativeExamples,
					currentNode.getCoveredNegativeExamples());
			int maxIntersectionIndex = i;

			// compare the number of intersection of the first element with the rest
			for (int j = i + 1; j < partialDefinitionsArr.length; j++) {
				int intersectionJ = countIntersection(coveredNegativeExamples,
						((ParCELExtraNode) partialDefinitionsArr[j]).getCoveredNegativeExamples());

				if (intersectionJ > maxIntersection) {
					maxIntersection = intersectionJ;
					maxIntersectionIndex = j;
				}
			}

			// swap the best counter partial definition into the top
			if (maxIntersectionIndex != i) {
				ParCELExtraNode tmpNode = currentNode;
				currentNode = (ParCELExtraNode) partialDefinitionsArr[maxIntersectionIndex];
				partialDefinitionsArr[maxIntersectionIndex] = tmpNode;
			}

			// remove the negative examples covered by the "best" counter partial definition
			coveredNegativeExamples.removeAll(currentNode.getCoveredNegativeExamples());

			// add the best definition in the current iteration into the result set
			combinableCounterPartialDefinitions.add(currentNode);

			// check for the termination of the loop: The covered negative examples of the
			// description is empty
			if (coveredNegativeExamples.isEmpty())
				break;
		}

		// check if the new partial definition is found, change the description
		if (i >= partialDefinitionsArr.length)
			return null;
		else
			return combinableCounterPartialDefinitions;
	}

	/**
	 * Count the number of common elements between two sets
	 * 
	 * @param set1 The 1st set of individual
	 * @param set2 The 2nd set of individual
	 * 
	 * @return Number of common elements of two sets
	 */
	private static int countIntersection(Set<OWLIndividual> set1, Set<OWLIndividual> set2) {
		int count = 0;

		for (OWLIndividual ind : set2) {
			if (set1.contains(ind))
				count++;
		}

		return count;
	}

}
