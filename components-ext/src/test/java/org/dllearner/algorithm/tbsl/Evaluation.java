package org.dllearner.algorithm.tbsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.sparql.WeightedQuery;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.algorithm.tbsl.util.LatexWriter;
import org.dllearner.algorithm.tbsl.util.RemoteKnowledgebase;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Evaluation{
	
//	List<Integer> yagoExclusions = Arrays.asList(new Integer[]{1,	3,	6,	11,	15,	22,	23,	46});
	List<Integer> exclusions = Arrays.asList(new Integer[]{1,5,8,9,16,28,30,32,38,51,52,53,74,86,94,95,96,97,98,99,100});
	Map<Integer, String> evalCodes = new HashMap<Integer, String>();
	
	private static Logger logger = Logger.getLogger(Evaluation.class);
	private static String PROPERTIES_PATH = "tbsl/evaluation/evaluation.properties";
	private static final boolean USE_IDEAL_TAGGER = true;
	
	private SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
	private SortedMap<Integer, Object> id2Answer = new TreeMap<Integer, Object>();
	
	private SparqlEndpoint endpoint;
	
	private SPARQLTemplateBasedLearner2 stbl;
	
	private int testID = -1;
	private Map<String, String> prefixMap;
	
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	
	
	public Evaluation(File ... evaluationFiles) throws FileNotFoundException, IOException{
		for(File file : evaluationFiles){
			readQueries(file);
		}
		
		SOLRIndex resourcesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
		resourcesIndex.setPrimarySearchField("label");
//		resourcesIndex.setSortField("pagerank");
		Index classesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_classes");
		Index propertiesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_properties");
		
		
		Knowledgebase kb = new RemoteKnowledgebase(endpoint, "DBpedia Live", "TODO", resourcesIndex, propertiesIndex, classesIndex, null);
		stbl = new SPARQLTemplateBasedLearner2(kb);
		stbl.setUseIdealTagger(USE_IDEAL_TAGGER);
		
		init();
		
		prefixMap = new HashMap<String, String>();
		prefixMap.put("rdf", RDF.getURI());
		prefixMap.put("rdfs", RDFS.getURI());
//		prefixMap.put("onto", "http://dbpedia.org/ontology/");
//		prefixMap.put("prop", "http://dbpedia.org/property/");
		prefixMap.put("dbo", "http://dbpedia.org/ontology/");
		prefixMap.put("dbp", "http://dbpedia.org/property/");
		prefixMap.put("res", "http://dbpedia.org/resource/");
		prefixMap.put("foaf", FOAF.getURI());
		prefixMap.put("yago", "http://dbpedia.org/class/yago/");
		
		
	}
	
	public void init() throws FileNotFoundException, IOException{
		//load properties for evaluation
		Properties props = new Properties();
		props.load(new FileInputStream(this.getClass().getClassLoader().getResource(PROPERTIES_PATH).getPath()));
		
		String endpointURL = props.getProperty("endpointURL", "http://live.dbpedia.org/sparql");
		String defaultGraphURI = props.getProperty("defaultGraphURI", "http://live.dbpedia.org");
		endpoint = new SparqlEndpoint(new URL(endpointURL), Collections.singletonList(defaultGraphURI), Collections.<String>emptyList());
		// TODO: use aksw-commons-sparql instead of sparql-scala
//		this.endpoint = new CachingSparqlEndpoint(new HttpSparqlEndpoint(endpointURL, defaultGraphURI), "cache");
		try {
			stbl.setEndpoint(new org.dllearner.kb.sparql.SparqlEndpoint(
					new URL(endpointURL), Collections.singletonList(defaultGraphURI), Collections.<String>emptyList()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		boolean useRemoteEndpointValidation = Boolean.parseBoolean(props.getProperty("useRemoteEndpointValidation", "True"));
		stbl.setUseRemoteEndpointValidation(useRemoteEndpointValidation);
		
		int maxTestedQueriesPerTemplate = Integer.parseInt(props.getProperty("maxTestedQueriesPerTemplate", "25"));
		stbl.setMaxTestedQueriesPerTemplate(maxTestedQueriesPerTemplate);
		
		int maxQueryExecutionTimeInSeconds = Integer.parseInt(props.getProperty("maxQueryExecutionTimeInSeconds", "20"));
		stbl.setMaxQueryExecutionTimeInSeconds(maxQueryExecutionTimeInSeconds);
		
		loadAnswers();
	}
	
	private void readQueries(File file){
		logger.info("Reading file containing queries and answers...");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			int id;
			String question;
			String query;
			Set<String> answers;
			
			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
				//read question ID
				id = Integer.valueOf(questionNode.getAttribute("id"));
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//Read SPARQL query
				query = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
//				//Read answers
//				answers = new HashSet<String>();
//				NodeList aswersNodes = questionNode.getElementsByTagName("answer");
//				for(int j = 0; j < aswersNodes.getLength(); j++){
//					Element answerNode = (Element) aswersNodes.item(j);
//					answers.add(((Element)answerNode.getElementsByTagName("uri").item(0)).getChildNodes().item(0).getNodeValue().trim());
//				}
				
				id2Question.put(id, question);
				id2Query.put(id, query);
//				question2Answers.put(question, answers);
				
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
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, String> e : id2Question.entrySet()){
			sb.append(e.getKey()+ ": " + extractSentence(e.getValue()) + "\n");
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("questions.txt"));
			out.write(sb.toString());
			out.close();
			} 
			catch (IOException e) 
			{ 
			System.out.println("Exception ");

			}
		logger.info("Done.");
	}
	
	private void loadAnswers(){
		int questionId;
		String question;
		Object answer;
		for(Entry<Integer, String> entry : id2Query.entrySet()){
			if(testID != -1 && entry.getKey() != testID || (exclusions.contains(entry.getKey())))continue;
			questionId = entry.getKey();
			question = entry.getValue();
			try {
				answer = getAnswerForSPARQLQuery(question, "uri");
				id2Answer.put(questionId, answer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Set<String> getResources(String query){
		Set<String> resources = new HashSet<String>();
		
		// TODO: use aksw-commons-sparql instead of sparql-scala
		ResultSet rs = executeSelect(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.getResource("uri").getURI());
		}
		
		return resources;
	}
	
	private Object getAnswerForSPARQLQuery(String query, String targetVar){
		logger.debug("Query: " + query);
		Object answer = null;
		
		// TODO: use aksw-commons-sparql instead of sparql-scala
		if(query.contains("ASK")){
			answer = executeAsk(query);
		}  else {
			answer = new HashSet<String>();
			if(!query.contains("LIMIT")){
				query = query + " LIMIT 500";
			}
			ResultSet rs = executeSelect(query);
			
			String variable;
			if(rs.getResultVars().size() == 1){
				variable = rs.getResultVars().get(0);
			} else {
				variable = targetVar;
			}
			variable = rs.getResultVars().get(0);
			
			QuerySolution qs;
			RDFNode node;
			while(rs.hasNext()){
				qs = rs.next();
				node = qs.get(variable);
				if(node != null){
					if(node.isURIResource()){
						((HashSet)answer).add(node.asResource().getURI());
					} else if(node.isLiteral()){
						((HashSet)answer).add(node.asLiteral().getLexicalForm());
					}
				}
			}
		}
		logger.debug("Answer: " + answer);
		
		return answer;
	}
	
	private ResultSet executeSelect(String query){
		return SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
	}
	
	private Boolean executeAsk(String query){
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		return queryExecution.execAsk();
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		// TODO: use aksw-commons-sparql instead of sparql-scala
		/*
		this.endpoint = endpoint;
		try {
			stbl.setEndpoint(new org.dllearner.kb.sparql.SparqlEndpoint(
					new URL(endpoint.id().substring(endpoint.id().indexOf("_")+1)), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void setUseRemoteValidation(boolean useRemoteValidation){
		stbl.setUseRemoteEndpointValidation(useRemoteValidation);
	}
	
	public void setMaxQueryExecutionTimeInSeconds(int maxQueryExecutionTimeInSeconds){
		stbl.setMaxQueryExecutionTimeInSeconds(maxQueryExecutionTimeInSeconds);
	}
	
	public void setMaxTestedQueriesPerTemplate(int maxTestedQueriesPerTemplate) {
		stbl.setMaxTestedQueriesPerTemplate(maxTestedQueriesPerTemplate);
	}
	
	
	public void run(){
		int topN2Print = 10;
		int correctAnswers = 0;
		
		int questionId = -1;
		String question = "";
		String targetQuery;
		Object targetAnswer;
		double precision = -1;
		double recall = -1;
		String errorCode = "";
		LatexWriter latex = new LatexWriter();
		int i = 0;
		int cnt = 0;
		for(Entry<Integer, String> entry : id2Question.entrySet()){//if(entry.getKey()==50)continue;
			if((testID != -1 && entry.getKey() != testID) || (exclusions.contains(entry.getKey()))) continue;
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				targetQuery = id2Query.get(questionId);
				targetAnswer = id2Answer.get(questionId);
				precision = -1;
				recall = -1;
				errorCode = "";
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("QUESTION: " + question + "\n");
				logger.info("TARGET QUERY:\n" + targetQuery + "\n");
				
				
				//write new section for query
				latex.beginSection(extractSentence(question), questionId);
				//write subsection for target
				latex.beginSubsection("Target");
				//write subsubsection for target query
				latex.beginSubSubsection("Query");
				latex.addListing(targetQuery);
				//write subsubsection for target result
				latex.beginSubSubsection("Result" + ((targetAnswer instanceof Collection<?>) ? "(" + ((Collection)targetAnswer).size()+")" : ""));
				latex.addText(escapeAnswerString(targetAnswer));
				
				//set the question
				stbl.setQuestion(question);
				//start learning
				stbl.learnSPARQLQueries();
				//get the best learned query
				String learnedQuery = stbl.getBestSPARQLQuery();
				//get result for best learned query if exists
				Object learnedAnswer = null;
				if(learnedQuery != null){
					learnedAnswer = getAnswerForSPARQLQuery(learnedQuery, "y");
				}
				//get the used templates
				List<Template> templates = new ArrayList<Template>(stbl.getTemplates());
				
				if(stbl.getLearnedPosition() == -1 || stbl.getLearnedPosition() > 10){
					cnt++;
				}
				i++;
				
				//start output
				//write templates subsection
				latex.beginSubsection("Templates (" + templates.size() + ")");
				latex.beginEnumeration();
				for(Template t : templates){
					latex.beginEnumerationItem();
					latex.addListing(t.toString());
					latex.endEnumerationItem();
				}
				latex.endEnumeration();
				
				//get the generated SPARQL query candidates
				Set<WeightedQuery> generatedQueries = stbl.getGeneratedQueries(15);
				
				//write generated queries subsection
				latex.beginSubsection("Top " + 15 + " generated queries");
				if(!generatedQueries.isEmpty()){
					latex.beginEnumeration();
					for(WeightedQuery wQ : generatedQueries){
						latex.beginEnumerationItem();
						com.hp.hpl.jena.query.Query q = QueryFactory.create(wQ.getQuery().toString(), Syntax.syntaxARQ);
						if(q.toString().contains("http://dbpedia.org/property/")){
							q.setPrefix("dbp", "http://dbpedia.org/property/");
						}
						if(q.toString().contains("http://dbpedia.org/ontology/")){
							q.setPrefix("dbo", "http://dbpedia.org/ontology/");
						}
						if(q.toString().contains("http://dbpedia.org/resource/")){
							q.setPrefix("dbr", "http://dbpedia.org/resource/");
						}
						String queryString = q.toString();
						queryString = queryString + "\n" + "Score(" + wQ.getScore() + ")";
						latex.addListing(queryString);
						latex.endEnumerationItem();
					}
					latex.endEnumeration();
				}
				
				//get the URIs for each template slot
				latex.beginSubsection("Covered entities");
				Map<Slot, List<String>> slot2URIsMap = stbl.getSlot2URIs();
//				Map<List<String>, List<String>> tokens2URIs = new HashMap<List<String>, List<String>>();
//				for(Entry<Slot, List<String>> slot2URI : slot2URIsMap.entrySet()){
//					tokens2URIs.put(slot2URI.getKey().getWords(), value)
//					if(slot2URI.getValue().contains(getFullEntity(entity))){
//						covered = true;
//						break;
//					}
//				}
				List<String> targetEntities = extractEntities(targetQuery);
				Map<String, Boolean> coveredEntitiesMap = new HashMap<String, Boolean>();
				for(String entity : targetEntities){
					boolean covered = false;
					for(Entry<Slot, List<String>> slot2URI : slot2URIsMap.entrySet()){
						if(slot2URI.getValue().contains(getFullURI(entity))){
							covered = true;
							break;
						}
					}
					
					coveredEntitiesMap.put(entity, covered);
					if(!covered){
//						errorCode = "NE";
					}
				}
				latex.beginSubSubsection("Target entities");
				StringBuilder sb = new StringBuilder();
				sb.append("\\begin{tabular}{| l | c |}\\hline\n");
				for(Entry<String, Boolean> e : coveredEntitiesMap.entrySet()){
					sb.append(escapeString(e.getKey())).append(" & ").append(e.getValue()).append("\\\\\\hline\n");
				}
				sb.append("\\end{tabular}\n");
				latex.addText(sb.toString());
				latex.beginSubSubsection("Keyword -> URIs");
				sb = new StringBuilder();
				sb.append("\\begin{tabular}{| l | p{10cm} |}\\hline\n");
				for(Entry<Slot, List<String>> slot2URI : slot2URIsMap.entrySet()){
					if(!slot2URI.getKey().getWords().isEmpty()){
						StringBuilder uris = new StringBuilder();
						for(String uri : slot2URI.getValue()){
							uris.append(escapeString(getPrefixedURI(uri))).append(", ");
						}
						StringBuilder slotWords = new StringBuilder();
						slotWords.append("[");
						for(String word : slot2URI.getKey().getWords()){
							slotWords.append(escapeString(word)).append(", ");
						}
						slotWords.append("]");
						sb.append(slotWords.toString() + "[" + slot2URI.getKey().getSlotType() + "]").append(" & ").append(uris.toString()).append("\\\\\\hline\n");
					}
				}
				sb.append("\\end{tabular}\n");
				latex.addText(sb.toString());
				
				//write solution subsection if exists
				if(learnedQuery != null){
					latex.beginSubsection("Solution");
					latex.beginSubSubsection("Query");
					latex.addListing(learnedQuery);
					latex.beginSubSubsection("Result" + ((learnedAnswer instanceof Collection<?>) ? "(" + ((Collection)learnedAnswer).size()+")" : ""));
					latex.addText(escapeAnswerString(learnedAnswer, targetAnswer));
					precision = computePrecision(targetAnswer, learnedAnswer);
					recall = computeRecall(targetAnswer, learnedAnswer);
					if(precision == 1 && recall == 1){
						correctAnswers++;
					}
				}
				latex.addSummaryTableEntry(questionId, extractSentence(question), precision, recall, errorCode);
				
			} catch (NoTemplateFoundException e) {cnt++;
				e.printStackTrace();
				logger.error("Template generation failed");
				errorCode = "NT";
				latex.addSummaryTableEntry(questionId, extractSentence(question), precision, recall, errorCode);
			} catch(Exception e){cnt++;
				e.printStackTrace();
				errorCode = "ERR";
				logger.error("ERROR");
				latex.addSummaryTableEntry(questionId, extractSentence(question), precision, recall, errorCode);
			}
		}
		System.out.println(cnt + "/" + i);
		latex.write("log/evaluation_" + System.currentTimeMillis()+  ".tex", Calendar.getInstance().getTime().toString(), correctAnswers);
	}
	
	public static List<String> extractEntities(String query){
		List<String> exclusions = Arrays.asList(new String[]{"rdf", "rdfs"});
		List<String> entities = new ArrayList<String>();
		//pattern to detect resources
		Pattern pattern = Pattern.compile("(\\w+):(\\w+)");
		Matcher matcher = pattern.matcher(query);
		String group;
		while(matcher.find()){
			group = matcher.group();
			boolean add = true;
			for(String ex : exclusions){
				if(group.contains(ex)){
					add = false;
					break;
				}
			}
			if(add){
				entities.add(group);
			}
		}
		//pattern to detect string literals
		pattern = Pattern.compile("'(\\w+)'@en");
		matcher = pattern.matcher(query);
		while(matcher.find()){
			group = matcher.group();
			entities.add(buildEntityFromLabel(group));
		}
		
		return entities;
	}
	
	private static String buildEntityFromLabel(String label){
		String base = "res:";
		String entity = label.substring(1).substring(0, label.lastIndexOf("'")-1).replace(" ", "_");
		return base + entity;
	}
	
	private String getFullURI(String prefixedURI){
		String fullURI = prefixedURI;
		String prefix;
		String uri;
		for(Entry<String, String> prefix2URI : prefixMap.entrySet()){
			prefix = prefix2URI.getKey();
			uri = prefix2URI.getValue();
			if(prefixedURI.startsWith(prefix)){
				fullURI = prefixedURI.replace(prefix + ":", uri);
				break;
			}
		}
		return fullURI;
	}
	
	private String getPrefixedURI(String fullURI){
		String prefixedURI = fullURI;
		String prefix;
		String uri;
		for(Entry<String, String> prefix2URI : prefixMap.entrySet()){
			prefix = prefix2URI.getKey();
			uri = prefix2URI.getValue();
			if(fullURI.startsWith(uri)){
				prefixedURI = fullURI.replace(uri, prefix + ":" );
				break;
			}
		}
		return prefixedURI;
	}
	
	private double computeRecall(Object targetAnswer, Object learnedAnswer){
		if(learnedAnswer == null){
			return -1;
		}
		double recall = 0;
		if(targetAnswer instanceof Collection<?> && learnedAnswer instanceof Collection<?>){
			Set<String> targetAnswerColl = new HashSet<String>((Collection<? extends String>) targetAnswer);
			Set<String> learnedAnswerColl = new HashSet<String>((Collection<? extends String>) learnedAnswer);
			int targetSize = targetAnswerColl.size();
			targetAnswerColl.retainAll(learnedAnswerColl);
			recall = (double)targetAnswerColl.size() / (double)targetSize;
			recall = Math.round( recall * 100. ) / 100.;
		} else {
			if(targetAnswer.equals(learnedAnswer)){
				recall = 1;
			} else {
				recall = 0;
			}
		}
		return recall;
	}
	
	private double computePrecision(Object targetAnswer, Object learnedAnswer){
		if(learnedAnswer == null){
			return -1;
		}
		double precision = 0;
		if(targetAnswer instanceof Collection<?> && learnedAnswer instanceof Collection<?>){
			Set<String> targetAnswerColl = new HashSet<String>((Collection<? extends String>) targetAnswer);
			Set<String> learnedAnswerColl = new HashSet<String>((Collection<? extends String>) learnedAnswer);
			int learnedSize = learnedAnswerColl.size();
			targetAnswerColl.retainAll(learnedAnswerColl);
			precision = (double)targetAnswerColl.size() / (double)learnedSize;
			precision = Math.round( precision * 100. ) / 100.;
		} else {
			if(targetAnswer.equals(learnedAnswer)){
				precision = 1;
			} else {
				precision = 0;
			}
		}
		return precision;
	}
	
	public void run_without_testing_answer(){
		int topN2Print = 25;
		
		
		int questionId;
		String question;
		String query;
		Object answer;
		LatexWriter latex = new LatexWriter();
		latex.beginDocument();
		int i = 0;
		for(Entry<Integer, String> entry : id2Question.entrySet()){
			if(entry.getKey() != testID)continue;
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				query = id2Query.get(questionId);
				answer = id2Answer.get(questionId);
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("QUESTION: " + question + "\n");
				logger.info("TARGET QUERY:\n" + query + "\n");
				
				
				//write new section for query
				latex.beginSection(question, questionId);
				//write subsection for target
				latex.beginSubsection("Target");
				//write subsubsection for target query
				latex.beginSubSubsection("Query");
				latex.addListing(query);
				//write subsubsection for target result
				latex.beginSubSubsection("Result" + ((answer instanceof Collection<?>) ? "(" + ((Collection)answer).size()+")" : ""));
				latex.addText(escapeAnswerString(answer));
				
				//set the question
				stbl.setQuestion(question);
				//get the generated SPARQL query candidates
				stbl.learnSPARQLQueries();
				List<String> queries = stbl.getGeneratedSPARQLQueries();
				//get the used templates
				Set<Template> templates = stbl.getTemplates();
				
				//start output
				//write templates subsection
				latex.beginSubsection("Templates (" + templates.size() + ")");
				latex.beginEnumeration();
				for(Template t : templates){
					latex.beginEnumerationItem();
					latex.addListing(t.toString());
					latex.endEnumerationItem();
				}
				latex.endEnumeration();
				
				//write generated queries subsection
				latex.beginSubsection("Top " + topN2Print + " generated queries per template");
				logger.info("LEARNED QUERIES(#" + queries.size() + "):\n");
				int cnt = 1;
				if(!queries.isEmpty()){
					latex.beginEnumeration();
				}
				//print queries to log file
				for(String q : queries){
					logger.info("QUERY " + cnt++ + ":\n" + q + "\n");
					logger.info("--------");
				}
				//print top n queries to latex file
				int max = Math.min(topN2Print, queries.size());
				for(int j = 0; j < max; j++){
					latex.beginEnumerationItem();
					latex.addListing(queries.get(j));
					latex.endEnumerationItem();
				}
				if(!queries.isEmpty()){
					latex.endEnumeration();
				}
				
				
			} catch (NoTemplateFoundException e) {
				e.printStackTrace();
				logger.error("Template generation failed");
			} catch(Exception e){
				e.printStackTrace();
				logger.error("ERROR");
			}
		}
		latex.endDocument();
		latex.write("log/evaluation_" + System.nanoTime() + ".tex", Calendar.getInstance().getTime().toString(), 0);
	}

	private String escapeAnswerString(Object learnedAnswer, Object targetAnswer){
		if(learnedAnswer instanceof Collection<?>){
			Collection<?> target = (Collection<?>) targetAnswer; 
			StringBuilder sb = new StringBuilder();
			try {
				int i = 1;
				for(String s : (Collection<String>)learnedAnswer){
					if(target.contains(s)){
						s = "\\textcolor{green}{" + s + "}";
					}
//					sb.append(URLDecoder.decode(s, "UTF-8").replace("_", "\\_").replace("http://dbpedia.org/resource/", "")).append(", ");
					sb.append(s.replace("_", "\\_").replace("&", "\\&").replace("%", "\\%").replace("#", "\\#").replace("http://dbpedia.org/resource/", "")).append(", ");
					if(i % 2 == 0){
						sb.append("\n");
					}
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sb.toString();
		} else {
			return learnedAnswer.toString();
		}
		
	}
	
	private String escapeString(String str){
		return str.replace("_", "\\_").replace("&", "\\&").replace("%", "\\%").replace("#", "\\#");
	}
	
	private String escapeAnswerString(Object learnedAnswer){
		if(learnedAnswer instanceof Collection<?>){
			StringBuilder sb = new StringBuilder();
			try {
				int i = 1;
				for(String s : (Collection<String>)learnedAnswer){
//					sb.append(URLDecoder.decode(s, "UTF-8").replace("_", "\\_").replace("http://dbpedia.org/resource/", "")).append(", ");
					sb.append(s.replace("_", "\\_").replace("&", "\\&").replace("%", "\\%").replace("#", "\\#").replace("http://dbpedia.org/resource/", "")).append(", ");
					if(i % 2 == 0){
						sb.append("\n");
					}
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sb.toString();
		} else {
			return learnedAnswer.toString();
		}
		
	}
	
	private String extractSentence(String taggedSentence){
    	int pos = taggedSentence.indexOf("/");
    	while(pos != -1){
    		String first = taggedSentence.substring(0, pos);
    		int endPos = taggedSentence.substring(pos).indexOf(" ");
    		if(endPos == -1){
    			endPos = taggedSentence.substring(pos).length();
    		}
    		String rest = taggedSentence.substring(pos + endPos);
    		
    		taggedSentence = first + rest;
    		pos = taggedSentence.indexOf("/");
    		
    	}
    	return taggedSentence;
    	
    }
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.INFO);
		Logger.getLogger(Evaluation.class).setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Layout layout = new PatternLayout("%m%n");
		FileAppender fileAppender = new FileAppender(
				layout, "log/evaluation.log", false);
		fileAppender.setThreshold(Level.INFO);
		Logger.getRootLogger().addAppender(fileAppender);
		Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
		
		if(args.length == 0){
			System.out.println("Usage: Evaluation <file>");
			System.exit(0);
		}
		File file = new File(Evaluation.class.getClassLoader().getResource(args[0]).getPath());
		
//		System.out.println(Evaluation.extractEntities("SELECT DISTINCT ?uri ?string WHERE {" +
//				"?uri rdf:type onto:Person ." +
//				"?uri onto:birthPlace ?city ." +
//				"?city rdfs:label 'Heraklion'@en" +
//				"OPTIONAL {?uri rdfs:label ?string . " +
//				"FILTER (lang(?string) = 'en') }" +
//				"}}")
//		);
		
		Evaluation eval = new Evaluation(file);
		eval.run();

	}

}
