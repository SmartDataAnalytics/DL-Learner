package org.dllearner.algorithm.tbsl.learning;

import hmm.HiddenMarkovModel;
import hmm.ResourceInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Pair;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_PairType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Property;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_QueryType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Triple;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Value;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.sparql.WeightedQuery;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.algorithm.tbsl.util.PopularityMap;
import org.dllearner.algorithm.tbsl.util.PopularityMap.EntityType;
import org.dllearner.algorithm.tbsl.util.Similarity;
import org.dllearner.algorithm.tbsl.util.UnknownPropertyHelper.SymPropertyDirection;
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
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.SPARQLReasoner;
import org.ini4j.Options;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
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

/** The old learner taken over by Konrad Höffner for experiments with the Hidden Markov Algorithm by Saedeeh Shekarpur.
 * */
public class SPARQLTemplateBasedLearner2 implements SparqlQueryLearningAlgorithm
{
	/** synonyms are great but are not used yet by the HMM algorithm. **/
	private static final boolean	HMM_USE_SYNONYMS	= true;
		/** The minimum score of items that are accepted from the Sindice search BOA index. **/
	private static final Double	BOA_THRESHOLD	=  0.5;
	enum Mode {BEST_QUERY, BEST_NON_EMPTY_QUERY}
	private Mode mode = Mode.BEST_QUERY;
	
	/** used to create a label out of the URI when there is no label available in the SPARQL endpoint.*/
	private static SimpleIRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
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
	public void learnSPARQLQueries() throws NoTemplateFoundException
	{
		learnSPARQLQueries(false);
	}
	
