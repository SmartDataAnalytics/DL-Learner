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
package org.dllearner.algorithms.qtl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.h2.CacheCoreH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.collections15.ListUtils;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.exception.EmptyLGGException;
import org.dllearner.algorithms.qtl.exception.NegativeTreeCoverageExecption;
import org.dllearner.algorithms.qtl.exception.TimeOutException;
import org.dllearner.algorithms.qtl.filters.QueryTreeFilter;
import org.dllearner.algorithms.qtl.filters.QuestionBasedQueryTreeFilter;
import org.dllearner.algorithms.qtl.operations.NBR;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.SparqlQueryLearningAlgorithm;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.CachingConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
@ComponentAnn(name="query tree learner", shortName="qtl", version=0.8)
public class QTL extends AbstractCELA implements SparqlQueryLearningAlgorithm {

	private static final Logger logger = Logger.getLogger(QTL.class);

	private LearningProblem learningProblem;
	private SparqlEndpointKS endpointKS;
	//	private QTLConfigurator configurator;

	private SparqlEndpoint endpoint;
	private Model model;
	private QueryExecutionFactory qef;
	private String cacheDirectory;

	private QueryTreeCache treeCache;

	private LGGGenerator<String> lggGenerator;
	private NBR<String> nbr;

	private List<String> posExamples;
	private List<String> negExamples;

	private List<QueryTree<String>> posExampleTrees;
	private List<QueryTree<String>> negExampleTrees;

	private QueryTreeFilter queryTreeFilter;

	private ConciseBoundedDescriptionGenerator cbdGenerator;

	private int maxExecutionTimeInSeconds = 60;
	private int maxQueryTreeDepth = 2;

	private QueryTree<String> lgg;
	private SortedSet<String> lggInstances;

