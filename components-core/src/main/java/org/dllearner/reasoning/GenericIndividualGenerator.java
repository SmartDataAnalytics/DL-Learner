/**
 * 
 */
package org.dllearner.reasoning;

import org.dllearner.core.owl.Individual;

/**
 * Generates generic individuals.
 * @author Lorenz Buehmann
 *
 */
public class GenericIndividualGenerator {
	
	public int cnt = 0;
	
	public Individual newIndividual(){
		return new Individual("http://dllearner.org#genInd_" + cnt++);
	}
}
