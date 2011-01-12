package org.dllearner.autosparql.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.examples.DBpediaExample;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;
import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

public class NBRTest {
	
	private static Logger logger = Logger.getLogger(NBRTest.class);
	
	private static final String CACHE_DIR = "cache";
	
	private int nodeId;
	private String lastQuery = "";
	
	@Test
	public void test1(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender(
					layout, "log/nbr_evaluation.log", false);
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.OFF);
			Logger.getLogger(NBR.class).setLevel(Level.INFO);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpQuery.urlLimit = 0;
		try {
			ExtractionDBCache cache = new ExtractionDBCache(CACHE_DIR);
			SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://db0.aksw.org:8999/sparql"),
					Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
			Set<String> predicateFilters = new HashSet<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			
			ModelGenerator modelGen = new ModelGenerator(endpoint, predicateFilters, cache);
			QueryTreeFactory<String> treeFactory = new QueryTreeFactoryImpl();
			LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
			NBR<String> nbrGen = new NBR<String>(endpoint, cache);
			
			String targetQuery = "PREFIX dbpedia: <http://dbpedia.org/resource/> PREFIX dbo: <http://dbpedia.org/ontology/> " +
			"SELECT DISTINCT ?var0 ?homepage ?genre WHERE {" +
			"?var0 a dbo:Band ." +
			"?var0 rdfs:label ?label." +
			"OPTIONAL { ?var0 foaf:homepage ?homepage } ." +
			"?var0 dbo:genre ?genre ." +
			"?genre dbo:instrument dbpedia:Electric_guitar ." +
			"?genre dbo:stylisticOrigin dbpedia:Jazz .}";
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, targetQuery));
			SortedSet<String> targetResources = new TreeSet<String>();
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("var0").isURIResource()){
					targetResources.add(qs.get("var0").asResource().getURI());
				}
			}
			
			List<QueryTree<String>> posTrees = new ArrayList<QueryTree<String>>();
			List<QueryTree<String>> negTrees = new ArrayList<QueryTree<String>>();
			List<String> knownResources = new ArrayList<String>();
			
			String uri = "http://dbpedia.org/resource/Foals";
			knownResources.add(uri);
			Model model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			QueryTree<String> tree = treeFactory.getQueryTree(uri, model);
			posTrees.add(tree);
			
			uri = "http://dbpedia.org/resource/31Knots";
			knownResources.add(uri);
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			tree = treeFactory.getQueryTree(uri, model);
			posTrees.add(tree);
			
			uri = "http://dbpedia.org/resource/Hot_Chip";
			knownResources.add(uri);
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			tree = treeFactory.getQueryTree(uri, model);
			negTrees.add(tree);
			
			QueryTree<String> lgg = lggGen.getLGG(posTrees);
			
			Example example = nbrGen.getQuestion(lgg, negTrees, knownResources);
			String learnedQuery = nbrGen.getQuery();
			while(!isEquivalentQuery(targetResources, learnedQuery, endpoint, cache)){
				uri = example.getURI();
				knownResources.add(uri);
				model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
				tree = treeFactory.getQueryTree(uri, model);
				if(targetResources.contains(uri)){
					System.out.println("Found new positive example " + uri);
					posTrees.add(tree);
					lgg = lggGen.getLGG(posTrees);
				} else {
					System.out.println("Found new negative example " + uri);
					negTrees.add(tree);
				}
				example = nbrGen.getQuestion(lgg, negTrees, knownResources);
				learnedQuery = nbrGen.getQuery();
			}
			
			
			
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void optimisedTest(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			consoleAppender.setThreshold(Level.INFO);
			FileAppender fileAppender = new FileAppender(
					layout, "log/nbr_evaluation.log", false);
			fileAppender.setThreshold(Level.DEBUG);
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
//			logger.setLevel(Level.OFF);
			Logger.getLogger(NBR.class).setLevel(Level.DEBUG);
			Logger.getLogger(PostLGG.class).setLevel(Level.DEBUG);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpQuery.urlLimit = 0;
		try {
			ExtractionDBCache cache = new ExtractionDBCache(CACHE_DIR);
			List<String> predicateFilters = new ArrayList<String>();
			SparqlEndpoint endpoint = new SPARQLEndpointEx(new URL("http://db0.aksw.org:8999/sparql"),
					Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList(), null, null, predicateFilters);
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			
			ModelGenerator modelGen = new ModelGenerator(endpoint, new HashSet<String>(predicateFilters), cache);
			QueryTreeFactory<String> treeFactory = new QueryTreeFactoryImpl();
			LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
			NBR<String> nbrGen = new NBR<String>(endpoint, cache);
			
			String targetQuery = "PREFIX dbpedia: <http://dbpedia.org/resource/> PREFIX dbo: <http://dbpedia.org/ontology/> " +
			"SELECT DISTINCT ?var0 ?homepage ?genre WHERE {" +
			"?var0 a dbo:Band ." +
			"?var0 rdfs:label ?label." +
			"OPTIONAL { ?var0 foaf:homepage ?homepage } ." +
			"?var0 dbo:genre ?genre ." +
			"?genre dbo:instrument dbpedia:Electric_guitar ." +
			"?genre dbo:stylisticOrigin dbpedia:Jazz .}";
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, targetQuery));
			SortedSet<String> targetResources = new TreeSet<String>();
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("var0").isURIResource()){
					targetResources.add(qs.get("var0").asResource().getURI());
				}
			}
			
			List<QueryTree<String>> posTrees = new ArrayList<QueryTree<String>>();
			List<QueryTree<String>> negTrees = new ArrayList<QueryTree<String>>();
			List<String> knownResources = new ArrayList<String>();
			
			String uri = "http://dbpedia.org/resource/Foals";
			knownResources.add(uri);
			Model model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			QueryTree<String> tree = treeFactory.getQueryTree(uri, model);
			tree = getFilteredTree(tree);
			posTrees.add(tree);
			
			uri = "http://dbpedia.org/resource/31Knots";
			knownResources.add(uri);
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			tree = treeFactory.getQueryTree(uri, model);
			tree = getFilteredTree(tree);
			posTrees.add(tree);
			
			uri = "http://dbpedia.org/resource/Hot_Chip";
			knownResources.add(uri);
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			tree = treeFactory.getQueryTree(uri, model);
			tree = getFilteredTree(tree);
			negTrees.add(tree);
			
			QueryTree<String> lgg = lggGen.getLGG(posTrees);
			
			Example example = nbrGen.getQuestionOptimised(lgg, negTrees, knownResources);
			String learnedQuery = nbrGen.getQuery();
			while(!isEquivalentQuery(targetResources, learnedQuery, endpoint, cache)){
				uri = example.getURI();
				knownResources.add(uri);
				model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
				tree = treeFactory.getQueryTree(uri, model);
				tree = getFilteredTree(tree);
				if(targetResources.contains(uri)){
					logger.info("Found new positive example " + uri);
					posTrees.add(tree);
					lgg = lggGen.getLGG(posTrees);
				} else {
					logger.info("Found new negative example " + uri);
					negTrees.add(tree);
				}
				example = nbrGen.getQuestionOptimised(lgg, negTrees, knownResources);
				learnedQuery = nbrGen.getQuery();
				/*
				System.out.println(learnedQuery);
				cache.executeSelectQuery(endpoint, learnedQuery);
				ResultSet rs2 = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, learnedQuery));
				SortedSet<String> learnedResources = new TreeSet<String>();
				QuerySolution qs2;
				while(rs2.hasNext()){
					qs2 = rs.next();
					if(qs2.get("var0").isURIResource()){
						learnedResources.add(qs2.get("var0").asResource().getURI());
					}
				}				
				System.out.println("learned tree covers " + learnedResources.size() + " resources (target: " + targetResources.size() + ")");
				*/
			}
			
			
			
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean isEquivalentQuery(SortedSet<String> originalResources, String query, SparqlEndpoint endpoint, ExtractionDBCache cache){
		if(query.equals("SELECT ?x0 WHERE {?x0 ?y ?z.}") || query.equals(lastQuery)){
			return false;
		}
		lastQuery = query;
		com.hp.hpl.jena.query.ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		
		SortedSet<String> learnedResources = new TreeSet<String>();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("x0").isURIResource()){
				learnedResources.add(qs.get("x0").asResource().getURI());
			}
		}
		logger.info("#Resource in learned query: " + learnedResources.size());
		logger.info("#Resource in target query : " + originalResources.size());
		return originalResources.equals(learnedResources);
	}
	
	@Test
	public void testAllowedGeneralisationsGeneration(){
		QueryTree<String> tree = DBpediaExample.getPosExampleTrees().get(0);
		NBR<String> nbrGen = new NBR<String>(null, null);
		System.out.println(tree.getStringRepresentation());
		System.out.println(tree.getNodeById(5));
		
		List<GeneralisedQueryTree<String>> gens = nbrGen.getAllowedGeneralisations(new GeneralisedQueryTree<String>(tree));
		GeneralisedQueryTree<String> genTree;
		QueryTree<String> queryTree;
		while(!gens.isEmpty()){
			genTree = gens.remove(0);
			queryTree = genTree.getQueryTree();
			System.out.println("Changes:" + genTree.getChanges());
//			System.out.println("Query:\n" + queryTree.toSPARQLQueryString());
			gens.addAll(0, nbrGen.getAllowedGeneralisations(genTree));
			
		}
		
	}
	
	@Test
	public void testAllowedGeneralisationsGeneration2(){
		QueryTree<String> tree = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("node1");
		child.setId(1);
		Object edge = "edge";
		tree.addChild(child, edge);
		System.out.println(tree.getStringRepresentation());
		NBR<String> nbrGen = new NBR<String>(null, null);
		
		List<GeneralisedQueryTree<String>> gens = nbrGen.getAllowedGeneralisations(new GeneralisedQueryTree<String>(tree));
		GeneralisedQueryTree<String> genTree;
		QueryTree<String> queryTree;
		while(!gens.isEmpty()){
			genTree = gens.remove(0);
			queryTree = genTree.getQueryTree();
			System.out.println("Changes:" + genTree.getChanges());
			System.out.println("Query:\n" + queryTree.toSPARQLQueryString());
			gens.addAll(0, nbrGen.getAllowedGeneralisations(genTree));
			
		}
		
	}
	
	@Test
	public void testAllowedGeneralisationsGeneration3(){
		Logger.getLogger(NBR.class).setLevel(Level.OFF);
		QueryTree<String> tree = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("node1");
		child.setId(1);
		Object edge = "edge";
		tree.addChild(child, edge);
		QueryTreeImpl<String> child2 = new QueryTreeImpl<String>("node2");
		child2.setId(2);
		child.addChild(child2, edge);
		child2 = new QueryTreeImpl<String>("node3");
		child2.setId(3);
		child.addChild(child2, edge);
		System.out.println(tree.getStringRepresentation());
		NBR<String> nbrGen = new NBR<String>(null, null);
		
		List<GeneralisedQueryTree<String>> gens = nbrGen.getAllowedGeneralisations(new GeneralisedQueryTree<String>(tree));
		GeneralisedQueryTree<String> genTree;
		QueryTree<String> queryTree;
		while(!gens.isEmpty()){
			genTree = gens.remove(0);
			queryTree = genTree.getQueryTree();
			System.out.println("Changes:" + genTree.getChanges());
//			System.out.println("Query:\n" + queryTree.toSPARQLQueryString());
			gens.addAll(0, nbrGen.getAllowedGeneralisations(genTree));
			
		}
		
	}
	
	@Test
	public void testAllowedGeneralisationsGeneration4(){
		QueryTree<String> tree = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("node1");
		child.setId(1);
		QueryTreeImpl<String> child2 = new QueryTreeImpl<String>("node2");
		child2.setId(2);
		child.addChild(child2, "edge2");
		child2 = new QueryTreeImpl<String>("node3");
		child2.setId(3);
		child.addChild(child2, "edge3");
		tree.addChild(child, "edge1");
		child = new QueryTreeImpl<String>("node4");
		child.setId(4);
		tree.addChild(child, "edge4");
		
		System.out.println(tree.getStringRepresentation());
		NBR<String> nbrGen = new NBR<String>(null, null);
		
		List<GeneralisedQueryTree<String>> gens = nbrGen.getAllowedGeneralisations(new GeneralisedQueryTree<String>(tree));
		GeneralisedQueryTree<String> genTree;
		QueryTree<String> queryTree;
		int cnt = 0;
		while(!gens.isEmpty()){cnt++;
			genTree = gens.remove(0);
			queryTree = genTree.getQueryTree();
			System.out.println("Changes:" + genTree.getChanges());
			System.out.println("Query:\n" + queryTree.toSPARQLQueryString());
			gens.addAll(0, nbrGen.getAllowedGeneralisations(genTree));
			
		}
		System.out.println(cnt);
		
	}
	
	private QueryTree<String> getFilteredTree(QueryTree<String> tree){
		nodeId = 0;
		QueryTree<String> filteredTree = createFilteredTree(tree);
		return filteredTree;
	}
	
	private QueryTree<String> createFilteredTree(QueryTree<String> tree){
		QueryTree<String> filteredTree = new QueryTreeImpl<String>(tree.getUserObject());
		filteredTree.setId(nodeId);
		QueryTree<String> subTree;
		Object predicate;
    	for(QueryTree<String> child : tree.getChildren()){
//    		if(child.isLiteralNode()){
//    			continue;
//    		}
    		predicate = tree.getEdge(child);
    		if(((String)predicate).startsWith("http://dbpedia.org/property")){
    			continue;
    		}
    		this.nodeId++;
    		subTree = createFilteredTree(child);
    		subTree.setLiteralNode(child.isLiteralNode());
    		subTree.setResourceNode(child.isResourceNode());
    		filteredTree.addChild((QueryTreeImpl<String>)subTree, tree.getEdge(child));
    	}
    	return filteredTree;
	}

}
