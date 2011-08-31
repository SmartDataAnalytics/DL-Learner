/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
package org.dllearner.algorithm.qtl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.ListUtils;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.cache.ModelCache;
import org.dllearner.algorithm.qtl.cache.QueryTreeCache;
import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithm.qtl.exception.EmptyLGGException;
import org.dllearner.algorithm.qtl.exception.NegativeTreeCoverageExecption;
import org.dllearner.algorithm.qtl.exception.TimeOutException;
import org.dllearner.algorithm.qtl.filters.QueryTreeFilter;
import org.dllearner.algorithm.qtl.operations.NBR;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithm.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.SparqlQueryLearningAlgorithm;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Helper;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * 
 * Learning algorithm for SPARQL queries based on so called query trees.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 *
 */
public class QTL extends AbstractComponent implements SparqlQueryLearningAlgorithm {
	
	private static final Logger logger = Logger.getLogger(QTL.class);
	
	private AbstractLearningProblem lp;
	private SparqlEndpointKS endpointKS;
//	private QTLConfigurator configurator;
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
	
	private QueryTreeCache treeCache;
	private ModelGenerator modelGen;
	private ModelCache modelCache;
	
	private LGGGenerator<String> lggGenerator;
	private NBR<String> nbr;
	
	private List<String> posExamples;
	private List<String> negExamples;
	
	private List<QueryTree<String>> posExampleTrees;
	private List<QueryTree<String>> negExampleTrees;
	
	private QueryTreeFilter queryTreeFilter;
	
	private int maxExecutionTimeInSeconds = 60;
	private int maxQueryTreeDepth = 2;
	
	private QueryTree<String> lgg;
	private SortedSet<String> lggInstances;

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds(10));
		options.add(new IntegerConfigOption("maxQueryTreeDepth", "recursion depth of query tree extraction", 2));
		return options;
	}
	
	public QTL(AbstractLearningProblem learningProblem, SparqlEndpointKS endpointKS) throws LearningProblemUnsupportedException{
		if(!(learningProblem instanceof PosOnlyLP || learningProblem instanceof PosNegLP)){
			throw new LearningProblemUnsupportedException(learningProblem.getClass(), getClass());
		}
		this.lp = learningProblem;
		this.endpointKS = endpointKS;
		
//		this.configurator = new QTLConfigurator(this);
	}
	
	public QTL(SPARQLEndpointEx endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
		
		treeCache = new QueryTreeCache();
		modelGen = new ModelGenerator(endpoint, endpoint.getPredicateFilters(), cache);
		modelCache = new ModelCache(modelGen);
		modelCache.setRecursionDepth(maxQueryTreeDepth);
		
		lggGenerator = new LGGGeneratorImpl<String>();
		nbr = new NBR<String>(endpoint, cache);
		nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		
		posExampleTrees = new ArrayList<QueryTree<String>>();
		negExampleTrees = new ArrayList<QueryTree<String>>();
	}
	
	public String getQuestion(List<String> posExamples, List<String> negExamples) throws EmptyLGGException, NegativeTreeCoverageExecption, TimeOutException {
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		generatePositiveExampleTrees();
		generateNegativeExampleTrees();
		
		if(negExamples.isEmpty()){
			QueryTree<String> dummyNegTree = new QueryTreeImpl<String>("?");
			dummyNegTree.addChild(new QueryTreeImpl<String>("?"), "dummy");
			negExampleTrees.add(dummyNegTree);
		}
		
		lgg = lggGenerator.getLGG(posExampleTrees);
		
		if(queryTreeFilter != null){
			lgg = queryTreeFilter.getFilteredQueryTree(lgg);
		}
		if(logger.isDebugEnabled()){
			logger.debug("LGG: \n" + lgg.getStringRepresentation());
		}
		if(lgg.isEmpty()){
			throw new EmptyLGGException();
		}
		
		int index = coversNegativeQueryTree(lgg);
		if(index != -1){
			throw new NegativeTreeCoverageExecption(negExamples.get(index));
		}
		
		lggInstances = getResources(lgg);
		nbr.setLGGInstances(lggInstances);
		
		String question = nbr.getQuestion(lgg, negExampleTrees, getKnownResources());
		
		return question;
	}
	
	public void setExamples(List<String> posExamples, List<String> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
	}
	
	public void addStatementFilter(Filter<Statement> filter){
		treeCache.setStatementFilter(filter);
	}
	
	public void addQueryTreeFilter(QueryTreeFilter queryTreeFilter){
		this.queryTreeFilter = queryTreeFilter;
	}
	
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds){
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
		nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
	}
	
	public void setMaxQueryTreeDepth(int maxQueryTreeDepth){
		this.maxQueryTreeDepth = maxQueryTreeDepth;
		modelCache.setRecursionDepth(maxQueryTreeDepth);
	}
	
	public String getSPARQLQuery(){
		if(lgg == null){
			lgg = lggGenerator.getLGG(getQueryTrees(posExamples));
		}
		return lgg.toSPARQLQueryString();
	}
	
	private void generatePositiveExampleTrees(){
		posExampleTrees.clear();
		posExampleTrees.addAll(getQueryTrees(posExamples));
	}
	
	private void generateNegativeExampleTrees(){
		negExampleTrees.clear();
		negExampleTrees.addAll(getQueryTrees(negExamples));
	}
	
	private List<QueryTree<String>> getQueryTrees(List<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		Model model;
		QueryTree<String> tree;
		for(String resource : resources){
			if(logger.isDebugEnabled()){
				logger.debug("Tree for resource " + resource);
			}
			model = modelCache.getModel(resource);
			tree = treeCache.getQueryTree(resource, model);
			if(logger.isDebugEnabled()){
				logger.debug(tree.getStringRepresentation());
			}
			trees.add(tree);
		}
		return trees;
	}
	
	private List<String> getKnownResources(){
		return ListUtils.union(posExamples, negExamples);
	}
	
