/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.utilities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxStylesheet;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.collections15.ListUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.VarUtils;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for SPARQL queries.
 */
public class QueryUtils extends ElementVisitorBase {
	
private static final Logger logger = LoggerFactory.getLogger(QueryUtils.class);	

	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub ((rdfs:subClassOf|owl:equivalentClass)|^owl:equivalentClass)+ ?sup .}");
	
	private Set<Triple> triplePattern;
	private Set<Triple> optionalTriplePattern;
	
	private boolean inOptionalClause = false;
	
	private int unionCount = 0;
	private int optionalCount = 0;
	private int filterCount = 0;
	
	private Map<Triple, ElementGroup> triple2Parent = new HashMap<>();
	
	Stack<ElementGroup> parents = new Stack<>();
	
	public static String addPrefix(String queryString, Map<String, String> prefix2Namespace){
		Query query = QueryFactory.create(queryString);
		for (Entry<String, String> entry : prefix2Namespace.entrySet()) {
			String prefix = entry.getKey();
			String namespace = entry.getValue();
			query.setPrefix(prefix, namespace);
		}
		return query.toString();
	}
	
	public static String addPrefixes(String queryString, String prefix, String namespace){
		Query query = QueryFactory.create(queryString);
		query.setPrefix(prefix, namespace);
		return query.toString();
	}

	/**
	 * Remove unused prefix declarations from preamble.
	 *
	 * @param query the query
	 */
	public static void prunePrefixes(Query query) {
		PrefixMapping pm = query.getPrefixMapping();
		String baseURI = query.getBaseURI();
		query.setBaseURI((String) null);

		Set<String> usedPrefixes = Sets.newHashSet();
		getNodes(query).forEach(node -> {
			String ns = null;
			if(node.isURI()) {
				ns = node.getNameSpace();
			} else if(node.isLiteral()) {
				RDFDatatype dt = node.getLiteralDatatype();
				if(dt != null) {
					String uri = dt.getURI();
					ns = uri.substring(0, Util.splitNamespaceXML(uri));
				}
			}

			if(ns != null) {
				// check if base URI has been used
				if(ns.equals(baseURI)) {
					query.setBaseURI(baseURI);
				}
				// check if one of the prefixes has been used
				String prefix = pm.getNsURIPrefix(ns);
				if(prefix != null) {
					usedPrefixes.add(prefix);
				}
			}
		});
		// override prefix map
		Map<String, String> prefixMap = pm.getNsPrefixMap();
		prefixMap.entrySet().removeIf(mapping -> !usedPrefixes.contains(mapping.getKey()));
		pm.clearNsPrefixMap();
		pm.setNsPrefixes(prefixMap);
	}

	/**
	 * @param query the query
	 * @return all nodes that occur in triple patterns of the query
	 */
	public static Set<Node> getNodes(Query query) {
		return getTriplePatterns(query).stream()
				.map(QueryUtils::getNodes)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * Convert triple pattern to a set of nodes {s, p, o}
	 *
	 * @param t the triple pattern
	 * @return set of nodes {s, p, o}
	 */
	public static Set<Node> getNodes(Triple t) {
		return Sets.newHashSet(t.getSubject(), t.getPredicate(), t.getObject());
	}
	
	/**
	 * Returns all variables that occur in a triple pattern of the SPARQL query.
	 * @param query the query
	 * @return
	 */
	public Set<Var> getVariables(Query query){
		Set<Var> vars = new HashSet<>();
		
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		
		for (Triple tp : triplePatterns) {
			if(tp.getSubject().isVariable()){
				vars.add(Var.alloc(tp.getSubject()));
			} else if(tp.getObject().isVariable()){
				vars.add(Var.alloc(tp.getObject()));
			} else if(tp.getPredicate().isVariable()){
				vars.add(Var.alloc(tp.getPredicate()));
			}
		}
		
		return vars;
	}
	
	/**
	 * Returns all variables that occur as subject in a triple pattern of the SPARQL query.
	 * @param query the query
	 * @return
	 */
	public Set<Var> getSubjectVariables(Query query){

		Set<Triple> triplePatterns = extractTriplePattern(query, false);

		Set<Var> vars = new HashSet<>();
		for (Triple tp : triplePatterns) {
			if(tp.getSubject().isVariable()){
				vars.add(Var.alloc(tp.getSubject()));
			}
		}
		
		return vars;
	}
	
	/**
	 * Returns all variables that occur as subject in a triple pattern of the SPARQL query.
	 * @param query the query
	 * @return
	 */
	public static Set<Var> getSubjectVars(Query query){
		final Set<Var> vars = new HashSet<>();
		
		ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase(){
			@Override
			public void visit(ElementTriplesBlock el) {
				Iterator<Triple> triples = el.patternElts();
	            while (triples.hasNext()) {
	            	Triple triple = triples.next();
	            	if(triple.getSubject().isVariable()) {
	            		vars.add(Var.alloc(triples.next().getSubject()));
	            	}
	            }
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
	            while (triples.hasNext()) {
	            	TriplePath triple = triples.next();
	            	if(triple.getSubject().isVariable()) {
	            		vars.add(Var.alloc(triples.next().getSubject()));
	            	}
	            }
			}
		});
		
		return vars;
	}
	
	public static Set<Triple> getTriplePatterns(Query query){
		final Set<Triple> triplePatterns = Sets.newHashSet();
		
		ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase(){
			@Override
			public void visit(ElementTriplesBlock el) {
				Iterator<Triple> triples = el.patternElts();
	            while (triples.hasNext()) {
	            	Triple triple = triples.next();
	            	triplePatterns.add(triple);
	            }
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triplePaths = el.patternElts();
	            while (triplePaths.hasNext()) {
	            	TriplePath tp = triplePaths.next();
	            	if(tp.isTriple()) {
	            		Triple triple = tp.asTriple();
	            		triplePatterns.add(triple);
	            	}
	            }
			}
		});
		return triplePatterns;
	}
	
	/**
	 * Given a SPARQL query and a start node, return the outgoing
	 * triple patterns.
	 * @param query the query
	 * @param source the start node
	 * @return
	 */
	public static Set<Triple> getOutgoingTriplePatterns(Query query, final Node source){
		final Set<Triple> outgoingTriples = Sets.newHashSet();
		
		ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase(){
			@Override
			public void visit(ElementTriplesBlock el) {
				Iterator<Triple> triples = el.patternElts();
	            while (triples.hasNext()) {
	            	Triple triple = triples.next();
	            	Node subject = triple.getSubject();
	            	if(subject.equals(source)) {
	            		outgoingTriples.add(triple);
	            	}
	            }
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triplePaths = el.patternElts();
	            while (triplePaths.hasNext()) {
	            	TriplePath tp = triplePaths.next();
	            	if(tp.isTriple()) {
	            		Triple triple = tp.asTriple();
		            	Node subject = triple.getSubject();
		            	if(subject.equals(source)) {
		            		outgoingTriples.add(triple);
		            	}
	            	}
	            }
			}
		});
		
		return outgoingTriples;
	}
	
	/**
	 * Given a SPARQL query and a start node, return the maximum subject-object
	 * join depth.
	 * @param query the query
	 * @param source the start node
	 * @return
	 */
	public static int getSubjectObjectJoinDepth(Query query, final Node source){
		final Set<Triple> outgoingTriples = Sets.newHashSet();
		
		ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase(){
			@Override
			public void visit(ElementTriplesBlock el) {
				Iterator<Triple> triples = el.patternElts();
	            while (triples.hasNext()) {
	            	Triple triple = triples.next();
	            	Node subject = triple.getSubject();
	            	if(subject.equals(source) && triple.getObject().isVariable()) {
	            		outgoingTriples.add(triple);
	            	}
	            }
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triplePaths = el.patternElts();
	            while (triplePaths.hasNext()) {
	            	TriplePath tp = triplePaths.next();
	            	if(tp.isTriple()) {
	            		Triple triple = tp.asTriple();
		            	Node subject = triple.getSubject();
		            	if(subject.equals(source) && triple.getObject().isVariable()) {
		            		outgoingTriples.add(triple);
		            	}
	            	}
	            }
			}
		});
		
		int maxDepth = 0;
		for (Triple triple : outgoingTriples) {
			maxDepth = Math.max(maxDepth, 1 + getSubjectObjectJoinDepth(query, triple.getObject()));
		}
		
		return maxDepth;
	}

	public static CBDStructureTree getOptimalCBDStructure(Query query) {
		CBDStructureTree tree = new CBDStructureTree("root");

		Var var = query.getProjectVars().get(0);

		getOptimalCBDStructure(query, tree, var.asNode(), null, "");

		return tree;
	}

	private static void getOptimalCBDStructure(Query query, CBDStructureTree structureTree, Node current, Node parent, String direction) {
		QueryUtils utils = new QueryUtils();

		// traverse the outgoing paths
		Set<Triple> tmp = utils.extractOutgoingTriplePatterns(query, current)
				.stream()
				.filter(tp -> !direction.equals("in") || !tp.getObject().matches(parent))
				.collect(Collectors.toSet());
		if(!tmp.isEmpty()) {
			List<CBDStructureTree> outChildren = structureTree.getChildren().stream().filter(CBDStructureTree::isOutNode).collect(Collectors.toList());
			CBDStructureTree outChild;
			if(outChildren.isEmpty()) {
				outChild = structureTree.addOutNode();
			} else {
				outChild = outChildren.get(0);
			}
			tmp.stream()
					.filter(tp -> tp.getObject().isVariable())
					.map(Triple::getObject)
					.forEach(node -> getOptimalCBDStructure(query, outChild, node, current, "out"));
		}
		// traverse the incoming paths
		tmp = utils.extractIncomingTriplePatterns(query, current)
				.stream()
				.filter(tp -> !direction.equals("out") || !tp.getSubject().matches(parent))
				.collect(Collectors.toSet());
		if(!tmp.isEmpty()) {
			CBDStructureTree inChild = structureTree.addInNode();

			tmp.stream()
					.filter(tp -> tp.getSubject().isVariable())
					.map(Triple::getSubject)
					.forEach(node -> getOptimalCBDStructure(query, inChild, node, current, "in"));
		}
	}

	/**
	 * Returns all variables that occur as object in a triple pattern of the SPARQL query.
	 * @param query the query
	 * @return
	 */
	public static Set<Var> getObjectVars(Query query){
		return getTriplePatterns(query).stream()
				.filter(tp -> tp.getObject().isVariable())
				.map(tp -> Var.alloc(tp.getObject()))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all variables that occur as subject in a triple pattern of the SPARQL query.
	 * @param query the query
	 * @return
	 */
	public Set<Var> getObjectVariables(Query query){
		return extractTriplePattern(query, false).stream()
				.filter(tp -> tp.getObject().isVariable())
				.map(tp -> Var.alloc(tp.getObject()))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node in subject position, i.e. the outgoing
	 * triple patterns.
	 * @param query The SPARQL query.
	 * @param node the node
	 * @return
	 */
	public Set<Triple> extractOutgoingTriplePatterns(Query query, Node node){
		return extractTriplePattern(query, false).stream()
				.filter(t -> t.subjectMatches(node))
				.collect(Collectors.toSet());
	}

	public Set<Triple> extractOutgoingTriplePatternsTrans(Query query, Node node) {
		return Stream.concat(extractOutgoingTriplePatterns(query, node).stream(),
							 extractOutgoingTriplePatterns(query, node).stream()
									 .map(tp -> extractOutgoingTriplePatternsTrans(query, tp.getObject()))
									 .flatMap(set -> set.stream()))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns all triple patterns in given SPARQL query that have the given node in object position, i.e. the incoming
	 * triple patterns.
	 * @param query The SPARQL query.
	 * @param node the node
	 * @return
	 */
	public Set<Triple> extractIncomingTriplePatterns(Query query, Node node){
		return extractTriplePattern(query, false).stream()
				.filter(tp -> tp.objectMatches(node))
				.collect(Collectors.toSet());
	}

	public Set<Triple> extractIncomingTriplePatternsTrans(Query query, Node node) {
		return Stream.concat(extractIncomingTriplePatterns(query, node).stream(),
							 extractIncomingTriplePatterns(query, node).stream()
									 .map(tp -> extractIncomingTriplePatternsTrans(query, tp.getSubject()))
									 .flatMap(set -> set.stream()))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns all triple patterns in given SPARQL query that have the given node either in subject or in object position, i.e. 
	 * the ingoing and outgoing triple patterns.
	 * @param query The SPARQL query.
	 * @param node the node
	 * @return
	 */
	public Set<Triple> extractTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = new HashSet<>();
		triplePatterns.addAll(extractIncomingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that contain the
	 * given predicate. 
	 * @param query the SPARQL query.
	 * @param predicate the predicate
	 * @return
	 */
	public Set<Triple> extractTriplePatternsWithPredicate(Query query, Node predicate){
		// get all triple patterns
		Set<Triple> triplePatterns = extractTriplePattern(query);
		
		// filter by predicate
		triplePatterns.removeIf(tp -> !tp.predicateMatches(predicate));
		
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node either in subject or in object position, i.e. 
	 * the incoming and outgoing triple patterns.
	 * @param query The SPARQL query.
	 * @param node the node
	 * @return
	 */
	public Set<Triple> extractNonOptionalTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = new HashSet<>();
		triplePatterns.addAll(extractIncomingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		triplePatterns.removeAll(optionalTriplePattern);
		return triplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is either in subject or object position.
	 * @param query The SPARQL query.
	 * @return
	 */
	public Map<Var,Set<Triple>> extractTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>();
			triplePatterns.addAll(extractIncomingTriplePatterns(query, var));
			triplePatterns.addAll(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in subject position.
	 * @param query The SPARQL query.
	 * @return
	 */
	public Map<Var,Set<Triple>> extractOutgoingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * @return the optionalTriplePattern
	 */
	public Set<Triple> getOptionalTriplePatterns() {
		return optionalTriplePattern;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in subject position.
	 * @param query The SPARQL query.
	 * @return
	 */
	public Map<Var,Set<Triple>> extractIncomingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>(extractIncomingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in object position.
	 * @param query The SPARQL query.
	 * @return
	 */
	public Map<Var,Set<Triple>> extractIngoingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>(extractIncomingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	public Set<Triple> extractTriplePattern(Query query){
		return extractTriplePattern(query, false);
	}
	
	public Set<Triple> extractTriplePattern(Query query, boolean ignoreOptionals){
		triplePattern = new HashSet<>();
		optionalTriplePattern = new HashSet<>();
		
		query.getQueryPattern().visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			if(query.isSelectType()){
				for(Triple t : optionalTriplePattern){
					if(!ListUtils.intersection(new ArrayList<>(VarUtils.getVars(t)), query.getProjectVars()).isEmpty()){
						triplePattern.add(t);
					}
				}
			}
		}
		return triplePattern;
	}
	
	public boolean isOptional(Triple triple){
		return optionalTriplePattern.contains(triple);
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group){
		return extractTriplePattern(group, false);
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group, boolean ignoreOptionals){
		triplePattern = new HashSet<>();
		optionalTriplePattern = new HashSet<>();
		
		group.visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			triplePattern.addAll(optionalTriplePattern);
		}
		
		return triplePattern;
	}
	
	public Query removeUnboundObjectVarTriples(Query query) {
		QueryUtils queryUtils = new QueryUtils();

		Var rootVar = query.getProjectVars().get(0);

		// 1. outgoing triple paths pruning
		Set<Triple> outgoingTriplePatterns = queryUtils.extractOutgoingTriplePatternsTrans(query, rootVar);
		Multimap<Var, Triple> var2OutgoingTriplePatterns = HashMultimap.create();

		// mapping from variable to triple pattern
		for (Triple tp : outgoingTriplePatterns) {
			var2OutgoingTriplePatterns.put(Var.alloc(tp.getSubject()), tp);
		}

		// remove triple patterns with object is var node and leaf node
		Iterator<Triple> iterator = outgoingTriplePatterns.iterator();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			Node object = triple.getObject();
			if(object.isVariable() && !var2OutgoingTriplePatterns.containsKey(Var.alloc(object))) {
				iterator.remove();
			}
		}

		// 2. incoming triple paths pruning
		Set<Triple> incomingTriplePatterns = queryUtils.extractIncomingTriplePatternsTrans(query, rootVar);
		Multimap<Var, Triple> var2IncomingTriplePatterns = HashMultimap.create();

		// mapping from variable to triple pattern
		for (Triple tp : incomingTriplePatterns) {
			var2IncomingTriplePatterns.put(Var.alloc(tp.getObject()), tp);
		}

		// remove triple patterns with object is var node and leaf node
		iterator = incomingTriplePatterns.iterator();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			Node s = triple.getSubject();
			if(s.isVariable() && !var2IncomingTriplePatterns.containsKey(Var.alloc(s))) {
				iterator.remove();
			}
		}
		
		Query newQuery = new Query();
		newQuery.addProjectVars(query.getProjectVars());
		ElementTriplesBlock el = new ElementTriplesBlock();
		for (Triple triple : Sets.union(outgoingTriplePatterns, incomingTriplePatterns)) {
			el.addTriple(triple);
		}
		newQuery.setQuerySelectType();
		newQuery.setDistinct(true);
		newQuery.setQueryPattern(el);
		
		return newQuery;
	}
	
	/**
	 * Removes triple patterns of form (s rdf:type A) if there exists a
	 * triple pattern (s rdf:type B) such that the underlying
	 * knowledge base entails (B rdfs:subClassOf A).
	 * @param qef the query execution factory
	 * @param query the query
	 */
	public void filterOutGeneralTypes(QueryExecutionFactory qef, Query query){
		// extract all rdf:type triple patterns
		Set<Triple> typeTriplePatterns = extractTriplePatternsWithPredicate(query, RDF.type.asNode());
		
		// group by subject
		Multimap<Node, Triple> subject2TriplePatterns = HashMultimap.create();
		for (Triple tp : typeTriplePatterns) {
			subject2TriplePatterns.put(tp.getSubject(), tp);
		}
		
		// keep the most specific types for each subject
		for (Node subject : subject2TriplePatterns.keySet()) {
			Collection<Triple> triplePatterns = subject2TriplePatterns.get(subject);
			Collection<Triple> triplesPatterns2Remove = new HashSet<>();
			
			for (Triple tp : triplePatterns) {
				if(!triplesPatterns2Remove.contains(tp)) {
					// get all super classes for the triple object
					Set<Node> superClasses = getSuperClasses(qef, tp.getObject());
					
					// remove triple patterns that have one of the super classes as object
					for (Triple tp2 : triplePatterns) {
						if(tp2 != tp && superClasses.contains(tp2.getObject())) {
							triplesPatterns2Remove.add(tp2);
						}
					}
				}
			}
			
			// remove triple patterns
			triplePatterns.removeAll(triplesPatterns2Remove);
		}
	}

	public static DefaultDirectedGraph<Node, LabeledEdge> asJGraphT(Query query) {
		QueryUtils utils = new QueryUtils();

		Set<Triple> tps = utils.extractTriplePattern(query);

		DefaultDirectedGraph<Node, LabeledEdge> g = new DefaultDirectedGraph<>(LabeledEdge.class);

		tps.forEach(tp -> {
			g.addVertex(tp.getSubject());
			g.addVertex(tp.getObject());
			g.addEdge(tp.getSubject(), tp.getObject(), new LabeledEdge(tp.getSubject(), tp.getObject(), tp.getPredicate()));
		});

		return g;
	}

	public static void exportAsGraph(Query query, File file) {
		DefaultDirectedGraph<Node, LabeledEdge> g = asJGraphT(query);
		System.out.println(g.edgeSet().size());

		JGraphXAdapter adapter = new JGraphXAdapter(g);


		// positioning via jgraphx layouts
		mxHierarchicalLayout layout = new mxHierarchicalLayout(adapter);
		layout.execute(adapter.getDefaultParent());

		Map<String, Object> edgeStyle = new HashMap<>();
//edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
		edgeStyle.put(mxConstants.STYLE_SHAPE,    mxConstants.SHAPE_CONNECTOR);
		edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		edgeStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		edgeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#ffffff");

		Map<String, Object> nodeStyle = new HashMap<>();
		nodeStyle.put(mxConstants.STYLE_SHAPE,    mxConstants.SHAPE_ELLIPSE);
		nodeStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);

		mxStylesheet stylesheet = new mxStylesheet();
		stylesheet.setDefaultEdgeStyle(edgeStyle);
		stylesheet.setDefaultVertexStyle(nodeStyle);

		adapter.setStylesheet(stylesheet);

//		JFrame frame = new JFrame();
//		frame.getContentPane().add(new mxGraphComponent(adapter));
//		frame.pack();
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.setVisible(true);



		BufferedImage image = mxCellRenderer.createBufferedImage(adapter, null, 1, Color.WHITE, true, null);
		mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);


		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
			if (image != null) {
				encoder.encode(image);
			}
			outputStream.close();
//			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private Set<Node> getSuperClasses(QueryExecutionFactory qef, Node cls){
		Set<Node> superClasses = new HashSet<>();
		
		superClassesQueryTemplate.setIri("sub", cls.getURI());
		
		String query = superClassesQueryTemplate.toString();
		
		try {
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				superClasses.add(qs.getResource("sup").asNode());
			}
			qe.close();
		} catch (Exception e) {
			logger.error("ERROR. Getting super classes of " + cls + " failed.", e);
		}
		
		return superClasses;
	}
	
	@Override
	public void visit(ElementGroup el) {
		parents.push(el);
		for (Element e : el.getElements()) {
			e.visit(this);
		}
		parents.pop();
	}

	@Override
	public void visit(ElementOptional el) {
		optionalCount++;
		inOptionalClause = true;
		el.getOptionalElement().visit(this);
		inOptionalClause = false;
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if(inOptionalClause){
				optionalTriplePattern.add(t);
			} else {
				triplePattern.add(t);
			}
			if(!parents.isEmpty()){
				triple2Parent.put(t, parents.peek());
			}
		}
	}

	@Override
	public void visit(ElementPathBlock el) {
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath tp = iterator.next();
			if(inOptionalClause){
				if(tp.isTriple()){
					optionalTriplePattern.add(tp.asTriple());
					if(!parents.isEmpty()){
						triple2Parent.put(tp.asTriple(), parents.peek());
					}
				}
			} else {
				if(tp.isTriple()){
					triplePattern.add(tp.asTriple());
					if(!parents.isEmpty()){
						triple2Parent.put(tp.asTriple(), parents.peek());
					}
				}
			}
			
		}
	}

	@Override
	public void visit(ElementUnion el) {
		unionCount++;
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}
	
	@Override
	public void visit(ElementFilter el) {
		filterCount++;
	}

	public int getUnionCount() {
		return unionCount;
	}

	public int getOptionalCount() {
		return optionalCount;
	}

	public int getFilterCount() {
		return filterCount;
	}
	
	/**
	 * Returns the ElementGroup object containing the triple pattern.
	 * @param triple the triple patterm
	 * @return
	 */
	public ElementGroup getElementGroup(Triple triple){
		return triple2Parent.get(triple);
	}

	public static class LabeledEdge extends DefaultEdge {
		private final Node s;
		private final Node t;
		private final Node edge;

		public LabeledEdge(Node s, Node t, Node edge) {
			this.s = s;
			this.t = t;
			this.edge = edge;
		}

		public Node getEdge() {
			return edge;
		}

		@Override
		public String toString() {
			return edge.toString();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Query q = QueryFactory.create(
				"PREFIX  dbp:  <http://dbpedia.org/resource/>\n" + 
				"PREFIX  dbo: <http://dbpedia.org/ontology/>\n" + 
				"SELECT  ?thumbnail\n" + 
				"WHERE\n" + 
				"  { dbp:total dbo:thumbnail ?thumbnail }");
		QueryUtils queryUtils = new QueryUtils();
		System.out.println(queryUtils.extractOutgoingTriplePatterns(q, q.getProjectVars().get(0)));
		System.out.println(queryUtils.extractIncomingTriplePatterns(q, q.getProjectVars().get(0)));
		
		q = QueryFactory.create("SELECT DISTINCT  ?x0\n" + 
				"WHERE\n" + 
				"  { ?x0  <http://dbpedia.org/ontology/activeYearsEndYear>  ?date5 ;\n" + 
				"         <http://dbpedia.org/ontology/activeYearsStartYear>  ?date4 ;\n" + 
				"         <http://dbpedia.org/ontology/birthDate>  ?date0 ;\n" + 
				"         <http://dbpedia.org/ontology/birthPlace>  <http://dbpedia.org/resource/Austria> ;\n" + 
				"         <http://dbpedia.org/ontology/birthPlace>  <http://dbpedia.org/resource/Austria-Hungary> ;\n" + 
				"         <http://dbpedia.org/ontology/birthPlace>  <http://dbpedia.org/resource/Vienna> ;\n" + 
				"         <http://dbpedia.org/ontology/birthYear>  ?date3 ;\n" + 
				"         <http://dbpedia.org/ontology/deathDate>  ?date2 ;\n" + 
				"         <http://dbpedia.org/ontology/deathPlace>  <http://dbpedia.org/resource/Berlin> ;\n" + 
				"         <http://dbpedia.org/ontology/deathPlace>  <http://dbpedia.org/resource/Germany> ;\n" + 
				"         <http://dbpedia.org/ontology/deathYear>  ?date1 ;\n" + 
				"         <http://dbpedia.org/ontology/occupation>  <http://dbpedia.org/resource/Hilde_K%C3%B6rber__1> ;\n" + 
				"         <http://dbpedia.org/ontology/viafId>  \"32259546\" ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Person> .\n" + 
				"    FILTER ( ( str(?date0) = \"1906-07-03+02:00\" ) || ( str(?date0) = \"1906-07-03\" ) )\n" + 
				"    FILTER ( ( str(?date1) = \"1969+02:00\" ) || ( str(?date1) = \"1969-01-01\" ) )\n" + 
				"    FILTER ( ( str(?date2) = \"1969-05-31+02:00\" ) || ( str(?date2) = \"1969-05-31\" ) )\n" + 
				"    FILTER ( ( str(?date3) = \"1906+02:00\" ) || ( str(?date3) = \"1906-01-01\" ) )\n" + 
				"    FILTER ( ( str(?date4) = \"1930+02:00\" ) || ( str(?date4) = \"1930-01-01\" ) )\n" + 
				"    FILTER ( ( str(?date5) = \"1964+02:00\" ) || ( str(?date5) = \"1964-01-01\" ) )\n" + 
				"  }");
		
		System.out.println(queryUtils.removeUnboundObjectVarTriples(q));
		
		String query = "SELECT DISTINCT ?s WHERE {"
				+ "?s a <http://dbpedia.org/ontology/BeautyQueen> ."
				+ "?s <http://dbpedia.org/ontology/birthPlace> ?o0 ."
				+ "?o0 <http://dbpedia.org/ontology/isPartOf> ?o1 ."
				+ "?o1 <http://dbpedia.org/ontology/timeZone> <http://dbpedia.org/resource/Eastern_Time_Zone> .}";
		
		System.out.println(QueryUtils.getSubjectObjectJoinDepth(QueryFactory.create(query), Var.alloc("s")));

		System.out.println(queryUtils.extractOutgoingTriplePatternsTrans(QueryFactory.create(query), Var.alloc("s")));
	}


}
