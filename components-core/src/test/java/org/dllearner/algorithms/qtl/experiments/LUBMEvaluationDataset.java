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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.filters.MostSpecificTypesFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author Lorenz Buehmann
 *
 */
public class LUBMEvaluationDataset extends EvaluationDataset {

	private static final Logger log = LoggerFactory.getLogger(LUBMEvaluationDataset.class);

	private static final String QUERIES_FILE = "src/test/resources/org/dllearner/algorithms/qtl/lubm_queries.txt";

	private static final String CACHE_DIR = "/tmp/qtl/benchmark/lubm/cache";

	public LUBMEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
		super("LUBM");
		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache-" + getName());
		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();
		qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI-cache;mv_store=false", false, TimeUnit.DAYS.toMillis(7) );
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI-cache;mv_store=false");
			ks.setQueryExecutionFactory(qef);
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}

		// read SPARQL queries
		try {
			List<String> lines = Files.readAllLines(Paths.get(QUERIES_FILE));

			String query = "";
			String id = null;
			for (String line : lines) {
				if(line.startsWith("#")) {
					query = "";
					if(id == null) {
						id = line.replace("#", "").trim();
					}
				} else if(line.isEmpty()) {
					 if(!query.isEmpty()) {
						 sparqlQueries.put(id, QueryFactory.create(query));
						 id = null;
						 query = "";
					 }
				} else {
					query += line + "\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		reasoner = new SPARQLReasoner(ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		baseIRI = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("ub", "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");

		treeFilters.add(new MostSpecificTypesFilter(reasoner));
		treeFilters.add(new PredicateExistenceFilter() {
			@Override
			public boolean isMeaningless(Node predicate) {
				return predicate.getURI().startsWith("http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Predicate<Statement>> getQueryTreeFilters() {
		return Lists.newArrayList(
			new PredicateDropStatementFilter(StopURIsRDFS.get()),
			new PredicateDropStatementFilter(StopURIsOWL.get()),
			new ObjectDropStatementFilter(StopURIsOWL.get()),
					new PredicateDropStatementFilter(
							Sets.newHashSet(
									"http://www.w3.org/2002/07/owl#equivalentClass", 
									"http://www.w3.org/2002/07/owl#disjointWith"))
			);
	}

	@Override
	public boolean usesStrictOWLTypes() {
		return false;
	}

	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://localhost:7200/repositories/lubm-inferred-owlhorst", Lists.newArrayList());
		LUBMEvaluationDataset ds = new LUBMEvaluationDataset(new File("/tmp/test"), endpoint);
		QueryExecutionFactory qef = ds.getKS().getQueryExecutionFactory();
		Map<String, Query> queries = ds.getSparqlQueries();
		System.out.println(queries.size());
		queries.forEach((key, query) -> {
            System.out.println(query);
            query.setLimit(1);
            try (QueryExecution qe = qef.createQueryExecution(query)) {
                ResultSet rs = qe.execSelect();
                System.out.println(rs.hasNext());
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    System.out.println(qs);
                }
            }
        });


	}

}
