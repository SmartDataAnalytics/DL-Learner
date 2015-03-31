/**
 * 
 */
package org.dllearner.algorithms.qtl.util.filters;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * A filter that drops statements which contain
 * subject, predicates and objects whose IRI starts with one of the given
 * namespaces.
 * @author Lorenz Buehmann
 *
 */
public class NamespaceDropStatementFilter extends Filter<Statement> {
	
	private Set<String> namespaces;

	public NamespaceDropStatementFilter(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.util.iterator.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept(Statement st) {
//		return !(
//				namespaces.contains(st.getSubject().getNameSpace()) ||
//				namespaces.contains(st.getPredicate().getNameSpace()) ||
//				(st.getObject().isURIResource() && namespaces.contains(st.getObject().asResource().getNameSpace()))
//				);
		for (String ns : namespaces) {
			if (st.getSubject().getURI().startsWith(ns) || st.getPredicate().getURI().startsWith(ns)
					|| st.getObject().isURIResource() && st.getObject().asResource().getURI().startsWith(ns)) {
				return false;
			}
		}
		return true;
	}

}
