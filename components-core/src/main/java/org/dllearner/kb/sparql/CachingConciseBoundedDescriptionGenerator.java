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
package org.dllearner.kb.sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

public class CachingConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator{
	
	private Map<String, Model> cache;
	private ConciseBoundedDescriptionGenerator delegatee;
	
	public CachingConciseBoundedDescriptionGenerator(ConciseBoundedDescriptionGenerator cbdGen) {
		this.delegatee = cbdGen;
		cache = new HashMap<>();
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI){
		return cache.computeIfAbsent(resourceURI, r -> delegatee.getConciseBoundedDescription(r));
	}
	
	@Override
	public Model getConciseBoundedDescription(String resource, int depth){
		return cache.computeIfAbsent(resource, r -> delegatee.getConciseBoundedDescription(r, depth));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
		return cache.computeIfAbsent(resource, r -> delegatee.getConciseBoundedDescription(r, depth, withTypesForLeafs));
	}

	@Override
	public void setAllowedPropertyNamespaces(Set<String> namespaces) {
		delegatee.setAllowedPropertyNamespaces(namespaces);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#addAllowedObjectNamespaces(java.util.Set)
	 */
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
		delegatee.addAllowedObjectNamespaces(namespaces);
	}
	
	@Override
	public void addPropertiesToIgnore(Set<String> properties) {
		delegatee.addPropertiesToIgnore(properties);
	}
}
