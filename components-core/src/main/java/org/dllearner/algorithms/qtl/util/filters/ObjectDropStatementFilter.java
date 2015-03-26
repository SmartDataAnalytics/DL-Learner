/**
 * 
 */
package org.dllearner.algorithms.qtl.util.filters;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * A filter that drops statements whose object IRI is in given blacklist.
 * @author Lorenz Buehmann
 *
 */
public class ObjectDropStatementFilter extends Filter<Statement> {
	
	
	private Set<String> objectIriBlackList;

	public ObjectDropStatementFilter(Set<String> objectIriBlackList) {
		this.objectIriBlackList = objectIriBlackList;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.util.iterator.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept(Statement st) {
		return !objectIriBlackList.contains(st.getObject().toString());
	}

}
