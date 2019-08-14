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

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.dllearner.algorithms.qtl.experiments.datasets.EvaluationDataset;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.*;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaEvaluationDataset extends EvaluationDataset {
	
	public DBpediaEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint, File queriesFile) {
		super("DBpedia");
		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache");
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI-cache;mv_store=false");
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		// load SPARQL queries
		try {
			sparqlQueries = new HashMap<>();
			int i = 1;
			List<String> lines = Files.readLines(queriesFile, Charsets.UTF_8);
			for (String line : lines) {
				sparqlQueries.put(String.valueOf(i++), QueryFactory.create(line));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		reasoner = new SPARQLReasoner(ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		baseIRI = "http://dbpedia.org/resource/";
		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		prefixMapping.setNsPrefix("wiki", "http://wikidata.dbpedia.org/resource/");
		prefixMapping.setNsPrefix("odp-dul", "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#");
		prefixMapping.setNsPrefix("schema", "http://schema.org/");

		PredicateExistenceFilter predicateFilter = new PredicateExistenceFilterDBpedia(null);
		setPredicateFilter(predicateFilter);
	}

	@Override
	public List<Predicate<Statement>> getQueryTreeFilters() {
		return queryTreeFilters();
	}

	@Override
	public boolean usesStrictOWLTypes() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public static List<Predicate<Statement>> queryTreeFilters() {
		return Lists.newArrayList(
			new PredicateDropStatementFilter(StopURIsDBpedia.get()),
			new ObjectDropStatementFilter(StopURIsDBpedia.get()),
			new PredicateDropStatementFilter(StopURIsRDFS.get()),
			new PredicateDropStatementFilter(StopURIsOWL.get()),
			new ObjectDropStatementFilter(StopURIsOWL.get()),
			new PredicateDropStatementFilter(StopURIsSKOS.get()),
			new ObjectDropStatementFilter(StopURIsSKOS.get()),
			new NamespaceDropStatementFilter(
			Sets.newHashSet(
					"http://dbpedia.org/property/", 
					"http://purl.org/dc/terms/",
					"http://dbpedia.org/class/yago/"
					,FOAF.getURI()
					)
					),
					new PredicateDropStatementFilter(
							Sets.newHashSet(
									"http://www.w3.org/2002/07/owl#equivalentClass", 
									"http://www.w3.org/2002/07/owl#disjointWith"))
			);
	}

}
