package org.dllearner.algorithm.tbsl.learning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.dllearner.algorithm.tbsl.nlp.Lemmatizer;
import org.dllearner.algorithm.tbsl.nlp.LingPipeLemmatizer;
import org.dllearner.algorithm.tbsl.search.SolrSearch;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.RatedQuery;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Prefix;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Similarity;
import org.dllearner.core.Oracle;
import org.dllearner.core.SparqlQueryLearningAlgorithm;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SPARQLTemplateBasedLearner implements SparqlQueryLearningAlgorithm{
	
	enum Ranking{
		LUCENE, SIMILARITY, NONE
	}
	
	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner.class);
	private Monitor mon = MonitorFactory.getTimeMonitor("stbl");
	
	private static final int TOP_K = 5;
	private static final String SOLR_SERVER_URL = "http://139.18.2.173:8080/apache-solr-3.1.0";
	private static final int RECURSION_DEPTH = 2;
	
	private Ranking ranking = Ranking.SIMILARITY;
	private boolean useRemoteEndpointValidation = true;
	private boolean stopIfQueryResultNotEmpty = true;
	private int maxTestedQueriesPerTemplate = 25;
	
	private SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private SolrSearch resource_index;
	private SolrSearch class_index;
	private SolrSearch property_index;
	private ModelGenerator modelGenenerator;
	private Templator templateGenerator;
	
	private String question;
	
	private Oracle oracle;
	
	private Map<String, List<String>> resourcesURICache;
	private Map<String, List<String>> classesURICache;
	private Map<String, List<String>> propertiesURICache;
	
	private Map<String, List<String>> learnedSPARQLQueries;
	private Set<Template> templates;
	private Collection<Query> sparqlQueryCandidates;
	private Map<Template, Collection<? extends Query>> template2Queries;
	private Map<Slot, List<String>> slot2URI;
	
	private Map<String, String> prefixMap;
	
	private Lemmatizer lemmatizer = new LingPipeLemmatizer();// StanfordLemmatizer();
	
	private int maxQueryExecutionTimeInSeconds = 20;
	
	
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
		
		prefixMap = new HashMap<String, String>();
		prefixMap.put(RDF.getURI(), "rdf");
		prefixMap.put(RDFS.getURI(), "rdfs");
		prefixMap.put("http://dbpedia.org/ontology/", "dbo");
		prefixMap.put("http://dbpedia.org/property/", "dbp");
		prefixMap.put("http://dbpedia.org/resource/", "dbr");
		prefixMap.put(FOAF.getURI(), "foaf");
		prefixMap.put("http://dbpedia.org/class/yago/", "yago");
		
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
		
		templateGenerator = new Templator();
		
		cache.setMaxExecutionTimeInSeconds(maxQueryExecutionTimeInSeconds);
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		Set<String> predicateFilters = new HashSet<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
	}
	
	public void setQuestion(String question){
		this.question = question;
	}
	
	public void setUseRemoteEndpointValidation(boolean useRemoteEndpointValidation){
		this.useRemoteEndpointValidation = useRemoteEndpointValidation;
	}
	
	public int getMaxQueryExecutionTimeInSeconds() {
		return maxQueryExecutionTimeInSeconds;
	}

	public void setMaxQueryExecutionTimeInSeconds(int maxQueryExecutionTimeInSeconds) {
		this.maxQueryExecutionTimeInSeconds = maxQueryExecutionTimeInSeconds;
	}

	public int getMaxTestedQueriesPerTemplate() {
		return maxTestedQueriesPerTemplate;
	}

	public void setMaxTestedQueriesPerTemplate(int maxTestedQueriesPerTemplate) {
		this.maxTestedQueriesPerTemplate = maxTestedQueriesPerTemplate;
	}

	public void setRanking(Ranking ranking) {
		this.ranking = ranking;
	}
	
	private void reset(){
		learnedSPARQLQueries = new HashMap<String, List<String>>();
		resourcesURICache = new HashMap<String, List<String>>();
		classesURICache = new HashMap<String, List<String>>();
		propertiesURICache = new HashMap<String, List<String>>();
		template2Queries = new HashMap<Template, Collection<? extends Query>>();
		slot2URI = new HashMap<Slot, List<String>>();
	}
	
	public void learnSPARQLQueries() throws NoTemplateFoundException{
		reset();
		//generate SPARQL query templates
		logger.info("Generating SPARQL query templates...");
		mon.start();
		templates = templateGenerator.buildTemplates(question);
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		if(templates.isEmpty()){
			throw new NoTemplateFoundException();
		}
		logger.info("Templates:");
		for(Template t : templates){
			logger.info(t);
		}
		
		//generate SPARQL query candidates, but select only a fixed number per template
		template2Queries = getSPARQLQueryCandidates(templates, ranking);
		sparqlQueryCandidates = getNBestQueryCandidatesForTemplates(template2Queries);
		
		//test candidates
		if(useRemoteEndpointValidation){ //on remote endpoint
			validateAgainstRemoteEndpoint(sparqlQueryCandidates);
		} else {//on local model
			validateAgainstLocalModel(sparqlQueryCandidates);
		}
		
	}
	
	public List<String> getSPARQLQueries() throws NoTemplateFoundException{
		logger.info("Generating SPARQL query templates...");
		mon.start();
		templates = templateGenerator.buildTemplates(question);
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		if(templates.isEmpty()){
			throw new NoTemplateFoundException();
		}
		logger.info("Templates:");
		for(Template t : templates){
			logger.info(t);
		}
		
		//generate SPARQL query candidates
		logger.info("Generating SPARQL query candidates...");
		mon.start();
		Map<Template, Collection<? extends Query>> template2Queries = getSPARQLQueryCandidates(templates, ranking);
		sparqlQueryCandidates = getNBestQueryCandidatesForTemplates(template2Queries);
		
		
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		
		List<String> queries = new ArrayList<String>();
		for(Query q : sparqlQueryCandidates){
			queries.add(q.toString());
		}
		
		return queries;
	}
	
	public Set<Template> getTemplates(){
		return templates;
	}
	
	public List<String> getGeneratedSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		for(Query q : sparqlQueryCandidates){
			queries.add(q.toString());
		}
		
		return queries;
	}
	
	public Map<Template, Collection<? extends Query>> getTemplates2SPARQLQueries(){
		return template2Queries;
	}
	
	public Map<Slot, List<String>> getSlot2URIs(){
		return slot2URI;
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
	
	private Map<Template,Collection<? extends Query>> getSPARQLQueryCandidates(Set<Template> templates, Ranking ranking){
		switch(ranking){
			case LUCENE: return getSPARQLQueryCandidatesSortedByLucene(templates);
			case SIMILARITY: return getSPARQLQueryCandidatesSortedBySimilarity(templates);
			case NONE: return getSPARQLQueryCandidates(templates);
			default: return null;
		}
	}
	
	private Map<Template, Collection<? extends Query>> getSPARQLQueryCandidates(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		Set<Query> queries = new HashSet<Query>();
		Map<Template, Collection<? extends Query>> template2Queries = new HashMap<Template, Collection<? extends Query>>();
		for(Template template : templates){
			queries = new HashSet<Query>();
			queries.add(template.getQuery());
			template2Queries.put(template, queries);
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
		return template2Queries;
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
	
	private Map<Template, Collection<? extends Query>> getSPARQLQueryCandidatesSortedByLucene(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		SortedSet<RatedQuery> ratedQueries = new TreeSet<RatedQuery>();
		Map<Template, Collection<? extends Query>> template2Queries = new HashMap<Template, Collection<? extends Query>>();
		
		Query query;
		for(Template template : templates){
			query = template.getQuery();
			ratedQueries = new TreeSet<RatedQuery>();
			ratedQueries.add(new RatedQuery(query, 0));
			template2Queries.put(template, ratedQueries);
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
		return template2Queries;
	}
	
	private Map<Template, Collection<? extends Query>> getSPARQLQueryCandidatesSortedBySimilarity(Set<Template> templates){
		logger.info("Generating candidate SPARQL queries...");
		mon.start();
		List<Query> queries = new ArrayList<Query>();
		Map<Template, Collection<? extends Query>> template2Queries = new HashMap<Template, Collection<? extends Query>>();
		List<String> uriCandidates;
		for(Template template : templates){
			queries = new ArrayList<Query>();
			queries.add(template.getQuery());
			template2Queries.put(template, queries);
			for(Slot slot : template.getSlots()){
				List<Query> tmp = new ArrayList<Query>();
				String var = slot.getAnchor();
				List<String> words = slot.getWords();
				SPARQL_Prefix prefix = null;
				uriCandidates = getCandidateURIsSortedBySimilarity(slot);
				for(String uri : uriCandidates){
					for(Entry<String, String> uri2prefix : prefixMap.entrySet()){
						if(uri.startsWith(uri2prefix.getKey())){
							prefix = new SPARQL_Prefix(uri2prefix.getValue(), uri2prefix.getKey());
							uri = uri.replace(uri2prefix.getKey(), uri2prefix.getValue() + ":");
							break;
						} 
					}
					for(Query query : queries){
						if(slot.getSlotType() == SlotType.SYMPROPERTY){
							Query reversedQuery = new Query(query);
							reversedQuery.getTriplesWithVar(var).iterator().next().reverse();
//							logger.info("NORMAL QUERY:\n" + query.toString());
//							logger.info("REVERSED QUERY:\n" + reversedQuery.toString());
							if(prefix != null){
								reversedQuery.addPrefix(prefix);
								reversedQuery.replaceVarWithPrefixedURI(var, uri);
							} else {
								reversedQuery.replaceVarWithURI(var, uri);
							}
							tmp.add(reversedQuery);
						}
						Query newQuery = new Query(query);
						if(prefix != null){
							newQuery.addPrefix(prefix);
							newQuery.replaceVarWithPrefixedURI(var, uri);
						} else {
							newQuery.replaceVarWithURI(var, uri);
						}
						tmp.add(newQuery);
					}
					prefix = null;
				}
				if(!words.isEmpty() && !uriCandidates.isEmpty()){
					queries.clear();
					queries.addAll(tmp);
				}
			}
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		return template2Queries;
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
	
	private List<String> getCandidateURIsSortedBySimilarity(Slot slot){
		logger.info("Generating URI candidates for " + slot.getWords() + "...");
		mon.start();
		List<String> sortedURIs = new ArrayList<String>();
		//get the appropriate index based on slot type
		SolrSearch index = getIndexBySlotType(slot);
		//get the appropriate cache for URIs to avoid redundant queries to index
		Map<String, List<String>> uriCache = getCacheBySlotType(slot);
		
		SortedSet<String> tmp;
		List<String> uris;
		
		//prune the word list only when slot type is not RESOURCE
		List<String> words;
		if(slot.getSlotType() == SlotType.RESOURCE){
			words = slot.getWords();
		} else {
			words = pruneList(slot.getWords());//getLemmatizedWords(slot.getWords());
		}
		
		for(String word : words){
			tmp = new TreeSet<String>(new StringSimilarityComparator(word));
			uris = uriCache.get(word);
			if(uris == null){
				uris = index.getResources("label:\"" + word + "\"~0.7");
				uriCache.put(word, uris);
			}
			tmp.addAll(uris);
			sortedURIs.addAll(tmp);
			tmp.clear();
		}
		slot2URI.put(slot, sortedURIs);
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("URIs: " + sortedURIs);
		return sortedURIs;
	}
	
	private List<String> pruneList(List<String> words){
		List<String> prunedList = new ArrayList<String>();
		for(String w1 : words){
			boolean smallest = true;
			for(String w2 : words){
				if(!w1.equals(w2)){
					if(w2.contains(w1)){
						smallest = false;
						break;
					}
				}
			}
			if(smallest){
				prunedList.add(w1);
			}
		}
		logger.info("Pruned list: " + prunedList);
//		return getLemmatizedWords(words);
		return prunedList;
	}
	
	private List<String> getLemmatizedWords(List<String> words){
		logger.info("Pruning word list " + words + "...");
		mon.start();
		List<String> pruned = new ArrayList<String>();
		for(String word : words){
			//currently only stem single words
			if(word.contains(" ")){
				pruned.add(word);
			} else {
				String lemWord = lemmatizer.stem(word);
				new LingPipeLemmatizer().stem(word);
				if(!pruned.contains(lemWord)){
					pruned.add(lemWord);
				}
			}
			
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Pruned list: " + pruned);
		return pruned;
	}
	
	class StringSimilarityComparator implements Comparator<String>{
		private String s;
		
		public StringSimilarityComparator(String s) {
			this.s = s;
		}
		
		@Override
		public int compare(String s1, String s2) {
			
			double sim1 = Similarity.getSimilarity(s, s1);
			double sim2 = Similarity.getSimilarity(s, s2);
			
			if(sim1 < sim2){
				return 1;
			} else if(sim1 > sim2){
				return -1;
			} else {
				return s1.compareTo(s2);
			}
		}
		
	}
	
	private SolrSearch getIndexBySlotType(Slot slot){
		SolrSearch index = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			index = class_index;
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			index = property_index;
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			index = resource_index;
		}
		return index;
	}
	
	private Map<String, List<String>> getCacheBySlotType(Slot slot){
		Map<String, List<String>> cache = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			cache = classesURICache;
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			cache = propertiesURICache;
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			cache = resourcesURICache;
		}
		return cache;
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
	
	private List<Query> getNBestQueryCandidatesForTemplates(Map<Template, Collection<? extends Query>> template2Queries){
		List<Query> queries = new ArrayList<Query>();
		for(Entry<Template, Collection<? extends Query>> entry : template2Queries.entrySet()){
			int max = Math.min(maxTestedQueriesPerTemplate, entry.getValue().size());
			int i = 0;
			for(Query q : entry.getValue()){
				queries.add(q);
				i++;
				if(i == max){
					break;
				}
			}
		}
		return queries;
	}
	
	private void validateAgainstRemoteEndpoint(Collection<? extends Query> queries){
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
			if(!results.isEmpty()){
				learnedSPARQLQueries.put(query, results);
				if(stopIfQueryResultNotEmpty){
					return;
				}
			}
			logger.info("Result: " + results);
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
	}
	
	private void validateAgainstLocalModel(Collection<? extends Query> queries){
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
		try {
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query + " LIMIT 10"));
			QuerySolution qs;
			String projectionVar;
			while(rs.hasNext()){
				qs = rs.next();
				projectionVar = qs.varNames().next();
				resources.add(qs.get(projectionVar).toString());
			}
		} catch (Exception e) {
			logger.error("Query execution failed.", e);
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
	 * @throws NoTemplateFoundException 
	 */
	public static void main(String[] args) throws MalformedURLException, NoTemplateFoundException {
//		Logger.getLogger(DefaultHttpParams.class).setLevel(Level.OFF);
//		Logger.getLogger(HttpClient.class).setLevel(Level.OFF);
//		Logger.getLogger(HttpMethodBase.class).setLevel(Level.OFF);
		String question = "Who developed the video game World of Warcraft?";
		SPARQLTemplateBasedLearner learner = new SPARQLTemplateBasedLearner();
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
				Collections.<String>singletonList(""), Collections.<String>emptyList());
		learner.setEndpoint(endpoint);
		learner.setQuestion(question);
		learner.learnSPARQLQueries();
		System.out.println(learner.getBestSPARQLQuery());

	}

	@Override
	public void start() {
	}

	@Override
	public List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries) {
		return new ArrayList<String>(learnedSPARQLQueries.keySet());
	}

	@Override
	public String getBestSPARQLQuery() {
		if(!learnedSPARQLQueries.isEmpty()){
			return learnedSPARQLQueries.keySet().iterator().next();
		} else {
			return null;
		}
	}

	

}
