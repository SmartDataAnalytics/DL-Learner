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
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.iterator.Filter;
import org.dllearner.algorithms.qtl.qald.QALDJsonLoader;
import org.dllearner.algorithms.qtl.qald.QALDPredicates;
import org.dllearner.algorithms.qtl.qald.schema.Question;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLQueryUtils;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDEvaluationDataset extends EvaluationDataset {

	private static final String TRAIN_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-train-multilingual.json?raw=true";
	private static final String TEST_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-test-multilingual.json?raw=true";

	public QALDEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache");
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI-cache;mv_store=false");
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}

		List<Question> questions = null;
		try {
			URL url = new URL(TRAIN_URL);
			try (InputStream is = url.openStream()) {
				questions = QALDJsonLoader.loadQuestions(is);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// prepend missing PREFIXES to SPARQL query
		questions.stream().forEach(q -> q.getQuery().setSparql(
				SPARQLQueryUtils.PREFIXES + " " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + q.getQuery().getSparql()));

		// filter the questions
		List<Question> filteredQuestions = questions.stream()
				.filter(q -> !q.isAggregation()) // no aggregation
				.filter(q -> q.getAnswertype().equals("resource")) // only resources
				.filter(q -> !q.getAnswers().isEmpty()) // skip no answers
				.filter(q -> !q.getQuery().getSparql().toLowerCase().contains(" union ")) // skip UNION queries
				.filter(q -> q.getAnswers().get(0).getResults().getBindings().size() >= 2) // result size >= 2
				.filter(QALDPredicates.isSubjectTarget())
//				.filter(q -> q.getQuery().getSparql().toLowerCase().contains("chessplayer"))
				.sorted((q1, q2) -> Integer.compare(q1.getId(), q2.getId())) // sort by ID
				.collect(Collectors.toList());
		
		// map to SPARQL queries
		sparqlQueries = filteredQuestions.stream()
				.map(q -> q.getQuery().getSparql())
				.collect(Collectors.toList());
		
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
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Filter<Statement>> getQueryTreeFilters() {
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

	public static void main(String[] args) {
		System.out.println(new QALDEvaluationDataset(new File("/tmp/test"), SparqlEndpoint.getEndpointDBpedia()).getSparqlQueries().size());
	}

}
