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
 * A filter that drops statements whose predicate is in given blacklist.
 * @author Lorenz Buehmann
 *
 */
public class PredicateDropStatementFilter implements Predicate<Statement> {
	
	
	private Set<String> predicateIriBlackList;

	public PredicateDropStatementFilter(Set<String> predicateIriBlackList) {
		this.predicateIriBlackList = predicateIriBlackList;
	}

	/* (non-Javadoc)
	 * @see org.apache.jena.util.iterator.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean test(Statement st) {
		return !predicateIriBlackList.contains(st.getPredicate().toString());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "!?p in " + predicateIriBlackList;
	}

}
