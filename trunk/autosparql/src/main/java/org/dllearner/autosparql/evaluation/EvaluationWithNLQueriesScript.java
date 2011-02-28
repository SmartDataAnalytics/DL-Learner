package org.dllearner.autosparql.evaluation;

import info.bliki.api.Page;
import info.bliki.api.XMLPagesParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.server.ExampleFinder;
import org.dllearner.autosparql.server.Generalisation;
import org.dllearner.autosparql.server.NBR;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.search.DBpediaSchemaIndex;
import org.dllearner.autosparql.server.search.LuceneSearch;
import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedQueryTreeFilter;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedStatementFilter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.ResultSet;

import de.simba.ner.WordnetQuery;

public class EvaluationWithNLQueriesScript {
	private static Logger logger = Logger.getLogger(EvaluationWithNLQueriesScript.class);
	private static Logger miniLogger = Logger.getLogger("mini");
	
	private static final boolean USE_SYNONYMS = false;
	private static final boolean USE_WIKIPEDIA_SEARCH = true;
	
	private static final String SOLR_SERVER_URL = "http://139.18.2.164:8983/solr/dbpediaCore/";
	private static final String QUERY_ANSWERS_FILE_PATH = "evaluation/dbpedia-train_cleaned.xml";
	private static final String SCHEMA_FILE_PATH = "evaluation/dbpedia_schema.owl";
//	private static final String LUCENE_INDEX_DIRECTORY = "/opt/autosparql/index";
	private static final String LUCENE_INDEX_DIRECTORY = "/home/jl/hdd/other_large_files/index/";
	private static final String WORDNET_DICTIONARY = "src/main/resources/de/simba/ner/dictionary";
	private static final SparqlEndpoint ENDPOINT = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	
	private static final int NR_OF_POS_START_EXAMPLES_COUNT = 3;
	private static final int NR_OF_NEG_START_EXAMPLES_COUNT = 3;
	
	private static final int TOP_K = 20;
	
	private static final double SIMILARITY_THRESHOLD = 0.4;
	
	
	private Map<String, String> question2query = new Hashtable<String, String>();
	private SortedMap<String, Set<String>> question2Answers = new TreeMap<String, Set<String>>();
	
	private SolrServer server;
	
	private ExtractionDBCache selectCache = new ExtractionDBCache("select-cache");
	private ExtractionDBCache constructCache = new ExtractionDBCache("construct-cache");
	
	private ExampleFinder exFinder;
	
	private DBpediaSchemaIndex schemaIndex;
	private LuceneSearch luceneSearch;
	private WordnetQuery wordNet;
	
