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
package org.dllearner.algorithms.qtl.heuristics;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.List;
import java.util.SortedSet;

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
