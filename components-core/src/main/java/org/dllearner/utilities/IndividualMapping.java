/**
 * 
 */
package org.dllearner.utilities;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public class IndividualMapping {
	
	private TObjectIntMap<OWLIndividual> mapping = new TObjectIntCustomHashMap<>();
	
	private volatile int value = 0;
	
	public int getMapping(OWLIndividual individual) {
		return mapping.putIfAbsent(individual, value++);
	}

}
