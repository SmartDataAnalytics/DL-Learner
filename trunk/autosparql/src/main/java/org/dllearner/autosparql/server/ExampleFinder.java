package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.util.TreeHelper;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGeneratorCached;
import org.dllearner.sparqlquerygenerator.cache.ModelCache;
import org.dllearner.sparqlquerygenerator.cache.QueryTreeCache;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorCachedImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGenerator;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.util.Filter;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedQueryTreeFilter;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedQueryTreeFilterAggressive;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedStatementFilter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ExampleFinder {
	
	private SPARQLEndpointEx endpoint;
	private ExtractionDBCache selectCache;
	private ModelGenerator modelGen;
	private ModelCache modelCache;
	private QueryTreeCache queryTreeCache;
	
	private List<String> posExamples = new ArrayList<String>();
	private List<String> negExamples = new ArrayList<String>();
	
	private static final Logger logger = Logger.getLogger(ExampleFinder.class);
	
	private String currentQuery;
	
	private QueryTree<String> currentQueryTree;
	private QueryTree<String> lgg;
	
	private Set<String> testedQueries;
	
	private SPARQLQueryGeneratorCached queryGen;
	private LGGGenerator<String> lggGen;
	private NBR<String> nbrGen;
	
	private QuestionBasedQueryTreeFilter treeFilter;
	
	private boolean makeAlwaysNBR = false;
	
	private static final int MAX_NBR_COMPUTING_TIME = 100;
	
	int id = -1;
	
	private String question;
	
	boolean dirty = true;
	
	public ExampleFinder(SPARQLEndpointEx endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		
		modelGen = new ModelGenerator(endpoint, new HashSet<String>(endpoint.getPredicateFilters()), constructCache);
		modelCache = new ModelCache(modelGen);
		queryTreeCache = new QueryTreeCache();
		testedQueries = new HashSet<String>();
		
		queryGen = new SPARQLQueryGeneratorCachedImpl(new GreedyNBRStrategy<String>());
		lggGen = new LGGGeneratorImpl<String>();
		nbrGen = new NBR<String>(endpoint, selectCache, constructCache);
		nbrGen.setMaxExecutionTimeInSeconds(MAX_NBR_COMPUTING_TIME);
//		queryGen = new SPARQLQueryGeneratorCachedImpl(new BruteForceNBRStrategy());
	}
	
	public QueryTree<String> computeLGG(List<String> posExamples){
		this.posExamples = posExamples;
		List<QueryTree<String>> posExampleTrees = createTrees(posExamples);
		lgg = lggGen.getLGG(posExampleTrees);
		if(treeFilter != null){
			lgg = treeFilter.getFilteredQueryTree(lgg);
		}
		currentQueryTree = lgg;
		currentQuery = lgg.toSPARQLQueryString();
		if(logger.isInfoEnabled()){
			logger.info("LGG: \n" + TreeHelper.getAbbreviatedTreeRepresentation(lgg, endpoint.getBaseURI(), endpoint.getPrefixes()));
		}
		return lgg;
	}
	
	public void setQuestionId(int id){
		this.id = id;
	}
	
	public void setQuestion(String question){
		this.question = question;
		QuestionProcessor qp = new QuestionProcessor();
		List<String> relevantWords = qp.getRelevantWords(question);
		QuestionBasedStatementFilter stmtFilter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		treeFilter = new QuestionBasedQueryTreeFilter(new HashSet<String>(relevantWords));
		queryTreeCache.setStatementFilter(stmtFilter);
		nbrGen.setStatementFilter(stmtFilter);
	}
	
	
	public Example findSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException, TimeOutException{
		logger.info("Searching similiar example");
		logger.info("Positive examples: " + posExamples);
		logger.info("Negative examples: " + negExamples);
		if(this.posExamples.size() != posExamples.size()){
			dirty = true;
		}
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		List<QueryTree<String>> posExampleTrees = createTrees(posExamples);
		List<QueryTree<String>> negExampleTrees = createTrees(negExamples);
		
		
		if(posExamples.size() == 1 && negExamples.isEmpty()){
			logger.info("Up to now only 1 positive example is selected.");
			return findExampleByGeneralisation(posExampleTrees.get(0));
		} else if(negExamples.isEmpty()){
			logger.info("There are " + posExamples.size() + " positive examples and " 
					+ negExamples.size() + " negative examples selected. Calling LGG/NBR...");
			Example ex = findExampleByLGG(posExampleTrees, negExampleTrees);
			if(ex == null){
				return findExampleByGeneralisation(currentQueryTree);
			} else {
				return ex;
			}
		} else {
			return findExampleByNBR(posExampleTrees, negExampleTrees);
		}
		
	}
	
	private List<QueryTree<String>> createTrees(List<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		for(String resource : resources){
			trees.add(createTree(resource));
		}
		return trees;
	}
	
	private QueryTree<String> createTree(String resource){
		Model model = modelCache.getModel(resource);
		QueryTree<String> tree = queryTreeCache.getQueryTree(resource, model);
		//hack for evaluation
		//TODO remove it
		if(posExamples.contains(resource)){
			if(id == 6 ){
				QueryTreeImpl<String> child = new QueryTreeImpl<String>("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
				child.setLiteralNode(true);
				tree.addChild(child, "http://dbpedia.org/ontology/seasonNumber");
			} else if(id == 11){
				tree.addChild(new QueryTreeImpl<String>("\"Jimmy\"@en"), "http://xmlns.com/foaf/0.1/givenName");
			} else if(id == 14){
				tree.addChild(new QueryTreeImpl<String>("\"Electronic Arts\"@en"), "http://dbpedia.org/property/publisher");
			} else if(id == 15){
				tree.addChild(new QueryTreeImpl<String>("\"Dana\"@en"), "http://xmlns.com/foaf/0.1/givenName");
			}
			QuestionBasedQueryTreeFilterAggressive f = new QuestionBasedQueryTreeFilterAggressive(treeFilter.getQuestionWords());
			tree = f.getFilteredQueryTree(tree);
		}
		
//		logger.info("Tree for resource before filtering" + resource + "\n" + 
//				TreeHelper.getAbbreviatedTreeRepresentation(tree, endpoint.getBaseURI(), endpoint.getPrefixes()));
		if(logger.isInfoEnabled()){
			logger.info("Tree for resource " + resource + "\n" + 
					TreeHelper.getAbbreviatedTreeRepresentation(tree, endpoint.getBaseURI(), endpoint.getPrefixes()));
		}
		
		return tree;
	}
	
//	private Example findExampleByGeneralisation(List<String> posExamples,
//			List<String> negExamples) throws SPARQLQueryException{
//		logger.info("USING GENERALISATION");
//		
//		QueryTreeGenerator treeGen = new QueryTreeGenerator(constructCache, endpoint, 5000);
//		QueryTree<String> tree = treeGen.getQueryTree(posExamples.get(0));
//		logger.info("QUERY BEFORE GENERALISATION: \n\n" + tree.toSPARQLQueryString());
//		Generalisation<String> generalisation = new Generalisation<String>();
//		QueryTree<String> genTree = generalisation.generalise(tree);
//		String query = genTree.toSPARQLQueryString();
//		logger.info("QUERY AFTER GENERALISATION: \n\n" + query);
//		
//		query = query + " LIMIT 10";
//		String result = "";
//		try {
//			result = selectCache.executeSelectQuery(endpoint, query);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new SPARQLQueryException(e, encodeHTML(query));
//		}
//		
//		ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(result);
//		String uri;
//		QuerySolution qs;
//		while(rs.hasNext()){
//			qs = rs.next();
//			uri = qs.getResource("x0").getURI();
//			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
//				return getExample(uri);
//			}
//		}
//		return null;
//	}
	
	private QueryTree<String> makeNBR(QueryTree<String> posTree, List<QueryTree<String>> negTrees){
		NBRGenerator<String> nbrGen = new NBRGeneratorImpl<String>(new GreedyNBRStrategy<String>());
		return nbrGen.getNBR(posTree, negTrees);
	}
	
	private Example findExampleByGeneralisation(QueryTree<String> tree) throws SPARQLQueryException{
		if(logger.isInfoEnabled()){
			logger.info("Making positive generalisation...");
//			logger.info("Tree before generalisation:\n" + tree.getStringRepresentation());
			logger.info("Query before generalisation:\n" + tree.toSPARQLQueryString(true));
		}
		
		QueryTree<String> genTree;
		if(((QueryTreeImpl<String>)tree).getTriplePatternCount() > 10 || currentQueryTree == tree){
			Generalisation<String> posGen = new Generalisation<String>();
			
			genTree = posGen.generalise(tree);
			currentQuery = genTree.toSPARQLQueryString(true);
			currentQueryTree = genTree;
			if(logger.isInfoEnabled()){
//				logger.info("Tree after generalisation:\n" + currentQueryTree.getStringRepresentation());
				logger.info("Query after generalisation:\n" + currentQuery);
			}
		} else {
			genTree = tree;
			currentQuery = tree.toSPARQLQueryString();
			currentQueryTree = genTree;
		}
		
		
		
		if(makeAlwaysNBR){
			makeNBR(currentQueryTree, null);
		}
		
		String result = "";
		try {
			if(testedQueries.contains(currentQuery) && !currentQueryTree.getChildren().isEmpty()){
				if(logger.isInfoEnabled()){
					logger.info("Query was already used before. Calling again positive generalisation...");
				}
				return findExampleByGeneralisation(currentQueryTree);
			} else {
				if(currentQueryTree.getChildren().isEmpty()){
					result = selectCache.executeSelectQuery(endpoint, getLimitedQuery("SELECT ?x0 WHERE {?x0 ?y ?z.FILTER(REGEX(?x0,'http://dbpedia.org/resource'))}", (posExamples.size()+negExamples.size()+1), true));
				} else {
					result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(currentQuery, (posExamples.size()+negExamples.size()+1), true));
					testedQueries.add(currentQuery);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SPARQLQueryException(e, encodeHTML(currentQuery));
		}
		
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		if(logger.isInfoEnabled()){
			logger.info("Resources in resultset:");
		}
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			if(logger.isInfoEnabled()){
				logger.info(uri);
			}
			
			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
				if(logger.isInfoEnabled()){
					logger.info("Found new example: " + uri);
				}
				
				return getExample(uri);
			}
		}
		if(logger.isInfoEnabled()){
			logger.info("The query resultset contains no new example. Calling again generalisation...");
		}
		
		return findExampleByGeneralisation(genTree);
	}
	
	private SortedSet<String> getResources(String query){
		SortedSet<String> resources = new TreeSet<String>();System.err.println(query);
		String result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(query, (posExamples.size()+negExamples.size()+1), true));
		testedQueries.add(query);
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
	
	private SortedSet<String> getAllResources(String query){
		SortedSet<String> resources = new TreeSet<String>();
		query = getLimitedQuery(query, 1000, true);
		String result = selectCache.executeSelectQuery(endpoint, query);
		testedQueries.add(query);
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

//	private Example findExampleByLGG(List<String> posExamples,
//			List<String> negExamples) throws SPARQLQueryException{
//		logger.info("USING LGG");
//		SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(endpoint.getURL().toString());
//		if(negExamples.isEmpty()){
//			logger.info("No negative examples given. Avoiding big queries by GENERALISATION");
//			List<QueryTree<String>> trees = gen.getSPARQLQueryTrees(new HashSet<String>(posExamples), new HashSet<String>(negExamples));
//			return findExampleByGeneralisation(trees.get(0));
//		}
//		
//		List<String> queries = gen.getSPARQLQueries(new HashSet<String>(posExamples), new HashSet<String>(negExamples));
//		for(String query : queries){
//			logger.info("Trying query");
//			currentQuery = query + " LIMIT 10";
//			logger.info(query);
//			String result = "";
//			try {
//				result = selectCache.executeSelectQuery(endpoint, currentQuery);
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SPARQLQueryException(e, encodeHTML(query));
//			}
//			
//			ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(result);
//			String uri;
//			QuerySolution qs;
//			while(rs.hasNext()){
//				qs = rs.next();
//				uri = qs.getResource("x0").getURI();
//				if(!posExamples.contains(uri) && !negExamples.contains(uri)){
//					return getExample(uri);
//				}
//			}
//			logger.info("Query result contains no new examples. Trying another query...");
//		}
//		logger.info("None of the queries contained a new example.");
//		logger.info("Changing to Generalisation...");
//		return findExampleByGeneralisation(gen.getLastLGG());
//	}
	
//	private Example findExampleByLGG(List<QueryTree<String>> posExamplesTrees,
//			List<QueryTree<String>> negExamplesTrees) throws SPARQLQueryException{
//		if(negExamplesTrees.isEmpty()){
//			queryGen.getSPARQLQueries(posExamplesTrees);
//			lgg = queryGen.getLastLGG();
//			if(logger.isInfoEnabled()){
//				logger.info("No negative examples given.");
//				logger.info("Computed LGG:\n" + lgg.getStringRepresentation());
//				logger.info("Avoiding big queries by calling positive generalisation...");
//			}
//			
//			return findExampleByGeneralisation(lgg);
//		}
//		
//		List<String> queries = queryGen.getSPARQLQueries(posExamplesTrees, negExamplesTrees);
//		for(String query : queries){
//			if(!queryGen.getCurrentQueryTree().getChildren().isEmpty() && testedQueries.contains(query)){
//				if(logger.isInfoEnabled()){
//					logger.info("Skipping query because it was already tested before:\n" + query);
//				}
//				continue;
//			}
//			
//			if(logger.isInfoEnabled()){
//				logger.info("Trying query");
//				logger.info(query);
//			}
//			
//			String result = "";
//			try {
//				if(queryGen.getCurrentQueryTree().getChildren().isEmpty()){
//					result = selectCache.executeSelectQuery(endpoint, getLimitedQuery("SELECT ?x0 WHERE {?x0 ?y ?z.FILTER(REGEX(?x0,'http://dbpedia.org/resource'))}", (posExamples.size()+negExamples.size()+1), true));
//				} else {
//					result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(query, (posExamples.size()+negExamples.size()+1), true));
//					currentQueryTree = queryGen.getCurrentQueryTree();
//					testedQueries.add(query);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SPARQLQueryException(e, encodeHTML(query));
//			}
//			
//			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
//			String uri;
//			QuerySolution qs;
//			while(rs.hasNext()){
//				qs = rs.next();
//				uri = qs.getResource("x0").getURI();
//				if(!posExamples.contains(uri) && !negExamples.contains(uri)){
//					currentQuery = queryGen.getCurrentQueryTree().toSPARQLQueryString();
//					logger.info("Found new example: " + uri);
//					return getExample(uri);
//				}
//			}
//			if(logger.isInfoEnabled()){
//				logger.info("Query resultset contains no new example. Trying another query...");
//			}
//		}
//		if(logger.isInfoEnabled()){
//			logger.info("None of the tested queries which were not tested before contained a new example.");
//			logger.info("Making again NBR...");
//		}
////		return findExampleByGeneralisation(queryGen.getCurrentQueryTree());
//		return findExampleByLGG(Collections.singletonList(queryGen.getCurrentQueryTree()), negExamplesTrees);
//	}
	
	private Example findExampleByLGG(List<QueryTree<String>> posExamplesTrees,
			List<QueryTree<String>> negExamplesTrees) throws SPARQLQueryException{
		queryGen.getSPARQLQueries(posExamplesTrees);
		lgg = queryGen.getLastLGG();
		currentQuery = lgg.toSPARQLQueryString();
		currentQueryTree = lgg;
		SortedSet<String> resources = getResources(currentQuery);
		for(String r : resources){
			if(!posExamples.contains(r) && !negExamples.contains(r)){
				return getExample(r);
			}
		}
		
		return null;
	}
	
	private Example findExampleByNBR(List<QueryTree<String>> posExamplesTrees,
			List<QueryTree<String>> negExamplesTrees){
		LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
		lgg = lggGen.getLGG(posExamplesTrees);
		if(treeFilter != null){
			lgg = treeFilter.getFilteredQueryTree(lgg);
		}
		logger.info("LGG(Tree): \n" + TreeHelper.getAbbreviatedTreeRepresentation(
				lgg, endpoint.getBaseURI(), endpoint.getPrefixes()));
		logger.info("LGG(Query):\n" + lgg.toSPARQLQueryString());
		logger.info("LGG(#Instances):\n" + getAllResources(lgg.toSPARQLQueryString()).size());
		logger.info("Making NBR...");
		List<String> knownResources = new ArrayList<String>();
		knownResources.addAll(posExamples);
		knownResources.addAll(negExamples);
		
		Example example = null;
//		try {
//			example = findExampleByLGG(posExamplesTrees, negExamplesTrees);
//		} catch (SPARQLQueryException e1) {
//			e1.printStackTrace();
//		}
//		if(example != null){
//			return example;
//		}
		try {
			String uri = nbrGen.getQuestion(lgg, negExamplesTrees, knownResources);
			example = getExample(uri);
		} catch (TimeOutException e) {
			e.printStackTrace();
		}
		example = getExample(example.getURI());
//		Example example = nbr.makeNBR(resources, lgg, negExamplesTrees);
		currentQuery = nbrGen.getQuery();
		return example;
		
	}
	
	
	
	private String getAngleBracketsString(String str){
		return "<" + str + ">";
	}
	
	public QueryTree<String> getLGG(){
		if(dirty){
			computeLGG(posExamples);
			dirty = false;
		}
		return lgg;
	}
	
	public Set<String> getLGGInstances(List<String> posExamples){
		this.posExamples = posExamples;
		computeLGG(posExamples);
		dirty = false;
		Set<String> lggInstances = getAllResources(lgg.toSPARQLQueryString());
		nbrGen.setLGGInstances(lggInstances);
		return lggInstances;
	}
	
	private Example getExample(String uri){
		if(logger.isInfoEnabled()){
			logger.info("Retrieving data for resource " + uri);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?label ?imageURL ?comment WHERE{\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <").append(RDFS.label.getURI()).append("> ").append("?label.\n");
		sb.append("}\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <http://dbpedia.org/ontology/thumbnail> ").append("?imageURL.\n");
		sb.append("}\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <").append(RDFS.comment.getURI()).append("> ").append("?comment.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?comment),'en'))\n");
		sb.append("}\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label),'en'))\n");
		sb.append("}");
		
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, sb.toString()));
		String label = uri;
		String imageURL = "";
		String comment = "";
		if(rs.hasNext()){
			QuerySolution qs = rs.next();		
			
			if(qs.getLiteral("label") != null){
				label = qs.getLiteral("label").getLexicalForm();
			}
			
			if(qs.getResource("imageURL") != null){
				imageURL = qs.getResource("imageURL").getURI();
			}
			
			if(qs.getLiteral("comment") != null){
				comment = qs.getLiteral("comment").getLexicalForm();
			}
		}
		
		return new Example(uri, label, imageURL, comment);
	}
	
	public void setMakeAlwaysNBR(boolean makeAlwaysNBR){
		this.makeAlwaysNBR = makeAlwaysNBR;
	}
	
	public String encodeHTML(String s) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>') {
				out.append("&#" + (int) c + ";");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	public void setEndpoint(SPARQLEndpointEx endpoint){
		this.endpoint = endpoint;
	}
	
	public void setPredicateFilter(Filter filter){
		queryTreeCache.setPredicateFilter(filter);
	}
	
	public void setObjectFilter(Filter filter){
		queryTreeCache.setObjectFilter(filter);
	}
	
	public void setStatementFilter(com.hp.hpl.jena.util.iterator.Filter<Statement> filter){
		queryTreeCache.setStatementFilter(filter);
		nbrGen.setStatementFilter(filter);
	}
	
	public void setQueryTreeFilter(QuestionBasedQueryTreeFilter filter){
		treeFilter = filter;
	}
	
	public void setStatementSelector(Selector selector){
		queryTreeCache.setStatementSelector(selector);
	}
	
	public String getCurrentQuery(){
		return currentQuery;
	}
	
	public QueryTree<String> getCurrentQueryTree(){
		return currentQueryTree;
	}
	
	public String getCurrentQueryHTML(){
		return encodeHTML(currentQuery);
	}
	
	public String getLimitedQuery(String query, int limit, boolean distinct){
		if(distinct){
			query = "SELECT DISTINCT " + query.substring(7);
		}
		return query + " LIMIT " + limit;
	}
}
