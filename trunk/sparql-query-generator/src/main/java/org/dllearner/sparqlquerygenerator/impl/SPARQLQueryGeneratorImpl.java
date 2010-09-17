package org.dllearner.sparqlquerygenerator.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGenerator;
import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SPARQLQueryGeneratorImpl implements SPARQLQueryGenerator{
	
	private Logger logger = Logger.getLogger(SPARQLQueryGeneratorImpl.class);
	private Monitor queryMonitor = MonitorFactory.getTimeMonitor("SPARQL Query monitor");
	
	private String endpointURL;
	
	private Set<String> posExamples;
	private Set<String> negExamples;
	
	private int recursionDepth = 3;
	
	private Set<QueryGraph> posQueryGraphs;
	private Set<QueryGraph> negQueryGraphs;
	
	
	public SPARQLQueryGeneratorImpl(String endpointURL){
		this.endpointURL = endpointURL;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples) {
		this.posExamples = posExamples;
		
		negExamples = new HashSet<String>();
		
		init();
		
		return null;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples,
			Set<String> negExamples) {
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		init();
		
		return null;
	}
	
	/**
	 * Here we build the initial Query graphs for the positive and negative examples.
	 */
	private void init(){
		posQueryGraphs = new HashSet<QueryGraph>();
		negQueryGraphs = new HashSet<QueryGraph>();
		
		QueryGraph graph;
		//build the query graphs for the positive examples
		for(String example : posExamples){
			graph = getQueryGraphForExample(example);
			posQueryGraphs.add(graph);
		}
		//build the query graphs for the negative examples
		for(String example : negExamples){
			graph = getQueryGraphForExample(example);
			negQueryGraphs.add(graph);
		}
		
		logger.debug("Overall query time: " + queryMonitor.getTotal());
		logger.debug("Average query time: " + queryMonitor.getAvg());
		logger.debug("Longest time for query: " + queryMonitor.getMax());
		logger.debug("Shortest time for query: " + queryMonitor.getMin());
		
	}
	
	private void learn(){
		logger.debug("Start learning ...");
		
	}
	
	/**
	 * Creating the Query graph for the given example.
	 * @param example The example for which a Query graph is created.
	 * @return The resulting Query graph.
	 */
	private QueryGraph getQueryGraphForExample(String example){
		Query query = makeConstructQuery(example);
		logger.debug("Sending SPARQL query ...");
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
		Model model = qexec.execConstruct();
		queryMonitor.stop();
		qexec.close();
		
		QueryGraph graph = new QueryGraphFactoryImpl().getQueryGraph();
		Node rootNode = graph.createNode(example);
		graph.setRootNode(rootNode);
		Node sourceNode;
		Node targetNode;
		Edge edge;
		for(Iterator<Statement> it = model.listStatements(); it.hasNext();){
			Statement s = it.next();
			if(!s.getObject().isLiteral()){
				sourceNode = graph.createNode(s.getSubject().toString());
				targetNode = graph.createNode(s.getObject().toString());
				edge = graph.createEdge(sourceNode, targetNode, s.getPredicate().toString());
			}
		}

		return graph;
		
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONTRCUT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQuery(String example){
		logger.debug("Building SPARQL CONSTRUCT query for example " + example);
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT ");
		sb.append("{\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE ");
		sb.append("{\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
//		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property/\"))");
		sb.append("}");
		logger.debug("Query: \n" + sb.toString());
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	public static void main(String[] args){
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		String endpointURL = "http://dbpedia-live.openlinksw.com/sparql/";
		String defaultGraphURI = "http://dbpedia.org";
		
		Set<String> posExamples = new HashSet<String>();
		
		posExamples.add("http://dbpedia.org/resource/Leipzig");
		posExamples.add("http://dbpedia.org/resource/Dresden");
		posExamples.add("http://dbpedia.org/resource/Chemnitz");
		
//		posExamples.add("http://dbpedia.org/resource/Berlin");
//		posExamples.add("http://dbpedia.org/resource/Hamburg");
//		posExamples.add("http://dbpedia.org/resource/Bremen");
		
//		posExamples.add("http://dbpedia.org/resource/Angela_Merkel");
//		posExamples.add("http://dbpedia.org/resource/Helmut_Kohl");
//		posExamples.add("http://dbpedia.org/resource/Konrad_Adenauer");
		
		SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(endpointURL);
		
		List<String> result = gen.getSPARQLQueries(posExamples);
		
	}
	

}
