/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.HashMap;
import java.util.List;

import org.dllearner.algorithms.isle.index.Token;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class EntityTokenizer extends HashMap<Entity, List<Token>>{
	
	
	
	
	/* (non-Javadoc)
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public List<Token> get(Object key) {
		return super.get(key);
	}
	

}
