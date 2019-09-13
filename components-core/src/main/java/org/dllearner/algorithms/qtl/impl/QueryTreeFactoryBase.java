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
package org.dllearner.algorithms.qtl.impl;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.NodeComparator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.util.*;
import java.util.function.Predicate;

/**
 * A base factory for query trees.
 *
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryBase implements QueryTreeFactory {

	private int nodeId;
	private final Comparator<Statement> comparator = new StatementComparator();

	private int maxDepth = 3;

	private Set<Predicate<Statement>> dropFilters = new HashSet<>();

	public QueryTreeFactoryBase() {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#setMaxDepth(int)
	 */
	@Override
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public int maxDepth() {
		return maxDepth;
	}

	/* (non-Javadoc)
		 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#getQueryTree(org.apache.jena.rdf.model.Resource, org.apache.jena.rdf.model.Model, int)
		 */
	@Override
	public RDFResourceTree getQueryTree(Resource resource, Model model, int maxDepth) {
		return createTree(resource, model, maxDepth);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.impl.QueryTreeFactory#addDropFilters(org.apache.jena.util.iterator.Filter)
	 */
	@SafeVarargs
	@Override
	public final void addDropFilters(Predicate<Statement>... dropFilters) {
		this.dropFilters.addAll(Arrays.asList(dropFilters));
	}

	private RDFResourceTree createTree(Resource resource, Model model, int maxDepth) {
		nodeId = 0;
		Map<Resource, SortedSet<Statement>> resource2Statements = new HashMap<>();

		fillMap(resource, model, resource2Statements);

		RDFResourceTree tree = new RDFResourceTree(resource.asNode());
		fillTree(resource, tree, resource2Statements, 0, maxDepth);

		return tree;
	}

	private void fillMap(Resource s, Model model, Map<Resource, SortedSet<Statement>> resource2Statements) {

		// get all statements with subject s
		ExtendedIterator<Statement> it = model.listStatements(s, null, (RDFNode) null);

		// filter statement if necessary
		if (!dropFilters.isEmpty()) {
			Iterator<Predicate<Statement>> iter = dropFilters.iterator();
			Predicate<Statement> keepFilter = iter.next();
			it = it.filterKeep(keepFilter);
			while (iter.hasNext()) {it = it.filterKeep(iter.next());
//				keepFilter = keepFilter.and(iter.next());
			}
//			it = it.filterKeep(keepFilter);
		}

		SortedSet<Statement> statements = resource2Statements.computeIfAbsent(s, k -> new TreeSet<>(comparator));

		while (it.hasNext()) {
			Statement st = it.next();
			statements.add(st);
			if ((st.getObject().isResource()) && !resource2Statements.containsKey(st.getObject().asResource())) {
				fillMap(st.getObject().asResource(), model, resource2Statements);
			}
		}
	}

	private void fillTree(Resource root, RDFResourceTree tree, Map<Resource, SortedSet<Statement>> resource2Statements,
			int currentDepth, int maxDepth) {
		currentDepth++;
		if (resource2Statements.containsKey(root)) {
			RDFResourceTree subTree;

			for (Statement st : resource2Statements.get(root)) {
				Node predicate = st.getPredicate().asNode();
				RDFNode object = st.getObject();

				if (object.isLiteral()) {
					subTree = new RDFResourceTree(nodeId++, object.asNode());
					tree.addChild(subTree, predicate);
				} else if (object.isURIResource()) {
					subTree = new RDFResourceTree(nodeId++, object.asNode());
					tree.addChild(subTree, predicate);
//					System.out.println(root + "::" + object + "::" + (currentDepth < maxDepth));
					if (currentDepth < maxDepth) {
						fillTree(object.asResource(), subTree, resource2Statements, currentDepth, maxDepth);
					}
				} else if (object.isAnon()) {
					subTree = new RDFResourceTree(nodeId++);
					tree.addChild(subTree, predicate);
					if (currentDepth < maxDepth) {
						fillTree(object.asResource(), subTree, resource2Statements, currentDepth, maxDepth);
					}
				}
			}
		}
		currentDepth--;
	}

	static class StatementComparator implements Comparator<Statement> {
		
		final NodeComparator nodeComparator = new NodeComparator();

		@Override
		public int compare(Statement s1, Statement s2) {
			return ComparisonChain.start()
					.compare(s1.getPredicate().asNode(), s2.getPredicate().asNode(), nodeComparator)
					.compare(s1.getObject().asNode(), s2.getObject().asNode(), nodeComparator)
					.result();
		}
	}

	public static String encode(String s) {
		char[] htmlChars = s.toCharArray();
		StringBuilder encodedHtml = new StringBuilder();
		for (char htmlChar : htmlChars) {
			switch (htmlChar) {
				case '<':
					encodedHtml.append("&lt;");
					break;
				case '>':
					encodedHtml.append("&gt;");
					break;
				case '&':
					encodedHtml.append("&amp;");
					break;
				case '\'':
					encodedHtml.append("&#39;");
					break;
				case '"':
					encodedHtml.append("&quot;");
					break;
				case '\\':
					encodedHtml.append("&#92;");
					break;
				case (char) 133:
					encodedHtml.append("&#133;");
					break;
				default:
					encodedHtml.append(htmlChar);
					break;
			}
		}
		return encodedHtml.toString();
	}

	

	public static void main(String[] args) throws Exception {
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		factory.setMaxDepth(2);
		factory.addDropFilters(
				new PredicateDropStatementFilter(Sets.union(Sets.union(StopURIsDBpedia.get(), StopURIsRDFS.get()), StopURIsOWL.get())),
				new ObjectDropStatementFilter(StopURIsOWL.get()),
				new NamespaceDropStatementFilter(
						Sets.newHashSet(
								"http://dbpedia.org/property/", 
								"http://purl.org/dc/terms/",
								"http://dbpedia.org/class/yago/",
								FOAF.getURI()
								)
								)
				);
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(
				SparqlEndpoint.getEndpointDBpedia());
		String resourceURI = "http://dbpedia.org/resource/Athens";
		Model cbd = cbdGen.getConciseBoundedDescription(resourceURI, 2);
		RDFResourceTree queryTree = factory.getQueryTree(resourceURI, cbd);
		System.out.println(queryTree.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQuery(queryTree));
	}

}
