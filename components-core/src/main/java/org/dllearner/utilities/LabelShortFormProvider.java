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

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import javax.annotation.Nonnull;

/**
 * A short form provider for OWL entities which uses the English rdfs:label if exist.
 *
 * @author Lorenz Buehmann
 */
public class LabelShortFormProvider implements ShortFormProvider{
	
	private final ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"SELECT ?label WHERE {?entity ?labelProperty ?label. FILTER(LANGMATCHES(LANG(?label),'en'))} LIMIT 1");
	private String labelProperty = RDFS.label.getURI();
	
	private final SimpleIRIShortFormProvider fallback = new SimpleIRIShortFormProvider();
	
	private final QueryExecutionFactory qef;

	public LabelShortFormProvider(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public void dispose() {
		try {
			qef.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Nonnull
	@Override
	public String getShortForm(@Nonnull OWLEntity entity) {
		queryTemplate.clearParams();
		queryTemplate.setIri("entity", entity.toStringID());
		queryTemplate.setIri("labelProperty", labelProperty);
		Query query = queryTemplate.asQuery();
		try(QueryExecution qe = qef.createQueryExecution(query)) {
			ResultSet rs = qe.execSelect();
			String label = null;
			if(rs.hasNext()){
				label = rs.next().getLiteral("label").asLiteral().getLexicalForm();
			} else {
				label = fallback.getShortForm(entity.getIRI());
			}
			return label;
		}
	}
}
