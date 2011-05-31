package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.core.Oracle;
import org.dllearner.core.owl.Individual;
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
	
	private Map<Integer, String> id2Question = new Hashtable<Integer, String>();
	private Map<Integer, String> id2Query = new Hashtable<Integer, String>();
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
		logger.info("Query: " + query);
		Object answer = null;
		
		if(query.contains("ASK")){
			answer = endpoint.executeAsk(query);
		} else if(query.contains("COUNT")){
			
		} else {
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
		logger.info("Answer: " + answer);
		return answer;
	}
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public void setUseRemoteValidation(boolean useRemoteValidation){
		stbl.setUseRemoteEndpointValidation(useRemoteValidation);
	}
	
	public void run(){
		SPARQLTemplateBasedLearner stbl = new SPARQLTemplateBasedLearner();
		int failed = 0;
		int learnedCnt = 0;
		int learnedCorrectlyCnt = 0;
		int questionId;
		String question;
		Object answer;
		for(Entry<Integer, String> entry : id2Question.entrySet()){
			try {
				questionId = entry.getKey();
				question = entry.getValue();
				answer = id2Answer.get(questionId);
				//set the question
				stbl.setQuestion(question);
				//start learning
				stbl.learnSPARQLQueries();
				String learnedQuery = stbl.getBestSPARQLQuery();
				//get result for best learned query
				Object learnedAnswer = getAnswerForSPARQLQuery(learnedQuery, "y");
				//compare to answers in target query
				if(learnedAnswer.equals(answer)){
					learnedCorrectlyCnt++;
				} else {
					learnedCnt++;
				}
			} catch (NoTemplateFoundException e) {
				e.printStackTrace();
				failed++;
			} catch(Exception e){
				e.printStackTrace();
				failed++;
			}
		}
		logger.info("Could generate SPARQL queries for " + learnedCnt + "/" + id2Question.size() 
				+ " question from which " + learnedCorrectlyCnt  +  " are the correct answer.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("src/main/resources/tbsl/evaluation/dbpedia-train.xml");
		SparqlEndpoint endpoint = new CachingSparqlEndpoint(new HttpSparqlEndpoint("http://live.dbpedia.org/sparql/", "http://dbpedia.org/sparql"), "cache");
		
		Evaluation eval = new Evaluation(file);
		eval.setEndpoint(endpoint);
		eval.setUseRemoteValidation(true);
		eval.init();
		eval.run();

	}

}