	public void learnSPARQLQueries(boolean useHMM) throws NoTemplateFoundException
	{
		reset();
		//generate SPARQL query templates
		logger.debug("Generating SPARQL query templates...");
		templateMon.start();
		if(multiThreaded){
			templates = templateGenerator.buildTemplatesMultiThreaded(question,!useHMM||HMM_USE_SYNONYMS);
		} else {
			templates = templateGenerator.buildTemplates(question);
		}
		templateMon.stop();
		logger.debug("Done in " + templateMon.getLastValue() + "ms.");
		relevantKeywords.addAll(templateGenerator.getUnknownWords());
		if(templates.isEmpty()){throw new NoTemplateFoundException();}
//		logger.debug("Templates:");
//		for(Template t : templates){
//			logger.debug(t);
//		}

		//get the weighted query candidates
		generatedQueries = getWeightedSPARQLQueries(templates,useHMM);
		sparqlQueryCandidates = new ArrayList<WeightedQuery>();
		int i = 0;
		for(WeightedQuery wQ : generatedQueries){
			logger.trace(wQ.explain());
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

	public Set<String> getRelevantKeywords(){return relevantKeywords;}

	private SortedSet<WeightedQuery> getWeightedSPARQLQueries(Set<Template> templates, boolean hmm)
	{
		return hmm?getWeightedSPARQLQueriesWithHMM(templates):getWeightedSPARQLQueriesWithoutHMM(templates);
	}
	
	private SortedSet<WeightedQuery> getWeightedSPARQLQueriesWithHMM(Set<Template> templates)
	{
		List<String> vars = new LinkedList<String>();
		if(templates.isEmpty()) throw new AssertionError("no templates");
		SortedSet<WeightedQuery> queries = new TreeSet<WeightedQuery>();
		for(Template template: templates)
		{
			{
				ArrayList<String> keywords = new ArrayList<String>();
				for(Slot slot: template.getSlots())
				{
					if(!slot.getWords().isEmpty())
					{
						// we don't have synonyms for hmm at the moment, so there should be just one word 
						if(slot.getWords().size()!=1) throw new AssertionError("more than one word with hmm for slot: "+slot.getWords());
						keywords.add(slot.getWords().get(0));
						vars.add(slot.getAnchor());
					}									
				}
//				if(!keywords.contains("Mean Hamster Software")) {continue;}
//				if(!keywords.contains("published")) {continue;}
				logger.debug("\"keywords\": "+keywords);
			}
			System.out.println(template);			
			Query query = template.getQuery();
			double score = 0;

			Map<List<String>,List<ResourceInfo>> segmentToURIs = new HashMap<List<String>,List<ResourceInfo>>();
//			Map<String,IndexResultItem> uriUniqueToResultItem = new HashMap<String,IndexResultItem>(); 
			for(Slot slot: template.getSlots())
			{
				if(!slot.getWords().isEmpty()){
					List<String> segment = new LinkedList<String>();
					segment.addAll(Arrays.asList(slot.getWords().get(0).split("\\s")));			
					List<ResourceInfo> resourceInfos = new LinkedList<ResourceInfo>();

					for(IndexResultItem item : getIndexResultItems(slot))
					{
						// if this gets used at another place, create a function IndexResultItemToResourceInfo()
						ResourceInfo info = new ResourceInfo();
						info.setUri(item.getUri());
						String label = item.getLabel();					
						// in dbpedia, the last part of the uri is transformed from the english label, reverse the transformation (should almost always work for dbpedia article resources)
						info.setLabel(label!=null?label:sfp.getShortForm(IRI.create(item.getUri())));
						// in saedeehs algorithm, the emission probabilty is formed by the string similarity
						// but we use the lucene index score
						double max = 0;
						for(String word: slot.getWords()) {max = Math.max(max, Similarity.getSimilarity(word, info.getLabel()));}					
						if(max<0||max>1) throw new AssertionError("max is not in [0,1], max="+max);
						info.setStringSimilarityScore(max);
						if(!info.setTypeFromDBpediaURI()) throw new AssertionError("could not set type for info "+info);
						System.err.println("info with type: "+info);
						resourceInfos.add(info);
					}
					segmentToURIs.put(segment,resourceInfos);
				}
				
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
					for(String uri : path){
						uri = uri.trim();
						String var = vars.get(path.indexOf(uri));
						q.replaceVarWithURI(var, uri);
					}
					System.out.println(q);


					WeightedQuery wQuery = new WeightedQuery(q, score);
					queries.add(wQuery);
				}
			}
			//System.exit(0);			
			//			>> SLOTS:
			//				y0: RESOURCE {Mean Hamster Software}
			//				p0: OBJECTPROPERTY {published,print}
			//				p1: CLASS {video games}


			//			System.out.println(template);			
		}
		// 		
		return queries;		
	}

	@SuppressWarnings("unused") private SortedSet<WeightedQuery> getWeightedSPARQLQueriesWithoutHMM(Set<Template> templates){
		logger.debug("Generating SPARQL query candidates...");

		Map<Slot, Set<Allocation>> slot2Allocations = new TreeMap<Slot, Set<Allocation>>(new Comparator<Slot>() {

			@Override
			public int compare(Slot o1, Slot o2) {
				if(o1.getSlotType() == o2.getSlotType()){
					return o1.getToken().compareTo(o2.getToken());
				} else {
					return -1;
				}
			}
		});
		slot2Allocations = Collections.synchronizedMap(new HashMap<Slot, Set<Allocation>>());


		SortedSet<WeightedQuery> allQueries = new TreeSet<WeightedQuery>();

		for(Template t : templates)
		{
			logger.info("Processing template:\n" + t.toString());
//			Set<Allocation> allocations = new TreeSet<Allocation>();
			boolean containsRegex = t.getQuery().toString().toLowerCase().contains("(regex(");

			ExecutorService executor = Executors.newFixedThreadPool(t.getSlots().size());
			List<Future<Map<Slot, SortedSet<Allocation>>>> list = new ArrayList<Future<Map<Slot, SortedSet<Allocation>>>>();

			long startTime = System.currentTimeMillis();

			for (Slot slot : t.getSlots()) {
				if(!slot2Allocations.containsKey(slot)){
					Callable<Map<Slot, SortedSet<Allocation>>> worker = new SlotProcessor(slot);
					Future<Map<Slot, SortedSet<Allocation>>> submit = executor.submit(worker);
					list.add(submit);
				} 
			}

			for (Future<Map<Slot, SortedSet<Allocation>>> future : list) {
				try {
					Map<Slot, SortedSet<Allocation>> result = future.get();
					Entry<Slot, SortedSet<Allocation>> item = result.entrySet().iterator().next();
					slot2Allocations.put(item.getKey(), item.getValue());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
//					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}

			executor.shutdown();


			/*for(Slot slot : t.getSlots()){
				allocations = slot2Allocations2.get(slot);
				if(allocations == null){
					allocations = computeAllocations(slot, 10);
					slot2Allocations2.put(slot, allocations);
				}
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
			}*/
			logger.debug("Time needed: " + (System.currentTimeMillis() - startTime) + "ms");

			Set<WeightedQuery> queries = new HashSet<WeightedQuery>();
			Query cleanQuery = t.getQuery();
			queries.add(new WeightedQuery(cleanQuery));

			Set<WeightedQuery> tmp = new TreeSet<WeightedQuery>();
			List<Slot> sortedSlots = new ArrayList<Slot>();
			Set<Slot> classSlots = new HashSet<Slot>();
			// TODO: can this be optimized to be in just one loop? (but I guess it won't give a noticable performance benefit anyways...)
			for(Slot slot : t.getSlots()){
				if(slot.getSlotType() == SlotType.CLASS){
					sortedSlots.add(slot);
					classSlots.add(slot);
				}
			}
			for(Slot slot : t.getSlots()){
				if(slot.getSlotType() == SlotType.PROPERTY || slot.getSlotType() == SlotType.OBJECTPROPERTY || slot.getSlotType() == SlotType.DATATYPEPROPERTY){
					sortedSlots.add(slot);
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
					if(slot.getSlotType() == SlotType.SYMPROPERTY || slot.getSlotType() == SlotType.OBJECTPROPERTY){
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
				if(!slot2Allocations.get(slot).isEmpty()){
					for(Allocation a : slot2Allocations.get(slot)){
						for(WeightedQuery query : queries){
							Query q = new Query(query.getQuery());

							boolean drop = false;
//							if(useDomainRangeRestriction){
//								if(slot.getSlotType() == SlotType.PROPERTY || slot.getSlotType() == SlotType.SYMPROPERTY){
//									for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
//										String objectVar = triple.getValue().getName();
//										String subjectVar = triple.getVariable().getName();
//
//										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(objectVar)){
//
//											if(true){//reasoner.isObjectProperty(a.getUri())){
//												Description range = reasoner.getRange(new ObjectProperty(a.getUri()));
//
//												if(range != null){
//													Set<Description> allRanges = new HashSet<Description>();
//													SortedSet<Description> superClasses;
//													if(range instanceof NamedClass){
//														superClasses = reasoner.getSuperClasses(range);
//														allRanges.addAll(superClasses);
//													} else {
//														for(Description nc : range.getChildren()){
//															superClasses = reasoner.getSuperClasses(nc);
//															allRanges.addAll(superClasses);
//														}
//													}
//													allRanges.add(range);
//													allRanges.remove(new NamedClass(Thing.instance.getURI()));
//
//													Set<Description> allTypes = new HashSet<Description>();
//													String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
//													Description type = new NamedClass(typeURI);
//													superClasses = reasoner.getSuperClasses(type);
//													allTypes.addAll(superClasses);
//													allTypes.add(type);
//
//													if(!org.mindswap.pellet.utils.SetUtils.intersects(allRanges, allTypes)){
//														drop = true;
//													} 
//												}
//											} else {
//												drop = true;
//											}
//
//										}
//										for(SPARQL_Triple typeTriple : q.getRDFTypeTriples(subjectVar)){
//											Description domain = reasoner.getDomain(new ObjectProperty(a.getUri()));
//
//											if(domain != null){
//												Set<Description> allDomains = new HashSet<Description>();
//												SortedSet<Description> superClasses;
//												if(domain instanceof NamedClass){
//													superClasses = reasoner.getSuperClasses(domain);
//													allDomains.addAll(superClasses);
//												} else {
//													for(Description nc : domain.getChildren()){
//														superClasses = reasoner.getSuperClasses(nc);
//														allDomains.addAll(superClasses);
//													}
//												}
//												allDomains.add(domain);
//												allDomains.remove(new NamedClass(Thing.instance.getURI()));
//
//												Set<Description> allTypes = new HashSet<Description>();
//												String typeURI = typeTriple.getValue().getName().substring(1,typeTriple.getValue().getName().length()-1);
//												Description type = new NamedClass(typeURI);
//												superClasses = reasoner.getSuperClasses(type);
//												allTypes.addAll(superClasses);
//												allTypes.add(type);
//
//												if(!org.mindswap.pellet.utils.SetUtils.intersects(allDomains, allTypes)){
//													drop = true;												
//												} else {
//
//												}
//											}
//										}
//									}
//								}
//							}

							if(!drop){
								if(slot.getSlotType() == SlotType.RESOURCE){//avoid queries where predicate is data property and object resource->add REGEX filter in this case
									for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
										SPARQL_Value object = triple.getValue();
//										if(object.isVariable() && object.getName().equals(slot.getAnchor())){//only consider triple where SLOT is in object position
//											SPARQL_Property predicate = triple.getProperty();
//											if(!predicate.isVariable()){//only consider triple where predicate is URI
//												String predicateURI = predicate.getName().replace("<", "").replace(">", "");
//												if(isDatatypeProperty(predicateURI)){//if data property
//													q.addFilter(new SPARQL_Filter(new SPARQL_Pair(
//															object, "'" + slot.getWords().get(0) + "'", SPARQL_PairType.REGEX)));
//												} else {
//													q.replaceVarWithURI(slot.getAnchor(), a.getUri());
//												}
//											} else {
//												q.replaceVarWithURI(slot.getAnchor(), a.getUri());
//											}
//										} else {
//										
//										}
									}
								} else {
									q.replaceVarWithURI(slot.getAnchor(), a.getUri());
								}
								WeightedQuery w = new WeightedQuery(q);
								double newScore = query.getScore() + a.getScore();
								w.setScore(newScore);
								w.addAllocations(query.getAllocations());
								w.addAllocation(a);
								tmp.add(w);
							}


						}
					}
					//lower queries with FILTER-REGEX
					if(containsRegex){
						for(WeightedQuery wQ : tmp){
							wQ.setScore(wQ.getScore() - 0.01);
						}
					}

					queries.clear();
					queries.addAll(tmp);
					tmp.clear();
				} else {//Add REGEX FILTER if resource slot is empty and predicate is datatype property
					if(slot.getSlotType() == SlotType.RESOURCE){
						for(WeightedQuery query : queries){
							Query q = query.getQuery();
							for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
								SPARQL_Value object = triple.getValue();
								if(object.isVariable() && object.getName().equals(slot.getAnchor())){//only consider triple where SLOT is in object position
									SPARQL_Property predicate = triple.getProperty();
									if(!predicate.isVariable()){//only consider triple where predicate is URI
										String predicateURI = predicate.getName().replace("<", "").replace(">", "");
										if(isDatatypeProperty(predicateURI)){//if data property
											q.addFilter(new SPARQL_Filter(new SPARQL_Pair(
													object, "'" + slot.getWords().get(0) + "'", SPARQL_PairType.REGEX)));
										}
									}
								}
							}

						}

					} else {
						if(slot.getSlotType() == SlotType.SYMPROPERTY){
							for(WeightedQuery wQ : queries){
								List<SPARQL_Triple> triples = wQ.getQuery().getTriplesWithVar(slot.getAnchor());
								for(SPARQL_Triple triple : triples){
									String typeVar;
									String resourceURI;
									SymPropertyDirection direction;
									if(triple.getValue().isVariable()){
										direction = SymPropertyDirection.VAR_RIGHT;
										typeVar = triple.getValue().getName();
										resourceURI = triple.getVariable().getName();
									} else {
										direction = SymPropertyDirection.VAR_LEFT;
										typeVar = triple.getVariable().getName();
										resourceURI = triple.getValue().getName();
									}
									resourceURI = resourceURI.replace("<", "").replace(">", "");
									List<SPARQL_Triple> typeTriples = wQ.getQuery().getRDFTypeTriples(typeVar);
									for(SPARQL_Triple typeTriple : typeTriples){
										String typeURI = typeTriple.getValue().getName().replace("<", "").replace(">", "");
										//										List<Entry<String, Integer>> mostFrequentProperties = UnknownPropertyHelper.getMostFrequentProperties(endpoint, cache, typeURI, resourceURI, direction);
										//										for(Entry<String, Integer> property : mostFrequentProperties){
										//											wQ.getQuery().replaceVarWithURI(slot.getAnchor(), property.getKey());
										//											wQ.setScore(wQ.getScore() + 0.1);
										//										}
									}

								}
							}
						}
					}
					//					else if(slot.getSlotType() == SlotType.CLASS){
					//						String token = slot.getWords().get(0);
					//						if(slot.getToken().contains("house")){
					//							String regexToken = token.replace("houses", "").replace("house", "").trim();
					//							try {
					//								Map<Slot, SortedSet<Allocation>> ret = new SlotProcessor(new Slot(null, SlotType.CLASS, Collections.singletonList("house"))).call();
					//								SortedSet<Allocation> alloc = ret.entrySet().iterator().next().getValue();
					//								if(alloc != null && !alloc.isEmpty()){
					//									String uri = alloc.first().getUri();
					//									for(WeightedQuery query : queries){
					//										Query q = query.getQuery();
					//										for(SPARQL_Triple triple : q.getTriplesWithVar(slot.getAnchor())){
					//											SPARQL_Term subject = triple.getVariable();
					//											SPARQL_Term object = new SPARQL_Term("desc");
					//											object.setIsVariable(true);
					//											object.setIsURI(false);
					//											q.addCondition(new SPARQL_Triple(subject, new SPARQL_Property("<http://purl.org/goodrelations/v1#description>"), object));
					//											q.addFilter(new SPARQL_Filter(new SPARQL_Pair(
					//													object, "'" + regexToken + "'", SPARQL_PairType.REGEX)));
					//										}
					//										q.replaceVarWithURI(slot.getAnchor(), uri);
					//										
					//									}
					//								}
					//							} catch (Exception e) {
					//								e.printStackTrace();
					//							}
					//						}
					//					}


				}

			}
			
			for (Iterator<WeightedQuery> iterator = queries.iterator(); iterator.hasNext();) {
				WeightedQuery wQ = iterator.next();
				if(dropZeroScoredQueries){
					if(wQ.getScore() <= 0){
						iterator.remove();
					}
				} else {
					if(t.getSlots().size()==0) throw new AssertionError("no slots for query "+wQ);
					wQ.setScore(wQ.getScore()/t.getSlots().size());
				}

			}
			allQueries.addAll(queries);
			List<Query> qList = new ArrayList<Query>();
			for(WeightedQuery wQ : queries){
				qList.add(wQ.getQuery());
			}
			template2Queries.put(t, qList);
		}
		logger.debug(allQueries);
		logger.debug("...done in ");
		return allQueries;
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
		logger.trace("popularity: "+popularity);

		//		if(cnt == 0){
		//			return 0;
		//		} 
		//		return Math.log(cnt);
		if(Double.isNaN(popularity)) {throw new AssertionError("prominence NaN for uri "+uri+", slot type "+type);}
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
					rs.add(index.getResourcesWithScores(word, 20,0));
				} else {
					if(slot.getSlotType() == SlotType.CLASS){
						word = PlingStemmer.stem(word); 
					}
					IndexResultSet tmp = index.getResourcesWithScores(word, 20,0,Collections.singleton("boa-score"));
					for(IndexResultItem item : tmp.getItems())
					{System.out.println(item);
						Double boaScore = (Double) item.getFields().get("boa-score");
						if(boaScore==null||boaScore>BOA_THRESHOLD) rs.addItem(item);
					}
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
	
	class SlotProcessor implements Callable<Map<Slot, SortedSet<Allocation>>>{

		private Slot slot;

		public SlotProcessor(Slot slot) {
			this.slot = slot;
		}

		@Override
		public Map<Slot, SortedSet<Allocation>> call() throws Exception {
			Map<Slot, SortedSet<Allocation>> result = new HashMap<Slot, SortedSet<Allocation>>();
			result.put(slot, computeAllocations(slot));
			return result;
		}

		private SortedSet<Allocation> computeAllocations(Slot slot){
			logger.debug("Computing allocations for slot: " + slot);
			SortedSet<Allocation> allocations = new TreeSet<Allocation>();

			Index index = getIndexBySlotType(slot);

			IndexResultSet rs;
			for(String word : slot.getWords()){
				rs = new IndexResultSet();
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


				for(IndexResultItem item : rs.getItems()){
					double similarity = Similarity.getSimilarity(word, item.getLabel());
					//					//get the labels of the redirects and compute the highest similarity
					//					if(slot.getSlotType() == SlotType.RESOURCE){
					//						Set<String> labels = getRedirectLabels(item.getUri());
					//						for(String label : labels){
					//							double tmp = Similarity.getSimilarity(word, label);
					//							if(tmp > similarity){
					//								similarity = tmp;
					//							}
					//						}
					//					}
					double prominence = getProminenceValue(item.getUri(), slot.getSlotType());
					allocations.add(new Allocation(item.getUri(), prominence, similarity));
				}

			}

			normProminenceValues(allocations);

			computeScore(allocations);
			logger.debug("Found " + allocations.size() + " allocations for slot " + slot);
			return new TreeSet<Allocation>(allocations);
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
	//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2HMM(endpoint, resourcesIndex, classesIndex, propertiesIndex);
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
