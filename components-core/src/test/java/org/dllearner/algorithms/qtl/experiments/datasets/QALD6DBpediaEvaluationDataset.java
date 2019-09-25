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

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.apache.jena.query.Query;
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
import org.dllearner.algorithms.qtl.util.filters.*;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALD6DBpediaEvaluationDataset extends EvaluationDataset {

	private static final Logger log = LoggerFactory.getLogger(QALD6DBpediaEvaluationDataset.class);

	private static final String TRAIN_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-train-multilingual.json?raw=true";
	private static final String TEST_URL = "https://github.com/ag-sc/QALD/blob/master/6/data/qald-6-test-multilingual.json?raw=true";
	private static final String[] DATASET_URLS = {
			TRAIN_URL,
			TEST_URL
	};

	private static final String RESOURCES_DIR = "org/dllearner/algorithms/qtl/";
	private static final String TRAIN_FILE = RESOURCES_DIR + "qald-6-train-multilingual.json";
	private static final String TEST_FILE = RESOURCES_DIR + "qald-6-test-multilingual.json";
	private static final Map<String, String> DATASET_FILES = new LinkedHashMap<>();
	static {
		DATASET_FILES.put(TRAIN_FILE, "qald-6-train");
		DATASET_FILES.put(TEST_FILE, "qald-6-test");
	}


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

	public QALD6DBpediaEvaluationDataset(File benchmarkDirectory) {
		this(benchmarkDirectory, endpoint);
	}

	public QALD6DBpediaEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
		super("QALD");
		// set KS
		File cacheDir = new File(benchmarkDirectory, "cache");
		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();
		qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI2017-cache;mv_store=false", false, TimeUnit.DAYS.toMillis(7) );
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.setCacheDir(cacheDir.getAbsolutePath() + "/sparql/qtl-AAAI2017-cache;mv_store=false");
			ks.setQueryExecutionFactory(qef);
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}

		sparqlQueries = new LinkedHashMap<>();

		DATASET_FILES.forEach(this::process);

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

	private void process(String datasetFile, String datasetPrefix) {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(datasetFile)) {
			final List<Question> questions = QALDJsonLoader.loadQuestions(is);

			// prepend missing PREFIXES to SPARQL query
			questions.stream().forEach(q -> q.getQuery().setSparql(
					SPARQLQueryUtils.PREFIXES + " " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + q.getQuery().getSparql()));

			// filter the questions
			List<Question> filteredQuestions = questions.stream()
					.filter(QALDPredicates.hasNoAnswer().negate()) // no answer SPARQL query
					.filter(q -> !q.isAggregation()) // no aggregation
					.filter(q -> q.getAnswertype().equals("resource")) // only resources
					.filter(q -> !q.getAnswers().isEmpty()) // skip no answers
					.filter(q -> !q.getAnswers().get(0).getAdditionalProperties().containsKey("boolean")) // only resources due to bug in QALD
					.filter(QALDPredicates.isUnion().negate()) // skip UNION queries
					.filter(QALDPredicates.hasFilter().negate()) // skip FILTER queries
					.filter(QALDPredicates.isOnlyDBO())
					.filter(q -> q.getAnswers().get(0).getResults().getBindings().size() >= 2) // result size >= 2
					.filter(QALDPredicates.isObjectTarget().or(QALDPredicates.isSubjectTarget()))
//				.filter(q -> q.getQuery().getSparql().toLowerCase().contains("three_dancers"))
					.sorted((q1, q2) -> ComparisonChain.start().compare(q1.getId(), q2.getId()).compare(q1.getQuery().getSparql(), q2.getQuery().getSparql()).result()) // sort by ID
					.collect(Collectors.toList());

			// map to SPARQL queries
			sparqlQueries.putAll(filteredQuestions.stream()
					.collect(LinkedHashMap::new,
							(m, q) -> m.put(datasetPrefix + "_" + String.valueOf(q.getId()), QueryFactory.create(q.getQuery().getSparql())),
							(m, u) -> {}));

		} catch (Exception e) {
			log.error("Failed to load QALD dataset.", e);
		}


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

	@Override
	public boolean usesStrictOWLTypes() {
		return true;
	}

	public static void main(String[] args) throws Exception{
		if(args.length == 0){
			System.out.println("Usage: QALD6DBpediaEvaluationDataset <queriesTargetFile");
			System.exit(0);
		}
		SparqlEndpoint endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql",
														"http://dbpedia.org");
		QALD6DBpediaEvaluationDataset ds = new QALD6DBpediaEvaluationDataset(new File(System.getProperty("java.io.tmpdir") + File.separator + "test"), endpoint);
		ds.saveToDisk(new File(args[0]));
		Map<String, Query> queries = ds.getSparqlQueries();
		System.out.println("#queries:" + queries.size());

		File graphsDir = new File("/home/user/work/experiments/qtl/QALD6/graphs/");
		graphsDir.mkdirs();

//		queries.forEach((id, query) -> QueryToGraphExporter.exportYedGraph(query, new File(graphsDir, id + ".png")));


	}

}
