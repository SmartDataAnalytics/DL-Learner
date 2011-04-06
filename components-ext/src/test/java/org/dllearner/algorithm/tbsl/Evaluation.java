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

public class Evaluation implements Oracle{
	
	private static Logger logger = Logger.getLogger(Evaluation.class);
	
	private File evaluationFile;
	
	private Map<String, String> question2query = new Hashtable<String, String>();
	private SortedMap<String, Set<String>> question2Answers = new TreeMap<String, Set<String>>();
	
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
			String question;
			String query;
			Set<String> answers;
			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
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
				
				question2query.put(question, query);
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
		String question;
		Set<String> answers;
		for(Entry<String, String> entry : question2query.entrySet()){
			question = entry.getKey();
			try {
				answers = getResources(entry.getValue());
				question2Answers.put(question, answers);
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
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public void setUseRemoteValidation(boolean useRemoteValidation){
		stbl.setUseRemoteEndpointValidation(useRemoteValidation);
	}
	
	public void run(){
		int cnt = 0;
		String question;
		for(Entry<String, Set<String>> entry : question2Answers.entrySet()){
			try {
				question = entry.getKey();
				SPARQLTemplateBasedLearner stbl = new SPARQLTemplateBasedLearner();
				stbl.setQuestion(question);
				stbl.setOracle(this);
				stbl.learnSPARQLQueries();
				List<String> queries = stbl.getCurrentlyBestSPARQLQueries(1);
				System.out.println(queries);
			} catch (NoTemplateFoundException e) {
				e.printStackTrace();
				cnt++;
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<Double> classifyIndividuals(List<Individual> individuals) {
		// TODO Auto-generated method stub
		return null;
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
