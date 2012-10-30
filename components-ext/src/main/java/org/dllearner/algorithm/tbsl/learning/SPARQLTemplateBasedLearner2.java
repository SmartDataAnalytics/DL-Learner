package org.dllearner.algorithm.tbsl.learning;

import hmm.HiddenMarkovModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections15.MultiMap;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.nlp.Lemmatizer;
import org.dllearner.algorithm.tbsl.nlp.LingPipeLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PlingStemmer;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.sparql.Allocation;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_QueryType;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.sparql.WeightedQuery;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.algorithm.tbsl.util.PopularityMap;
import org.dllearner.algorithm.tbsl.util.PopularityMap.EntityType;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.IndexResultItem;
import org.dllearner.common.index.IndexResultSet;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import org.dllearner.common.index.SPARQLPropertiesIndex;
import org.dllearner.common.index.VirtuosoDatatypePropertiesIndex;
import org.dllearner.common.index.VirtuosoObjectPropertiesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.SparqlQueryLearningAlgorithm;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.SPARQLReasoner;
import org.ini4j.Options;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SPARQLTemplateBasedLearner2 implements SparqlQueryLearningAlgorithm{

	enum Mode{
		BEST_QUERY, BEST_NON_EMPTY_QUERY
	}

	private Mode mode = Mode.BEST_QUERY;

	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner2.class);
	private Monitor templateMon = MonitorFactory.getTimeMonitor("template");
	private Monitor sparqlMon = MonitorFactory.getTimeMonitor("sparql");

	private boolean useRemoteEndpointValidation;
	private boolean stopIfQueryResultNotEmpty;
	private int maxTestedQueriesPerTemplate = 50;
	private int maxQueryExecutionTimeInSeconds;
	private int maxTestedQueries = 200;
	private int maxIndexResults;

	private SparqlEndpoint endpoint = null;
	private Model model = null;

	private ExtractionDBCache cache = new ExtractionDBCache("cache");

	private Index resourcesIndex;
	private Index classesIndex;
	private Index propertiesIndex;

	private Index datatypePropertiesIndex;
	private Index objectPropertiesIndex;

	private MappingBasedIndex mappingIndex;

	private Templator templateGenerator = null;
	private Lemmatizer lemmatizer;
	private PartOfSpeechTagger posTagger;
	private WordNet wordNet;

	private String question;
	private int learnedPos = -1;

	private Set<Template> templates;
	private Map<Template, Collection<? extends Query>> template2Queries;
	private Map<Slot, List<String>> slot2URI;

	private Collection<WeightedQuery> sparqlQueryCandidates;
	private SortedSet<WeightedQuery> learnedSPARQLQueries;
	private SortedSet<WeightedQuery> generatedQueries;

	private SPARQLReasoner reasoner;

	private String currentlyExecutedQuery;

	private boolean dropZeroScoredQueries = true;
	private boolean useManualMappingsIfExistOnly = true;

	private boolean multiThreaded = true;

	private String [] grammarFiles = new String[]{"tbsl/lexicon/english.lex"};

	private PopularityMap popularityMap;

	private Set<String> relevantKeywords;

	private boolean useDomainRangeRestriction = true;

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex){
		this(endpoint, resourcesIndex, classesIndex, propertiesIndex, new StanfordPartOfSpeechTagger());
	}

	public SPARQLTemplateBasedLearner2(Knowledgebase knowledgebase, PartOfSpeechTagger posTagger, WordNet wordNet, Options options){
		this(knowledgebase.getEndpoint(), knowledgebase.getResourceIndex(), knowledgebase.getClassIndex(),knowledgebase.getPropertyIndex(), posTagger, wordNet, options);
	}

	public SPARQLTemplateBasedLearner2(Knowledgebase knowledgebase){
		this(knowledgebase.getEndpoint(), knowledgebase.getResourceIndex(), knowledgebase.getClassIndex(),knowledgebase.getPropertyIndex(), new StanfordPartOfSpeechTagger(), new WordNet(), new Options());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index index){
		this(endpoint, index, new StanfordPartOfSpeechTagger());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger){
		this(endpoint, resourcesIndex, classesIndex, propertiesIndex, posTagger, new WordNet(), new Options());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index index, PartOfSpeechTagger posTagger){
		this(endpoint, index, posTagger, new WordNet(), new Options());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex, WordNet wordNet){
		this(endpoint, resourcesIndex, classesIndex, propertiesIndex, new StanfordPartOfSpeechTagger(), wordNet, new Options());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index index, WordNet wordNet){
		this(endpoint, index, new StanfordPartOfSpeechTagger(), wordNet, new Options());
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger, WordNet wordNet){
		this(endpoint, resourcesIndex, classesIndex, propertiesIndex, posTagger, wordNet, new Options(), new ExtractionDBCache("cache"));
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index index, PartOfSpeechTagger posTagger, WordNet wordNet){
		this(endpoint, index, index, index, posTagger, wordNet, new Options(), new ExtractionDBCache("cache"));
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger, WordNet wordNet, Options options){
		this(endpoint, resourcesIndex, classesIndex, propertiesIndex, posTagger, wordNet, options, new ExtractionDBCache("cache"));
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index index, PartOfSpeechTagger posTagger, WordNet wordNet, Options options){
		this(endpoint, index, index, index, posTagger, wordNet, options, new ExtractionDBCache("cache"));
	}

	public SPARQLTemplateBasedLearner2(SparqlEndpoint endpoint, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger, WordNet wordNet, Options options, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.resourcesIndex = resourcesIndex;
		this.classesIndex = classesIndex;
		this.propertiesIndex = propertiesIndex;
		this.posTagger = posTagger;
		this.wordNet = wordNet;
		this.cache = cache;

		setOptions(options);

		if(propertiesIndex instanceof SPARQLPropertiesIndex){
			if(propertiesIndex instanceof VirtuosoPropertiesIndex){
				datatypePropertiesIndex = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			} else {
				datatypePropertiesIndex = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			}
		} else {
			datatypePropertiesIndex = propertiesIndex;
			objectPropertiesIndex = propertiesIndex;
		}
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cache);
	}

	public SPARQLTemplateBasedLearner2(Model model, Index resourcesIndex, Index classesIndex, Index propertiesIndex){
		this(model, resourcesIndex, classesIndex, propertiesIndex, new StanfordPartOfSpeechTagger());
	}

	public SPARQLTemplateBasedLearner2(Model model, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger){
		this(model, resourcesIndex, classesIndex, propertiesIndex, posTagger, new WordNet(), new Options());
	}

	public SPARQLTemplateBasedLearner2(Model model, Index resourcesIndex, Index classesIndex, Index propertiesIndex, WordNet wordNet){
		this(model, resourcesIndex, classesIndex, propertiesIndex, new StanfordPartOfSpeechTagger(), wordNet, new Options());
	}

	public SPARQLTemplateBasedLearner2(Model model, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger, WordNet wordNet, Options options){
		this(model, resourcesIndex, classesIndex, propertiesIndex, posTagger, wordNet, options, new ExtractionDBCache("cache"));
	}

	public SPARQLTemplateBasedLearner2(Model model, MappingBasedIndex mappingBasedIndex, PartOfSpeechTagger posTagger)
	{
		this(model, new SPARQLIndex(model),new SPARQLIndex(model),new SPARQLIndex(model),posTagger);
		setMappingIndex(mappingBasedIndex);
	}

	public SPARQLTemplateBasedLearner2(Model model, Index resourcesIndex, Index classesIndex, Index propertiesIndex, PartOfSpeechTagger posTagger, WordNet wordNet, Options options, ExtractionDBCache cache){
		this.model = model;
		this.resourcesIndex = resourcesIndex;
		this.classesIndex = classesIndex;
		this.propertiesIndex = propertiesIndex;
		this.posTagger = posTagger;
		this.wordNet = wordNet;
		this.cache = cache;

		setOptions(options);

		if(propertiesIndex instanceof SPARQLPropertiesIndex){
			if(propertiesIndex instanceof VirtuosoPropertiesIndex){
				datatypePropertiesIndex = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			} else {
				datatypePropertiesIndex = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			}
		} else {
			datatypePropertiesIndex = propertiesIndex;
			objectPropertiesIndex = propertiesIndex;
		}
		reasoner = new SPARQLReasoner(new LocalModelBasedSparqlEndpointKS(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM, model)), cache);
	}

	public void setGrammarFiles(String[] grammarFiles)
	{
		if(templateGenerator==null) {throw new AssertionError("Learner not initialized. Please call init();");}
		templateGenerator.setGrammarFiles(grammarFiles);
	}

	@Override
	public void init() throws ComponentInitException {
		templateGenerator = new Templator(posTagger, wordNet, grammarFiles);
		lemmatizer = new LingPipeLemmatizer();
	}

	public void setMappingIndex(MappingBasedIndex mappingIndex) {
		this.mappingIndex = mappingIndex;
	}

	public void setCache(ExtractionDBCache cache) {
		this.cache = cache;
	}

	public void setKnowledgebase(Knowledgebase knowledgebase){
		this.endpoint = knowledgebase.getEndpoint();
		this.resourcesIndex = knowledgebase.getResourceIndex();
		this.classesIndex = knowledgebase.getClassIndex();
		this.propertiesIndex = knowledgebase.getPropertyIndex();
		this.mappingIndex = knowledgebase.getMappingIndex();
		if(propertiesIndex instanceof SPARQLPropertiesIndex){
			if(propertiesIndex instanceof VirtuosoPropertiesIndex){
				datatypePropertiesIndex = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			} else {
				datatypePropertiesIndex = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
				objectPropertiesIndex = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)propertiesIndex);
			}
		} else {
			datatypePropertiesIndex = propertiesIndex;
			objectPropertiesIndex = propertiesIndex;
		}
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));
	}

	public void setUseDomainRangeRestriction(boolean useDomainRangeRestriction) {
		this.useDomainRangeRestriction = useDomainRangeRestriction;
	}

	/*
	 * Only for Evaluation useful.
	 */
	public void setUseIdealTagger(boolean value){
		templateGenerator.setUNTAGGED_INPUT(!value);
	}

	private void setOptions(Options options){
		maxIndexResults = Integer.parseInt(options.get("solr.query.limit", "10"));

		maxQueryExecutionTimeInSeconds = Integer.parseInt(options.get("sparql.query.maxExecutionTimeInSeconds", "20"));
		cache.setMaxExecutionTimeInSeconds(maxQueryExecutionTimeInSeconds);

		useRemoteEndpointValidation = options.get("learning.validationType", "remote").equals("remote") ? true : false;
		stopIfQueryResultNotEmpty = Boolean.parseBoolean(options.get("learning.stopAfterFirstNonEmptyQueryResult", "true"));
		maxTestedQueriesPerTemplate = Integer.parseInt(options.get("learning.maxTestedQueriesPerTemplate", "20"));

		String wordnetPath = options.get("wordnet.dictionary", "tbsl/dict");
		wordnetPath = this.getClass().getClassLoader().getResource(wordnetPath).getPath();
		System.setProperty("wordnet.database.dir", wordnetPath);
	}

	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;

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

	private void reset(){
		learnedSPARQLQueries = new TreeSet<WeightedQuery>();
		template2Queries = new HashMap<Template, Collection<? extends Query>>();
		slot2URI = new HashMap<Slot, List<String>>();
		relevantKeywords = new HashSet<String>();
		currentlyExecutedQuery = null;

		//		templateMon.reset();
		//		sparqlMon.reset();
	}

	public void learnSPARQLQueries() throws NoTemplateFoundException{
		reset();
		//generate SPARQL query templates
		logger.debug("Generating SPARQL query templates...");
		templateMon.start();
		if(multiThreaded){
			templates = templateGenerator.buildTemplatesMultiThreaded(question);
		} else {
			templates = templateGenerator.buildTemplates(question);
		}
		templateMon.stop();
		logger.debug("Done in " + templateMon.getLastValue() + "ms.");
		relevantKeywords.addAll(templateGenerator.getUnknownWords());
		if(templates.isEmpty()){
			throw new NoTemplateFoundException();

		}
		logger.debug("Templates:");
		for(Template t : templates){
			logger.debug(t);
		}

		//get the weighted query candidates
		generatedQueries = getWeightedSPARQLQueries(templates);
		sparqlQueryCandidates = new ArrayList<WeightedQuery>();
		int i = 0;
		for(WeightedQuery wQ : generatedQueries){
			logger.debug(wQ.explain());
			sparqlQueryCandidates.add(wQ);
			if(i == maxTestedQueries){
				break;
			}
			i++;
		}

		if(mode == Mode.BEST_QUERY){
			double bestScore = -1;
			for(WeightedQuery candidate : generatedQueries){
				double score = candidate.getScore();
				if(score >= bestScore){
					bestScore = score;
					learnedSPARQLQueries.add(candidate);
				} else {
					break;
				}
			}
		} else if(mode == Mode.BEST_NON_EMPTY_QUERY){
			//test candidates
			if(useRemoteEndpointValidation){ //on remote endpoint
				validateAgainstRemoteEndpoint(sparqlQueryCandidates);
			} else {//on local model

			}
		}
	}

	public SortedSet<WeightedQuery> getGeneratedQueries() {
		return generatedQueries;
	}

	public SortedSet<WeightedQuery> getGeneratedQueries(int topN) {
		SortedSet<WeightedQuery> topNQueries = new TreeSet<WeightedQuery>();
		int max = Math.min(topN, generatedQueries.size());
		for(WeightedQuery wQ : generatedQueries){
			topNQueries.add(wQ);
			if(topNQueries.size() == max){
				break;
			}
		}
		return topNQueries;
	}

	public Set<Template> getTemplates(){
		return templates;
	}

	public List<String> getGeneratedSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		for(WeightedQuery wQ : sparqlQueryCandidates){
			queries.add(wQ.getQuery().toString());
		}

		return queries;
	}

	public Map<Template, Collection<? extends Query>> getTemplates2SPARQLQueries(){
		return template2Queries;
	}

	public Map<Slot, List<String>> getSlot2URIs(){
		return slot2URI;
	}

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
		if(min==max) {return;}
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

	public Set<String> getRelevantKeywords(){
		return relevantKeywords;
	}

	// just for testing the HMM integration, getWeightedSPARQLQueriesOld is the original one
	private SortedSet<WeightedQuery> getWeightedSPARQLQueries(Set<Template> templates)
	{
		// for testing 
		for(Template template: templates)
		{
			{
				ArrayList<String> keywords = new ArrayList<String>();
				for(Slot slot: template.getSlots())
				{
					keywords.add(slot.getWords().get(0));
				}
				if(template.getSlots().size()!=3) {continue;}
				if(!keywords.contains("Mean Hamster Software")) {continue;}
				if(!keywords.contains("published")) {continue;}
				System.out.println("\"keywords\": "+keywords);
			}
			System.out.println(template);
			SortedSet<WeightedQuery> queries = new TreeSet<WeightedQuery>();
			Query query = template.getQuery();
			double score = 0;
			
			Map<List<String>,Collection<String>> segmentToURIs = new HashMap<List<String>,Collection<String>>();
			for(Slot slot: template.getSlots())
			{
				List<String> segment = new LinkedList<String>();
				segment.add(slot.getWords().get(0)); // TODO: split it up?
			
				Set<String> uris = new HashSet<String>();
																
				for(IndexResultItem item : getIndexResultItems(slot))
				{
					uris.add(item.getUri());					
				}
				segmentToURIs.put(segment,uris); 
			}
			HiddenMarkovModel hmm = new HiddenMarkovModel();
			hmm.initialization();
			hmm.startMarkovModel(segmentToURIs,true);
			MultiMap<Double,List<String>> paths = hmm.getPaths();

			//			System.out.println(hmm.getPaths());
			// die keywords jetzt in sadeehs algorithmus reinwerfen 
			// da kommen jetzt pfade raus mit unterschiedlichen wahrscheinlichkeiten	
			//			HiddenMarkovModel HMM = new HiddenMarkovModel();
			//			HMM.StartMarkovModel();
			// jetzt die variablen aus der query ersetzen mit den kandidaten
			// ranked list der pfade, die die observation sequence generieren

			for(Double d : paths.keySet())
			{
				for(List<String> path : paths.get(d))
				{
					Query q = new Query(query);
					// TODO: which variable stands for which resource? do it randomly now to check if the replacement works and then correct the order later 
					System.out.println(q.getVariablesAsStringList());
					System.out.println();
					int i = 0;
					for(String var : q.getVariablesAsStringList())
					{						
						q.replaceVarWithURI(var, path.get(i));
						i++;
					}
					System.out.println(q);


					WeightedQuery wQuery = new WeightedQuery(q, score);
					queries.add(wQuery);
				}
			}
			//System.exit(0);
			return queries;
			//			>> SLOTS:
			//				y0: RESOURCE {Mean Hamster Software}
			//				p0: OBJECTPROPERTY {published,print}
			//				p1: CLASS {video games}


			//			System.out.println(template);			
		}
		// 		
		return null;
	}

	private double getProminenceValue(String uri, SlotType type){
		Integer popularity = null;
		if(popularityMap != null){
			if(type == SlotType.CLASS){
				popularity = popularityMap.getPopularity(uri, EntityType.CLASS);
			} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY 
					|| type == SlotType.DATATYPEPROPERTY || type == SlotType.OBJECTPROPERTY){
				popularity = popularityMap.getPopularity(uri, EntityType.PROPERTY);
			} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
				popularity = popularityMap.getPopularity(uri, EntityType.RESOURCE);
			} 
		} 
		if(popularity == null){
			String query = null;
			if(type == SlotType.CLASS){
				query = "SELECT COUNT(?s) WHERE {?s a <%s>}";
			} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY 
					|| type == SlotType.DATATYPEPROPERTY || type == SlotType.OBJECTPROPERTY){
				query = "SELECT COUNT(*) WHERE {?s <%s> ?o}";
			} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
				query = "SELECT COUNT(*) WHERE {?s ?p <%s>}";
			}
			query = String.format(query, uri);

			ResultSet rs = executeSelect(query);
			QuerySolution qs;
			String projectionVar;
			while(rs.hasNext()){
				qs = rs.next();
				projectionVar = qs.varNames().next();
				popularity = qs.get(projectionVar).asLiteral().getInt();
			}
		}
		if(popularity == null){
			popularity = Integer.valueOf(0);
		}
		System.out.println(popularity);


		//		if(cnt == 0){
		//			return 0;
		//		} 
		//		return Math.log(cnt);
		if(popularity!=popularity) {throw new AssertionError("prominence NaN for uri "+uri+", slot type "+type);}
		return popularity;
	}

	public void setPopularityMap(PopularityMap popularityMap) {
		this.popularityMap = popularityMap;
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
		//		mon.start();
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
		//		mon.stop();
		//		logger.info("Done in " + mon.getLastValue() + "ms.");
		logger.info("Pruned list: " + pruned);
		return pruned;
	}


	private Index getIndexBySlotType(Slot slot){
		Index index = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			index = classesIndex;
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			index = propertiesIndex;
		} else if(type == SlotType.DATATYPEPROPERTY){
			index = datatypePropertiesIndex;
		} else if(type == SlotType.OBJECTPROPERTY){
			index = objectPropertiesIndex;
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			index = resourcesIndex;
		}
		return index;
	}

	private void validateAgainstRemoteEndpoint(Collection<WeightedQuery> queries){
		SPARQL_QueryType queryType = queries.iterator().next().getQuery().getQt();
		validate(queries, queryType);
	}

	private void validate(Collection<WeightedQuery> queries, SPARQL_QueryType queryType){
		logger.debug("Testing candidate SPARQL queries on remote endpoint...");
		sparqlMon.start();
		if(queryType == SPARQL_QueryType.SELECT){
			for(WeightedQuery query : queries){
				learnedPos++;
				List<String> results;
				try {
					logger.debug("Testing query:\n" + query);
					com.hp.hpl.jena.query.Query q = QueryFactory.create(query.getQuery().toString(), Syntax.syntaxARQ);
					q.setLimit(1);
					ResultSet rs = executeSelect(q.toString());

					results = new ArrayList<String>();
					QuerySolution qs;
					String projectionVar;
					while(rs.hasNext()){
						qs = rs.next();
						projectionVar = qs.varNames().next();
						if(qs.get(projectionVar).isLiteral()){
							results.add(qs.get(projectionVar).asLiteral().getLexicalForm());
						} else if(qs.get(projectionVar).isURIResource()){
							results.add(qs.get(projectionVar).asResource().getURI());
						}

					}
					if(!results.isEmpty()){
						try{
							int cnt = Integer.parseInt(results.get(0));
							if(cnt > 0){
								learnedSPARQLQueries.add(query);
								if(stopIfQueryResultNotEmpty){
									return;
								}
							}
						} catch (NumberFormatException e){
							learnedSPARQLQueries.add(query);
							if(stopIfQueryResultNotEmpty){
								return;
							}
						}
						logger.debug("Result: " + results);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} else if(queryType == SPARQL_QueryType.ASK){
			for(WeightedQuery query : queries){
				learnedPos++;
				logger.debug("Testing query:\n" + query);
				boolean result = executeAskQuery(query.getQuery().toString());
				learnedSPARQLQueries.add(query);
				//				if(stopIfQueryResultNotEmpty && result){
				//					return;
				//				}
				if(stopIfQueryResultNotEmpty){
					return;
				}
				logger.debug("Result: " + result);
			}
		}

		sparqlMon.stop();
		logger.debug("Done in " + sparqlMon.getLastValue() + "ms.");
	}

	private boolean executeAskQuery(String query)
	{
		if(query==null) throw new NullPointerException("Parameter query == null");
		currentlyExecutedQuery = query;		

		boolean ret;
		if (model == null)
		{		
			QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			ret = qe.execAsk();			
		}
		else {ret = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execAsk();}
		return ret;
	}

	private ResultSet executeSelect(String query)
	{
		if(query==null) throw new NullPointerException("Parameter query == null");
		currentlyExecutedQuery = query;
		ResultSet rs;
		if (model == null) {
			if (cache == null) {
				QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
				qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
				rs = qe.execSelect();
			} else {
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			}
		} else {
			rs = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model)
					.execSelect();
		}

		return rs;
	}

	public String getCurrentlyExecutedQuery() {
		return currentlyExecutedQuery;
	}

	public int getLearnedPosition() {
		if(learnedPos >= 0){
			return learnedPos+1;
		}
		return learnedPos;
	}

	@Override
	public void start() {
	}

	@Override
	public List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries) {
		List<String> bestQueries = new ArrayList<String>();
		for(WeightedQuery wQ : learnedSPARQLQueries){
			bestQueries.add(wQ.getQuery().toString());
		}
		return bestQueries;
	}

	@Override
	public String getBestSPARQLQuery() {
		if(!learnedSPARQLQueries.isEmpty()){
			return learnedSPARQLQueries.iterator().next().getQuery().toString();
		} else {
			return null;
		}
	}

	public SortedSet<WeightedQuery> getLearnedSPARQLQueries() {
		return learnedSPARQLQueries;
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

	private Set<IndexResultItem> getIndexResultItems(Slot slot)
	{
//		List<String> uris = new LinkedList<String>();
		Set<IndexResultItem> indexResultItems = new HashSet<IndexResultItem>();
		
		Index index = getIndexBySlotType(slot);
		
		for(String word : slot.getWords())
		{
			IndexResultSet rs = new IndexResultSet();
			if(mappingIndex != null){
				SlotType type = slot.getSlotType();
				if(type == SlotType.CLASS){
					rs.add(mappingIndex.getClassesWithScores(word));
				} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
					rs.add(mappingIndex.getPropertiesWithScores(word));
				} else if(type == SlotType.DATATYPEPROPERTY){
					rs.add(mappingIndex.getDatatypePropertiesWithScores(word));
				} else if(type == SlotType.OBJECTPROPERTY){
					rs.add(mappingIndex.getObjectPropertiesWithScores(word));
				} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
					rs.add(mappingIndex.getResourcesWithScores(word));
				}
			}
			//use the non manual indexes only if mapping based resultset is not empty and option is set
			if(!useManualMappingsIfExistOnly || rs.isEmpty()){
				if(slot.getSlotType() == SlotType.RESOURCE){
					rs.add(index.getResourcesWithScores(word, 20));
				} else {
					if(slot.getSlotType() == SlotType.CLASS){
						word = PlingStemmer.stem(word); 
					}
					rs.add(index.getResourcesWithScores(word, 20));
				}
			}
//			for(IndexResultItem item: rs.getItems())
//			{
//				uris.add(item.getUri());
//			}
			indexResultItems.addAll(rs.getItems());
		}
		return indexResultItems;
	}


	public String getTaggedInput()
	{
		if(templateGenerator==null) {throw new AssertionError("Learner not initialized. Please call init();");}
		return templateGenerator.getTaggedInput();
	}

	private boolean isDatatypeProperty(String uri){
		Boolean isDatatypeProperty = null;
		if(mappingIndex != null){
			isDatatypeProperty = mappingIndex.isDataProperty(uri);
		}
		if(isDatatypeProperty == null){
			String query = String.format("ASK {<%s> a <http://www.w3.org/2002/07/owl#DatatypeProperty> .}", uri);
			isDatatypeProperty = executeAskQuery(query);
		}
		return isDatatypeProperty;
	}

	//	/**
	//	 * @param args
	//	 * @throws NoTemplateFoundException 
	//	 * @throws IOException 
	//	 * @throws FileNotFoundException 
	//	 * @throws InvalidFileFormatException 
	//	 */
	//	public static void main(String[] args) throws Exception {
	//		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
	//				Collections.<String>singletonList(""), Collections.<String>emptyList());
	//		Index resourcesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_resources");
	//		Index classesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_classes");
	//		Index propertiesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_properties");
	//
	//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
	//		learner.init();
	//
	//		String question = "What is the highest mountain?";
	//
	//		learner.setQuestion(question);
	//		learner.learnSPARQLQueries();
	//		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
	//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
	//		System.out.println(learner.getLearnedPosition());
	//
	//	}



}
