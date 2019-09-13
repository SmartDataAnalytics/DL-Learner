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
import com.google.common.collect.Lists;
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
import org.dllearner.algorithms.qtl.experiments.PredicateExistenceFilterBiomedical;
import org.dllearner.algorithms.qtl.experiments.SPARQLUtils;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLQueryUtils;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALD4BiomedicalChallengeEvaluationDataset extends EvaluationDataset {

	private static final Logger log = LoggerFactory.getLogger(QALD4BiomedicalChallengeEvaluationDataset.class);

	private static final String RESOURCES_DIR = "org/dllearner/algorithms/qtl/";
	private static final String TRAIN_FILE = RESOURCES_DIR + "qald-4_biomedical_train.xml";
	private static final String TEST_FILE = RESOURCES_DIR + "qald-4_biomedical_test.xml";
	private static final Map<String, String> DATASET_FILES = new LinkedHashMap<>();
	static {
		DATASET_FILES.put(TRAIN_FILE, "qald-4-bio-train");
		DATASET_FILES.put(TEST_FILE, "qald-4-bio-test");
	}

	private static SparqlEndpoint endpoint;
	static {
		try {
			endpoint = SparqlEndpoint.create(
					"http://sake.informatik.uni-leipzig.de:8890/sparql",
					Lists.newArrayList("http://biomedical.org"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public QALD4BiomedicalChallengeEvaluationDataset(File benchmarkDirectory) {
		this(benchmarkDirectory, endpoint);
	}

	public QALD4BiomedicalChallengeEvaluationDataset(File benchmarkDirectory, SparqlEndpoint endpoint) {
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

		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("drugbank", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
		prefixMapping.setNsPrefix("drugs", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/");
		prefixMapping.setNsPrefix("drug-targets", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/");
		prefixMapping.setNsPrefix("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
		prefixMapping.setNsPrefix("side-effects", "http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/");
		prefixMapping.setNsPrefix("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
		prefixMapping.setNsPrefix("diseases", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/");

		DATASET_FILES.forEach((key, value) -> {
            try {
                process(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


		reasoner = new SPARQLReasoner(ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}

		PredicateExistenceFilterBiomedical predicateFilter = new PredicateExistenceFilterBiomedical();
		predicateFilter.init();
		setPredicateFilter(predicateFilter);
	}

	private void process(String datasetFile, String datasetPrefix) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(datasetFile)) {
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");

			QueryUtils triplePatternExtractor = new QueryUtils();

			for( int i = 0; i < questionNodes.getLength(); i++){

				org.w3c.dom.Element questionNode = (org.w3c.dom.Element) questionNodes.item(i);

				String id = datasetPrefix + "_" + Integer.valueOf(questionNode.getAttribute("id"));
//	            	String answerType = questionNode.getAttribute("answerType");
				boolean aggregation = false;//Boolean.valueOf(questionNode.getAttribute("aggregation"));


				// Read SPARQL query
				String sparqlQuery = questionNode.getElementsByTagName("query").item(0).getChildNodes().item(0).getNodeValue().trim();
				sparqlQuery = SPARQLQueryUtils.PREFIXES + " " + sparqlQuery;
				if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }")){
					sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }", "");
					sparqlQuery = sparqlQuery.replace("FILTER (lang(?string) = 'en')", "");
					sparqlQuery = sparqlQuery.replace("?string", "");
				}
				if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }")){
					sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }", "");
					sparqlQuery = sparqlQuery.replace("?string", "");
				}

//	            	System.out.println(sparqlQuery);
				// check if OUT OF SCOPE marked
				boolean outOfScope = sparqlQuery.toUpperCase().contains("OUT OF SCOPE");

				//check if ASK query
				boolean askQuery = sparqlQuery.toUpperCase().contains("ASK");

				boolean containsLimit = sparqlQuery.toUpperCase().contains("LIMIT");

				boolean containsCount = sparqlQuery.toUpperCase().contains("COUNT");

				boolean containsFilter = sparqlQuery.toUpperCase().contains("FILTER");

				boolean containsUNION = sparqlQuery.toUpperCase().contains("UNION");

				boolean needsSPARQL11 = sparqlQuery.toUpperCase().contains("MINUS") ||
						sparqlQuery.toUpperCase().contains("EXISTS");

				boolean singleProjectionVariable = true;


				if(true
						&& !needsSPARQL11
						&& !aggregation
						&& !outOfScope
						&& !containsCount
						&& !askQuery
						&& singleProjectionVariable
						&& !containsLimit
						&& !containsFilter
	            		&& !containsUNION
						){
					Query query = QueryFactory.create(sparqlQuery);
					adjustPrefixes(query);
					List<String> result = SPARQLUtils.getResult(ks.getQueryExecutionFactory(), query);
					boolean isResourceTarget = false;
					if(result.get(0).startsWith("http://")) {
						isResourceTarget = true;
					}
					if(isResourceTarget) {
						sparqlQueries.put(id, query);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to load QALD dataset.", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Predicate<Statement>> getQueryTreeFilters() {
		return Lists.newArrayList(
			new PredicateDropStatementFilter(StopURIsRDFS.get()));
	}

	@Override
	public boolean usesStrictOWLTypes() {
		return false;
	}

	public static void main(String[] args) throws Exception{
		if(args.length == 0){
			System.out.println("Usage: QALD4BiomedicalChallengeEvaluationDataset <queriesTargetFile");
			System.exit(0);
		}
		QALD4BiomedicalChallengeEvaluationDataset ds = new QALD4BiomedicalChallengeEvaluationDataset(new File(System.getProperty("java.io.tmpdir") + File.separator + "test"), endpoint);
		ds.saveToDisk(new File(args[0]));
//		List<String> queries = ds.getSparqlQueries();
//		System.out.println(queries.size());
//		queries.forEach(q -> System.out.println(QueryFactory.create(q)));
//		queries.forEach(q -> System.out.println(ds.getKS().getQueryExecutionFactory().createQueryExecution(q).execSelect().hasNext()));
//
//		ds.analyze();
	}

}
