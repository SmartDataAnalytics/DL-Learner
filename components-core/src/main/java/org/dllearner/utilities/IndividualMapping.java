/**
 * 
 */
package org.dllearner.utilities;

import org.dllearner.core.owl.Individual;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;

/**
 * @author Lorenz Buehmann
 *
 */
public class IndividualMapping {
	
	private TObjectIntMap<Individual> mapping = new TObjectIntCustomHashMap<Individual>();
	
	private volatile int value = 0;
	
	public int getMapping(Individual individual) {
		return mapping.putIfAbsent(individual, value++);
	}

}
