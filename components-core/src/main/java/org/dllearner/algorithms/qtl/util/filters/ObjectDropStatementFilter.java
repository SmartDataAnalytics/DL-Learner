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
