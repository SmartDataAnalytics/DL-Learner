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
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGeneratorCached;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGenerator;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.NBRStrategy;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class SPARQLQueryGeneratorCachedImpl implements SPARQLQueryGeneratorCached{
	
	private Logger logger = Logger.getLogger(SPARQLQueryGeneratorCachedImpl.class);
	
	private LGGGenerator<String> lggGenerator;
	private NBRGenerator<String> nbrGenerator;
	
	private List<QueryTree<String>> posQueryTrees;
	private List<QueryTree<String>> negQueryTrees;
	
	private List<String> resultQueries = new ArrayList<String>();
	
	private List<QueryTree<String>> resultTrees = new ArrayList<QueryTree<String>>();
	
	private QueryTree<String> lgg;
	
	private QueryTree<String> newPosExample;
	
	
	public SPARQLQueryGeneratorCachedImpl(){
		this(new BruteForceNBRStrategy());
	}
	
	public SPARQLQueryGeneratorCachedImpl(NBRStrategy nbrStrategy){
		posQueryTrees = new ArrayList<QueryTree<String>>();
		negQueryTrees = new ArrayList<QueryTree<String>>();
		
		lggGenerator = new LGGGeneratorImpl<String>();
		nbrGenerator = new NBRGeneratorImpl<String>(nbrStrategy);
		
	}
	
	public void addPosExample(QueryTree<String> tree){
		posQueryTrees.add(tree);
		newPosExample = tree;
	}
	
	public void addNegExample(QueryTree<String> tree){
		negQueryTrees.add(tree);
		newPosExample = null;
	}

	@Override
	public List<String> getSPARQLQueries() {
		if(negQueryTrees.isEmpty()){
			learnPosOnly();
		} else {
			learnPosNeg();
		}
		
		return resultQueries;
	}
	
	@Override
	public List<String> getSPARQLQueries(boolean learnFilters) {
		if(negQueryTrees.isEmpty()){
			learnPosOnly();
		} else {
			learnPosNeg();
		}
		
		return resultQueries;
	}
	
	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples) {
		return getSPARQLQueries(posExamples, false);
	}

	@Override
	public List<String> getSPARQLQueries(List<QueryTree<String>> posExamples,
			boolean learnFilters) {
		if(posExamples.size() > this.posQueryTrees.size()){
			newPosExample = posExamples.get(posExamples.size()-1);
		}
		this.posQueryTrees = posExamples;
		this.negQueryTrees = new ArrayList<QueryTree<String>>();
		
		
		learnPosOnly();
		
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
		if(posExamples.size() > this.posQueryTrees.size()){
			newPosExample = posExamples.get(posExamples.size()-1);
		}
		this.posQueryTrees = posExamples;
		this.negQueryTrees = negExamples;
		
		learnPosNeg();
		
		return resultQueries;
	}
	
	
	@Override
	public QueryTree<String> getLastLGG(){
		return lgg;
	}
	
	private void learnPosOnly(){
		resultQueries.clear();
		if(posQueryTrees.size() == 2 || newPosExample != null){
			if(logger.isDebugEnabled()){
				logger.debug("Computing LGG ...");
			}
			
			Monitor monitor = MonitorFactory.getTimeMonitor("LGG monitor");
			
			monitor.start();
			if(posQueryTrees.size() == 2){
				lgg = lggGenerator.getLGG(posQueryTrees);
			} else {
				lgg = lggGenerator.getLGG(lgg, newPosExample);
			}
			
			monitor.stop();
			
			newPosExample = null;
			
			if(logger.isDebugEnabled()){
				logger.debug("LGG");
				logger.debug(lgg.getStringRepresentation());
				logger.debug("LGG computation time: " + monitor.getTotal() + " ms");
			}
		}
		
		resultQueries.add(lgg.toSPARQLQueryString(true));
		resultTrees.add(lgg);
	}
	
	private void learnPosNeg(){
		resultQueries.clear();
		if(newPosExample != null){
			if(logger.isDebugEnabled()){
				logger.debug("Computing LGG ...");
			}
			
			Monitor lggMonitor = MonitorFactory.getTimeMonitor("LGG monitor");
			
			lggMonitor.start();
			if(lgg != null){
				lgg = lggGenerator.getLGG(lgg, newPosExample);
			} else {
				lgg = newPosExample;
			}
			
			lggMonitor.stop();
			
			newPosExample = null;
			
			if(logger.isDebugEnabled()){
				logger.debug("LGG");
				logger.debug(lgg.getStringRepresentation());
				logger.debug("LGG computation time: " + lggMonitor.getTotal() + " ms");
			}
		}
		
		
		Monitor nbrMonitor = MonitorFactory.getTimeMonitor("NBR monitor");
		
		nbrMonitor.start();
		
		int i = 1;
		for(QueryTree<String> nbr : nbrGenerator.getNBRs(lgg, negQueryTrees)){
			if(logger.isDebugEnabled()){
				logger.debug("NBR " + i++);
				logger.debug(nbr.getStringRepresentation());
			}
			resultQueries.add(nbr.toSPARQLQueryString(true));
			resultTrees.add(nbr);
		}
		
		nbrMonitor.stop();
		if(logger.isDebugEnabled()){
			logger.debug("Time to make NBR: " + nbrMonitor.getTotal() + " ms");
		}
		
	}
	
	public QueryTree<String> getCurrentQueryTree(){
		return resultTrees.get(0);
	}

}
