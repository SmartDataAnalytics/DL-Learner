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
package org.dllearner.algorithms.qtl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeSubsumptionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.rendering.Edge;
import org.dllearner.algorithms.qtl.datastructures.rendering.Vertex;
import org.dllearner.algorithms.qtl.operations.traversal.LevelOrderTreeTraversal;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.algorithms.qtl.util.VarGenerator;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeUtils {
	
	private static final VarGenerator varGen = new VarGenerator("x");
	private static final String TRIPLE_PATTERN_TEMPLATE = "%s %s %s .";
	private static final OWLDataFactory df = new OWLDataFactoryImpl();
	
	public static String EMPTY_QUERY_TREE_QUERY = "SELECT ?s WHERE {?s ?p ?o.}";
	
	private static Reasoner reasoner = ReasonerRegistry.getRDFSSimpleReasoner();
	
	/**
	 * Returns the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree the query tree
	 * @param node the node
	 */
	public static List<RDFResourceTree> getPathToRoot(RDFResourceTree tree, RDFResourceTree node) {
		if(node.isRoot()) {
			return Collections.singletonList(node);
		}
		List<RDFResourceTree> path = new ArrayList<>();
		
		// add node itself
		path.add(node);
		
		// add parent node
		RDFResourceTree parent = node.getParent();
		path.add(parent);
		
		// traversal up to root node
		while(!parent.isRoot()) {
			parent = parent.getParent();
			path.add(parent);
		}
		
		return path;
	}
	
	/**
	 * Print the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree the query tree
	 * @param node the node
	 */
	public static String printPathToRoot(RDFResourceTree tree, RDFResourceTree node) {
		List<RDFResourceTree> path = getPathToRoot(tree, node);
		
		StringBuilder sb = new StringBuilder();
		Iterator<RDFResourceTree> iterator = path.iterator();
		
		RDFResourceTree child = iterator.next();
		sb.append(child).append("(").append(child.getID()).append(")");
		while (iterator.hasNext()) {
			RDFResourceTree parent = iterator.next();
			sb.append(" <").append(parent.getEdgeToChild(child)).append("> ");
			sb.append(parent).append("(").append(parent.getID()).append(")");
			child = parent;
		}
		return sb.toString();
	}
	
	/**
	 * Returns the number of nodes in the given query tree, i.e. the size of
	 * the children closure.
	 * @param tree the query tree
	 * @return the number of nodes
	 */
	public static int getNrOfNodes(RDFResourceTree tree) {
		return getNodes(tree).size();
	}
	
	/**
	 * Returns the set of edges that occur in the given query tree, i.e. the 
	 * closure of the edges.
	 * @param tree the query tree
	 * @return the set of edges in the query tree
	 */
	public static Set<Node> getEdges(RDFResourceTree tree) {
		Set<Node> edges = new HashSet<>();

		for(Iterator<RDFResourceTree> it = new LevelOrderTreeTraversal(tree); it.hasNext();) {
			edges.addAll(it.next().getEdges());
		}

		return edges;
	}
	
	/**
	 * Returns the number of edges that occur in the given query tree, which
	 * is obviously `n-1` where n is the number of nodes.
	 * @param tree the query tree
	 * @return the number of edges in the query tree
	 */
	public static int getNrOfEdges(RDFResourceTree tree) {
		return getNrOfNodes(tree) - 1;
	}
	
	/**
	 * Returns the complexity of the given query tree. 
	 * <div>
	 * Given a query tree T = (V,E) comprising a set V of vertices or nodes 
	 * together with a set E of edges or links. Moreover we have that 
	 * V = U ∪ L ∪ VAR , where U denotes the nodes that are URIs, L denotes
	 * the nodes that are literals and VAR contains the nodes that are variables.
	 * We define the complexity c(T) of query tree T as follows:
	 * </div>
	 * <code>c(T) = 1 + log(|U| * α + |L| * β + |VAR| * γ) </code>
	 * <div>
	 * with <code>α, β, γ</code> being the weight of the particular node types.
	 * </div>
	 * @param tree the query tree
	 * @return the complexity value
	 */
	public static double getComplexity(RDFResourceTree tree) {
		
		double varNodeWeight = 0.8;
		double resourceNodeWeight = 1.0;
		double literalNodeWeight = 1.0;
		
		double complexity = 0;
		
		List<RDFResourceTree> nodes = getNodes(tree);
		for (RDFResourceTree node : nodes) {
			if(node.isVarNode()) {
				complexity += varNodeWeight;
			} else if(node.isResourceNode()) {
				complexity += resourceNodeWeight;
			} else if(node.isLiteralNode()) {
				complexity += literalNodeWeight;
			}
		}
		
		return 1 + Math.log(complexity);
	}
	
	/**
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1
	 * @param tree2
	 * @return
	 */
    public static <N> boolean isSubsumedBy(QueryTree<N> tree1, QueryTree<N> tree2) {
    	// 1.compare the root nodes
    	// if both nodes denote the same resource or literal
    	if(tree1.isVarNode() && !tree2.isVarNode() && tree1.getUserObject().equals(tree2.getUserObject())){
    		return true;
    	}
    	
    	// if node2 is more specific than node1
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	Object edge;
    	for(QueryTree<N> child2 : tree2.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree2.getEdge(child2);
    		for(QueryTree<N> child1 : tree1.getChildren(edge)){
    			if(child1.isSubsumedBy(child2)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
				return false;
			}
    	}
    	return true;
    }
    
    /**
     * Returns all nodes in the given query tree.
     * @param tree the query tree
     * @return the nodes
     */
    public static List<RDFResourceTree> getNodes(RDFResourceTree tree) {
		List<RDFResourceTree> nodes = new ArrayList<>();
		nodes.add(tree);
		
		for (RDFResourceTree child : tree.getChildren()) {
			nodes.addAll(getNodes(child));
		}
		
		return nodes;
	}
    
    /**
     * Returns all nodes in the given query tree.
     * @param tree the query tree
     * @return the leaf nodes
     */
    public static List<RDFResourceTree> getLeafs(RDFResourceTree tree) {
		return getNodes(tree).stream().filter(GenericTree::isLeaf).collect(Collectors.toList());
	}
    
    /**
     * Returns the depth of the query tree
     * @param tree the query tree
     * @return the depth
     */
    public static int getDepth(RDFResourceTree tree) {
		int maxDepth = 0;
		
		for(RDFResourceTree child : tree.getChildren()) {
			int depth;
			if(child.isLeaf()) {
				depth = 1;
			} else {
				depth = 1 + getDepth(child);
			}
			maxDepth = Math.max(maxDepth, depth);
		}
		
		return maxDepth;
	}
    
    /**
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1 the first query tree
	 * @param tree2 the second query tree
	 * @return whether <code>tree1</code> is subsumed by <code>tree2</code>
	 */
    public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2) {
    	// 1.compare the root nodes
    	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
    	if(tree1.isResourceNode() && tree2.isResourceNode()) {
    		return tree1.getData().equals(tree2.getData());
    	} else if(tree1.isLiteralNode() && tree2.isLiteralNode()) {
    		if(tree1.isLiteralValueNode()) {
    			if(tree2.isLiteralValueNode()) {
    				return tree1.getData().equals(tree2.getData());
    			} else {
    				RDFDatatype d1 = tree1.getData().getLiteralDatatype();
    				return tree2.getDatatype().equals(d1);
    			}
    		} else {
    			if(tree2.isLiteralValueNode()) {
    				return false;
    			} else {
    				RDFDatatype d1 = tree1.getDatatype();
    				return tree2.getDatatype().equals(d1);
    			}
    		}
    		
    	}
    	
    	// (T_1 = ?) and (T_2 != ?) --> FALSE
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	for(Node edge2 : tree2.getEdges()){ // for each edge in T_2
    		List<RDFResourceTree> children1 = tree1.getChildren(edge2);
      		if(children1 != null) {
	    		for(RDFResourceTree child2 : tree2.getChildren(edge2)) { // and each child in T_2
	    			boolean isSubsumed = false;
	        		for(RDFResourceTree child1 : children1){ // there has to be at least one child in T_1 that is subsumed 
	        			if(QueryTreeUtils.isSubsumedBy(child1, child2)){ 
	        				isSubsumed = true;
	        				break;
	        			}
	        		}
	        		if(!isSubsumed){
	    				return false;
	    			}
	    		}
      		} else {
      			return false;
      		}
    	}
    	return true;
    }
    
    public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, LiteralNodeSubsumptionStrategy strategy) {
		return isSubsumedBy(tree1, tree2);
	}
    
    /**
   	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
   	 * tree1.
   	 * @param tree1
   	 * @param tree2
   	 * @param entailment
   	 * @return
   	 */
	public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, Entailment entailment) {
		Resource root = ResourceFactory.createResource("http://example.org/root");
		
		Model m1 = toModel(tree1, root);
		Model m2 = toModel(tree2, root);
		
		Model m1closure = ModelFactory.createDefaultModel();
		m1closure.add(ModelFactory.createInfModel(reasoner, m1));
		
		Model m2closure = ModelFactory.createDefaultModel();
		m2closure.add(ModelFactory.createInfModel(reasoner, m2));
		
		boolean sameClosure = m1closure.isIsomorphicWith(m2closure);
		if(sameClosure) {
			return true;
		}

		// check if each statement of m1 is contained in m2
		StmtIterator iterator = m2closure.listStatements();
		while (iterator.hasNext()) {
			Statement st = iterator.next();
			if (!st.getSubject().isAnon() && !st.getObject().isAnon()
					&& !(st.getPredicate().equals(RDFS.subClassOf) && st.getSubject().equals(st.getObject()))
					&& !m1closure.contains(st)) {
				return false;
			} 
		}
		return true;
	}
    
    /**
   	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
   	 * tree1.
   	 * @param tree1
   	 * @param tree2
   	 * @return
   	 */
       public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, SPARQLReasoner reasoner) {
       	// 1.compare the root nodes
       	
       	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
       	if(!tree1.isVarNode() && !tree2.isVarNode()) {
       		if(tree1.isResourceNode() && tree2.isResourceNode()) {
       			
       		}
       		return tree1.getData().equals(tree2.getData());
       	}
       	
       	// (T_1 = ?) and (T_2 != ?) --> FALSE
       	if(tree1.isVarNode() && !tree2.isVarNode()) {
       		return false;
       	}
       	
       	// 2. compare the children
       	for(Node edge2 : tree2.getEdges()){
       		List<RDFResourceTree> children1 = tree1.getChildren(edge2);
      		if(children1 != null) {
	       		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
	       			boolean isSubsumed = false;
	       			
	           		for(RDFResourceTree child1 : children1){
	           			if(QueryTreeUtils.isSubsumedBy(child1, child2, reasoner, edge2.equals(RDF.type.asNode()))){
	           				isSubsumed = true;
	           				break;
	           			}
	           		}
	           		if(!isSubsumed){
	       				return false;
	       			}
	       		}
      		} else {
      			return false;
      		}
       	}
       	return true;
       }
       
       /**
      	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
      	 * tree1.
      	 * @param tree1
      	 * @param tree2
      	 * @return
      	 */
          public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, AbstractReasonerComponent reasoner, boolean typeNode) {
          	// 1.compare the root nodes
          	
          	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
          	if(!tree1.isVarNode() && !tree2.isVarNode()) {
          		if(tree1.getData().equals(tree2.getData())) {
          			return true;
          		} else if(typeNode && tree1.isResourceNode() && tree2.isResourceNode()) {
          			return reasoner.isSuperClassOf(
          					new OWLClassImpl(IRI.create(tree2.getData().getURI())), 
          					new OWLClassImpl(IRI.create(tree1.getData().getURI())));
          		}
          		return false;
          	}
          	
          	// (T_1 = ?) and (T_2 != ?) --> FALSE
          	if(tree1.isVarNode() && !tree2.isVarNode()) {
          		return false;
          	}
          	
          	if(typeNode) {
//          		return isSubsumedBy(tree1, tree2, Entailment.RDFS);
          	}
          	
          	// 2. compare the children
          	for(Node edge2 : tree2.getEdges()){
          		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
          			boolean isSubsumed = false;
              		List<RDFResourceTree> children = tree1.getChildren(edge2);
              		if(children != null) {
              			for(RDFResourceTree child1 : children){
                  			if(QueryTreeUtils.isSubsumedBy(child1, child2, reasoner, edge2.equals(RDF.type.asNode()))){
                  				isSubsumed = true;
                  				break;
                  			}
                  		}
              		}
              		if(!isSubsumed){
          				return false;
          			}
          		}
          	}
          	return true;
          }
    
    /**
	 * Determines if the trees are equivalent from a subsumptional point of view.
	 * @param trees
	 * @return
	 */
    @SafeVarargs
	public static <N> boolean sameTrees(QueryTree<N>... trees) {
    	for(int i = 0; i < trees.length; i++) {
    		QueryTree<N> tree1 = trees[i];
    		for(int j = i; j < trees.length; j++) {
    			QueryTree<N> tree2 = trees[j];
    			if(!sameTrees(tree1, tree2)) {
    				return false;
    			}
        	}
    	}
    	
    	return true;
    }
    
	/**
	 * Determines if both trees are equivalent from a subsumptional point of
	 * view.
	 * 
	 * @param tree1
	 * @param tree2
	 * @return
	 */
	public static <N> boolean sameTrees(QueryTree<N> tree1, QueryTree<N> tree2) {
		return isSubsumedBy(tree1, tree2) && isSubsumedBy(tree2, tree1);
	}
	
	public static <N> boolean sameTrees(RDFResourceTree tree1, RDFResourceTree tree2) {
		return
				tree1.getData().equals(tree2.getData()) && // root(t1) == root(t2)
				tree1.getNumberOfChildren() == tree2.getNumberOfChildren() && // #children(t1) == #children(t2)
				isSubsumedBy(tree1, tree2) && isSubsumedBy(tree2, tree1); // t1 <= t2 && t2 <= t1
	}
	
	public static Model toModel(RDFResourceTree tree) {
		Model model = ModelFactory.createDefaultModel();
		buildModel(model, tree, model.asRDFNode(NodeFactory.createAnon()).asResource());
		return model;
	}
	
	public static Model toModel(RDFResourceTree tree, Resource subject) {
		Model model = ModelFactory.createDefaultModel();
		buildModel(model, tree, subject);
		return model;
	}
	
	private static void buildModel(Model model, RDFResourceTree tree, Resource subject) {
		int i = 0;
		for (Node edge : tree.getEdges()) {
			Property p = model.getProperty(edge.getURI());
			for (RDFResourceTree child : tree.getChildren(edge)) {
				RDFNode object = child.isVarNode() ? model.asRDFNode(NodeFactory.createAnon()) : model.asRDFNode(child.getData());
				model.add(subject, p, object);
//				if (child.isVarNode()) {
					buildModel(model, child, object.asResource());
//				}
			}
		}
	}
	
	public static OWLClassExpression toOWLClassExpression(RDFResourceTree tree) {
    	return toOWLClassExpression(tree, LiteralNodeConversionStrategy.DATATYPE);
	}
	
	public static OWLClassExpression toOWLClassExpression(RDFResourceTree tree, LiteralNodeConversionStrategy literalConversion) {
    	return buildOWLClassExpression(tree, literalConversion);
	}
	
	private static OWLClassExpression buildOWLClassExpression(RDFResourceTree tree, LiteralNodeConversionStrategy literalConversion) {
		Set<OWLClassExpression> classExpressions = new HashSet<>();
		for(Node edge : tree.getEdges()) {
			for (RDFResourceTree child : tree.getChildren(edge)) {
				if(edge.equals(RDF.type.asNode()) || edge.equals(RDFS.subClassOf.asNode())) {
					if(child.isVarNode()) {
						classExpressions.add(buildOWLClassExpression(child, literalConversion));
					} else {
						classExpressions.add(df.getOWLClass(IRI.create(child.getData().getURI())));
					}
				} else {
					// create r some C
					if(child.isLiteralNode()) {
						OWLDataProperty dp = df.getOWLDataProperty(IRI.create(edge.getURI()));
						if(!child.isLiteralValueNode()) {
							classExpressions.add(df.getOWLDataSomeValuesFrom(dp, df.getOWLDatatype(IRI.create(child.getDatatype().getURI()))));
						} else {
							OWLLiteral value = OwlApiJenaUtils.getOWLLiteral(child.getData().getLiteral());
							classExpressions.add(df.getOWLDataHasValue(dp, value));
						}
						
					} else {
						OWLClassExpression filler = null;
						if(child.isVarNode()) {
							filler = buildOWLClassExpression(child, literalConversion);
							classExpressions.add(df.getOWLObjectSomeValuesFrom(
									df.getOWLObjectProperty(IRI.create(edge.getURI())), 
									filler));
						} else if (child.isResourceNode()) {
							classExpressions.add(df.getOWLObjectHasValue(
									df.getOWLObjectProperty(IRI.create(edge.getURI())), 
									df.getOWLNamedIndividual(IRI.create(child.getData().getURI()))));
						}
					}
				}
			}
		}
		if(classExpressions.isEmpty()) {
			return df.getOWLThing();
		} else if(classExpressions.size() == 1){
    		return classExpressions.iterator().next();
    	} else {
    		return df.getOWLObjectIntersectionOf(classExpressions);
    	}
	}
	
	/**
	 * Returns a SPARQL query representing the query tree. Note, for empty trees
	 * it just returns 
	 * <p><code>SELECT ?s WHERE {?s ?p ?o.}</code></p>
	 * @param tree
	 * @return
	 */
	public static Query toSPARQLQuery(RDFResourceTree tree) {
		return QueryFactory.create(toSPARQLQueryString(tree));
	}
	
	public static String toSPARQLQueryString(RDFResourceTree tree) {
    	return toSPARQLQueryString(tree, PrefixMapping.Standard);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, PrefixMapping pm) {
    	return toSPARQLQueryString(tree, null, pm, LiteralNodeConversionStrategy.DATATYPE);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, String baseIRI, PrefixMapping pm) {
    	return toSPARQLQueryString(tree, baseIRI, pm, LiteralNodeConversionStrategy.DATATYPE);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, String baseIRI, PrefixMapping pm, LiteralNodeConversionStrategy literalConversion) {
		if(!tree.hasChildren()){
    		return EMPTY_QUERY_TREE_QUERY;
    	}
    	
    	varGen.reset();
    	
    	SerializationContext context = new SerializationContext(pm);
    	context.setBaseIRI(baseIRI);
    	
    	StringBuilder sb = new StringBuilder();
    	
    	// Add BASE declaration
        if (baseIRI != null) {
            sb.append("BASE ");
            sb.append(FmtUtils.stringForURI(baseIRI, null, null));
            sb.append('\n');
        }

        // Then pre-pend prefixes
        for (String prefix : pm.getNsPrefixMap().keySet()) {
            sb.append("PREFIX ");
            sb.append(prefix);
            sb.append(": ");
            sb.append(FmtUtils.stringForURI(pm.getNsPrefixURI(prefix), null, null));
            sb.append('\n');
        }
        
        List<ExprNode> filters = new ArrayList<>();
        
        // target var
        String targetVar = "?s";
        
        // header
    	sb.append(String.format("SELECT DISTINCT %s WHERE {\n", targetVar));
    	
    	// triple patterns
    	buildSPARQLQueryString(tree, targetVar, sb, filters, context);
        
    	// filters
    	if(!filters.isEmpty()) {
    		Iterator<ExprNode> it = filters.iterator();
    		ExprNode filter = it.next();
    		while(it.hasNext()) {
    			filter = new E_LogicalAnd(filter, it.next());
    		}
    		sb.append("FILTER(").append(filter.toString()).append(")\n");
    	}
    	
        sb.append("}");
    	
    	Query query = QueryFactory.create(sb.toString(), Syntax.syntaxSPARQL_11);
    	query.setPrefixMapping(pm);
    	
    	return query.toString();
	}
	
	private static int buildGraph(Integer parentId, DirectedGraph<Vertex, Edge> graph, RDFResourceTree tree, SerializationContext context){
    	Vertex parent = new Vertex(parentId, FmtUtils.stringForNode(tree.getData(), context));
    	graph.addVertex(parent);
    	
    	int childId = parentId;
    	
    	for (Node edgeNode : tree.getEdges()) {
    		String edgeLabel = FmtUtils.stringForNode(edgeNode, context);
	    	for (RDFResourceTree child : tree.getChildren(edgeNode)) {
	    		childId++;
	    		String childLabel = FmtUtils.stringForNode(child.getData(), context);
	    		
	    		Vertex childVertex = new Vertex(childId, childLabel);
	    		graph.addVertex(childVertex);
	    		
	    		Edge edge = new Edge(Long.valueOf(parentId + "0" + childId), edgeLabel);
				graph.addEdge(parent, childVertex, edge);
				System.err.println(edgeLabel);
				System.err.println(graph.getEdgeSource(edge).getId());
				System.err.println(graph.getEdgeTarget(edge).getId());
				System.err.println(childId + "::" + childLabel);
				
				childId = buildGraph(childId, graph, child, context);
			}
    	}
    	
    	return childId;
    }
	
	public static void asGraph(RDFResourceTree tree, String baseIRI, PrefixMapping pm, File outputFile) {
		SerializationContext context = new SerializationContext(pm);
		context.setBaseIRI(baseIRI);
		
		final DirectedGraph<Vertex, Edge> graph = new DefaultDirectedGraph<>(Edge.class);
		buildGraph(0, graph, tree, context);
		VertexNameProvider<Vertex> vertexIDProvider = vertex -> String.valueOf(vertex.getId());

		VertexNameProvider<Vertex> vertexNameProvider = Vertex::getLabel;

		EdgeNameProvider<Edge> edgeIDProvider = edge -> String.valueOf(edge.getId());

		EdgeNameProvider<Edge> edgeLabelProvider = Edge::getLabel;
		GraphMLExporter<Vertex, Edge> exporter = new GraphMLExporter<>(vertexIDProvider,
				vertexNameProvider, edgeIDProvider, edgeLabelProvider);
		try {
			exporter.export(new FileWriter(outputFile), graph);
		} catch (TransformerConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
		}
	}
    
	private static void buildSPARQLQueryString(RDFResourceTree tree,
			String subjectStr, StringBuilder sb, Collection<ExprNode> filters,
			SerializationContext context) {
		if (!tree.isLeaf()) {
			for (Node edge : tree.getEdges()) {
				// process predicate
				String predicateStr = FmtUtils.stringForNode(edge, context);
				for (RDFResourceTree child : tree.getChildren(edge)) {
					// pre-process object
					Node object = child.getData();
					
					if(child.isVarNode()) {
						// set a fresh var in the SPARQL query
						object = varGen.newVar();
					} else if(child.isLiteralNode() && !child.isLiteralValueNode()) { 
						// set a fresh var in the SPARQL query
						object = varGen.newVar();
						
						// literal node describing a set of literals is rendered depending on the conversion strategy
						if(child.getDatatype() != null) {
							ExprNode filter = new E_Equals(
									new E_Datatype(new ExprVar(object)), 
									NodeValue.makeNode(NodeFactory.createURI(child.getDatatype().getURI())));
//							filters.add(filter);
						}
						
					} 
					
					// process object
					String objectStr = FmtUtils.stringForNode(object, context);
					sb.append(String.format(TRIPLE_PATTERN_TEMPLATE, subjectStr, predicateStr, objectStr)).append("\n");
					
					/*
					 * only if child is var node recursively process children if
					 * exist because for URIs it doesn't make sense to add the
					 * triple pattern and for literals there can't exist a child
					 * in the tree
					 */
					if (child.isVarNode()) {
						buildSPARQLQueryString(child, objectStr, sb, filters, context);
					}
				}
			}
		}
    }
	
	/**
	 * Remove trivial statements according to the given entailment semantics:
	 * <h3>RDFS</h3>
	 * <ul>
	 * <li>remove trivial statements like <code>?s a ?x</code>
	 * <li>remove type statements if this is given by domain and range 
	 * of used statements.</li>
	 * 
	 * </ul>
	 * @param tree the tree
	 * @param entailment the entailment regime
	 */
	public static void prune(RDFResourceTree tree, AbstractReasonerComponent reasoner, Entailment entailment) {
		
		// remove trivial statements
		for(Node edge : new TreeSet<>(tree.getEdges())) {
			if(edge.equals(RDF.type.asNode())) { // check outgoing rdf:type edges
				List<RDFResourceTree> children = new ArrayList<>(tree.getChildren(edge));
				children.stream().filter(child -> !isNonTrivial(child, entailment)).forEach(child -> tree.removeChild(child, edge));
			} else {// recursively apply pruning on all subtrees
				List<RDFResourceTree> children = tree.getChildren(edge);
				
				for (RDFResourceTree child : children) {
					prune(child, reasoner, entailment);
				}
			}
		}
		
		if(entailment == Entailment.RDFS) {
//			// 1. rdfs:domain:
//			// remove rdf:type edges if this is implicitly given by the other outgoing edges
//			// 2. rdfs:range:
//			// remove rdf:type edges if this is implicitly given by the incoming edge
//			if (!tree.isLeaf()) {
//				SortedSet<Node> edges = tree.getEdges(NodeType.RESOURCE);
//				
//				List<RDFResourceTree> typeChildren = tree.getChildren(RDF.type.asNode());
//				
//				if(typeChildren != null && !typeChildren.isEmpty()) {
//					// get domains
//					Set<Node> domains = new HashSet<Node>();
//					for (Node edge : edges) {
//						OWLClassExpression domain = reasoner.getDomain(new OWLObjectPropertyImpl(IRI.create(edge.getURI())));
//						if(!domain.isAnonymous()) {
//							domains.add(NodeFactory.createURI(domain.asOWLClass().toStringID()));
//						}
//					}
//					
//					// get range of incoming edge
//					Set<Node> ranges = new HashSet<Node>();
//					
//					if(!tree.isRoot()) {
//						// get the incoming edge from parent node
//						Node incomingEdge = tree.getParent().getEdgeToChild(tree);
//						
//						OWLClassExpression rangeExpression = reasoner.getRange(new OWLObjectPropertyImpl(IRI.create(incomingEdge.getURI())));
//						if(rangeExpression.isAnonymous()) {
//							// TODO we have to handle complex class expressions, e.g. split intersections
//						} else {
//							ranges.add(NodeFactory.createURI(rangeExpression.asOWLClass().toStringID()));
//						}
//					}
//
//					// remove rdf:type children if implicitly given by domain or range
//					for (RDFResourceTree child : new ArrayList<>(typeChildren)) {
//						if(domains.contains(child.getData()) || ranges.contains(child.getData())) {
//							tree.removeChild(child, RDF.type.asNode());
//						}
//					}
//				}
//			}
//			
//			// apply recursively on children
//			SortedSet<Node> edges = tree.getEdges();
//			for (Node edge : edges) {
//				if(!edge.equals(RDF.type.asNode())) {
//					for (RDFResourceTree child : tree.getChildren(edge)) {
//						prune(child, reasoner, entailment);
//					}
//				}
//			}
		}
		
		// we have to run the subsumption check one more time to prune the tree
		for (Node edge : tree.getEdges()) {
			Set<RDFResourceTree> children2Remove = new HashSet<>();
			List<RDFResourceTree> children = tree.getChildren(edge);
			for(int i = 0; i < children.size(); i++) {
				RDFResourceTree child1 = children.get(i);
				if(!children2Remove.contains(child1)) {
					for(int j = i + 1; j < children.size(); j++) {
						RDFResourceTree child2 = children.get(j);
//						System.out.println(QueryTreeUtils.getPathToRoot(tree, child1));
//						System.out.println(QueryTreeUtils.getPathToRoot(tree, child2));
						if(!children2Remove.contains(child2)) {
							if (QueryTreeUtils.isSubsumedBy(child1, child2)) {
								children2Remove.add(child2);
							} else if (QueryTreeUtils.isSubsumedBy(child2, child1)) {
								children2Remove.add(child1);
							}
						}
					}
				}
				
			}
			
			for (RDFResourceTree child : children2Remove) {
				tree.removeChild(child, edge);
			}
		}
		
//		if(entailment == Entailment.RDFS) {
//			if(reasoner != null) {
//				List<RDFResourceTree> typeChildren = tree.getChildren(RDF.type.asNode());
//				
//				// compute implicit types
//				Set<OWLClassExpression> implicitTypes = new HashSet<OWLClassExpression>();
//				for(Node edge : tree.getEdges()) {
//					if(!edge.equals(RDF.type.asNode())) {
//						// get domain for property
//						implicitTypes.add(reasoner.getDomain(new OWLObjectPropertyImpl(IRI.create(edge.getURI()))));
//					}
//				}
//				if(typeChildren != null) {
//					// remove type children which are already covered implicitely
//					for (RDFResourceTree child : new ArrayList<RDFResourceTree>(tree.getChildren(RDF.type.asNode()))) {
//						if(child.isResourceNode() && implicitTypes.contains(new OWLClassImpl(IRI.create(child.getData().getURI())))) {
//							tree.removeChild(child, RDF.type.asNode());
//							System.out.println("removing " + child.getData().getURI());
//						}
//					}
//				}
//				
//			}
//		}
	}
	
	/**
	 * Recursively removes edges that lead to a leaf node which is a variable.
	 * @param tree the tree
	 */
	public static boolean removeVarLeafs(RDFResourceTree tree) {
		SortedSet<Node> edges = new TreeSet<>(tree.getEdges());
		
		boolean modified = false;
		for (Node edge : edges) {
			List<RDFResourceTree> children = new ArrayList<>(tree.getChildren(edge));
//			
			for (RDFResourceTree child : children) {
				if(child.isLeaf() && child.isVarNode()) {
					tree.removeChild(child, edge);
					modified = true;
				} else {
					modified = removeVarLeafs(child);
					if(modified && child.isLeaf() && child.isVarNode()) {
						tree.removeChild(child, edge);
						modified = true;
					}
				}
			}
			
			
		}
		return modified;
	}
	
	public static boolean isNonTrivial(RDFResourceTree tree, Entailment entailment) {
		if(tree.isResourceNode() || tree.isLiteralNode()){
    		return true;
    	} else {
    		for (Node edge : tree.getEdges()) {
    			for (RDFResourceTree child : tree.getChildren(edge)) {
        			if(!edge.equals(RDFS.subClassOf.asNode())){
        				return true;
        			} else if(child.isResourceNode()){
        				return true;
        			} else if(isNonTrivial(child, entailment)){
        				return true;
        			}
        		}
			}
    		
    	}
    	return false;
	}

	/**
	 * @param tree1
	 * @param tree2
	 * @param entailment
	 * @param reasoner
	 * @return
	 */
	public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, Entailment entailment,
			AbstractReasonerComponent reasoner) {

		if(entailment == Entailment.SIMPLE) {
			return isSubsumedBy(tree1, tree2);
		}

		// 1.compare the root nodes

		// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
		if (!tree1.isVarNode() && !tree2.isVarNode()) {
			if (tree1.isResourceNode() && tree2.isResourceNode()) {
				return tree1.getData().equals(tree2.getData());
			} else if(tree1.isLiteralNode() && tree2.isLiteralNode()) {
	    		if(tree1.isLiteralValueNode()) {
	    			if(tree2.isLiteralValueNode()) {
	    				return tree1.getData().equals(tree2.getData());
	    			} else {
	    				RDFDatatype d1 = tree1.getData().getLiteralDatatype();
	    				return tree2.getDatatype().equals(d1);
	    			}
	    		} else {
	    			if(tree2.isLiteralValueNode()) {
	    				return false;
	    			} else {
	    				RDFDatatype d1 = tree1.getDatatype();
	    				return tree2.getDatatype().equals(d1);
	    			}
	    		}

	    	}
		}

		// (T_1 = ?) and (T_2 != ?) --> FALSE
		if (tree1.isVarNode() && !tree2.isVarNode()) {
			return false;
		}

		// 2. compare the children
		for (Node edge2 : tree2.getEdges()) {

			// get sub properties
			OWLObjectProperty prop2 = OwlApiJenaUtils.asOWLEntity(edge2, EntityType.OBJECT_PROPERTY);
			SortedSet<OWLObjectProperty> subProperties = reasoner.getSubProperties(prop2);
			subProperties.add(prop2);

			// for each subtree T2_sub in T2
			for (RDFResourceTree child2 : tree2.getChildren(edge2)) {
				boolean childSubsumed = false;

				// for each sub edge
				for(OWLObjectProperty subProp : subProperties) {
					// check if there is a child in T_1 that is subsumed by
					Node edge1 = OwlApiJenaUtils.asNode(subProp);
					List<RDFResourceTree> children1 = tree1.getChildren(edge1);

					if(children1 != null) {
						for (RDFResourceTree child1 : children1) {
							if (QueryTreeUtils.isSubsumedBy(child1, child2, entailment, reasoner)) {
								childSubsumed = true;
								break;
							}
						}
					}
					if(childSubsumed) {
						break;
					}
				}

				// we found no subtree in T1 that is subsumed by t2_sub
				if(!childSubsumed) {
					return false;
				}
			}
		}

		// 2. compare the children
//		for (Node edge2 : tree2.getEdges()) {
//
//			// get sub properties
//			OWLObjectProperty prop2 = OwlApiJenaUtils.asOWLEntity(edge2, EntityType.OBJECT_PROPERTY);
//			SortedSet<OWLObjectProperty> subProperties = reasoner.getSubProperties(prop2);
//			subProperties.add(prop2);
//
//			boolean edgeSubsumed = false;
//
//			Iterator<OWLObjectProperty> iterator = subProperties.iterator();
//			while (!edgeSubsumed && iterator.hasNext()) {
//				OWLObjectProperty subProp  = iterator.next();
//
//				Node edge1 = OwlApiJenaUtils.asNode(subProp);
//
//				List<RDFResourceTree> children1 = tree1.getChildren(edge1);
//
//				if (children1 != null) {
//
//					boolean childrenSubsumed = true;
//					for (RDFResourceTree child2 : tree2.getChildren(edge2)) {
//						boolean childSubsumed = false;
//
//						for (RDFResourceTree child1 : children1) {
//							if (QueryTreeUtils.isSubsumedBy(child1, child2, entailment, reasoner)) {
//								childSubsumed = true;
//								break;
//							}
//						}
//						if(!childSubsumed) {
//							childrenSubsumed = false;
//						}
//					}
//
//					if(childrenSubsumed) {
//						edgeSubsumed = true;
//					}
//				}
//			}
//
//			if(!edgeSubsumed) {
//				System.err.println("edge not subsumed");
//				return false;
//			}
//		}
		return true;
	}

	/*
	 * For each edge in tree 1 we compute the related edges in tree 2.
	 */
	private static Multimap<Node, Node> getRelatedEdges(RDFResourceTree tree1, RDFResourceTree tree2, AbstractReasonerComponent reasoner) {
		Multimap<Node, Node> relatedEdges = HashMultimap.create();

		for(Node edge1 : tree1.getEdges()) {
			// trivial
			if(tree2.getEdges().contains(edge1)) {
				relatedEdges.put(edge1, edge1);
			}
			// check if it's not a built-in properties
			if (!edge1.getNameSpace().equals(RDF.getURI())
					&& !edge1.getNameSpace().equals(RDFS.getURI())
					&& !edge1.getNameSpace().equals(OWL.getURI())) {

				// get related edges by subsumption
				OWLProperty prop;
				if(tree1.isObjectPropertyEdge(edge1)) {
					prop = new OWLObjectPropertyImpl(IRI.create(edge1.getURI()));
				} else {
					prop = new OWLDataPropertyImpl(IRI.create(edge1.getURI()));
				}

				for (OWLProperty p : reasoner.getSuperProperties(prop)) {
					Node edge = NodeFactory.createURI(p.toStringID());
					if(tree2.getEdges().contains(edge)) {
						relatedEdges.put(edge1, edge);
					}
				}
				for (OWLProperty p : reasoner.getSubProperties(prop)) {
					Node edge = NodeFactory.createURI(p.toStringID());
					if(tree2.getEdges().contains(edge)) {
						relatedEdges.put(edge1, edge);
					}
				}
			}
		}
		return relatedEdges;
	}
}
