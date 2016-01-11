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
package org.dllearner.utilities;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LabelShortFormProvider implements ShortFormProvider{
	
	private final ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"SELECT ?label WHERE {?entity ?labelProperty ?label. FILTER(LANGMATCHES(LANG(?label),'en'))} LIMIT 1");
	private String labelProperty = RDFS.label.getURI();
	
	private final SimpleIRIShortFormProvider fallback = new SimpleIRIShortFormProvider();
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	private Model model;
	
	public LabelShortFormProvider(Model model) {
		this.model = model;
	}
	
	public LabelShortFormProvider(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public LabelShortFormProvider(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		queryTemplate.clearParams();
		queryTemplate.setIri("entity", entity.toStringID());
		queryTemplate.setIri("labelProperty", labelProperty);
		Query query = queryTemplate.asQuery();
		ResultSet rs = executeSelect(query);
		String label = null;
		if(rs.hasNext()){
			label = rs.next().getLiteral("label").asLiteral().getLexicalForm();
		} else {
			label = fallback.getShortForm(entity.getIRI());
		}
		return label;
	}
	
	protected ResultSet executeSelect(Query query){
		ResultSet rs = null;
		if(endpoint != null){
			if(cache != null){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
			} else {
				QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
				for(String uri : endpoint.getDefaultGraphURIs()){
					qe.addDefaultGraph(uri);
				}
				rs = qe.execSelect();
			}
		} else {
			rs = QueryExecutionFactory.create(query, model).execSelect();
		}
		
		return rs;
	}

	

}
