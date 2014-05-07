/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.EvaluatedQueryTree;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.TriplePatternExtractor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDExperiment {
	
	
	private static final Logger logger = Logger.getLogger(QALDExperiment.class.getName());
	
	List<String> datasetFiles = Lists.newArrayList(
//			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald1/dbpedia-train.xml",
			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/3/dbpedia-train.xml",
			"http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/3/dbpedia-test.xml"
			);
	
	static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	static {
		try {
			endpoint = new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql"), "http://dbpedia.org");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	Set<String> allowedNamespaces = Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/");
	Set<String> ignoredProperties = Sets.newHashSet("http://dbpedia.org/ontology/wikiPageID","http://dbpedia.org/ontology/wikiPageRevisionID",
			"http://dbpedia.org/ontology/wikiPageExtracted", "http://dbpedia.org/ontology/wikiPageModified");
	
	QueryExecutionFactory qef;
	String cacheDirectory = "cache";
	
	int minNrOfPositiveExamples = 5;
	int maxDepth = 2;
	double noise = 0.4d;
	
	QueryTreeFactory<String> queryTreeFactory;
	ConciseBoundedDescriptionGenerator cbdGen;
//	private LGGGenerator<String> lggGenerator;
	private QTL2 lggGenerator;
	
	private Random rnd = new Random(123);
	
	public QALDExperiment() {
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
			try {
				long timeToLive = TimeUnit.DAYS.toMillis(60);
				CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
				CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.addAllowedNamespaces(allowedNamespaces);
		queryTreeFactory.addIgnoredPropperties(ignoredProperties);
		queryTreeFactory.setMaxDepth(maxDepth);
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint, cacheDirectory);
		cbdGen.setRecursionDepth(maxDepth);
		
		lggGenerator = new QTL2();// LGGGeneratorImpl<String>();
	}
	
	public void run(){
		DescriptiveStatistics precisionStats = new DescriptiveStatistics();
		DescriptiveStatistics recallStats = new DescriptiveStatistics();
		DescriptiveStatistics fMeasureStats = new DescriptiveStatistics();
		
		List<String> sparqlQueries = loadSPARQLQueries();
		logger.info("Overall number of queries: " + sparqlQueries.size());
		
		for (String sparqlQuery : sparqlQueries) {
			try {
					logger.info("Processing query\n" + sparqlQuery);
					
					//convert examples to query trees
					List<QueryTree<String>> queryTrees = generateExamples(sparqlQuery, noise);
					for (QueryTree<String> queryTree : queryTrees) {
						logger.info(queryTree.getStringRepresentation());
					}
					
//					//run LGG
//					List<EvaluatedQueryTree<String>> lggs = lggGenerator.computeLGG(queryTrees);
//					EvaluatedQueryTree<String> bestLGG = lggs.get(0);
//					logger.info("Got " + lggs.size() + " LGG query trees. Best computed LGG:\n" + bestLGG);
//					
//					//find the best extensionally matching tree in the list
//					EvaluatedQueryTree<String> bestTree = findBestMatchingTree(lggs, sparqlQuery);
//					
//					//convert to SPARQL query
//					String learnedSPARQLQuery = bestLGG.getTree().toSPARQLQueryString(true, false);
//					logger.info("Learned Query:\n" + learnedSPARQLQuery);
//					
//					
//					double precision = precision(sparqlQuery, learnedSPARQLQuery);
//					precisionStats.addValue(precision);
//					
//					double recall = recall(sparqlQuery, learnedSPARQLQuery);
//					recallStats.addValue(recall);
//					
//					double fmeasure = fMeasure(sparqlQuery, learnedSPARQLQuery);
//					fMeasureStats.addValue(fmeasure);
//					
//					logger.info(String.format("P=%f\nR=%f\nF-Score=%f", precision, recall, fmeasure));
			} catch (Exception e) {
				logger.error("Error occured.", e);
			}
		}
		logger.info("Overall Precision:\n" + precisionStats);
		logger.info("Overall Recall:\n" + recallStats);
		logger.info("Overall FMeasure:\n" + fMeasureStats);
	}
	
	private EvaluatedQueryTree<String> findBestMatchingTree(List<EvaluatedQueryTree<String>> trees, String targetSPARQLQuery){
		logger.info("Finding best matching query tree...");
		//get the tree with the highest fmeasure
		EvaluatedQueryTree<String> bestTree = null;
		double bestFMeasure = -1;
		for (EvaluatedQueryTree<String> tree : trees) {
			String learnedSPARQLQuery = tree.getTree().toSPARQLQueryString();
			logger.info(learnedSPARQLQuery);
			logger.info("Tree Score: " + tree.getScore());
			double fMeasure = fMeasure(targetSPARQLQuery, learnedSPARQLQuery);
			if(fMeasure > bestFMeasure){
				bestTree = tree;
			}
			logger.info("Extensional fMeasure: " + fMeasure);
		}
		return bestTree;
	}
	
	private List<QueryTree<String>> generateExamples(String sparqlQuery, double noise){
		//get all resources returned by the query
		List<String> resources = getResult(sparqlQuery);
		
		//shuffle the list to avoid bias
		Collections.shuffle(resources, new Random(123));
		
		//pick some positive examples from the list
		List<String> positiveExamples = resources.subList(0, Math.min(minNrOfPositiveExamples, resources.size()));
		if(positiveExamples.size() < minNrOfPositiveExamples){
			return new ArrayList<>();
		}
		
		//convert examples to query trees
		List<QueryTree<String>> posExampleQueryTrees = getQueryTrees(positiveExamples);
		
		//generate noise
		generateNoise1(sparqlQuery, posExampleQueryTrees);;
		
		return posExampleQueryTrees;
	}
	
	private QueryTree<String> getSimilarTree(QueryTree<String> tree, String property, int maxTreeDepth){
		String query = "SELECT ?o WHERE {?s <" + property + "> ?o. FILTER(isURI(?o) && ?o != <" + tree.getUserObject() + ">)} LIMIT 1";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()){
			String uri = rs.next().getResource("o").getURI();
			Model cbd = cbdGen.getConciseBoundedDescription(uri, maxTreeDepth);
			QueryTree<String> similarTree = queryTreeFactory.getQueryTree(uri, cbd);
			similarTree.setUserObject(uri);
			return similarTree;
		}
		return null;
	}
	
	private void generateNoise2(String sparqlQuery, List<QueryTree<String>> posExampleQueryTrees){
		//add some noise, i.e.
				//a)add wrong negative examples
				//b)take positive examples and modify relevant attributes
				Random randomGen = new Random(123);
				TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
				List<Triple> triplePatterns = new ArrayList<Triple>(triplePatternExtractor.extractTriplePattern(QueryFactory.create(sparqlQuery, Syntax.syntaxARQ)));
				for (QueryTree<String> queryTree : posExampleQueryTrees) {
					double rnd = randomGen.nextDouble();
					if(rnd <= noise){
						//pick a random property to modify
						Collections.shuffle(triplePatterns, randomGen);
						Triple triplePattern = triplePatterns.get(0);
						String predicate = triplePattern.getPredicate().getURI();
						Node object = triplePattern.getObject();
						logger.info("Modifying edge <" + queryTree + ", " + predicate + ", " + object + ">");
						//get the corresponding edge in the tree
						Set<Object> edges = queryTree.getEdges();
						for (Object edge : edges) {
							if (predicate.equals(edge)) {
								List<QueryTree<String>> children = queryTree.getChildren(edge);
								for (QueryTree<String> child : children) {
									if (child.getUserObject().replace("<", "").replace(">", "").equals(object.toString())) {
										if (child.isResourceNode()) {
											QueryTree<String> similarTree = getSimilarTree(child, predicate, 1);
											if (similarTree != null) {
												int id = child.getId();
												queryTree.removeChild((QueryTreeImpl<String>) child);
												similarTree.setId(id);
												queryTree.addChild((QueryTreeImpl<String>) similarTree, edge);
											} else {
												child.setUserObject("http://dl-learner.org/qtl#noiseModification");
											}
										} else if (child.isLiteralNode()) {
											child.setUserObject("\"-999999\"^^<http://www.w3.org/2001/XMLSchema#integer>");
											child.getLiterals().clear();
											child.getLiterals().add(ResourceFactory.createTypedLiteral(-999999));
										}
									}
								}
							}
						}
					}
				}
	}
	
	private void generateNoise1(String sparqlQuery, List<QueryTree<String>> posExampleQueryTrees){
		NegativeExampleSPARQLQueryGenerator negExGen = new NegativeExampleSPARQLQueryGenerator();
		
		//get the positive examples
		List<String> posExamples = getResult(sparqlQuery);
		
		//generate a modified SPARQL query
		Query query = negExGen.generateSPARQLQuery(QueryFactory.create(sparqlQuery));
		query.setLimit(1000);
		
		//get a random number of negative examples
		List<String> negExamples = getResult(query.toString());
		negExamples.removeAll(posExamples);
		Collections.shuffle(negExamples);
		
		//replace some of the query trees of the positive examples with negative related ones
		List<QueryTree<String>> negExampleTrees = new ArrayList<>();
		for (Iterator<QueryTree<String>> iterator = posExampleQueryTrees.iterator(); iterator.hasNext();) {
			QueryTree<String> tree = iterator.next();
			if(rnd.nextDouble() <= noise){
				QueryTree<String> newTree = getQueryTree(negExamples.remove(0));
				negExampleTrees.add(newTree);
				iterator.remove();
			}
		}
		posExampleQueryTrees.addAll(negExampleTrees);
	}
	
	private List<QueryTree<String>> getQueryTrees(List<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		
		for (String resource : resources) {
			trees.add(getQueryTree(resource));
		}
		
		return trees;
	}
	
	private QueryTree<String> getQueryTree(String resource){
		Model cbd = cbdGen.getConciseBoundedDescription(resource);
		QueryTree<String> tree = queryTreeFactory.getQueryTree(resource, cbd);
		return tree;
	}
	
	private List<String> getResult(String sparqlQuery){
		List<String> resources = new ArrayList<String>();
		
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("uri").isURIResource()){
				resources.add(qs.getResource("uri").getURI());
			} else if(qs.get("uri").isLiteral()){
				resources.add(qs.getLiteral("uri").toString());
			}
		}
		return resources;
	}
	
	private int getResultCount(String sparqlQuery){
		int cnt = 0;
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		while(rs.hasNext()){
			rs.next();
			cnt++;
		}
		return cnt;
	}
	
	private List<String> loadSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		try {
			for(String file : datasetFiles){
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            Document doc = db.parse(new URL(file).openStream());
	            doc.getDocumentElement().normalize();
	            NodeList questionNodes = doc.getElementsByTagName("question");
	            
	            TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
	            
	            for( int i = 0; i < questionNodes.getLength(); i++){
	                
	            	org.w3c.dom.Element questionNode = (org.w3c.dom.Element) questionNodes.item(i);
	                
	            	int id = Integer.valueOf(questionNode.getAttribute("id")); //if(id != 44)continue;
//	            	String answerType = questionNode.getAttribute("answerType");
	            	boolean aggregation = false;//Boolean.valueOf(questionNode.getAttribute("aggregation"));
	            	boolean onlydbo = true;//Boolean.valueOf(questionNode.getAttribute("onlydbo"));
	            	
	            	
	            	
	                // Read SPARQL query
	            	String sparqlQuery = ((org.w3c.dom.Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
	            	sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + sparqlQuery;
	            	if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }")){
	            		sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }", "");
	            		sparqlQuery = sparqlQuery.replace("FILTER (lang(?string) = 'en')", "");
	            		sparqlQuery = sparqlQuery.replace("?string", "");
	            	}
	            	if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }")){
	            		sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }", "");
	            		sparqlQuery = sparqlQuery.replace("?string", "");
	            	}
	            	
