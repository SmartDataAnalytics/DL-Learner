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

import java.util.ArrayList;
import java.util.HashSet;
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
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.rdf.model.Model;
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
	
	private ModelGenerator modelGen;
	
	private Set<String> posExamples;
	private Set<String> negExamples;
	
	private Set<QueryTree<String>> posQueryTrees;
	private Set<QueryTree<String>> negQueryTrees;
	
	private List<String> resultQueries = new ArrayList<String>();
	
	private List<QueryTree<String>> resultTrees = new ArrayList<QueryTree<String>>();
	
	private QueryTreeFactory<String> factory = new QueryTreeFactoryImpl();
	
	private QueryTree<String> lgg;
	
	
	
	
	public SPARQLQueryGeneratorImpl(String endpointURL){
		this.endpointURL = endpointURL;
		modelGen = new ModelGenerator(endpointURL);
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
		return resultQueries;
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
		
		return resultQueries;
	}
	
	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples) {
		return getSPARQLQueries(posExamples, false);
	}

	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples,
			boolean learnFilters) {
		if(negExamples.isEmpty()){
			return getSPARQLQueries(posExamples, learnFilters);
		}
		this.posQueryTrees = new HashSet<QueryTree<String>>(posExamples);
		this.negExamples = new HashSet<String>();
		
		learnPosNeg();
		
		return resultQueries;
	}

	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples,
			List<QueryTree<String>> negExamples) {
		return getSPARQLQueries(posExamples, negExamples, false);
	}

	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples,
			List<QueryTree<String>> negExamples, boolean learnFilters) {
		if(negExamples.isEmpty()){
			return getSPARQLQueries(posExamples, learnFilters);
		}
		this.posQueryTrees = new HashSet<QueryTree<String>>(posExamples);
		this.negQueryTrees = new HashSet<QueryTree<String>>(negExamples);
		
		learnPosNeg();
		
		return resultQueries;
	}
	
	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(Set<String> posExamples) {
		return getSPARQLQueryTrees(posExamples, false);
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(Set<String> posExamples,
			boolean learnFilters) {
		this.posExamples = posExamples;
		negExamples = new HashSet<String>();
		
		buildQueryTrees();
		learnPosOnly();
		return resultTrees;
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(Set<String> posExamples,
			Set<String> negExamples) {
		return getSPARQLQueryTrees(posExamples, negExamples, false);
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(Set<String> posExamples,
			Set<String> negExamples, boolean learnFilters) {
		if(negExamples.isEmpty()){
			return getSPARQLQueryTrees(posExamples, learnFilters);
		}
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		buildQueryTrees();
		learnPosNeg();
		
		return resultTrees;
	}
	
	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(
			List<QueryTree<String>> posExamples) {
		return getSPARQLQueryTrees(posExamples, false);
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(
			List<QueryTree<String>> posExamples, boolean learnFilters) {
		this.posQueryTrees = new HashSet<QueryTree<String>>(posExamples);
		negExamples = new HashSet<String>();
		
		learnPosOnly();
		
		return resultTrees;
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(
			List<QueryTree<String>> posExamples,
			List<QueryTree<String>> negExamples) {
		return getSPARQLQueryTrees(posExamples, negExamples, false);
	}

	@Override
	public List<QueryTree<String>> getSPARQLQueryTrees(
			List<QueryTree<String>> posExamples,
			List<QueryTree<String>> negExamples, boolean learnFilters) {
		if(negExamples.isEmpty()){
			return getSPARQLQueryTrees(posExamples, learnFilters);
		}
		this.posQueryTrees = new HashSet<QueryTree<String>>(posExamples);
		this.negQueryTrees = new HashSet<QueryTree<String>>(negExamples);
		
		learnPosNeg();
		
		return resultTrees;
	}
	
	@Override
	public QueryTree<String> getLastLGG(){
		return lgg;
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
			tree = getQueryTree(example);
			posQueryTrees.add(tree);
		}
		//build the query graphs for the negative examples
		for(String example : negExamples){
			tree = getQueryTree(example);
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
		lgg = lggGenerator.getLGG(posQueryTrees);
		
		monitor.stop();
		
		logger.debug("LGG");
		logger.debug(lgg.getStringRepresentation());
		
		logger.debug("LGG computation time: " + monitor.getTotal() + " ms");
		
		
		resultQueries.add(lgg.toSPARQLQueryString(true));
		resultTrees.add(lgg);
	}
	
	private void learnPosNeg(){
		logger.info("Computing LGG ...");
		Monitor lggMonitor = MonitorFactory.getTimeMonitor("LGG monitor");
		
		lggMonitor.start();
		
		LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
		QueryTree<String> lgg = lggGenerator.getLGG(posQueryTrees);
		
		lggMonitor.stop();
		
		logger.info("LGG");
		logger.info(lgg.getStringRepresentation());
		
		logger.info("LGG computation time: " + lggMonitor.getTotal() + " ms");
		
		Monitor nbrMonitor = MonitorFactory.getTimeMonitor("NBR monitor");
		
		nbrMonitor.start();
		
		NBRGenerator<String> nbrGenerator = new NBRGeneratorImpl<String>();
//		QueryTree<String> nbr = nbrGenerator.getNBR(lgg, negQueryTrees);
		
		
		int i = 1;
		for(QueryTree<String> nbr : nbrGenerator.getNBRs(lgg, negQueryTrees)){
			logger.info("NBR " + i++);
			logger.info(nbr.getStringRepresentation());
			resultQueries.add(nbr.toSPARQLQueryString(true));
			resultTrees.add(nbr);
		}
		
		nbrMonitor.stop();
		
		logger.info("Time to make NBR: " + nbrMonitor.getTotal() + " ms");
		
	}
	
	/**
	 * Creates the Query tree for the given example.
	 * @param example The example for which a Query tree is created.
	 * @return The resulting Query tree.
	 */
	private QueryTree<String> getQueryTree(String resource){
		Model model = modelGen.createModel(resource, ModelGenerator.Strategy.CHUNKS, 2);
		logger.debug("Returned " + model.size() + " triple");
		QueryTree<String> tree = factory.getQueryTree(resource, model);
		return tree;
	}


}