//	private boolean coversNegativeQueryTree(QueryTree<String> tree){
//		for(QueryTree<String> negTree : negExampleTrees){
//			if(negTree.isSubsumedBy(tree)){
//				return true;
//			}
//		}
//		return false;
//	}
	
	private int coversNegativeQueryTree(QueryTree<String> tree){
		for(int i = 0; i < negExampleTrees.size(); i++){
			if(negExampleTrees.get(i).isSubsumedBy(tree)){
				return i;
			}
		}
		return -1;
	}
	
	private SortedSet<String> getResources(QueryTree<String> tree){
		SortedSet<String> resources = new TreeSet<String>();
		String query = getDistinctSPARQLQuery(tree);
		String result = cache.executeSelectQuery(endpoint, query);
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			resources.add(uri);
		}
		return resources;
	}
	
	private String getDistinctSPARQLQuery(QueryTree<String> tree){
		String query = tree.toSPARQLQueryString();
		query = "SELECT DISTINCT " + query.substring(7);
		return query;
	}

	@Override
	public void start(){
		generatePositiveExampleTrees();
		
		lgg = lggGenerator.getLGG(posExampleTrees);
		
		if(queryTreeFilter != null){
			lgg = queryTreeFilter.getFilteredQueryTree(lgg);
		}
		if(logger.isDebugEnabled()){
			logger.debug("LGG: \n" + lgg.getStringRepresentation());
		}
		
	}

	@Override
	public List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries) {
		return Collections.singletonList(getBestSPARQLQuery());
	}

	@Override
	public String getBestSPARQLQuery() {
		return lgg.toSPARQLQueryString();
	}

	public void init() {
		if(lp instanceof PosOnlyLP){
			this.posExamples = convert(((PosOnlyLP)lp).getPositiveExamples());
		} else if(lp instanceof PosNegLP){
			this.posExamples = convert(((PosNegLP)lp).getPositiveExamples());
			this.negExamples = convert(((PosNegLP)lp).getNegativeExamples());
		}
		endpoint = endpointKS.getEndpoint();
		
		treeCache = new QueryTreeCache();
		modelGen = new ModelGenerator(endpoint);
		modelCache = new ModelCache(modelGen);
		modelCache.setRecursionDepth(maxQueryTreeDepth);
		
		lggGenerator = new LGGGeneratorImpl<String>();
		nbr = new NBR<String>(endpoint);
		nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		
		posExampleTrees = new ArrayList<QueryTree<String>>();
		negExampleTrees = new ArrayList<QueryTree<String>>();
	}
	
	private List<String> convert(Set<Individual> individuals){
		List<String> list = new ArrayList<String>();
		for(Individual ind : individuals){
			list.add(ind.toString());
		}
		return list;
	}
	
	public static void main(String[] args) throws Exception {
		Set<String> positiveExamples = new HashSet<String>();
		positiveExamples.add("http://dbpedia.org/resource/Liverpool_F.C.");
		positiveExamples.add("http://dbpedia.org/resource/Chelsea_F.C.");
		
		ComponentManager cm = ComponentManager.getInstance();
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		ks.init();
		PosOnlyLP lp = new PosOnlyLP();	
		cm.getPool().registerComponent(lp);
		lp.setPositiveExamples(Helper.getIndividualSet(positiveExamples));
		QTL qtl = new QTL(lp, ks);
		qtl.init();
		qtl.start();
		String query = qtl.getBestSPARQLQuery();
		System.out.println(query);
	}

	
}
