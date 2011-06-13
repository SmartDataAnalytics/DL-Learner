package org.dllearner.algorithm.tbsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.commons.sparql.core.SparqlEndpoint;
import org.aksw.commons.sparql.core.decorator.CachingSparqlEndpoint;
import org.aksw.commons.sparql.core.impl.HttpSparqlEndpoint;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.util.LatexWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Evaluation{
	
	private static Logger logger = Logger.getLogger(Evaluation.class);
	
	private File evaluationFile;
	
	private SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
	private SortedMap<Integer, Object> id2Answer = new TreeMap<Integer, Object>();
	
	private SparqlEndpoint endpoint;
	
	private SPARQLTemplateBasedLearner stbl;
	
	public Evaluation(File ... evaluationFiles){
		for(File file : evaluationFiles){
			readQueries(file);
		}
		stbl = new SPARQLTemplateBasedLearner();
	}
	
	public void init(){
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
		logger.info("Done.");
	}
	
	private void loadAnswers(){
		int questionId;
		String question;
		Object answer;
		for(Entry<Integer, String> entry : id2Query.entrySet()){
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
		
		ResultSet rs = endpoint.executeSelect(query);
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
		
		if(query.contains("ASK")){
			answer = endpoint.executeAsk(query);
		}  else {
			answer = new HashSet<String>();
			ResultSet rs = endpoint.executeSelect(query);
			String variable;
			if(rs.getResultVars().size() == 1){
				variable = rs.getResultVars().get(0);
			} else {
				variable = targetVar;
			}
			QuerySolution qs;
			RDFNode node;
			while(rs.hasNext()){
				qs = rs.next();
				node = qs.get(variable);
				if(node.isURIResource()){
					((HashSet)answer).add(node.asResource().getURI());
				} else if(node.isLiteral()){
					((HashSet)answer).add(node.asLiteral().getLexicalForm());
				}
				
			}
		}
		logger.debug("Answer: " + answer);
		return answer;
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public void setUseRemoteValidation(boolean useRemoteValidation){
		stbl.setUseRemoteEndpointValidation(useRemoteValidation);
	}
	
	
	public void run(){
		int topN2Print = 25;
		
		
		int questionId;
		String question;
		String query;
		Object answer;
		LatexWriter latex = new LatexWriter();
		latex.beginDocument();
		int i = 0;
		for(Entry<Integer, String> entry : id2Question.entrySet()){
			if(i++ == 1)break;
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				query = id2Query.get(questionId);
				answer = id2Answer.get(questionId);
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("QUESTION: " + question + "\n");
				logger.info("TARGET QUERY:\n" + query + "\n");
				
				
				//write new section for query
				latex.beginSection(question);
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
				//start learning
				stbl.learnSPARQLQueries();
				//get the best learned query
				String learnedQuery = stbl.getBestSPARQLQuery();
				//get result for best learned query if exists
				Object learnedAnswer = null;
				if(learnedQuery != null){
					learnedAnswer = getAnswerForSPARQLQuery(learnedQuery, "y");
				}
				//get the generated SPARQL query candidates
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
				latex.beginSubsection("Top " + topN2Print + " generated queries (max. " + queries.size() + ")");
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
				
				//write solution subsection if exists
				if(learnedQuery != null){
					latex.beginSubsection("Solution");
					latex.beginSubSubsection("Query");
					latex.addListing(learnedQuery);
					latex.beginSubSubsection("Result" + ((learnedAnswer instanceof Collection<?>) ? "(" + ((Collection)learnedAnswer).size()+")" : ""));
					latex.addText(escapeAnswerString(learnedAnswer, answer));
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
		latex.write("log/evaluation.tex");
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
			if(i++ == 1)break;
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				query = id2Query.get(questionId);
				answer = id2Answer.get(questionId);
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("QUESTION: " + question + "\n");
				logger.info("TARGET QUERY:\n" + query + "\n");
				
				
				//write new section for query
				latex.beginSection(question);
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
				List<String> queries = stbl.getSPARQLQueries();
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
				latex.beginSubsection("Top " + topN2Print + " generated queries (max. " + queries.size() + ")");
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
		latex.write("log/evaluation.tex");
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
					sb.append(URLDecoder.decode(s, "UTF-8").replace("_", "\\_").replace("http://dbpedia.org/resource/", "")).append(", ");
					if(i % 2 == 0){
						sb.append("\n");
					}
					i++;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return sb.toString();
		} else {
			return learnedAnswer.toString();
		}
		
	}
	
	private String escapeAnswerString(Object learnedAnswer){
		if(learnedAnswer instanceof Collection<?>){
			StringBuilder sb = new StringBuilder();
			try {
				int i = 1;
				for(String s : (Collection<String>)learnedAnswer){
					sb.append(URLDecoder.decode(s, "UTF-8").replace("_", "\\_").replace("http://dbpedia.org/resource/", "")).append(", ");
					if(i % 2 == 0){
						sb.append("\n");
					}
					i++;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return sb.toString();
		} else {
			return learnedAnswer.toString();
		}
		
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Logger.getLogger(SPARQLTemplateBasedLearner.class).setLevel(Level.OFF);
		Logger.getLogger(Evaluation.class).setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Layout layout = new PatternLayout("%m%n");
		FileAppender fileAppender = new FileAppender(
				layout, "log/evaluation.log", false);
		fileAppender.setThreshold(Level.INFO);
		Logger.getRootLogger().addAppender(fileAppender);
		
		
		File file = new File("src/main/resources/tbsl/evaluation/dbpedia-train.xml");
		SparqlEndpoint endpoint = new CachingSparqlEndpoint(new HttpSparqlEndpoint("http://live.dbpedia.org/sparql/", "http://dbpedia.org/sparql"), "cache");
		
		Evaluation eval = new Evaluation(file);
		eval.setEndpoint(endpoint);
		eval.setUseRemoteValidation(true);
		eval.init();
		eval.run();

	}

}
