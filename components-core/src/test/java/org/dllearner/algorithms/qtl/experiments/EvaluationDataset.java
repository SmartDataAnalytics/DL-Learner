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
package org.dllearner.algorithms.qtl.experiments;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.SparqlEndpointKS;

import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.Filter;

/**
 * Contains a knowledge base and a set of SPARQL queries.
 * @author Lorenz Buehmann
 *
 */
public class EvaluationDataset {

	SparqlEndpointKS ks;
	String baseIRI;
	PrefixMapping prefixMapping;
	
	AbstractReasonerComponent reasoner;
	
	List<String> sparqlQueries;
	List<Filter<Statement>> queryTreeFilters = new ArrayList<>();
	
	public SparqlEndpointKS getKS() {
		return ks;
	}
	
	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}
	
	public String getBaseIRI() {
		return baseIRI;
	}
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}
	
	public List<String> getSparqlQueries() {
		return sparqlQueries;
	}
	
	public List<Filter<Statement>> getQueryTreeFilters() {
		return queryTreeFilters;
	}
}