//	            	System.out.println(sparqlQuery);
	            	// check if OUT OF SCOPE marked
	            	boolean outOfScope = sparqlQuery.toUpperCase().contains("OUT OF SCOPE");
	            	
	            	//check if ASK query
	            	boolean askQuery = sparqlQuery.toUpperCase().contains("ASK");
	            	
	            	boolean containsLimit = sparqlQuery.toUpperCase().contains("LIMIT");
	            	
	            	boolean containsCount = sparqlQuery.toUpperCase().contains("COUNT");
	            	
	            	boolean containsFilter = sparqlQuery.toUpperCase().contains("FILTER");
	            	
	            	boolean containsUNION = sparqlQuery.toUpperCase().contains("UNION");
	            	
	            	//check if projection variable is somewhere in object position
	            	boolean ingoingLinks = false;
	            	if(!outOfScope && !askQuery && !containsLimit && !containsFilter){
	            		Query q = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ);
	                	List<Var> projectVars = q.getProjectVars();
	                	Set<Triple> ingoingTriplePatterns = triplePatternExtractor.extractIngoingTriplePatterns(q, projectVars.get(0).asNode());
	                	ingoingLinks = !ingoingTriplePatterns.isEmpty();
	            	}
	            	
	            	if(true
	            			&& !aggregation 
	            			&& !outOfScope 
	            			&& !containsCount 
	            			&& !askQuery 
	            			&& !ingoingLinks 
	            			&& !containsFilter
	            			&& !containsUNION
	            			&& onlydbo
	            			&& (getResultCount(sparqlQuery) >= minNrOfPositiveExamples)
	            			){
	            		queries.add(sparqlQuery);
	            	}
	            }
			}
            
		} 
		catch (DOMException e) {
	            e.printStackTrace();
	    }
		catch (ParserConfigurationException e) {
	            e.printStackTrace();
	    }
		catch (SAXException e) {
	            e.printStackTrace();
	    } 
		catch (IOException e) {
	            e.printStackTrace();
	    }
