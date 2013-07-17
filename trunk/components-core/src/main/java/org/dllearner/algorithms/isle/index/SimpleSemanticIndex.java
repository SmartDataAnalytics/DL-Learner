/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.Set;

import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleSemanticIndex implements SemanticIndex{
	

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#getDocuments(org.dllearner.core.owl.Entity)
	 */
	@Override
	public Set<String> getDocuments(Entity entity) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#count(java.lang.String)
	 */
	@Override
	public int count(Entity entity) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SemanticIndex#getSize()
	 */
	@Override
	public int getSize() {
		return 0;
	}
	
	

}
