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
package org.dllearner.algorithms.qtl.experiments.datasets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
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

/**
 * @author Lorenz Buehmann
 *
 */
public class LUBMEvaluationDataset extends EvaluationDataset {

	private static final Logger log = LoggerFactory.getLogger(LUBMEvaluationDataset.class);

	private static final String QUERIES_FILE = "src/test/resources/org/dllearner/algorithms/qtl/lubm_queries.txt";


	public LUBMEvaluationDataset(File benchmarkDirectory, SparqlEndpoint examplesEndpoint, SparqlEndpoint dataEndpoint) {
		super("LUBM");

		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache-" + getName());
		try {
			ks = new SparqlEndpointKS(dataEndpoint);
			ks.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl;mv_store=false");
			ks.init();

			if(examplesEndpoint != dataEndpoint) {
				examplesKS = new SparqlEndpointKS(examplesEndpoint);
				examplesKS.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl;mv_store=false");
				examplesKS.init();
			} else {
				examplesKS = ks;
			}

			reasoner = new SPARQLReasoner(ks);
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

		// read SPARQL queries
		readQueries();
	}

	private void readQueries() {
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
	}

	public LUBMEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
		this(benchmarkDirectory, endpoint, endpoint);
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
		LUBMEvaluationDataset ds = new LUBMEvaluationDataset(new File(System.getProperty("java.io.tmpdir") + File.separator + "test"), endpoint);
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
