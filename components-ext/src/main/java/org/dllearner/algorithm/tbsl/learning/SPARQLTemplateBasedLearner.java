package org.dllearner.algorithm.tbsl.learning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.search.HierarchicalSolrSearch;
import org.dllearner.algorithm.tbsl.search.SolrQueryResultItem;
import org.dllearner.algorithm.tbsl.search.SolrQueryResultSet;
import org.dllearner.algorithm.tbsl.search.SolrSearch;
import org.dllearner.algorithm.tbsl.search.ThresholdSlidingSolrSearch;
import org.dllearner.algorithm.tbsl.sparql.Allocation;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.RatedQuery;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Prefix;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_QueryType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Triple;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.sparql.WeightedQuery;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Prefixes;
import org.dllearner.algorithm.tbsl.util.Similarity;
import org.dllearner.algorithm.tbsl.util.SolrQueryResultStringSimilarityComparator;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.Oracle;
import org.dllearner.core.SparqlQueryLearningAlgorithm;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.SPARQLReasoner;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SPARQLTemplateBasedLearner implements SparqlQueryLearningAlgorithm{
	
	//for debugging
	List<String> exclusions = Arrays.asList(new String[]{"http://dbpedia.org/ontology/GeopoliticalOrganisation", 
	"http://dbpedia.org/ontology/Non-ProfitOrganisation"});
	
	enum Ranking{
		LUCENE, SIMILARITY, NONE
	}
	
	private static final String OPTIONS_FILE = SPARQLTemplateBasedLearner.class.getClassLoader().getResource("tbsl/tbsl.properties").getPath();
	
	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner.class);
	private Monitor mon = MonitorFactory.getTimeMonitor("tbsl");
	
	private static final int RECURSION_DEPTH = 2;
	private static final int MAX_URIS_PER_SLOT = 10;
	
	private Ranking ranking;
	private boolean useRemoteEndpointValidation;
	private boolean stopIfQueryResultNotEmpty;
	private int maxTestedQueriesPerTemplate = 50;
	private int maxQueryExecutionTimeInSeconds;
	
	private int maxTestedQueries = 200;
	
	private SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	private SolrSearch resource_index;
	private SolrSearch class_index;
	private SolrSearch property_index;
	private SolrSearch boa_pattern_property_index;
	private ModelGenerator modelGenenerator;
	private Templator templateGenerator;
	
	private String question;
	private int learnedPos = -1;
	
	private Oracle oracle;
	
	private Map<String, SolrQueryResultSet> resourcesURICache;
	private Map<String, SolrQueryResultSet> classesURICache;
	private Map<String, SolrQueryResultSet> propertiesURICache;
	
	private Map<String, Object> learnedSPARQLQueries;
	private Set<Template> templates;
	private Collection<Query> sparqlQueryCandidates;
	private Map<Template, Collection<? extends Query>> template2Queries;
	private Map<Slot, List<String>> slot2URI;
	
	private Set<WeightedQuery> generatedQueries;
	
	private Map<String, String> prefixMap;
	
	private Lemmatizer lemmatizer = new LingPipeLemmatizer();// StanfordLemmatizer();
	
	private SPARQLReasoner reasoner;
	
	public SPARQLTemplateBasedLearner() throws InvalidFileFormatException, FileNotFoundException, IOException{
		this(OPTIONS_FILE);
	}
	
	public SPARQLTemplateBasedLearner(String optionsFile) throws InvalidFileFormatException, FileNotFoundException, IOException{
		this(new Options(new FileInputStream(optionsFile)));
	}
	
	public SPARQLTemplateBasedLearner(Options options){
		this(options, new StanfordPartOfSpeechTagger());
	}
	
	public SPARQLTemplateBasedLearner(Options options, PartOfSpeechTagger tagger){
		this(options, tagger, new WordNet());
	}
	
	public SPARQLTemplateBasedLearner(Options options, PartOfSpeechTagger tagger, WordNet wordNet){
		this(options, tagger, wordNet, "cache");
	}
	
	public SPARQLTemplateBasedLearner(Options options, PartOfSpeechTagger tagger, WordNet wordNet, String cacheDir){
		init(options);
		
		Set<String> predicateFilters = new HashSet<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		
		prefixMap = Prefixes.getPrefixes();
		
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
		
		templateGenerator = new Templator(tagger, wordNet);
		cache = new ExtractionDBCache(cacheDir);
	}
	
	/*
	 * Only for Evaluation useful.
	 */
	public void setUseIdealTagger(boolean value){
		templateGenerator.setUNTAGGED_INPUT(!value);
	}

	private void init(Options options){
		String resourcesIndexUrl = options.fetch("solr.resources.url");
		String resourcesIndexSearchField = options.fetch("solr.resources.searchfield");
		resource_index = new ThresholdSlidingSolrSearch(resourcesIndexUrl, resourcesIndexSearchField, "label", 1.0, 0.1);
		
		String classesIndexUrl = options.fetch("solr.classes.url");
		String classesIndexSearchField = options.fetch("solr.classes.searchfield");
		SolrSearch dbpediaClassIndex = new SolrSearch(classesIndexUrl, classesIndexSearchField, "label");
		
		String yagoClassesIndexUrl = options.fetch("solr.yago.classes.url");
		String yagoClassesIndexSearchField = options.fetch("solr.yago.classes.searchfield");
		SolrSearch yagoClassIndex = new SolrSearch(yagoClassesIndexUrl, yagoClassesIndexSearchField);
		
		class_index = new ThresholdSlidingSolrSearch(dbpediaClassIndex);// new HierarchicalSolrSearch(dbpediaClassIndex, yagoClassIndex);
		
		String propertiesIndexUrl = options.fetch("solr.properties.url");
		String propertiesIndexSearchField = options.fetch("solr.properties.searchfield");
		SolrSearch labelBasedPropertyIndex = new ThresholdSlidingSolrSearch(propertiesIndexUrl, propertiesIndexSearchField, "label", 1.0, 0.1);
		
		String boaPatternIndexUrl = options.fetch("solr.boa.properties.url");
		String boaPatternIndexSearchField = options.fetch("solr.boa.properties.searchfield");
		SolrSearch patternBasedPropertyIndex = new SolrSearch(boaPatternIndexUrl, boaPatternIndexSearchField, "nlr");
		
		//first BOA pattern then label based
//		property_index = new HierarchicalSolrSearch(patternBasedPropertyIndex, labelBasedPropertyIndex);
		
		//first label based then BOA pattern
		property_index = new HierarchicalSolrSearch(labelBasedPropertyIndex, patternBasedPropertyIndex);
		
		int maxIndexResults = Integer.parseInt(options.fetch("solr.query.limit"), 10);
		
		maxQueryExecutionTimeInSeconds = Integer.parseInt(options.get("sparql.query.maxExecutionTimeInSeconds", "20"));
		cache.setMaxExecutionTimeInSeconds(maxQueryExecutionTimeInSeconds);
		
		ranking = Ranking.valueOf(options.get("learning.ranking", "similarity").toUpperCase());
		useRemoteEndpointValidation = options.get("learning.validationType", "remote").equals("remote") ? true : false;
		stopIfQueryResultNotEmpty = Boolean.parseBoolean(options.get("learning.stopAfterFirstNonEmptyQueryResult", "true"));
		maxTestedQueriesPerTemplate = Integer.parseInt(options.get("learning.maxTestedQueriesPerTemplate", "20"));
		
		String wordnetPath = options.get("wordnet.dictionary", "tbsl/dict");
		wordnetPath = this.getClass().getClassLoader().getResource(wordnetPath).getPath();
		System.setProperty("wordnet.database.dir", wordnetPath);
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		Set<String> predicateFilters = new HashSet<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		modelGenenerator = new ModelGenerator(endpoint, predicateFilters);
		
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));
		reasoner.setCache(cache);
		reasoner.prepareSubsumptionHierarchy();
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
		learnedSPARQLQueries = new HashMap<String, Object>();
		resourcesURICache = new HashMap<String, SolrQueryResultSet>();
		classesURICache = new HashMap<String, SolrQueryResultSet>();
		propertiesURICache = new HashMap<String, SolrQueryResultSet>();
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
		
