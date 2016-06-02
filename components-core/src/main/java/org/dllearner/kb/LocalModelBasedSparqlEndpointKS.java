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
package org.dllearner.kb;

import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.core.ComponentAnn;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ComponentAnn(name = "Local Endpoint", shortName = "local_sparql", version = 0.9)
public class LocalModelBasedSparqlEndpointKS extends SparqlEndpointKS {
	
	private OntModel model;
	
	public LocalModelBasedSparqlEndpointKS() {}
	
	/**
	 * Create new Sparql Endpoint based on Jena Ontology
	 * @param model ontology model
	 */
	public LocalModelBasedSparqlEndpointKS(OntModel model) {
		this.model = model;
	}
	
	/**
	 * Create new Sparql Endpoint based on Jena model
	 * @param model rdf model
	 * @param reasoningEnabled whether to use Jena RDFS inferencing
	 */
	public LocalModelBasedSparqlEndpointKS(Model model, boolean reasoningEnabled) {
		this(model, reasoningEnabled ? OntModelSpec.OWL_MEM_RDFS_INF : OntModelSpec.OWL_MEM);
	}
	
	/**
	 * Create new Sparql Endpoint based on Jena Model and reasoning spec
	 * @param model rdf model
	 * @param reasoning type of reasoning
	 */
	public LocalModelBasedSparqlEndpointKS(Model model, OntModelSpec reasoning) {
		this.model = ModelFactory.createOntologyModel(reasoning, model);
	}
	
	/**
	 * Create new Sparql Endpoint based on Jena Model. No reasoning enabled by default.
	 * @param model rdf model
	 */
	public LocalModelBasedSparqlEndpointKS(Model model) {
		this(model, false);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.SparqlEndpointKS#buildQueryExecutionFactory()
	 */
	@Override
	protected QueryExecutionFactory buildQueryExecutionFactory() {
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(model);
		
		// we are working on an in-memory model, but still should enable caching by default
		qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir, true, cacheTTL);
		
		return qef;
	}
	
	public OntModel getModel() {
		return model;
	}
	
	@Override
	public boolean isRemote() {
		return false;
	}
	
	@Override
	public boolean supportsSPARQL_1_1() {
		return true;
	}
	
	@Override
	public String toString() {
		String out = String.format("%-15s %-25s%n", "Endpoint:", "in-memory model");
		out += String.format("%-15s %-25s%n", "Profile:", model.getProfile());
		return out;
	}
}