	private QuestionProcessor qProcessor = new QuestionProcessor();
	
	
	public EvaluationWithNLQueriesScript(){
		try {
			server = new CommonsHttpSolrServer(SOLR_SERVER_URL);
			
			//predicate filters used when sending sparql query for model creation
			List<String> predicateFilters = new ArrayList<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			//prefixes and baseURI to improve readability of trees
			String baseURI = "http://dbpedia.org/resource/";
			Map<String,String> prefixes = new HashMap<String,String>();
			prefixes.put("dbo","http://dbpedia.org/ontology/");
			prefixes.put("dbprop","http://dbpedia.org/property/");
			prefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
			prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			prefixes.put("skos","http://www.w3.org/2004/02/skos/core#");
			prefixes.put("geo","http://www.w3.org/2003/01/geo/wgs84_pos#");
			prefixes.put("georss","http://www.georss.org/georss/");
			prefixes.put("owl","http://www.w3.org/2002/07/owl#");
			prefixes.put("yago","http://dbpedia.org/class/yago/");
			prefixes.put("cyc","http://sw.opencyc.org/concept/");
			prefixes.put("foaf","http://xmlns.com/foaf/0.1/");
			exFinder = new ExampleFinder(new SPARQLEndpointEx(new URL("http://live.dbpedia.org/sparql"), //new URL("http://lod.openlinksw.com/sparql"), 
							Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList(), null, baseURI, prefixes, predicateFilters), selectCache, constructCache);
//			schemaIndex = new DBpediaSchemaIndex(SCHEMA_FILE_PATH);
			luceneSearch = new LuceneSearch(LUCENE_INDEX_DIRECTORY);
			luceneSearch.setHitsPerPage(TOP_K);
			wordNet = new WordnetQuery(WORDNET_DICTIONARY);
			
			
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		readQueries();
	}
	
	private void readQueries(){
		logger.info("Reading file containing queries and answers...");
		try {
			File file = new File(QUERY_ANSWERS_FILE_PATH);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			String question;
			String query;
			Set<String> answers;
			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//Read SPARQL query
				query = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//Read answers
				answers = new HashSet<String>();
				NodeList aswersNodes = questionNode.getElementsByTagName("answer");
				for(int j = 0; j < aswersNodes.getLength(); j++){
					Element answerNode = (Element) aswersNodes.item(j);
					answers.add(((Element)answerNode.getElementsByTagName("uri").item(0)).getChildNodes().item(0).getNodeValue().trim());
				}
				
				question2query.put(question, query);
				question2Answers.put(question, answers);
				
			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Done.");
	}
	
	private Set<String> getResourcesByNLQuery(String question){
		logger.info("Getting Top " + TOP_K + " resources related to question with Solr...");
		Set<String> resources = new HashSet<String>();
		QueryResponse response;
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", question);
			params.set("rows", TOP_K);
			response = server.query(params);
			for(SolrDocument d : response.getResults()){
				resources.add((String) d.get("id"));
			}
			logger.info("Got " + resources.size() + " resources in " + response.getElapsedTime() + "ms.");
			logger.info(resources);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	private List<String> getResourcesByNLQueryWithLucene(String question){
		logger.info("Getting Top " + TOP_K + " resources related to question with Lucene...");
		List<String> resources = luceneSearch.getResources(question);
		logger.info("Got " + resources.size() + " resources:");
		logger.info(resources);
		return resources;
	}
	
	private List<String> getRelevantWords(String question){
		return qProcessor.getRelevantWords(question);
//		Properties props = new Properties();
//	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	}
	
	private List<String> getResourcesByWikipedia(String query) {
		logger.info("Getting Top " + TOP_K + " resources related to question with Wikipedia...");
		long startTime = System.currentTimeMillis();
		List<String> resources = new ArrayList<String>();
		try {
			String[] words = query.split(" ");
			String modifiedQuery = "";
			for(int i = 0; i < words.length-1; i++){
				modifiedQuery += words[i] + "+";
			}
			modifiedQuery += words[words.length-1];
			URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&list=search&format=xml&srsearch=" + modifiedQuery + "&srlimit="+TOP_K);
			URLConnection conn = url.openConnection ();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			XMLPagesParser parser = new XMLPagesParser(sb.toString());
			parser.parse();
			for (Page page : parser.getPagesList()) {
				resources.add("http://dbpedia.org/resource/" + page.getTitle().replace(" ", "_"));
			}
			logger.info("Got " + resources.size() + " resources in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info(resources);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	private List<String> getSchemaElementsByQuery(String query){
		logger.info("Getting Top " + TOP_K + " schema elements related to question with Lucene...");
		long startTime = System.currentTimeMillis();
		List<String> elements = schemaIndex.getResources(query);
		logger.info("Got " + elements.size() + " elements in " + (System.currentTimeMillis()-startTime) + "ms.");
		logger.info(elements);
		return elements;
	}
	
	private Set<String> getSynonyms(Collection<String> words){
		Set<String> synonyms = new HashSet<String>();
		for(String w : words){
			synonyms.addAll(wordNet.getSynset(w));
		}
		return synonyms;
	}
	
	private Set<String> getResourcesBySPARQLQuery(String query, String varName){
		if(query.equals("SELECT ?x0 WHERE {?x0 ?y ?z.}")){
			return Collections.emptySet();
		}
		query = query + " LIMIT 1000";
		logger.info("Sending query...");
		long startTime = System.currentTimeMillis();
		Set<String> resources = new HashSet<String>();
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(ENDPOINT, query));
		while(rs.hasNext()){
			resources.add(rs.nextSolution().get(varName).asResource().getURI());
		}
		logger.info("Done in " + (System.currentTimeMillis()-startTime) + "ms");
		return resources;
	}
	
	private boolean LGGIsSolution(List<String> posExamples, Set<String> answers){
		logger.info("Checking if LGG is already a solution...");
		QueryTree<String> lgg = exFinder.computeLGG(posExamples);
		String query = lgg.toSPARQLQueryString();
		query = "SELECT DISTINCT " + query.substring(7);
		Set<String> resources = getResourcesBySPARQLQuery(query, "x0");
		boolean isSolution = resources.equals(answers);
		logger.info(isSolution);
		return isSolution;
	}
	
	public void evaluate(){
		String targetQuery;
		Set<String> answers;
		List<String> examples;
		Set<String> relatedResources;
		List<String> relevantWords;
		int i = 1;
		int learnedQueries = 0;
		for(String question : question2Answers.keySet()){question = "Give me all soccer clubs in the Premier League.";
			logger.debug(getNewQuestionString(i, question));
			try {
				targetQuery = question2query.get(question);
				logger.debug("Target query: \n" + targetQuery);
				answers = getResourcesBySPARQLQuery(targetQuery, "uri");//question2Answers.get(question);
				logger.debug("Answers (" + answers.size() + "): " + answers);
				printStartingPosition(i++, question, targetQuery, answers);
				//preprocess question to extract only relevant words and set them as filter for statements
				relevantWords = getRelevantWords(question);
				QuestionBasedStatementFilter filter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
				filter.setThreshold(SIMILARITY_THRESHOLD);
				QuestionBasedQueryTreeFilter treeFilter = new QuestionBasedQueryTreeFilter(new HashSet<String>(relevantWords));
				treeFilter.setThreshold(SIMILARITY_THRESHOLD);
				exFinder.setStatementFilter(filter);
				exFinder.setQueryTreeFilter(treeFilter);
//				exFinder.setStatementSelector(new QuestionBasedStatementSelector(new HashSet<String>(relevantWords)));
				
				//expand with synonyms
				if(USE_SYNONYMS){
					relevantWords.addAll(getSynonyms(relevantWords));
					logger.debug("Extended with synonyms: " + relevantWords);
				}
				question = "";
				for(String word : relevantWords){
					question += " " + word; 
				}
				question.trim();
				logger.debug("Rebuilt question string: " + question);
				
				//get examples
				if(USE_WIKIPEDIA_SEARCH){
					examples = getResourcesByWikipedia(question);
				} else {
					examples = getResourcesByNLQueryWithLucene(question);
				}
				miniLogger.info("AutoSPARQL: Should some of the following resources belong to query result?\n" + examples);
				//get resources which are relevant for query and add them as filter for objects
//				relatedResources = getResourcesByNLQuery(question.substring(0, question.length()-1));
//				relatedResources.addAll(getSchemaElementsByQuery(question.substring(0, question.length()-1)));
//				exFinder.setObjectFilter(new ExactMatchFilter(relatedResources));
				
				//select some positive and negative examples
				List<String> posExamples = new ArrayList<String>();
				List<String> negExamples = new ArrayList<String>();
				for(String ex : examples){
					if(answers.contains(ex)){
						if(posExamples.size() < NR_OF_POS_START_EXAMPLES_COUNT){
							miniLogger.info("User: YES, " + ex + ".");
							posExamples.add(ex);
						}
					} else {
						if(negExamples.size() < NR_OF_NEG_START_EXAMPLES_COUNT){
//							miniLogger.info("User: Select " + ex + " as negative example.");
							negExamples.add(ex);
						}
					}
				}
				//if there are not enough positive examples in search we select some from the answer set which simulates manually addition of user
				if(posExamples.size() < NR_OF_POS_START_EXAMPLES_COUNT){
					logger.debug("Found only " + posExamples.size() + " positive example(s) in search result. Adding more from the answer set...");
					miniLogger.info("AutoSPARQL: I need " + (NR_OF_POS_START_EXAMPLES_COUNT-posExamples.size())
							+ " more positive example(s) before start with learning!");
					for(String answer : answers){
						if(posExamples.add(answer)){
							miniLogger.info("User: Choose " + answer + " as additional positive example.");
						}
						if(posExamples.size() == NR_OF_POS_START_EXAMPLES_COUNT){
							break;
						}
					}
					
				}
				if(posExamples.isEmpty()){
					logger.warn("Current search returned no positive example in the Top " + TOP_K + ".\n" +
							"Skipping query...");
					continue;
				}
				logger.info("Starting positive example(s) (#" +posExamples.size() + "): " + posExamples);
				logger.info("Starting negative example(s) (#" +negExamples.size() + "): " + negExamples);
				
				//start learning
				miniLogger.info("AutoSPARQL: Started learning...");
				if(LGGIsSolution(posExamples, answers)){
					logger.info("Learned successful.");
					miniLogger.info("Learning successful.");
					logger.info("Learned SPARQL query:\n" + exFinder.getCurrentQuery());
					miniLogger.info("Learned SPARQL query:\n" + exFinder.getCurrentQuery());
					learnedQueries++;
					continue;
				}
				Set<String> learnedResources;
				String oldLearnedQuery = "";
				boolean learningFailed = false;
				do {
					// compute new similiar example
					logger.info("Computing similar example...");
					long startTime = System.currentTimeMillis();
					String example = exFinder.findSimilarExample(posExamples,
							negExamples).getURI();
					logger.debug("Computed similar example \"" + example
							+ "\" in " + (System.currentTimeMillis() - startTime)
							+ "ms");
					miniLogger.info("AutoSPARQL: Should \"" + example + "\" belong to the query result?");
					// print learned query up to here
					String learnedQuery = exFinder.getCurrentQuery();
//					if(oldLearnedQuery.equals(learnedQuery)){
//						learningFailed = true;
//						logger.info("Aborting because learned again same query.");
//						break;
//					}oldLearnedQuery = learnedQuery;
					logger.debug("Learned SPARQL query: \n" + learnedQuery);
//					learnedQuery = "SELECT DISTINCT " + learnedQuery.substring(7);
					learnedQuery = "SELECT " + learnedQuery.substring(7);
					learnedResources = getResourcesBySPARQLQuery(learnedQuery, "x0");
					logger.debug("Number of resources in learned query: "
							+ learnedResources.size());
					if (answers.contains(example)) {
						posExamples.add(example);
						miniLogger.info("User: YES");
					} else {
						negExamples.add(example);
						miniLogger.info("User: NO");
					}
					miniLogger.info("Learned SPARQL query:\n" + learnedQuery);
				} while (!answers.equals(learnedResources));
				if(!learningFailed){
					logger.info("Learned successfully query for question \""+ question + "\".");
					miniLogger.info("Learning successful.");
					learnedQueries++;
				}else {
					miniLogger.info("AutoSPARQL: Could not learn query.");
				}
			} catch (TimeOutException e) {
				e.printStackTrace();
			} catch (SPARQLQueryException e) {
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("Something went wrong. Trying next question...", e);
				miniLogger.info("AutoSPARQL: Could not learn query.");
			}
		}
		logger.info("Learned " + learnedQueries + "/" + question2query.keySet().size() + " queries.");
	}
	
	private String getNewQuestionString(int i, String question){
		String s = "*****************************************************\n" +
				   "*                  " + i + ". QUESTION                      *\n       " +
				   question + 
				   "\n*****************************************************\n";
		return s;
	}
	
	private void printStartingPosition(int i, String question, String targetQuery, Set<String> answers){
		String s = "*****************************************************\n" +
		"*****************************************************\n" +
		   "*                  " + i + ". QUESTION                      *\n       " +
		   question + 
		   "\n*****************************************************\n" + 
		   "          TARGET SPARQL QUERY: \n" +
		   targetQuery + 
		   "\n*****************************************************" +
		   "\n*****************************************************";
			miniLogger.info(s);
	}

	/**
	 * @param args
	 * @throws SPARQLQueryException 
	 * @throws TimeOutException 
	 * @throws SolrServerException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws TimeOutException, SPARQLQueryException, SolrServerException, ParserConfigurationException, SAXException, IOException {
		Logger.getLogger(Generalisation.class).setLevel(Level.OFF);
		Logger.getLogger(LGGGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(NBR.class).setLevel(Level.DEBUG);
		
		Logger.getRootLogger().removeAllAppenders();
		Layout layout = new PatternLayout("%m%n");
		ConsoleAppender appender = new ConsoleAppender(layout);
		Logger.getRootLogger().addAppender(appender);
		FileAppender fileAppender = new FileAppender(
				layout, "log/evaluation.log", false);
		fileAppender.setThreshold(Level.DEBUG);
		Logger.getRootLogger().addAppender(fileAppender);
		
		FileAppender fileAppender2 = new FileAppender(
				layout, "log/mini.log", false);
		fileAppender2.setThreshold(Level.INFO);
		
		Logger.getLogger("mini").removeAllAppenders();
		Logger.getLogger("mini").addAppender(fileAppender2);
		
		
		
		
		new EvaluationWithNLQueriesScript().evaluate();
	}
	
	

}
