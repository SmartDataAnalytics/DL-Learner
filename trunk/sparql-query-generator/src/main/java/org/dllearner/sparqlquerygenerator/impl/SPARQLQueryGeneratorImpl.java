/**
 * Copyright (C) 2007-2010, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.sparqlquerygenerator.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGenerator;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGenerator;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class SPARQLQueryGeneratorImpl implements SPARQLQueryGenerator{
	
	private Logger logger = Logger.getLogger(SPARQLQueryGeneratorImpl.class);
	private Monitor queryMonitor = MonitorFactory.getTimeMonitor("SPARQL Query monitor");
	
	private String endpointURL;
	
	private Set<String> posExamples;
	private Set<String> negExamples;
	
	private int recursionDepth = 2;
	
	private int maxModelSizePerExample = 3000;
	
	private final static int LIMIT = 1000;
	private final static int OFFSET = 1000;
	
	private Set<QueryTree<String>> posQueryTrees;
	private Set<QueryTree<String>> negQueryTrees;
	
	private List<String> result = new LinkedList<String>();
	
	private QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
	
	
	
	
	public SPARQLQueryGeneratorImpl(String endpointURL){
		this.endpointURL = endpointURL;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples) {
		return getSPARQLQueries(posExamples, false);
	}
	
	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples,
			boolean learnFilters) {
		this.posExamples = posExamples;
		negExamples = new HashSet<String>();
		
		buildQueryTrees();
		learnPosOnly();
		return result;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples, Set<String> negExamples) {
		return getSPARQLQueries(posExamples, negExamples, false);
	}
	
	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples,
			Set<String> negExamples, boolean learnFilters) {
		if(negExamples.isEmpty()){
			return getSPARQLQueries(posExamples, learnFilters);
		}
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		buildQueryTrees();
		learnPosNeg();
		
		return result;
	}
	
	/**
	 * Here we build the initial Query graphs for the positive and negative examples.
	 */
	private void buildQueryTrees(){
		posQueryTrees = new HashSet<QueryTree<String>>();
		negQueryTrees = new HashSet<QueryTree<String>>();
		
		QueryTree<String> tree;
		//build the query graphs for the positive examples
		for(String example : posExamples){
			tree = getQueryTreeForExample(example);
			posQueryTrees.add(tree);
		}
		//build the query graphs for the negative examples
		for(String example : negExamples){
			tree = getQueryTreeForExample(example);
			negQueryTrees.add(tree);
		}
		
		logger.debug("Overall query time: " + queryMonitor.getTotal());
		logger.debug("Average query time: " + queryMonitor.getAvg());
		logger.debug("Longest time for query: " + queryMonitor.getMax());
		logger.debug("Shortest time for query: " + queryMonitor.getMin());
		
	}
	
	private void learnPosOnly(){
		logger.debug("Computing LGG ...");
		Monitor monitor = MonitorFactory.getTimeMonitor("LGG monitor");
		
		monitor.start();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posQueryTrees);
		
		monitor.stop();
		
		logger.debug("LGG");
		logger.debug(lgg.getStringRepresentation());
		
		logger.debug("LGG computation time: " + monitor.getTotal() + " ms");
		
		result.add(lgg.toSPARQLQueryString());
	}
	
	private void learnPosNeg(){
		logger.debug("Computing LGG ...");
		Monitor lggMonitor = MonitorFactory.getTimeMonitor("LGG monitor");
		
		lggMonitor.start();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posQueryTrees);
		
		lggMonitor.stop();
		
		logger.debug("LGG");
		logger.debug(lgg.getStringRepresentation());
		
		logger.debug("LGG computation time: " + lggMonitor.getTotal() + " ms");
		
		Monitor nbrMonitor = MonitorFactory.getTimeMonitor("NBR monitor");
		
		nbrMonitor.start();
		
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>();
		QueryTree<String> nbr = nbrGenerator.getNBR(lgg, negQueryTrees);
		
		for(QueryTree<String> n : nbrGenerator.getNBRs(lgg, negQueryTrees)){
			n.dump();
		}
		
		nbrMonitor.stop();
		
		logger.debug("NBR");
		logger.debug(nbr.getStringRepresentation());
		
		logger.debug("Time to make NBR: " + nbrMonitor.getTotal() + " ms");
		
		result.add(nbr.toSPARQLQueryString());
	}
	
	
//	/**
//	 * Creating the Query graph for the given example.
//	 * @param example The example for which a Query graph is created.
//	 * @return The resulting Query graph.
//	 */
//	private QueryTree<String> getQueryTreeForExample(String example){
//		Query query = makeConstructQuery(example);
//		logger.debug("Sending SPARQL query ...");
//		queryMonitor.start();
//		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
//		Model model = qexec.execConstruct();
//		queryMonitor.stop();
//		qexec.close();
//		logger.debug("Returned " + model.size() + " triple");
//		QueryTree<String> tree = factory.getQueryTree(example, model);
//		return tree;
//	}
	
	/**
	 * Creates the Query tree for the given example.
	 * @param example The example for which a Query tree is created.
	 * @return The resulting Query tree.
	 */
	private QueryTree<String> getQueryTreeForExample(String example){
		Model model = getModelForExample(example, maxModelSizePerExample);
//		Model model = getModelForExampleIncremantally(example);
		logger.debug("Returned " + model.size() + " triple");
		QueryTree<String> tree = factory.getQueryTree(example, model);
		return tree;
	}
	
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQuery(String example, int limit, int offset){
		logger.debug("Building SPARQL CONSTRUCT query for example " + example);
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(example).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		
		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property\"))");
		sb.append("}\n");
		sb.append("ORDER BY ");
		for(int i = 0; i < recursionDepth; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		sb.append("\n");
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		logger.debug("Query: \n" + sb.toString());
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	/**
	 * A SPARQL DESCRIBE query is created, to get a RDF graph for the given resource
	 * @param example The resource for which a DESCRIBE query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeDescribeQuery(String resource){
		logger.debug("Building SPARQL DESCRIBE query for resource " + resource);
		
		StringBuilder sb = new StringBuilder();
		sb.append("DESCRIBE ").append("<").append(resource).append(">");
		logger.debug("Query: \n" + sb.toString());
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	private Model getModelForExample(String example, int maxSize){
		Query query = makeConstructQuery(example, LIMIT, 0);
		logger.debug("Sending SPARQL query ...");
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
		Model all = ModelFactory.createDefaultModel();
		Model model = qexec.execConstruct();
		all.add(model);
		queryMonitor.stop();
		qexec.close();
		int i = 1;
		while(model.size() != 0 && all.size() < maxSize){
			query = makeConstructQuery(example, LIMIT, i * OFFSET);
			logger.debug("Sending SPARQL query ...");
			queryMonitor.start();
			qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
			model = qexec.execConstruct();
			all.add(model);
			queryMonitor.stop();
			qexec.close();
			i++;
		}
		return all;
	}
	
	private Model getModelForExampleIncrementally(String example){
		Query query = makeDescribeQuery(example);
		logger.debug("Sending SPARQL query ...");
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
		Model model = qexec.execDescribe();
		Statement st;
		for(Iterator<Statement> i = model.listStatements(); i.hasNext();){
			st = i.next();
			if(st.getObject().isURIResource()){
				query = makeDescribeQuery(st.getObject().toString());
				qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
				qexec.execDescribe(model);
			}
		}
		return model;
	}
	
}
