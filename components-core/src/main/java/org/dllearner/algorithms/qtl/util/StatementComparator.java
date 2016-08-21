package org.dllearner.algorithms.qtl.util;

import com.google.common.collect.ComparisonChain;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.NodeComparator;

import java.util.Comparator;

/**
 * A comparator for Jena Statements.
 *
 * @author Lorenz Buehmann
 */
public class StatementComparator implements Comparator<Statement> {
		
		final NodeComparator nodeComparator = new NodeComparator();

		@Override
		public int compare(Statement s1, Statement s2) {
			return ComparisonChain.start()
					.compare(s1.getSubject().asNode(), s2.getSubject().asNode(), nodeComparator)
					.compare(s1.getPredicate().asNode(), s2.getPredicate().asNode(), nodeComparator)
					.compare(s1.getObject().asNode(), s2.getObject().asNode(), nodeComparator)
					.result();
		}
	}