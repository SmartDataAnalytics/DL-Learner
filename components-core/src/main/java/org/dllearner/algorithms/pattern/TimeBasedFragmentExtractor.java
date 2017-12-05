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
package org.dllearner.algorithms.pattern;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.PaginationUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.concurrent.TimeUnit;

/**
 * @author Lorenz Buehmann
 *
 */
public class TimeBasedFragmentExtractor implements FragmentExtractor{
	
	public static final FragmentExtractionStrategy extractionStrategy = FragmentExtractionStrategy.TIME;
	private SparqlEndpointKS ks;
	private QueryExecutionFactory qef;
	
	private long maxExecutionTimeInMilliseconds;
	private long startTime;
	
	public TimeBasedFragmentExtractor(SparqlEndpointKS ks, int maxExecutionTimeInMilliseconds, TimeUnit timeUnit) {
		this.ks = ks;
		this.maxExecutionTimeInMilliseconds = timeUnit.toMillis(maxExecutionTimeInMilliseconds);

		qef = ks.getQueryExecutionFactory();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.pattern.FragmentExtractor#extractFragment(org.dllearner.core.owl.NamedClass)
	 */
	@Override
	public Model extractFragment(OWLClass cls, int maxFragmentDepth) {
		startTime = System.currentTimeMillis();
		Model fragment = ModelFactory.createDefaultModel();
		
		Query query = buildConstructQuery(cls, maxFragmentDepth);
		
		long pageSize = PaginationUtils.adjustPageSize(qef, 10000);
		query.setLimit(pageSize);
		int offset = 0;
		while(getRemainingRuntime() > 0){
			query.setOffset(offset);System.out.println(query);
			Model model = qef.createQueryExecution(query).execConstruct();
			fragment.add(model);
			offset += pageSize;
		}
		return fragment;
	}
	
	private Query buildConstructQuery(OWLClass cls, int depth){
		StringBuilder sb = new StringBuilder();
		int maxVarCnt = 0;
		sb.append("CONSTRUCT {\n");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
			maxVarCnt++;
		}
		sb.append("?o").append(maxVarCnt).append(" a ?type.\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("?s a ?cls.");
		sb.append("?s").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("OPTIONAL{?o").append(maxVarCnt).append(" a ?type}.\n");
		for(int i = 1; i < depth-1; i++){
			sb.append("}");
		}
		
		sb.append("}\n");
		ParameterizedSparqlString template = new ParameterizedSparqlString(sb.toString());
		template.setIri("cls", cls.toStringID());
		return template.asQuery();
	}
	
	private long getRemainingRuntime(){
		return maxExecutionTimeInMilliseconds - (System.currentTimeMillis() - startTime);
	}

}
