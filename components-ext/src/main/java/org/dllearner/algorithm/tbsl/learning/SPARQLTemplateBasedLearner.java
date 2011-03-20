package org.dllearner.algorithm.tbsl.learning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.algorithm.tbsl.search.SolrSearch;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SPARQLTemplateBasedLearner {
	
	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner.class);
	private Monitor mon = MonitorFactory.getTimeMonitor("stbl");
	
	private static final int TOP_K = 5;
	private static final String SOLR_SERVER_URL = "http://139.18.2.173:8080/apache-solr-1.4.1";
	private static final int RECURSION_DEPTH = 2;
	
	private SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private SolrSearch resource_index;
	private SolrSearch class_index;
	private SolrSearch property_index;
	private ModelGenerator modelGenenerator;
	private Templator templateGenerator;
	
	private String question;
	
	
	public SPARQLTemplateBasedLearner(){
		resource_index = new SolrSearch(SOLR_SERVER_URL + "/dbpedia_resources");
		resource_index.setHitsPerPage(TOP_K);
		class_index = new SolrSearch(SOLR_SERVER_URL + "/dbpedia_classes");
		class_index.setHitsPerPage(TOP_K);
		property_index = new SolrSearch(SOLR_SERVER_URL + "/dbpedia_properties");
		property_index.setHitsPerPage(TOP_K);
		
		Set<String> predicateFilters = new HashSet<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
		
		templateGenerator = new Templator();
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		Set<String> predicateFilters = new HashSet<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
	}
	
	public void learnSPARQLQueries(String question){
		this.question = question;
		
		//generate SPARQL query templates
		logger.info("Generating SPARQL query templates...");
		mon.start();
		Set<Template> templates = templateGenerator.buildTemplates(question);
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Templates:");
		for(Template t : templates){
			logger.info(t);
		}
		
		//generate candidate SPQRL queries
		List<String> possibleSPARQLQueries = getPossibleSPARQLQueries(templates);
		
		//test candidates on remote endpoint
		validateAgainstRemoteEndpoint(possibleSPARQLQueries);
		
		//test candidates on local model
		validateAgainstLocalModel(possibleSPARQLQueries);
		
	}
	
	private Model getWorkingModel(List<String> resources){
		logger.info("Generating local model...");
		mon.start();
		Model workingModel = ModelFactory.createDefaultModel();
		Model model;
		for(String resource : resources){
			model = modelGenenerator.createModel(resource, Strategy.CHUNKS, RECURSION_DEPTH);
			workingModel.add(model);
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Local model contains " + workingModel.size() + " triples.");
		return workingModel;
	}
	
	private List<String> getPossibleSPARQLQueries(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		List<String> queries = new ArrayList<String>();
		Query query;
		for(Template template : templates){
			query = template.getQuery();
			queries.add(query.toString());
			for(Slot slot : template.getSlots()){
				Set<String> tmp = new HashSet<String>();
				String var = slot.getAnchor();
				List<String> words = slot.getWords();
				for(String uri : getCandidateURIs(slot)){
					for(String q : queries){
						tmp.add(q.replace("?" + var, "<" + uri + ">"));
					}
				}
				if(!words.isEmpty()){
					queries.clear();
					queries.addAll(tmp);
				}
			}
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		return queries;
	}
	
	private Set<String> getCandidateURIs(Slot slot){
		logger.info("Generating candidate URIs for " + slot.getWords() + "...");
		mon.start();
		SolrSearch index = null;
		Set<String> uris = new HashSet<String>();
		if(slot.getSlotType() == SlotType.CLASS){
			index = class_index;
		} else if(slot.getSlotType() == SlotType.PROPERTY){
			index = property_index;
		} else if(slot.getSlotType() == SlotType.RESOURCE){
			index = resource_index;
		}
		for(String word : slot.getWords()){
			uris.addAll(index.getResources("label:" + word));
			
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Candiate URIs: " + uris);
		return uris;
	}
	
	private void validateAgainstRemoteEndpoint(List<String> queries){
		logger.info("Testing candidate SPARQL queries on remote endpoint...");
		mon.start();
		for(String query : queries){
			logger.info("Testing query:\n" + query);
			List<String> results = getResultFromRemoteEndpoint(query);
			logger.info("Result: " + results);
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
	}
	
	private void validateAgainstLocalModel(List<String> queries){
		List<String> resources = resource_index.getResources(question);
		
		Model model = getWorkingModel(resources);
		
		for(String query : queries){
			System.out.println("Testing query:\n" + query);
			List<String> results = getResultFromLocalModel(query, model);
			System.out.println("Result: " + results);
		}
	}
	
	private List<String> getResultFromRemoteEndpoint(String query){
		List<String> resources = new ArrayList<String>();
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query + " LIMIT 1"));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.get("y").toString());
		}
		return resources;
	}
	
	private List<String> getResultFromLocalModel(String query, Model model){
		List<String> resources = new ArrayList<String>();
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.get("y").toString());
		}
		return resources;
	}
	

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		SPARQLTemplateBasedLearner learner = new SPARQLTemplateBasedLearner();
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://db0.aksw.org:8999/sparql"), 
				Collections.<String>singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		learner.setEndpoint(endpoint);
//		learner.learnSPARQLQueries("Give me all countries in Europe");
		learner.learnSPARQLQueries("Give me all soccer clubs in Premier League");

	}

}
