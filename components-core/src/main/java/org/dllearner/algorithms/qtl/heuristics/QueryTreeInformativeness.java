package org.dllearner.algorithms.qtl.heuristics;

import java.util.List;
import java.util.SortedSet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.semanticweb.owlapi.model.OWLProperty;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Lorenz Buehmann 
 * @since May 4, 2015
 */
public class QueryTreeInformativeness {
	
	private QueryExecutionFactory qef;

	public QueryTreeInformativeness(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public double getQueryTreeInformativeness(RDFResourceTree tree) {
		double informativeness = 0;
		SortedSet<Node> edges = tree.getEdges();
		
		for (Node edge : edges) {
			// ITF of edge in KB
			double itf = getInverseTripleFrequency(edge.getURI());
			
			double childrenInformativeness = 0;
			
			List<RDFResourceTree> children = tree.getChildren(edge);
			for (RDFResourceTree child : children) {
				childrenInformativeness += getQueryTreeInformativeness(child);
			}
			
			// divide by number of children for current edge
			childrenInformativeness /= children.size();
			
			// add itf(e) * informativeness(c) for all children c
			informativeness += itf * childrenInformativeness;
		}
		
		// divide by number of distinct edges
		informativeness /= edges.size();
		
		return informativeness;
	}
	
	public double getInverseTripleFrequency(String property) {
		// total number of triples
		String query = "SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o .}";
		QueryExecution qe = qef.createQueryExecution(query);
		int total = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();
		
		// number of triples with predicate
		int frequency = getPropertyFrequency(property);
		
		double itf = Math.log(total / (double) frequency);
		
		return itf;
	}
	
	public int getPropertyFrequency(String property) {
		// number of triples with predicate
		String query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o .}", property);
		QueryExecution qe = qef.createQueryExecution(query);
		int frequency = qe.execSelect().next().getLiteral("cnt").getInt();
		qe.close();

		return frequency;
	}

}
