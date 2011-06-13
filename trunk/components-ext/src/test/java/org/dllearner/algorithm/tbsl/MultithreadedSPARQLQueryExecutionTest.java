package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dllearner.algorithm.tbsl.util.MultithreadedSPARQLQueryExecutor;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class MultithreadedSPARQLQueryExecutionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("src/main/resources/tbsl/evaluation/dbpedia-train.xml");
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

		List<String> queries = new ArrayList<String>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			String query;
			for (int i = 0; i < questionNodes.getLength(); i++) {
				Element questionNode = (Element) questionNodes.item(i);
				// Read SPARQL query
				query = ((Element) questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0)
						.getNodeValue().trim();
				queries.add(query);
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
		
		int threadCount = 5;
		MultithreadedSPARQLQueryExecutor executor = new MultithreadedSPARQLQueryExecutor(endpoint, threadCount);
		List<List<String>> lists = splitList(queries, threadCount);
		long startTime = System.currentTimeMillis();
		for(List<String> list : lists){
			executor.executeQueries(list);
		}
		System.out.format("Multithreaded needed %d ms.\n", System.currentTimeMillis()-startTime);
		executor.close();
		
		
		startTime = System.currentTimeMillis();
		for(String query : queries){
			executeSPARQLQuery(endpoint, query);
		}
		System.out.format("Sequentially needed %d ms.", System.currentTimeMillis()-startTime);	
	}
	
	public static <T> List<List<T>> splitList(List<T> list, int splitSize){
		List<List<T>> lists = new ArrayList<List<T>>();
		int partitionCount = list.size()/splitSize + 1;
		for(int partition = 0; partition < partitionCount; partition++){
			int start = partition * splitSize;
			int end = Math.min(start + splitSize, list.size());
			lists.add(list.subList(start, end));
		}
		return lists;
	}
	
	public static void executeSPARQLQuery(SparqlEndpoint endpoint, String query){
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}
		
		ResultSet rs = null;
		if(query.contains("SELECT")){
			rs = queryExecution.execSelect();
		} else if(query.contains("ASK")){
			queryExecution.execAsk();
		}
	}

}
