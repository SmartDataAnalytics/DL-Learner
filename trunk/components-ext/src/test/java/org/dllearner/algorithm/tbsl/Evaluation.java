package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.dllearner.algorithm.tbsl.sparql.Query;
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
			if(entry.getKey() != 23)continue;
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
			if(!query.contains("LIMIT")){
				query = query + " LIMIT 200";
			}System.out.println(query);
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
		try {
			stbl.setEndpoint(new org.dllearner.kb.sparql.SparqlEndpoint(
					new URL(endpoint.id().substring(endpoint.id().indexOf("_")+1)), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void setUseRemoteValidation(boolean useRemoteValidation){
		stbl.setUseRemoteEndpointValidation(useRemoteValidation);
	}
	
	
	public void run(){
		int topN2Print = 10;
		
		
		int questionId = -1;
		String question = "";
		String targetQuery;
		Object targetAnswer;
		double precision = -1;
		double recall = -1;
		LatexWriter latex = new LatexWriter();
		int i = 0;
		for(Entry<Integer, String> entry : id2Question.entrySet()){
			if(entry.getKey() != 23)continue;
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				targetQuery = id2Query.get(questionId);
				targetAnswer = id2Answer.get(questionId);
				precision = -1;
				recall = -1;
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("QUESTION: " + question + "\n");
				logger.info("TARGET QUERY:\n" + targetQuery + "\n");
				
				
				//write new section for query
				latex.beginSection(question);
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
				Map<Template, Collection<? extends Query>> template2Queries = stbl.getTemplates2SPARQLQueries();
				
				//write generated queries subsection
				latex.beginSubsection("Top " + topN2Print + " generated queries per template");
				int k = 1;
				List<Query> queries;
				for(Template t : templates){
					latex.beginSubSubsection("Template " + k);
					queries = new ArrayList<Query>(template2Queries.get(t));
					if(!queries.isEmpty()){
						latex.beginEnumeration();
					}
					//print top n queries to latex file
					int max = Math.min(topN2Print, queries.size());
					for(int j = 0; j < max; j++){
						latex.beginEnumerationItem();
						latex.addListing(queries.get(j).toString());
						latex.endEnumerationItem();
					}
					if(!queries.isEmpty()){
						latex.endEnumeration();
					}
					k++;
				}
				
				//write solution subsection if exists
				if(learnedQuery != null){
					latex.beginSubsection("Solution");
					latex.beginSubSubsection("Query");
					latex.addListing(learnedQuery);
					latex.beginSubSubsection("Result" + ((learnedAnswer instanceof Collection<?>) ? "(" + ((Collection)learnedAnswer).size()+")" : ""));
					latex.addText(escapeAnswerString(learnedAnswer, targetAnswer));
					precision = computePrecision(targetAnswer, learnedAnswer);
					recall = computeRecall(targetAnswer, learnedAnswer);
				}
				latex.addSummaryTableEntry(questionId, question, precision, recall);
				
			} catch (NoTemplateFoundException e) {
				e.printStackTrace();
				logger.error("Template generation failed");
				latex.addSummaryTableEntry(questionId, question, precision, recall);
			} catch(Exception e){
				e.printStackTrace();
				logger.error("ERROR");
				latex.addSummaryTableEntry(questionId, question, precision, recall);
			}
		}
		latex.write("log/evaluation.tex");
	}
	
	private double computeRecall(Object targetAnswer, Object answer){
		if(answer == null){
			return -1;
		}
		double recall = 0;
		if(targetAnswer instanceof Collection<?> && answer instanceof Collection<?>){
			Set<String> targetAnswerColl = new HashSet<String>((Collection<? extends String>) targetAnswer);
			Set<String> answerColl = new HashSet<String>((Collection<? extends String>) answer);
			int targetSize = targetAnswerColl.size();
			targetAnswerColl.retainAll(answerColl);
			recall = targetAnswerColl.size() / targetSize;
		} else {
			if(targetAnswer.equals(answer)){
				recall = 1;
			} else {
				recall = 0;
			}
		}
		return recall;
	}
	
	private double computePrecision(Object targetAnswer, Object answer){
		if(answer == null){
			return -1;
		}
		double precision = 0;
		if(targetAnswer instanceof Collection<?> && answer instanceof Collection<?>){
			Set<String> targetAnswerColl = new HashSet<String>((Collection<? extends String>) targetAnswer);
			Set<String> answerColl = new HashSet<String>((Collection<? extends String>) answer);
			int learnedSize = targetAnswerColl.size();
			targetAnswerColl.retainAll(answerColl);
			precision = targetAnswerColl.size() / learnedSize;
		} else {
			if(targetAnswer.equals(answer)){
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
			if(entry.getKey() != 23)continue;
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
		SparqlEndpoint endpoint = new CachingSparqlEndpoint(new HttpSparqlEndpoint("http://139.18.2.96:8910/sparql", "http://dbpedia.org"), "cache");System.out.println(endpoint.id());
		Evaluation eval = new Evaluation(file);
		eval.setEndpoint(endpoint);
		eval.setUseRemoteValidation(true);
		eval.init();
		eval.run();

	}

}