//		//generate SPARQL query candidates, but select only a fixed number per template
//		template2Queries = getSPARQLQueryCandidates(templates, ranking);
//		sparqlQueryCandidates = getNBestQueryCandidatesForTemplates(template2Queries);
		
		//get the weighted query candidates
		generatedQueries = getWeightedSPARQLQueries(templates);
		sparqlQueryCandidates = new ArrayList<Query>();
		int i = 0;
		for(WeightedQuery wQ : generatedQueries){
			System.out.println(wQ.explain());
			sparqlQueryCandidates.add(wQ.getQuery());
			if(i == maxTestedQueries){
				break;
			}
			i++;
		}
		
		//test candidates
		if(useRemoteEndpointValidation){ //on remote endpoint
			validateAgainstRemoteEndpoint(sparqlQueryCandidates);
		} else {//on local model
			validateAgainstLocalModel(sparqlQueryCandidates);
		}
		
	}
	
	public Set<WeightedQuery> getGeneratedQueries() {
		return generatedQueries;
	}
	
	public Set<WeightedQuery> getGeneratedQueries(int topN) {
		Set<WeightedQuery> topNQueries = new TreeSet<WeightedQuery>();
		int max = Math.min(topN, generatedQueries.size());
		for(WeightedQuery wQ : generatedQueries){
			topNQueries.add(wQ);
			if(topNQueries.size() == max){
				break;
			}
		}
		return topNQueries;
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
	
	/*
	private Set<WeightedQuery> getWeightedSPARQLQueries(Set<Template> templates){
		double alpha = 0.8;
		double beta = 1 - alpha;
		Map<Slot, Set<Allocation>> slot2Allocations = new HashMap<Slot, Set<Allocation>>();
		
		Set<WeightedQuery> allQueries = new TreeSet<WeightedQuery>();
		
		Set<Allocation> allAllocations;
		for(Template t : templates){
			allAllocations = new HashSet<Allocation>();
			
			for(Slot slot : t.getSlots()){
				Set<Allocation> allocations = computeAllocation(slot);
				allAllocations.addAll(allocations);
				slot2Allocations.put(slot, allocations);
			}
			
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for(Allocation a : allAllocations){
				if(a.getInDegree() < min){
					min = a.getInDegree();
				}
				if(a.getInDegree() > max){
					max = a.getInDegree();
				}
			}
			for(Allocation a : allAllocations){
				double prominence = a.getInDegree()/(max-min);
				a.setProminence(prominence);
				
				double score = alpha * a.getSimilarity() + beta * a.getProminence();
				a.setScore(score);
				
			}
//			System.out.println(allAllocations);
			
			Set<WeightedQuery> queries = new HashSet<WeightedQuery>();
			Query cleanQuery = t.getQuery();
			queries.add(new WeightedQuery(cleanQuery));
			
			Set<WeightedQuery> tmp = new HashSet<WeightedQuery>();
			List<Slot> sortedSlots = new ArrayList<Slot>();
			Set<Slot> classSlots = new HashSet<Slot>();
			for(Slot slot : t.getSlots()){
				if(slot.getSlotType() == SlotType.CLASS){
					sortedSlots.add(slot);
					classSlots.add(slot);
				}
			}
			for(Slot slot : t.getSlots()){
				if(!sortedSlots.contains(slot)){
					sortedSlots.add(slot);
				}
			}
			for(Slot slot : sortedSlots){
				if(!slot2Allocations.get(slot).isEmpty()){
					for(Allocation a : slot2Allocations.get(slot)){
						for(WeightedQuery query : queries){
								//check if the query is possible
								if(slot.getSlotType() == SlotType.SYMPROPERTY){
									Query reversedQuery = new Query(query.getQuery());
									reversedQuery.getTriplesWithVar(slot.getAnchor()).iterator().next().reverse();
									
									boolean drop = false;
									for(SPARQL_Triple triple : reversedQuery.getTriplesWithVar(slot.getAnchor())){
										String objectVar = triple.getValue().getName();
										String subjectVar = triple.getVariable().getName();
//										System.out.println(triple);
										for(SPARQL_Triple typeTriple : reversedQuery.getRDFTypeTriples(objectVar)){
//											System.out.println(typeTriple);
											Set<String> ranges = getRanges(a.getUri());
//											System.out.println(a);
											if(!ranges.isEmpty()){
												Set<String> allRanges = new HashSet<String>();
												for(String range : ranges){
													allRanges.addAll(getSuperClasses(range));
												}
												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
												Set<String> allTypes = getSuperClasses(typeURI);
												allTypes.add(typeTriple.getValue().getName());
//												System.out.println("RANGES: " + ranges);
//												System.out.println("TYPES: " + allTypes);
												
												if(!org.mindswap.pellet.utils.SetUtils.intersects(allRanges, allTypes)){
													drop = true;
												} else {
													System.out.println("DROPPING: \n" + reversedQuery.toString());
												}
											}
										}
										for(SPARQL_Triple typeTriple : reversedQuery.getRDFTypeTriples(subjectVar)){
//											System.out.println(typeTriple);
											Set<String> domains = getDomains(a.getUri());
//											System.out.println(a);
											if(!domains.isEmpty()){
												Set<String> allDomains = new HashSet<String>();
												for(String domain : domains){
													allDomains.addAll(getSuperClasses(domain));
												}
												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
												Set<String> allTypes = getSuperClasses(typeURI);
												allTypes.add(typeTriple.getValue().getName());
//												System.out.println("DOMAINS: " + domains);
//												System.out.println("TYPES: " + allTypes);
												
												if(!org.mindswap.pellet.utils.SetUtils.intersects(allDomains, allTypes)){
													drop = true;
												} else {
													System.out.println("DROPPING: \n" + reversedQuery.toString());
												}
											}
										}
									}
								
									if(!drop){
										reversedQuery.replaceVarWithURI(slot.getAnchor(), a.getUri());
										WeightedQuery w = new WeightedQuery(reversedQuery);
										double newScore = query.getScore() + a.getScore();
										w.setScore(newScore);
										tmp.add(w);
									}
								
							}
								Query q = new Query(query.getQuery());
								
								boolean drop = false;
								if(slot.getSlotType() == SlotType.PROPERTY || slot.getSlotType() == SlotType.SYMPROPERTY){
									for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
										String objectVar = triple.getValue().getName();
										String subjectVar = triple.getVariable().getName();
//										System.out.println(triple);
										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(objectVar)){
//											System.out.println(typeTriple);
											Set<String> ranges = getRanges(a.getUri());
//											System.out.println(a);
											if(!ranges.isEmpty()){
												Set<String> allRanges = new HashSet<String>();
												for(String range : ranges){
													allRanges.addAll(getSuperClasses(range));
												}
												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
												Set<String> allTypes = getSuperClasses(typeURI);
												allTypes.add(typeTriple.getValue().getName());
//												System.out.println("RANGES: " + ranges);
//												System.out.println("TYPES: " + allTypes);
												
												if(!org.mindswap.pellet.utils.SetUtils.intersects(allRanges, allTypes)){
													drop = true;
												} else {
													System.out.println("DROPPING: \n" + q.toString());
												}
											}
										}
										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(subjectVar)){
//											System.out.println(typeTriple);
											Set<String> domains = getDomains(a.getUri());
//											System.out.println(a);
											if(!domains.isEmpty()){
												Set<String> allDomains = new HashSet<String>();
												for(String domain : domains){
													allDomains.addAll(getSuperClasses(domain));
												}
												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
												Set<String> allTypes = getSuperClasses(typeURI);
												allTypes.add(typeTriple.getValue().getName());
//												System.out.println("DOMAINS: " + domains);
//												System.out.println("TYPES: " + allTypes);
												
												if(!org.mindswap.pellet.utils.SetUtils.intersects(allDomains, allTypes)){
													drop = true;
												} else {
													System.out.println("DROPPING: \n" + q.toString());
												}
											}
										}
									}
								}
								
								
								if(!drop){
									q.replaceVarWithURI(slot.getAnchor(), a.getUri());
									WeightedQuery w = new WeightedQuery(q);
									double newScore = query.getScore() + a.getScore();
									w.setScore(newScore);
									tmp.add(w);
								}
								
							
						}
					}
					queries.clear();
					queries.addAll(tmp);System.out.println(tmp);
					tmp.clear();
				}
				
			}
			for(WeightedQuery q : queries){
				q.setScore(q.getScore()/t.getSlots().size());
			}
			allQueries.addAll(queries);
			List<Query> qList = new ArrayList<Query>();
			for(WeightedQuery wQ : queries){//System.err.println(wQ.getQuery());
				qList.add(wQ.getQuery());
			}
			template2Queries.put(t, qList);
		}
		return allQueries;
	}
	*/
	
	private void normProminenceValues(Set<Allocation> allocations){
		double min = 0;
		double max = 0;
		for(Allocation a : allocations){
			if(a.getProminence() < min){
				min = a.getProminence();
			}
			if(a.getProminence() > max){
				max = a.getProminence();
			}
		}
		for(Allocation a : allocations){
			double prominence = a.getProminence()/(max-min);
			a.setProminence(prominence);
		}
	}
	
	private void computeScore(Set<Allocation> allocations){
		double alpha = 0.8;
		double beta = 1 - alpha;
		
		for(Allocation a : allocations){
			double score = alpha * a.getSimilarity() + beta * a.getProminence();
			a.setScore(score);
		}
		
	}
	
	private Set<WeightedQuery> getWeightedSPARQLQueries(Set<Template> templates){
		
		Map<Slot, Set<Allocation>> slot2Allocations = new HashMap<Slot, Set<Allocation>>();
		
		Set<WeightedQuery> allQueries = new TreeSet<WeightedQuery>();
		
		Set<Allocation> allocations;
		for(Template t : templates){
			allocations = new TreeSet<Allocation>();
			
			for(Slot slot : t.getSlots()){
				allocations = computeAllocations(slot, 50);
				
				slot2Allocations.put(slot, allocations);
				
				//for tests add the property URI with http://dbpedia.org/property/ namespace
				//TODO should be replaced by usage of a separate SOLR index
				Set<Allocation> tmp = new HashSet<Allocation>();
				if(slot.getSlotType() == SlotType.PROPERTY || slot.getSlotType() == SlotType.SYMPROPERTY){
					for(Allocation a : allocations){
						String uri = "http://dbpedia.org/property/" + a.getUri().substring(a.getUri().lastIndexOf("/")+1);
						Allocation newA = new Allocation(uri, a.getSimilarity(), a.getProminence());
						newA.setScore(a.getScore()-0.000001);
						tmp.add(newA);
					}
				}
				allocations.addAll(tmp);
			}
			
			
			Set<WeightedQuery> queries = new HashSet<WeightedQuery>();
			Query cleanQuery = t.getQuery();
			queries.add(new WeightedQuery(cleanQuery));
			
			Set<WeightedQuery> tmp = new TreeSet<WeightedQuery>();
			List<Slot> sortedSlots = new ArrayList<Slot>();
			Set<Slot> classSlots = new HashSet<Slot>();
			for(Slot slot : t.getSlots()){
				if(slot.getSlotType() == SlotType.CLASS){
					sortedSlots.add(slot);
					classSlots.add(slot);
				}
			}
			for(Slot slot : t.getSlots()){
				if(!sortedSlots.contains(slot)){
					sortedSlots.add(slot);
				}
			}
			//add for each SYMPROPERTY Slot the reversed query
			for(Slot slot : sortedSlots){
				for(WeightedQuery wQ : queries){
					if(slot.getSlotType() == SlotType.SYMPROPERTY){
						Query reversedQuery = new Query(wQ.getQuery());
						reversedQuery.getTriplesWithVar(slot.getAnchor()).iterator().next().reverse();
						tmp.add(new WeightedQuery(reversedQuery));
					}
					tmp.add(wQ);
				}
				queries.clear();
				queries.addAll(tmp);
				tmp.clear();
			}
			
			for(Slot slot : sortedSlots){
				if(!slot2Allocations.get(slot).isEmpty()){System.out.println(slot2Allocations.get(slot));
					for(Allocation a : slot2Allocations.get(slot)){
						for(WeightedQuery query : queries){
								Query q = new Query(query.getQuery());
								
								boolean drop = false;
								if(slot.getSlotType() == SlotType.PROPERTY || slot.getSlotType() == SlotType.SYMPROPERTY){
									for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
										String objectVar = triple.getValue().getName();
										String subjectVar = triple.getVariable().getName();
//										System.out.println(triple);
										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(objectVar)){
//											System.out.println(typeTriple);
											if(isObjectProperty(a.getUri())){
												Set<String> ranges = getRanges(a.getUri());
//												System.out.println(a);
												if(!ranges.isEmpty()){
													Set<String> allRanges = new HashSet<String>();
													for(String range : ranges){
														allRanges.addAll(getSuperClasses(range));
													}
													allRanges.addAll(ranges);
													allRanges.remove("http://www.w3.org/2002/07/owl#Thing");
													String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
													Set<String> allTypes = getSuperClasses(typeURI);
													allTypes.add(typeURI);
//													if(typeURI.equals("http://dbpedia.org/ontology/Organisation") && a.getUri().equals("http://dbpedia.org/ontology/developer")){
//														System.out.println("RANGES: " + allRanges);
//														System.out.println("TYPES: " + allTypes);
//													}
													
													if(!org.mindswap.pellet.utils.SetUtils.intersects(allRanges, allTypes)){
														drop = true;
//														if(typeURI.equals("http://dbpedia.org/ontology/Organisation") && a.getUri().equals("http://dbpedia.org/ontology/developer") && q.toString().contains("/Software>")){
//															System.out.println("RANGES: " + allRanges);
//															System.out.println("TYPES: " + allTypes);
//															System.out.println("DROPPING: \n" + q.toString());
//														}
													} else {
														
													}
												}
											} else {
												drop = true;
											}
											
										}
										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(subjectVar)){
//											System.out.println(typeTriple);
											Set<String> domains = getDomains(a.getUri());
//											System.out.println(a);
											if(!domains.isEmpty()){
												Set<String> allDomains = new HashSet<String>();
												for(String domain : domains){
													allDomains.addAll(getSuperClasses(domain));
												}
												allDomains.addAll(domains);
												allDomains.remove("http://www.w3.org/2002/07/owl#Thing");
												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
												Set<String> allTypes = getSuperClasses(typeURI);
												allTypes.add(typeTriple.getValue().getName());
//												if(typeURI.equals("http://dbpedia.org/ontology/Organisation") && a.getUri().equals("http://dbpedia.org/ontology/developer")){
//													System.out.println("DOMAINS: " + allDomains);
//													System.out.println("TYPES: " + allTypes);
//												}
												
												if(!org.mindswap.pellet.utils.SetUtils.intersects(allDomains, allTypes)){
													drop = true;
//													System.out.println("DROPPING: \n" + q.toString());
												} else {
														
												}
											}
										}
									}
								}
								
								
								if(!drop){
									q.replaceVarWithURI(slot.getAnchor(), a.getUri());
									WeightedQuery w = new WeightedQuery(q);
									double newScore = query.getScore() + a.getScore();
									w.setScore(newScore);
									w.addAllocations(query.getAllocations());
									w.addAllocation(a);
									tmp.add(w);
								}
								
							
						}
					}
					queries.clear();
					queries.addAll(tmp);//System.out.println(tmp);
					tmp.clear();
				}
				
			}
			for(WeightedQuery q : queries){
				q.setScore(q.getScore()/t.getSlots().size());
			}
			allQueries.addAll(queries);
			List<Query> qList = new ArrayList<Query>();
			for(WeightedQuery wQ : queries){//System.err.println(wQ.getQuery());
				qList.add(wQ.getQuery());
			}
			template2Queries.put(t, qList);
		}
		return allQueries;
	}

