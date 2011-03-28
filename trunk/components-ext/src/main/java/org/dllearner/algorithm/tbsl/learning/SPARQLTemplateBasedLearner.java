package org.dllearner.algorithm.tbsl.learning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.algorithm.tbsl.search.SolrSearch;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.RatedQuery;
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
	private boolean USE_LUCENE_RANKING = true;
	
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
		
		//generate SPARQL query candidates
		Set<? extends Query> sparqlQueryCandidates;
		if(USE_LUCENE_RANKING){
			sparqlQueryCandidates = getRatedSPARQLQueryCandidates(templates);
		} else {
			sparqlQueryCandidates = getSPARQLQueryCandidates(templates);
		}
		
		//test candidates on remote endpoint
		validateAgainstRemoteEndpoint(sparqlQueryCandidates);
		
		//test candidates on local model
		validateAgainstLocalModel(sparqlQueryCandidates);
		
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
	
//	private List<String> getSPARQLQueryCandidates(Set<Template> templates){
//		logger.info("Generating candidate SPARQL queries...");
//		mon.start();
//		List<String> queries = new ArrayList<String>();
//		Query query;
//		for(Template template : templates){
//			query = template.getQuery();
//			queries.add(query.toString());
//			for(Slot slot : template.getSlots()){
//				Set<String> tmp = new HashSet<String>();
//				String var = slot.getAnchor();
//				List<String> words = slot.getWords();
//				for(String uri : getCandidateURIs(slot)){
//					for(String q : queries){
//						tmp.add(q.replace("?" + var, "<" + uri + ">"));
//					}
//				}
//				if(!words.isEmpty()){
//					queries.clear();
//					queries.addAll(tmp);
//				}
//			}
//		}
//		mon.stop();
//		logger.info("Done in " + mon.getLastValue() + "ms.");
//		return queries;
//	}
	
	private Set<Query> getSPARQLQueryCandidates(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		Set<Query> queries = new HashSet<Query>();
		
		for(Template template : templates){
			queries.add(template.getQuery());
			for(Slot slot : template.getSlots()){
				Set<Query> tmp = new HashSet<Query>();
				String var = slot.getAnchor();
				List<String> words = slot.getWords();
				for(Entry<String, Float> entry1 : getCandidateURIsWithScore(slot).entrySet()){
					for(Query query : queries){
						Query newQuery = new Query(query);
						newQuery.replaceVarWithURI(var, entry1.getKey());
						tmp.add(newQuery);
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
	
	private Map<String, Float> getCandidateRatedSPARQLQueries(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		Map<String, Float> query2Score = new HashMap<String, Float>();
		
		Query query;
		for(Template template : templates){
			query = template.getQuery();
			query2Score.put(query.toString(), Float.valueOf(0));
			for(Slot slot : template.getSlots()){
				Map<String, Float> tmp = new HashMap<String, Float>();
				String var = slot.getAnchor();
				List<String> words = slot.getWords();
				for(Entry<String, Float> entry1 : getCandidateURIsWithScore(slot).entrySet()){
					for(Entry<String, Float> entry2 : query2Score.entrySet()){
						tmp.put(entry2.getKey().replace("?" + var, "<" + entry1.getKey() + ">"), Float.valueOf(entry1.getValue()+entry2.getValue()));
					}
				}
				if(!words.isEmpty()){
					query2Score.clear();
					query2Score.putAll(tmp);
				}
			}
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		return query2Score;
	}
	
	private Set<RatedQuery> getRatedSPARQLQueryCandidates(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		SortedSet<RatedQuery> ratedQueries = new TreeSet<RatedQuery>();
		
		Query query;
		for(Template template : templates){
			query = template.getQuery();
			ratedQueries.add(new RatedQuery(query, 0));
			for(Slot slot : template.getSlots()){
				Set<RatedQuery> tmp = new HashSet<RatedQuery>();
				String var = slot.getAnchor();
				List<String> words = slot.getWords();
				for(Entry<String, Float> entry1 : getCandidateURIsWithScore(slot).entrySet()){
					for(RatedQuery rQ : ratedQueries){
						RatedQuery newRQ = new RatedQuery(rQ, rQ.getScore());
						newRQ.replaceVarWithURI(var, entry1.getKey());
						newRQ.setScore(newRQ.getScore()+entry1.getValue());
						tmp.add(newRQ);
					}
				}
				if(!words.isEmpty()){
					ratedQueries.clear();
					ratedQueries.addAll(tmp);
				}
			}
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		return ratedQueries;
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
		logger.info("Candidate URIs: " + uris);
		return uris;
	}
	
	private Map<String, Float> getCandidateURIsWithScore(Slot slot){
		logger.info("Generating candidate URIs for " + slot.getWords() + "...");
		mon.start();
		SolrSearch index = null;
		Map<String, Float> uri2Score = new HashMap<String, Float>();
		boolean sorted = false;
		if(slot.getSlotType() == SlotType.CLASS){
			index = class_index;
		} else if(slot.getSlotType() == SlotType.PROPERTY){
			index = property_index;
		} else if(slot.getSlotType() == SlotType.RESOURCE){
			index = resource_index;
			sorted = true;
		}
		for(String word : slot.getWords()){
			uri2Score.putAll(index.getResourcesWithScores("label:" + word, sorted));
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Candidate URIs: " + uri2Score.keySet());
		return uri2Score;
	}
	
	private void validateAgainstRemoteEndpoint(Set<? extends Query> queries){
		List<String> queryStrings = new ArrayList<String>();
		for(Query query : queries){
			queryStrings.add(query.toString());
		}
		validateAgainstRemoteEndpoint(queryStrings);
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
	
	private void validateAgainstLocalModel(Set<? extends Query> queries){
		List<String> queryStrings = new ArrayList<String>();
		for(Query query : queries){
			queryStrings.add(query.toString());
		}
		validateAgainstLocalModel(queryStrings);
	}
	
	private void validateAgainstLocalModel(List<String> queries){
		logger.info("Testing candidate SPARQL queries on remote endpoint...");
		mon.start();
		List<String> resources = resource_index.getResources(question);
		
		Model model = getWorkingModel(resources);
		
		for(String query : queries){
			logger.info("Testing query:\n" + query);
			List<String> results = getResultFromLocalModel(query, model);
			logger.info("Result: " + results);
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
	}
	
	private List<String> getResultFromRemoteEndpoint(String query){
		List<String> resources = new ArrayList<String>();
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query + " LIMIT 10"));
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
