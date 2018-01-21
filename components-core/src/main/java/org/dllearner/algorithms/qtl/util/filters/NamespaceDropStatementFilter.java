/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.util.filters;

import org.apache.jena.rdf.model.Statement;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A filter that drops statements which contain
 * subject, predicates and objects whose IRI starts with one of the given
 * namespaces.
 * @author Lorenz Buehmann
 *
 */
public class NamespaceDropStatementFilter implements Predicate<Statement> {
	
	private Set<String> namespaces;

	public NamespaceDropStatementFilter(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	/* (non-Javadoc)
	 * @see org.apache.jena.util.iterator.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean test(Statement st) {
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