//		queries.clear();
//		queries.add("select ?uri where {?uri a <http://dbpedia.org/ontology/Book>. ?uri <http://dbpedia.org/ontology/author> <http://dbpedia.org/resource/Dan_Brown>.}");
		return queries;
	}
	
	private double precision(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("uri") != null) {
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		// get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}

		int overlap = Sets.intersection(referenceResources, learnedResources).size();

		double precision = overlap / (double) learnedResources.size();

		return precision;
	}
	
	private double recall(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("uri") != null) {
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		// get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}

		int overlap = Sets.intersection(referenceResources, learnedResources).size();

		double recall = overlap / (double) referenceResources.size();

		return recall;
	}
	
	private double fMeasure(String referenceSparqlQuery, String learnedSPARQLQuery){
		//get the reference resources
		Set<String> referenceResources = new HashSet<String>();
		ResultSet rs = qef.createQueryExecution(referenceSparqlQuery).execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("uri") != null){
				referenceResources.add(qs.getResource("uri").getURI());
			}
		}
		//get the learned resources
		Set<String> learnedResources = new HashSet<String>();
		rs = qef.createQueryExecution(learnedSPARQLQuery).execSelect();
		while (rs.hasNext()) {
			qs = rs.next();
			if (qs.get("x0") != null) {
				learnedResources.add(qs.getResource("x0").getURI());
			}
		}
		
		int overlap = Sets.intersection(referenceResources, learnedResources).size();
		
		double precision = overlap / (double) learnedResources.size();
		double recall = overlap / (double) referenceResources.size();
		
		double fMeasure = 0;
		if(precision + recall > 0){
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		
		return fMeasure;
	}
	
	public static void main(String[] args) throws Exception {
		new QALDExperiment().run();
	}
	
	class NegativeExampleSPARQLQueryGenerator extends ElementVisitorBase{
		
		private boolean inOptionalClause;
		private Stack<ElementGroup> parentGroup = new Stack<>();
		private TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
		private Triple triple;
		Random randomGen = new Random(123);

		/**
		 * Returns a modified SPARQL query such that it is similar but different by choosing one of the triple patterns and use
		 * the negation of its existence.
		 * @param query
		 */
		public Query generateSPARQLQuery(Query query){
			//choose a random triple for the modification
			List<Triple> triplePatterns = new ArrayList<Triple>(triplePatternExtractor.extractTriplePattern(query));
			Collections.shuffle(triplePatterns, randomGen);
			triple = triplePatterns.get(0);
			
			Query modifiedQuery = query.cloneQuery();
			modifiedQuery.getQueryPattern().visit(this);
			logger.info("Negative examples query:\n" + modifiedQuery.toString());
			return modifiedQuery;
		}
		
		@Override
		public void visit(ElementGroup el) {
			parentGroup.push(el);
			for (Iterator<Element> iterator = new ArrayList<Element>(el.getElements()).iterator(); iterator.hasNext();) {
				Element e = iterator.next();
				e.visit(this);
			}
			parentGroup.pop();
		}

		@Override
		public void visit(ElementOptional el) {
			inOptionalClause = true;
			el.getOptionalElement().visit(this);
			inOptionalClause = false;
		}

		@Override
		public void visit(ElementTriplesBlock el) {
			for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
				Triple t = iterator.next();
				if(inOptionalClause){
					
				} else {
					if(t.equals(triple)){
						ElementGroup parent = parentGroup.peek();
						ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
						elementTriplesBlock.addTriple(t);
						ElementGroup eg = new ElementGroup();
						eg.addElement(elementTriplesBlock);
						parent.addElement(new ElementFilter(new E_NotExists(eg)));
						iterator.remove();
					}
				}
			}
		}

		@Override
		public void visit(ElementPathBlock el) {
			for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
				TriplePath tp = iterator.next();
				if(inOptionalClause){
					
				} else {
					if(tp.asTriple().equals(triple)){
						ElementGroup parent = parentGroup.peek();
						ElementPathBlock elementTriplesBlock = new ElementPathBlock();
						elementTriplesBlock.addTriple(tp);
						ElementGroup eg = new ElementGroup();
						eg.addElement(elementTriplesBlock);
						parent.addElement(new ElementFilter(new E_NotExists(eg)));
						iterator.remove();
					}
				}
			}
		}

		@Override
		public void visit(ElementUnion el) {
			for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
				Element e = iterator.next();
				e.visit(this);
			}
		}
		
		@Override
		public void visit(ElementFilter el) {
		}

	}

}
