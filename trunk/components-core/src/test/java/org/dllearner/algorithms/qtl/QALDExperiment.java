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
import java.util.Random;
import java.util.Set;
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
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDExperiment {
	
	
	private static final Logger logger = Logger.getLogger(QALDExperiment.class.getName());
	
	String qaldQuestionsURL = "http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/3/dbpedia-train.xml";
	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	Set<String> allowedNamespaces = Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/");
	Set<String> ignoredProperties = Sets.newHashSet("http://dbpedia.org/ontology/wikiPageID","http://dbpedia.org/ontology/wikiPageRevisionID");
	
	QueryExecutionFactory qef;
	String cacheDirectory = "cache";
	int nrOfPositiveExamples = 5;
	int maxDepth = 1;
	
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
		List<String> sparqlQueries = loadSPARQLQueries();
		
		for (String sparqlQuery : sparqlQueries) {
			logger.info("Processing query\n" + sparqlQuery);
			try {
				//get result set of SPARQL query
				List<String> resources = getResult(sparqlQuery);
				
				if(resources.size() >= nrOfPositiveExamples){
					//shuffle the list to avoid bias
					Collections.shuffle(resources, new Random(123));
					
					//pick some positive examples from the list
					List<String> positiveExamples = resources.subList(0, Math.min(nrOfPositiveExamples, resources.size()));
					
					//convert examples to query trees
					List<QueryTree<String>> queryTrees = getQueryTrees(positiveExamples);
					
					//run LGG
					QueryTree<String> lgg = lggGenerator.getLGG(queryTrees);
					logger.info("Computed LGG:\n" + lgg.getStringRepresentation());
					String learnedSPARQLQuery = lgg.toSPARQLQueryString();
					
					double fmeasure = fMeasure(sparqlQuery, learnedSPARQLQuery);
					logger.info("F-Score: " + fmeasure);
				}
			} catch (Exception e) {
				logger.error("Error occured.", e);
			}
		}
	}
	
	private List<QueryTree<String>> getQueryTrees(List<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		
		for (String resource : resources) {
			Model cbd = cbdGen.getConciseBoundedDescription(resource);
			QueryTree<String> tree = queryTreeFactory.getQueryTree(resource, cbd);
			trees.add(tree);
		}
		
		return trees;
	}
	
	private List<String> getResult(String sparqlQuery){
		List<String> resources = new ArrayList<String>();
		
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("uri") != null){
				resources.add(qs.getResource("uri").getURI());
			}
		}
		return resources;
	}
	
	private List<String> loadSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		try {
			
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(qaldQuestionsURL).openStream());
            doc.getDocumentElement().normalize();
            NodeList questionNodes = doc.getElementsByTagName("question");
            
            for( int i = 0; i < questionNodes.getLength(); i++){
                
            	Element questionNode = (Element) questionNodes.item(i);
                
            	int id = Integer.valueOf(questionNode.getAttribute("id")); 
            	String answerType = questionNode.getAttribute("answerType");
            	boolean aggregation = Boolean.valueOf(questionNode.getAttribute("aggregation"));
            	boolean onlydbo = Boolean.valueOf(questionNode.getAttribute("onlydbo"));
            	
                // Read SPARQL query
            	String sparqlQuery = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
            	
            	// check if OUT OF SCOPE marked
            	boolean outOfScope = sparqlQuery.toUpperCase().contains("OUT OF SCOPE");
            	
            	//check if ASK query
            	boolean askQuery = sparqlQuery.toUpperCase().contains("ASK");
            	
            	if(!aggregation && !outOfScope && !askQuery){
            		queries.add(sparqlQuery);
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
