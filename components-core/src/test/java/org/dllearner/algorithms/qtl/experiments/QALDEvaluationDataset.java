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

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.vocabulary.FOAF;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDEvaluationDataset extends EvaluationDataset {

	private static final Logger log = LoggerFactory.getLogger(QALDEvaluationDataset.class);

	private static final String TRAIN_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-train-multilingual.json?raw=true";
	private static final String TEST_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-test-multilingual.json?raw=true";

	private static final String[] DATASET_URLS = {
			TRAIN_URL,
			TEST_URL
	};

	private static SparqlEndpoint endpoint;
	static {
		try {
			endpoint = SparqlEndpoint.create(
					"http://sake.informatik.uni-leipzig.de:8890/sparql",
					Lists.newArrayList("http://dbpedia.org", "http://dbpedia.org/categories"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public QALDEvaluationDataset(File benchmarkDirectory) {
		this(benchmarkDirectory, endpoint);
	}

	public QALDEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
		super("QALD");
		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache");
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

		final List<Question> questions = new ArrayList<>();
		Arrays.stream(DATASET_URLS).forEach(ds -> {
			try {
				URL url = new URL(ds);
				try (InputStream is = url.openStream()) {
					questions.addAll(QALDJsonLoader.loadQuestions(is));
				} catch (Exception e) {
					log.error("Failed to load QALD dataset.", e);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});


		// prepend missing PREFIXES to SPARQL query
		questions.stream().forEach(q -> q.getQuery().setSparql(
				SPARQLQueryUtils.PREFIXES + " " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + q.getQuery().getSparql()));

		// filter the questions
		List<Question> filteredQuestions = questions.stream()
				.filter(q -> !q.isAggregation()) // no aggregation
				.filter(q -> q.getAnswertype().equals("resource")) // only resources
				.filter(q -> !q.getAnswers().isEmpty()) // skip no answers
				.filter(q -> !q.getAnswers().get(0).getAdditionalProperties().containsKey("boolean")) // only resources due to bug in QALD
				.filter(q -> !q.getQuery().getSparql().toLowerCase().contains(" union ")) // skip UNION queries
				.filter(q -> q.getAnswers().get(0).getResults().getBindings().size() >= 2) // result size >= 2
				.filter(QALDPredicates.isObjectTarget())
				.filter(QALDPredicates.hasFilter().negate())
//				.filter(q -> q.getQuery().getSparql().toLowerCase().contains("chessplayer"))
				.sorted((q1, q2) -> ComparisonChain.start().compare(q1.getId(), q2.getId()).compare(q1.getQuery().getSparql(), q2.getQuery().getSparql()).result()) // sort by ID
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
	public List<Predicate<Statement>> getQueryTreeFilters() {
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
//					"http://purl.org/dc/terms/",
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

	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql",
														"http://dbpedia.org");
		QALDEvaluationDataset ds = new QALDEvaluationDataset(new File("/tmp/test"), endpoint);
		List<String> queries = ds.getSparqlQueries();
		System.out.println(queries.size());
		queries.forEach(q -> System.out.println(QueryFactory.create(q)));
		queries.forEach(q -> System.out.println(ds.getKS().getQueryExecutionFactory().createQueryExecution(q).execSelect().hasNext()));


	}

}