/*
 * for(SPARQL_Triple triple : t.getQuery().getTriplesWithVar(slot.getAnchor())){System.out.println(triple);
										for(SPARQL_Triple typeTriple : t.getQuery().getRDFTypeTriples(triple.getVariable().getName())){
											System.out.println(typeTriple);
											for(Allocation a : allocations){
												Set<String> domains = getDomains(a.getUri());
												System.out.println(a);
												System.out.println(domains);
												for(Slot s : classSlots){
													if(s.getAnchor().equals(triple.getVariable().getName())){
														for(Allocation all : slot2Allocations.get(s)){
															if(!domains.contains(all.getUri())){
																System.out.println("DROP " + a);
															}
														}
													}
												}
											}
											
											
										}
 */
	
	private SortedSet<Allocation> computeAllocations(Slot slot){
		SortedSet<Allocation> allocations = new TreeSet<Allocation>();
		
		SolrSearch index = getIndexBySlotType(slot);
		
		SolrQueryResultSet rs;
		for(String word : slot.getWords()){
			if(slot.getSlotType() == SlotType.RESOURCE){
				rs = index.getResourcesWithScores(word, 250);
			} else {
				rs = index.getResourcesWithScores(word, 30);
			}
			
			
			//debugging
//			for(Iterator<SolrQueryResultItem> iter = rs.getItems().iterator();iter.hasNext();){
//				SolrQueryResultItem item = iter.next();
//				if(exclusions.contains(item.getUri())){
//					iter.remove();
//				}
//			}
			
			System.out.println(word + "->" + rs);
			for(SolrQueryResultItem item : rs.getItems()){
				double similarity = Similarity.getSimilarity(word, item.getLabel());
				//get the labels of the redirects and compute the highest similarity
				if(slot.getSlotType() == SlotType.RESOURCE){
					Set<String> labels = getRedirectLabels(item.getUri());
					for(String label : labels){
						double tmp = Similarity.getSimilarity(word, label);
						if(tmp > similarity){
							similarity = tmp;
						}
					}
				}
				double prominence = getProminenceValue(item.getUri(), slot.getSlotType());
				allocations.add(new Allocation(item.getUri(), prominence, similarity));
			}
			
		}
		
		normProminenceValues(allocations);
		
		computeScore(allocations);
		return new TreeSet<Allocation>(allocations);
	}
	
	private Set<Allocation> computeAllocations(Slot slot, int limit){
		SortedSet<Allocation> allocations = computeAllocations(slot);
		
		if(allocations.isEmpty()){
			return allocations;
		}
		
		ArrayList<Allocation> l = new ArrayList<Allocation>(allocations);
		Collections.sort(l, new Comparator<Allocation>() {

			@Override
			public int compare(Allocation o1, Allocation o2) {
				double dif = o1.getScore() - o2.getScore();
				if(dif < 0){
					return 1;
				} else if(dif > 0){
					return -1;
				} else {
					return o1.getUri().compareTo(o2.getUri());
				}
			}
		});
		
		return new TreeSet<Allocation>(l.subList(0, Math.min(limit, allocations.size())));
	}
	
	private Set<String> getRedirectLabels(String uri){
		Set<String> labels = new HashSet<String>();
		String query = String.format("SELECT ?label WHERE {?s <http://dbpedia.org/ontology/wikiPageRedirects> <%s>. ?s <%s> ?label.}", uri, RDFS.label.getURI());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			labels.add(qs.getLiteral("label").getLexicalForm());
			
		}
		return labels;
	}
	
	private double getProminenceValue(String uri, SlotType type){
		int cnt = 1;
		String query = null;
		if(type == SlotType.CLASS){
			query = "SELECT COUNT(?s) WHERE {?s a <%s>}";
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			query = "SELECT COUNT(*) WHERE {?s <%s> ?o}";
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			query = "SELECT COUNT(*) WHERE {?s ?p <%s>}";
		}
		query = String.format(query, uri);
		
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		String projectionVar;
		while(rs.hasNext()){
			qs = rs.next();
			projectionVar = qs.varNames().next();
			cnt = qs.get(projectionVar).asLiteral().getInt();
		}
//		if(cnt == 0){
//			return 0;
//		} 
//		return Math.log(cnt);
		return cnt;
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
				for(SolrQueryResultItem item : getCandidateURIsWithScore(slot).getItems()){
					for(Query query : queries){
						Query newQuery = new Query(query);
						newQuery.replaceVarWithURI(var, item.getUri());
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
				for(SolrQueryResultItem item : getCandidateURIsWithScore(slot).getItems()){
					for(Entry<String, Float> entry2 : query2Score.entrySet()){
						tmp.put(entry2.getKey().replace("?" + var, "<" + item.getUri() + ">"), item.getScore() + entry2.getValue());
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
				for(SolrQueryResultItem item : getCandidateURIsWithScore(slot).getItems()){
					for(RatedQuery rQ : ratedQueries){
						RatedQuery newRQ = new RatedQuery(rQ, rQ.getScore());
						newRQ.replaceVarWithURI(var, item.getUri());
						newRQ.setScore(newRQ.getScore() + item.getScore());
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
//					for(Entry<String, String> uri2prefix : prefixMap.entrySet()){
//						if(uri.startsWith(uri2prefix.getKey())){
//							prefix = new SPARQL_Prefix(uri2prefix.getValue(), uri2prefix.getKey());
//							uri = uri.replace(uri2prefix.getKey(), uri2prefix.getValue() + ":");
//							break;
//						} 
//					}
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
	
	private SolrQueryResultSet getCandidateURIs(Slot slot, int limit){
		logger.info("Generating candidate URIs for " + slot.getWords() + "...");
		mon.start();
		SolrSearch index = null;
		if(slot.getSlotType() == SlotType.CLASS){
			index = class_index;
		} else if(slot.getSlotType() == SlotType.PROPERTY){
			index = property_index;
		} else if(slot.getSlotType() == SlotType.RESOURCE){
			index = resource_index;
		}
		SolrQueryResultSet rs = new SolrQueryResultSet();
		for(String word : slot.getWords()){
			rs.add(index.getResourcesWithScores(word, limit));
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		return rs;
	}
	
	private List<String> getCandidateURIsSortedBySimilarity(Slot slot){
		logger.info("Generating URI candidates for " + slot.getWords() + "...");
		mon.start();
		List<String> sortedURIs = new ArrayList<String>();
		//get the appropriate index based on slot type
		SolrSearch index = getIndexBySlotType(slot);
		//get the appropriate cache for URIs to avoid redundant queries to index
		Map<String, SolrQueryResultSet> uriCache = getCacheBySlotType(slot);
		
		SortedSet<SolrQueryResultItem> tmp;
		SolrQueryResultSet rs;
		
		//prune the word list only when slot type is not RESOURCE
		List<String> words;
		if(slot.getSlotType() == SlotType.RESOURCE){
			words = slot.getWords();
		} else {
//			words = pruneList(slot.getWords());//getLemmatizedWords(slot.getWords());
			words = pruneList(slot.getWords());
		}
		
		for(String word : words){
			tmp = new TreeSet<SolrQueryResultItem>(new SolrQueryResultStringSimilarityComparator(word));
			rs = uriCache.get(word);
			
			if(rs == null){
				rs = index.getResourcesWithScores(word, 50);
				uriCache.put(word, rs);
			}
		
			tmp.addAll(rs.getItems());
			
			for(SolrQueryResultItem item : tmp){
				if(!sortedURIs.contains(item.getUri())){
					sortedURIs.add(item.getUri());
				}
				if(sortedURIs.size() == MAX_URIS_PER_SLOT){
					break;
				}
				
			}
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
					if(w1.contains(w2)){
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
	
	private Map<String, SolrQueryResultSet> getCacheBySlotType(Slot slot){
		Map<String, SolrQueryResultSet> cache = null;
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
	
	private SolrQueryResultSet getCandidateURIsWithScore(Slot slot){
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
		SolrQueryResultSet resultSet = new SolrQueryResultSet();
		for(String word : slot.getWords()){
			resultSet.add(index.getResourcesWithScores("label:" + word, sorted));
		}
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Candidate URIs: " + uri2Score.keySet());
		return resultSet;
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
		SPARQL_QueryType queryType = SPARQL_QueryType.SELECT;
		for(Query query : queries){
			if(query.getQt() == SPARQL_QueryType.ASK){
				queryType = SPARQL_QueryType.ASK;
			} else if(query.getQt() == SPARQL_QueryType.SELECT){
				queryType = SPARQL_QueryType.SELECT;
			}
			queryStrings.add(query.toString());
		}
		validateAgainstRemoteEndpoint(queryStrings, queryType);
	}
	
	private void validateAgainstRemoteEndpoint(List<String> queries, SPARQL_QueryType queryType){
		logger.info("Testing candidate SPARQL queries on remote endpoint...");
		mon.start();
		if(queryType == SPARQL_QueryType.SELECT){
			for(String query : queries){
				logger.info("Testing query:\n" + query);
				List<String> results = getResultFromRemoteEndpoint(query);
				if(!results.isEmpty()){
					try{
						int cnt = Integer.parseInt(results.get(0));
						if(cnt > 0){learnedPos = queries.indexOf(query);
							learnedSPARQLQueries.put(query, results);
							if(stopIfQueryResultNotEmpty){
								return;
							}
						}
					} catch (NumberFormatException e){
						learnedSPARQLQueries.put(query, results);
						learnedPos = queries.indexOf(query);
						if(stopIfQueryResultNotEmpty){
							return;
						}
					}
					
				}
				logger.info("Result: " + results);
			}
		} else if(queryType == SPARQL_QueryType.ASK){
			for(String query : queries){
				logger.info("Testing query:\n" + query);
				boolean result = executeAskQuery(query);
				learnedSPARQLQueries.put(query, result);
//				if(stopIfQueryResultNotEmpty && result){
//					return;
//				}
				if(stopIfQueryResultNotEmpty){learnedPos = queries.indexOf(query);
					return;
				}
				logger.info("Result: " + result);
			}
		}
		
		mon.stop();
		logger.info("Done in " + mon.getLastValue() + "ms.");
	}
	
	private boolean executeAskQuery(String query){
		QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for(String uri : endpoint.getDefaultGraphURIs()){
			qe.addDefaultGraph(uri);
		}
		boolean ret = qe.execAsk();
		return ret;
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
			String queryString = query;
			if(!query.contains("LIMIT") && !query.contains("ASK")){
				queryString = query + " LIMIT 10";
			}
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, queryString));
			QuerySolution qs;
			String projectionVar;
			while(rs.hasNext()){
				qs = rs.next();
				projectionVar = qs.varNames().next();
				if(qs.get(projectionVar).isLiteral()){
					resources.add(qs.get(projectionVar).asLiteral().getLexicalForm());
				} else if(qs.get(projectionVar).isURIResource()){
					resources.add(qs.get(projectionVar).asResource().getURI());
				}
				
			}
		} catch (Exception e) {e.printStackTrace();
			logger.error("Query execution failed.", e);
		}
		return resources;
	}
	
	public int getLearnedPosition() {
		if(learnedPos >= 0){
			return learnedPos+1;
		}
		return learnedPos;
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
	
	private Set<String> getDomains(String property){
		String tmp = property;
		if(property.startsWith("http://dbpedia.org/property/")){
			tmp = "http://dbpedia.org/ontology" + property.substring(property.lastIndexOf("/"));
		}
		Set<String> domains = new HashSet<String>();
		String query = String.format("SELECT ?domain WHERE {<%s> <%s> ?domain}", tmp, RDFS.domain.getURI());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(qs.getResource("domain").getURI());
		}
		return domains;
	}
	
	private Set<String> getRanges(String property){
		String tmp = property;
		if(property.startsWith("http://dbpedia.org/property/")){
			tmp = "http://dbpedia.org/ontology" + property.substring(property.lastIndexOf("/"));
		}
		Set<String> ranges = new HashSet<String>();
		String query = String.format("SELECT ?range WHERE {<%s> <%s> ?range}", tmp, RDFS.range.getURI());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			ranges.add(qs.getResource("range").getURI());
		}
		
		return ranges;
	}
	
	private boolean isObjectProperty(String property){
		String query = String.format("SELECT * WHERE {<%s> a <%s>}", property, OWL.ObjectProperty.getURI());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		if(rs.hasNext()){
			return true;
		}
		return false;
	}
	
	private Set<String> getSuperClasses(String cls){
		Set<String> superClasses = new HashSet<String>();
		
		for(Description d : reasoner.getClassHierarchy().getSuperClasses((new NamedClass(cls)))){
			superClasses.add(((NamedClass)d).getName());
			for(Description sup : reasoner.getClassHierarchy().getSuperClasses(d)){
				superClasses.add(((NamedClass)sup).getName());
			}
			
		}
		return superClasses;
	}
	
	
	

	/**
	 * @param args
	 * @throws NoTemplateFoundException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidFileFormatException 
	 */
	public static void main(String[] args) throws NoTemplateFoundException, InvalidFileFormatException, FileNotFoundException, IOException {
//		Logger.getLogger(DefaultHttpParams.class).setLevel(Level.OFF);
//		Logger.getLogger(HttpClient.class).setLevel(Level.OFF);
//		Logger.getLogger(HttpMethodBase.class).setLevel(Level.OFF);
//		String question = "Who/WP was/VBD the/DT wife/NN of/IN president/NN Lincoln/NNP";
//		String question = "Who/WP produced/VBD the/DT most/JJS films/NNS";
//		String question = "Which/WDT country/NN does/VBZ the/DT Airedale/NNP Terrier/NNP come/VBP from/IN";
//		String question = "When/WRB was/VBD Capcom/NNP founded/VBD";
//		String question = "Which/WDT organizations/NNS were/VBD founded/VBN in/IN 1950/CD";
//		String question = "Is/VBZ there/RB a/DT video/NN game/NN called/VBN Battle/NNP Chess/NNP";
//		String question = "Which/WDT software/NN has/VBZ been/VBN developed/VBN by/IN organizations/NNS founded/VBN in/IN California/NNP";
//		String question = "How/WRB many/JJ films/NNS did/VBD Leonardo/NNP DiCaprio/NNP star/VB in/IN";
//		String question = "Which/WDT music/NN albums/NNS contain/VBP the/DT song/NN Last/NNP Christmas/NNP";
//		String question = "Which/WDT companies/NNS are/VBP located/VBN in/IN California/NNP USA/NNP";
//		String question = "Who/WP wrote/VBD the/DT book/NN The/NNP pillars/NNP of/NNP the/NNP Earth/NNP";
		String question = "Give me all books written by Dan Brown";
		SPARQLTemplateBasedLearner learner = new SPARQLTemplateBasedLearner();//learner.setUseIdealTagger(true);
//		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
//				Collections.<String>singletonList(""), Collections.<String>emptyList());
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
				Collections.<String>singletonList(""), Collections.<String>emptyList());
		learner.setEndpoint(endpoint);
		learner.setQuestion(question);
		learner.learnSPARQLQueries();
		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		System.out.println(learner.getLearnedPosition());
		
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

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LearningProblem getLearningProblem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLearningProblem(LearningProblem learningProblem) {
		// TODO Auto-generated method stub
		
	}

	

}