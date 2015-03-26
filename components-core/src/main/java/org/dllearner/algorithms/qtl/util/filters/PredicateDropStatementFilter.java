/**
 * 
 */
package org.dllearner.algorithms.qtl.util.filters;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * A filter that drops statements whose predicate is in given blacklist.
 * @author Lorenz Buehmann
 *
 */
public class PredicateDropStatementFilter extends Filter<Statement> {
	
	
	private Set<String> predicateIriBlackList;

	public PredicateDropStatementFilter(Set<String> predicateIriBlackList) {
		this.predicateIriBlackList = predicateIriBlackList;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.util.iterator.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept(Statement st) {
		return !predicateIriBlackList.contains(st.getPredicate().toString());
	}

}
