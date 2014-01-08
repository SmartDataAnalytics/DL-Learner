/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.TriplePatternExtractor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDExperiment {
	
	
	private static final Logger logger = Logger.getLogger(QALDExperiment.class.getName());
	
	List<String> datasetFiles = Lists.newArrayList(
			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald1/dbpedia-train.xml"
//			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/3/dbpedia-train.xml",
//			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/3/dbpedia-test.xml"
			);
	
	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	Set<String> allowedNamespaces = Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/");
	Set<String> ignoredProperties = Sets.newHashSet("http://dbpedia.org/ontology/wikiPageID","http://dbpedia.org/ontology/wikiPageRevisionID",
			"http://dbpedia.org/ontology/wikiPageExtracted", "http://dbpedia.org/ontology/wikiPageModified");
	
	QueryExecutionFactory qef;
	String cacheDirectory = "cache";
	
	int minNrOfPositiveExamples = 3;
	int maxDepth = 2;
	double noise = 0d;
	
	QueryTreeFactory<String> queryTreeFactory;
	ConciseBoundedDescriptionGenerator cbdGen;
	private LGGGenerator<String> lggGenerator;
	
	
	public QALDExperiment() {
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
			try {
				long timeToLive = TimeUnit.DAYS.toMillis(60);
				CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
				CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.addAllowedNamespaces(allowedNamespaces);
		queryTreeFactory.addIgnoredPropperties(ignoredProperties);
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint);
		cbdGen.setRecursionDepth(maxDepth);
		
		lggGenerator = new LGGGeneratorImpl<String>();
	}
	
	public void run(){
		DescriptiveStatistics precisionStats = new DescriptiveStatistics();
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		DescriptiveStatistics fMeasureStats = new DescriptiveStatistics();
		
		List<String> sparqlQueries = loadSPARQLQueries();
		logger.info("Overall number of queries: " + sparqlQueries.size());
		
		for (String sparqlQuery : sparqlQueries) {
			try {
				//generate a set of examples as input for the learning algorithm
				Set<String> examples = generateExamples(sparqlQuery, noise);
				
				if(examples.size() >= minNrOfPositiveExamples){
					logger.info("Processing query\n" + sparqlQuery);
					
					//convert examples to query trees
					List<QueryTree<String>> queryTrees = getQueryTrees(examples);
					
					//run LGG
					QueryTree<String> lgg = lggGenerator.getLGG(queryTrees);
					logger.info("Computed LGG:\n" + lgg.getStringRepresentation());
					
					//convert to SPARQL query
					String learnedSPARQLQuery = lgg.toSPARQLQueryString(true, false);
					logger.info("Learned Query:\n" + learnedSPARQLQuery);
					
					
					double precision = precision(sparqlQuery, learnedSPARQLQuery);
					precisionStats.addValue(precision);
					
					double recall = recall(sparqlQuery, learnedSPARQLQuery);
					recallStats.addValue(recall);
					
					double fmeasure = fMeasure(sparqlQuery, learnedSPARQLQuery);
					fMeasureStats.addValue(fmeasure);
					
					logger.info(String.format("P=%f\nR=%f\nF-Score=%f", precision, recall, fmeasure));
				}
			} catch (Exception e) {
				logger.error("Error occured.", e);
			}
		}
		logger.info("Overall Precision:\n" + precisionStats);
		logger.info("Overall Recall:\n" + recallStats);
		logger.info("Overall FMeasure:\n" + fMeasureStats);
	}
	
	private Set<String> generateExamples(String sparqlQuery, double noise){
		Set<String> examples = new TreeSet<>();
		
		//get all resources returned by the query
		List<String> resources = getResult(sparqlQuery);
		
		//shuffle the list to avoid bias
		Collections.shuffle(resources, new Random(123));
		
		//pick some positive examples from the list
		List<String> positiveExamples = resources.subList(0, Math.min(minNrOfPositiveExamples, resources.size()));
		
		//add some noise
		///TODO
		
		examples.addAll(positiveExamples);
		return examples;
	}
	
	private List<QueryTree<String>> getQueryTrees(Set<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		
		for (String resource : resources) {
			Model cbd = cbdGen.getConciseBoundedDescription(resource);
			QueryTree<String> tree = queryTreeFactory.getQueryTree(resource, cbd);
			trees.add(tree);
			System.out.println(tree.getStringRepresentation());
		}
		
		return trees;
	}
	
	private List<String> getResult(String sparqlQuery){
		List<String> resources = new ArrayList<String>();
		
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("uri").isURIResource()){
				resources.add(qs.getResource("uri").getURI());
			} else if(qs.get("uri").isLiteral()){
				resources.add(qs.getLiteral("uri").toString());
			}
		}
		return resources;
	}
	
	private int getResultCount(String sparqlQuery){
		int cnt = 0;
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		while(rs.hasNext()){
			rs.next();
			cnt++;
		}
		return cnt;
	}
	
	private List<String> loadSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		try {
			for(String file : datasetFiles){
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            Document doc = db.parse(new URL(file).openStream());
	            doc.getDocumentElement().normalize();
	            NodeList questionNodes = doc.getElementsByTagName("question");
	            
	            TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
	            
	            for( int i = 0; i < questionNodes.getLength(); i++){
	                
	            	Element questionNode = (Element) questionNodes.item(i);
	                
	            	int id = Integer.valueOf(questionNode.getAttribute("id")); if(id != 44)continue;
//	            	String answerType = questionNode.getAttribute("answerType");
	            	boolean aggregation = false;//Boolean.valueOf(questionNode.getAttribute("aggregation"));
	            	boolean onlydbo = true;//Boolean.valueOf(questionNode.getAttribute("onlydbo"));
	            	
	            	
	            	
	                // Read SPARQL query
	            	String sparqlQuery = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
	            	sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + sparqlQuery;
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
	            	
	            	//check if projection variable is somewhere in object position
	            	boolean ingoingLinks = false;
	            	if(!outOfScope && !askQuery && !containsLimit && !containsFilter){
	            		Query q = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ);
	                	List<Var> projectVars = q.getProjectVars();
	                	Set<Triple> ingoingTriplePatterns = triplePatternExtractor.extractIngoingTriplePatterns(q, projectVars.get(0).asNode());
	                	ingoingLinks = !ingoingTriplePatterns.isEmpty();
	            	}
	            	
	            	if(true
	            			&& !aggregation 
	            			&& !outOfScope 
	            			&& !containsCount 
	            			&& !askQuery 
	            			&& !ingoingLinks 
	            			&& !containsFilter
	            			&& !containsUNION
	            			&& onlydbo
	            			&& (getResultCount(sparqlQuery) >= minNrOfPositiveExamples)
	            			){
	            		queries.add(sparqlQuery);
	            	}
	            }
			}
            
		} 
		catch (DOMException e) {
	            e.printStackTrace();
	    }
		catch (ParserConfigurationException e) {
	            e.printStackTrace();
	    }
		catch (SAXException e) {
	            e.printStackTrace();
	    } 
		catch (IOException e) {
	            e.printStackTrace();
	    }
		return queries;
	}
	
	private double precision(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("uri") != null) {
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		// get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}

		int overlap = Sets.intersection(referenceResources, learnedResources).size();

		double precision = overlap / (double) learnedResources.size();

		return precision;
	}
	
	private double recall(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("uri") != null) {
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		// get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}

		int overlap = Sets.intersection(referenceResources, learnedResources).size();

		double recall = overlap / (double) referenceResources.size();

		return recall;
	}
	
	private double fMeasure(String referenceSparqlQuery, String learnedSPARQLQuery){
		//get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("uri") != null){
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		//get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}
		
		int overlap = Sets.intersection(referenceResources, learnedResources).size();
		
		double precision = overlap / (double) learnedResources.size();
		double recall = overlap / (double) referenceResources.size();
		
		double fMeasure = 0;
		if(precision + recall > 0){
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		
		return fMeasure;
	}
	
	public static void main(String[] args) throws Exception {
		new QALDExperiment().run();
	}

}