	private Set<String> objectNamespacesToIgnore = new HashSet<String>();
	private Set<String> allowedNamespaces = new HashSet<String>();
	private Map<String, String> prefixes = new HashMap<String, String>();
	private boolean enableNumericLiteralFilters = false;

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds(10));
		options.add(new IntegerConfigOption("maxQueryTreeDepth", "recursion depth of query tree extraction", 2));
		return options;
	}

	//	public QTL() {
	//	}

	public QTL(AbstractLearningProblem learningProblem, SparqlEndpointKS endpointKS) throws LearningProblemUnsupportedException{
		this(learningProblem, endpointKS, null);
	}

	public QTL(AbstractLearningProblem learningProblem, SparqlEndpointKS endpointKS, String cacheDirectory) throws LearningProblemUnsupportedException{
		if(!(learningProblem instanceof PosOnlyLP || learningProblem instanceof PosNegLP)){
			throw new LearningProblemUnsupportedException(learningProblem.getClass(), getClass());
		}
		this.learningProblem = learningProblem;
		this.endpointKS = endpointKS;
		this.cacheDirectory = cacheDirectory;
	}

	public QTL(SPARQLEndpointEx endpoint, String cacheDirectory) {
		this.endpoint = endpoint;
		this.cacheDirectory = cacheDirectory;

		treeCache = new QueryTreeCache();
		cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(endpoint, cacheDirectory));
		cbdGenerator.setRecursionDepth(maxQueryTreeDepth);

		lggGenerator = new LGGGeneratorImpl<String>();
		nbr = new NBR<String>(endpoint, cacheDirectory);
		nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);

		posExampleTrees = new ArrayList<QueryTree<String>>();
		negExampleTrees = new ArrayList<QueryTree<String>>();
	}

	public QTL(SparqlEndpointKS endpointKS, String cacheDirectory) {
		this.endpointKS = endpointKS;
		this.cacheDirectory = cacheDirectory;

		treeCache = new QueryTreeCache();
		cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(endpoint, cacheDirectory));
		cbdGenerator.setRecursionDepth(maxQueryTreeDepth);

		lggGenerator = new LGGGeneratorImpl<String>();
		nbr = new NBR<String>(endpoint, cacheDirectory);
		nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);

		posExampleTrees = new ArrayList<QueryTree<String>>();
		negExampleTrees = new ArrayList<QueryTree<String>>();
	}

	public QTL(Model model) {
		this.model = model;

		treeCache = new QueryTreeCache();
		cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(model));
		cbdGenerator.setRecursionDepth(maxQueryTreeDepth);

		lggGenerator = new LGGGeneratorImpl<String>();
		nbr = new NBR<String>(model);
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

		String question;
		if(negExamples.isEmpty()){
			question = nbr.getQuestion(lgg, negExampleTrees, getKnownResources());
		} else {
			question = nbr.getQuestion(lgg, negExampleTrees, getKnownResources());
		}


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
		//		cbdGenerator.setRecursionDepth(maxQueryTreeDepth);
	}

	public int getMaxQueryTreeDepth() {
		return maxQueryTreeDepth;
	}

	public void setPrefixes(Map<String, String> prefixes) {
		this.prefixes = prefixes;
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	public String getSPARQLQuery(){
		if(lgg == null){
			lgg = lggGenerator.getLGG(getQueryTrees(posExamples));
		}
		return lgg.toSPARQLQueryString();
	}

	public void setObjectNamespacesToIgnore(Set<String> namespacesToIgnore){
		this.objectNamespacesToIgnore = namespacesToIgnore;
	}

	public void setRestrictToNamespaces(List<String> namespaces){
		cbdGenerator.setRestrictToNamespaces(namespaces);
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
			try {
				logger.debug("Generating tree for " + resource);
				model = cbdGenerator.getConciseBoundedDescription(resource);
				applyFilters(model);
				tree = treeCache.getQueryTree(resource, model);
				if(logger.isDebugEnabled()){
					logger.debug("Tree for resource " + resource);
					logger.debug(tree.getStringRepresentation());

				}
				trees.add(tree);
			} catch (Exception e) {
				logger.error("Failed to create tree for resource " + resource + ".", e);
			}
		}
		return trees;
	}

	private void applyFilters(Model model){
		Statement st;
		for(StmtIterator iter = model.listStatements(); iter.hasNext();){
			st = iter.next();
			for(String ns : objectNamespacesToIgnore){
				if(st.getObject().isURIResource() && st.getObject().asResource().getURI().startsWith(ns)){
					iter.remove();
					break;
				}
			}
		}
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
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();

		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.getResource("x0").getURI());
		}
		qe.close();
		return resources;
	}

	private String getDistinctSPARQLQuery(QueryTree<String> tree){
		String query = tree.toSPARQLQueryString();
		//		query = "SELECT DISTINCT " + query.substring(7);
		return query;
	}

	//	@Override
	//	public void start(){
	//		generatePositiveExampleTrees();
	//		
	//		lgg = lggGenerator.getLGG(posExampleTrees);
	//		
	//		if(queryTreeFilter != null){
	//			lgg = queryTreeFilter.getFilteredQueryTree(lgg);
	//		}
	//		if(logger.isDebugEnabled()){
	//			logger.debug("LGG: \n" + lgg.getStringRepresentation());
	//		}
	//		if(logger.isInfoEnabled()){
	//			logger.info("Generated SPARQL query:\n" + lgg.toSPARQLQueryString(true, enableNumericLiteralFilters, prefixes));
	//		}
	//	}

	@Override
	public void start(){
		//build the query trees for the positive examples
		generatePositiveExampleTrees();

		//compute the LGG
		lgg = lggGenerator.getLGG(posExampleTrees);
		if(queryTreeFilter != null){
			lgg = queryTreeFilter.getFilteredQueryTree(lgg);
		}
		if(logger.isDebugEnabled()){
			logger.debug("LGG: \n" + lgg.getStringRepresentation());
		}
		if(logger.isInfoEnabled()){
			logger.info("Generated SPARQL query:\n" + lgg.toSPARQLQueryString(true, enableNumericLiteralFilters, prefixes));
		}

		//build the query trees for the negative examples
		if(!negExamples.isEmpty()){
			generateNegativeExampleTrees();

			try {
				//check if the LGG covers a negative example
				int index = coversNegativeQueryTree(lgg);
				if(index != -1){
					throw new NegativeTreeCoverageExecption(negExamples.get(index));
				}

				lggInstances = getResources(lgg);
				nbr.setLGGInstances(lggInstances);

				String question;
				if(negExamples.isEmpty()){
					question = nbr.getQuestion(lgg, negExampleTrees, getKnownResources());
				} else {
					question = nbr.getQuestion(lgg, negExampleTrees, getKnownResources());
				}
				logger.info("Question:\n" + question);
			} catch (NegativeTreeCoverageExecption e) {
				e.printStackTrace();
			} catch (TimeOutException e) {
				e.printStackTrace();
			}
		}
	}

	public void setEnableNumericLiteralFilters(boolean enableNumericLiteralFilters) {
		this.enableNumericLiteralFilters = enableNumericLiteralFilters;
	}

	public boolean isEnableNumericLiteralFilters() {
		return enableNumericLiteralFilters;
	}

	@Override
	public List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries) {
		return Collections.singletonList(getBestSPARQLQuery());
	}

	@Override
	public String getBestSPARQLQuery() {
		return lgg.toSPARQLQueryString();
	}

	public void init() {// TODO: further improve code quality 
		//	private QTL() {
		if(endpointKS==null)
		{
			qef = new QueryExecutionFactoryModel(this.model);
			cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(model));
			nbr = new NBR<String>(model);
		}
		else
		{	
			if(endpointKS.isRemote())
			{
				SparqlEndpoint endpoint = endpointKS.getEndpoint();
				QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
				if(cacheDirectory != null){
					try {
						long timeToLive = TimeUnit.DAYS.toMillis(30);
						CacheBackend cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
						CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);
						qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				//			qef = new QueryExecutionFactoryPaginated(qef, 10000);
			} else
			{
				qef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)endpointKS).getModel());
			}
		} 

		if(learningProblem instanceof PosOnlyLP){
			this.posExamples = convert(((PosOnlyLP)learningProblem).getPositiveExamples());
			this.negExamples = new ArrayList<String>();
		} else if(learningProblem instanceof PosNegLP){
			this.posExamples = convert(((PosNegLP)learningProblem).getPositiveExamples());
			this.negExamples = convert(((PosNegLP)learningProblem).getNegativeExamples());
		}
		treeCache = new QueryTreeCache();
		treeCache.addAllowedNamespaces(allowedNamespaces);

		if(endpointKS==null) {}
		else
		{
			nbr = new NBR<String>(endpoint);
			nbr.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
			
			if(endpointKS instanceof LocalModelBasedSparqlEndpointKS){
				cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(((LocalModelBasedSparqlEndpointKS) endpointKS).getModel()));
			} else {
				endpoint = endpointKS.getEndpoint();
				cbdGenerator = new CachingConciseBoundedDescriptionGenerator(new ConciseBoundedDescriptionGeneratorImpl(endpoint, endpointKS.getCache()));
			}
		}
		cbdGenerator.setRecursionDepth(maxQueryTreeDepth);

		lggGenerator = new LGGGeneratorImpl<String>();

		

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

	public QueryTree<String> getLgg() {
		return lgg;
	}

	@Autowired
	public void setLearningProblem(LearningProblem learningProblem) {
		this.learningProblem = learningProblem;
	}

	public SparqlEndpointKS getEndpointKS() {
		return endpointKS;
	}

	@Autowired
	public void setEndpointKS(SparqlEndpointKS endpointKS) {
		this.endpointKS = endpointKS;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#stop()
	 */
	@Override
	public void stop() {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestDescription()
	 */
	@Override
	public Description getCurrentlyBestDescription() {
		return (lgg == null) ? null : DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(lgg.asOWLClassExpression());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestEvaluatedDescription()
	 */
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return null;
	}

	/**
	 * @param allowedNamespaces the allowedNamespaces to set
	 */
	public void setAllowedNamespaces(Set<String> allowedNamespaces) {
		this.allowedNamespaces = allowedNamespaces;
	}

	public static void main(String[] args) throws Exception {
		Set<String> positiveExamples = new HashSet<String>();
		positiveExamples.add("http://dbpedia.org/resource/Liverpool_F.C.");
		positiveExamples.add("http://dbpedia.org/resource/Chelsea_F.C.");

		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks.init();
		PosOnlyLP lp = new PosOnlyLP();	
		lp.setPositiveExamples(Helper.getIndividualSet(positiveExamples));
		QTL qtl = new QTL(lp, ks, "cache");
		qtl.setAllowedNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"));
		qtl.addQueryTreeFilter(new QuestionBasedQueryTreeFilter(Arrays.asList("soccer club", "Premier League")));
		qtl.init();
		qtl.start();
		String query = qtl.getBestSPARQLQuery();
		System.out.println(query);
		System.out.println(qtl.getCurrentlyBestDescription());
	}

}
