/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.VarGenerator;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeUtils {
	
	private static final VarGenerator varGen = new VarGenerator("x");
	private static final String TRIPLE_PATTERN_TEMPLATE = "%s %s %s .";
	
	/**
	 * Returns the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree
	 * @param node
	 */
	public static <N> List<QueryTree<N>> getPathToRoot(QueryTree<N> tree, QueryTree<N> node) {
		if(node.isRoot()) {
			return Collections.singletonList(node);
		}
		List<QueryTree<N>> path = new ArrayList<QueryTree<N>>();
		
		// add node itself
		path.add(node);
		
		// add parent node
		QueryTree<N> parent = node.getParent();
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
	 * @param tree
	 * @param node
	 */
	public static <N> String printPathToRoot(QueryTree<N> tree, QueryTree<N> node) {
		List<QueryTree<N>> path = getPathToRoot(tree, node);
		
		StringBuilder sb = new StringBuilder();
		Iterator<QueryTree<N>> iterator = path.iterator();
		
		QueryTree<N> child = iterator.next();
		sb.append(child + "(" + child.getId() + ")");
		while (iterator.hasNext()) {
			QueryTree<N> parent = (QueryTree<N>) iterator.next();
			sb.append(" <").append(parent.getEdge(child)).append("> ");
			sb.append(parent + "(" + parent.getId() + ")");
			child = parent;
		}
		return sb.toString();
	}
	
	/**
	 * Returns all nodes in the given query tree, i.e. the closure of 
	 * the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns all nodes of the given node type in the query tree, i.e. 
	 * the closure of the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree, NodeType nodeType) {
		// get all nodes
		List<QueryTree<N>> nodes = tree.getChildrenClosure();
		
		// filter by type
		Iterator<QueryTree<N>> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			QueryTree<N> node = (QueryTree<N>) iterator.next();
			if(node.getNodeType() != nodeType) {
				iterator.remove();
			}
			
		}
		return nodes;
	}
	
	/**
	 * Returns the number of nodes in the given query tree, i.e. the number of 
	 * the children closure.
	 * @param tree
	 * @return 
	 */
	public static <N> int getNrOfNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure().size();
	}
	
	/**
	 * Returns the set of edges that occur in the given query tree, i.e. the 
	 * closure of the edges.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> List<QueryTree<N>> getEdges(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns the number of edges that occur in the given query tree, which
	 * is obviously n-1 where n is the number of nodes.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> int getNrOfEdges(QueryTree<N> tree) {
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
	 * with <code>α, β, γ</code> being weight of the particular node types.
	 * </div>
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> double getComplexity(QueryTree<N> tree) {
		
		double varNodeWeight = 0.8;
		double resourceNodeWeight = 1.0;
		double literalNodeWeight = 1.0;
		
		double complexity = 0;
		
		List<QueryTree<N>> nodes = getNodes(tree);
		for (QueryTree<N> node : nodes) {
			switch (node.getNodeType()) {
			case VARIABLE:
				complexity += varNodeWeight;
				break;
			case RESOURCE:
				complexity += resourceNodeWeight;
				break;
			case LITERAL:
				complexity += literalNodeWeight;
				break;
			default:
				break;
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
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1
	 * @param tree2
	 * @return
	 */
    public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2) {
    	// 1.compare the root nodes
    	
    	// (T_1 != ?) and (T_2 != ?) --> T_1 = T_2
    	if(!tree1.isVarNode() && !tree2.isVarNode()) {
    		return tree1.getData().equals(tree2.getData());
    	}
    	
    	// (T_1 = ?) and (T_2 != ?) --> FALSE
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	for(Node edge2 : tree2.getEdges()){
    		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
    			boolean isSubsumed = false;
        		for(RDFResourceTree child1 : tree1.getChildren(edge2)){
        			if(QueryTreeUtils.isSubsumedBy(child1, child2)){
        				isSubsumed = true;
        				break;
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
       		for(RDFResourceTree child2 : tree2.getChildren(edge2)) {
       			boolean isSubsumed = false;
           		for(RDFResourceTree child1 : tree1.getChildren(edge2)){
           			if(QueryTreeUtils.isSubsumedBy(child1, child2, reasoner, edge2.equals(RDF.type.asNode()))){
           				isSubsumed = true;
           				break;
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
      	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
      	 * tree1.
      	 * @param tree1
      	 * @param tree2
      	 * @return
      	 */
          public static boolean isSubsumedBy(RDFResourceTree tree1, RDFResourceTree tree2, SPARQLReasoner reasoner, boolean typeNode) {
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
	
	public static Query toSPARQLQuery(RDFResourceTree tree) {
		return QueryFactory.create(toSPARQLQueryString(tree));
	}
	
	public static String toSPARQLQueryString(RDFResourceTree tree) {
    	return toSPARQLQueryString(tree, PrefixMapping.Standard);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, PrefixMapping pm) {
    	return toSPARQLQueryString(tree, null, pm);
    }
	
	public static String toSPARQLQueryString(RDFResourceTree tree, String baseIRI, PrefixMapping pm) {
		if(!tree.hasChildren()){
    		return "SELECT ?x0 WHERE {?x0 ?p ?o.}";
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
        
        //
        String targetVar = "?s";
        
        // header
    	sb.append(String.format("SELECT DISTINCT %s WHERE {\n", targetVar));
    	
    	// triple patterns
    	buildSPARQLQueryString(tree, targetVar, sb, context);
        
        sb.append("}");
    	
    	Query query = QueryFactory.create(sb.toString(), Syntax.syntaxSPARQL_11);
    	query.setPrefixMapping(pm);
    	
    	return query.toString();
	}
    
    private static void buildSPARQLQueryString(RDFResourceTree tree, String subjectStr, StringBuilder sb, SerializationContext context){
		if (!tree.isLeaf()) {
			for (Node edge : tree.getEdges()) {
				// process predicate
				String predicateStr = FmtUtils.stringForNode(edge, context);
				for (RDFResourceTree child : tree.getChildren(edge)) {
					// process object
					Node object = child.getData();
					String objectStr = object.isVariable() ? varGen.newVar() : FmtUtils.stringForNode(object, context);
					sb.append(String.format(TRIPLE_PATTERN_TEMPLATE, subjectStr, predicateStr, objectStr)).append("\n");
					if (object.isVariable()) {
						buildSPARQLQueryString(child, objectStr, sb, context);
					}
				}
			}
		}
    }
}
