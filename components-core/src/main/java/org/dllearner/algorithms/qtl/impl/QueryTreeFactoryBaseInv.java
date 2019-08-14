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

import java.util.*;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.StatementComparator;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;

/**
 * A factory for query trees that also considers incoming triples.
 *
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryBaseInv implements QueryTreeFactory {

	private int nodeId;
	private final Comparator<Statement> comparator = new StatementComparator();

	private int maxDepth = 3;

	private Set<Predicate<Statement>> dropFilters = new HashSet<>();

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
	@Override
	@SuppressWarnings("unchecked")
	public void addDropFilters(Predicate<Statement>... dropFilters) {
		this.dropFilters.addAll(Arrays.asList(dropFilters));
	}

	private RDFResourceTree createTree(Resource resource, Model model, int maxDepth) {
		nodeId = 0;

		// create mapping from resources to statements, both in subject an object position
		Map<Resource, SortedSet<Statement>> resource2InStatements = new HashMap<>();
		Map<Resource, SortedSet<Statement>> resource2OutStatements = new HashMap<>();
		fillMaps(resource, model, resource2InStatements, resource2OutStatements, 0, maxDepth);

		// start with an empty tree whose root is the resource
		RDFResourceTree tree = new RDFResourceTree(resource.asNode());

		// fill the tree
		fillTree(resource, null, tree, resource2InStatements, resource2OutStatements, 0, maxDepth);

		return tree;
	}

	@SuppressWarnings("unchecked")
	private void fillMaps(Resource s, Model model,
						  Map<Resource, SortedSet<Statement>> resource2InStatements,
						  Map<Resource, SortedSet<Statement>> resource2OutStatements,
						  int currentDepth, int maxDepth) {

		if(currentDepth < maxDepth) {
			// incoming triples
			SortedSet<Statement> statements = resource2InStatements.computeIfAbsent(s, k -> new TreeSet<>(comparator));
			ExtendedIterator stmtIterator = model.listStatements(null, null, s);
			for (Predicate<Statement> filter : dropFilters) {
				stmtIterator = stmtIterator.filterKeep(filter);
			}
			statements.addAll(stmtIterator.toSet());
			statements.forEach(st -> fillMaps(st.getSubject(), model, resource2InStatements, resource2OutStatements, currentDepth + 1, maxDepth));

			// outgoing triples
			statements = resource2OutStatements.computeIfAbsent(s, k -> new TreeSet<>(comparator));
			stmtIterator = model.listStatements(s, null, (RDFNode) null);
			for (Predicate<Statement> filter : dropFilters) {
				stmtIterator = stmtIterator.filterKeep(filter);
			}
			statements.addAll(stmtIterator.toSet());
			statements.stream().filter(st -> st.getObject().isResource()).forEach(st ->
				fillMaps(st.getObject().asResource(), model, resource2InStatements, resource2OutStatements, currentDepth + 1, maxDepth)
			);
		}
	}

	private int nextNodeId() {
		return nodeId++;
	}

	private void fillTree(Resource root, Statement statementFromParent, RDFResourceTree tree,
						  Map<Resource, SortedSet<Statement>> resource2InStatements,
						  Map<Resource, SortedSet<Statement>> resource2OutStatements,
						  int currentDepth, int maxDepth) {
		if(resource2InStatements.containsKey(root)) {
			resource2InStatements.get(root).stream().filter(st -> !st.equals(statementFromParent)).forEach(st -> {

				// check if path to parent is rdf:type, i.e. we have a class node
				// if so, we avoid incoming edges
				if(!tree.isRoot() && tree.getEdgeToParent().matches(RDF.type.asNode())) {
					return;
				}

				Node predicate = new NodeInv(st.getPredicate().asNode());

				RDFNode data = st.getSubject();

				// create the subtree
				RDFResourceTree subTree = new RDFResourceTree(nextNodeId(), data.asNode());
				tree.addChild(subTree, predicate);

				// if current depth is < max depth recursive call
				if (currentDepth + 1 < maxDepth) {
					fillTree(data.asResource(), st, subTree, resource2InStatements, resource2OutStatements, currentDepth + 1, maxDepth);
				}
			});
		}
		if(resource2OutStatements.containsKey(root)) {
			resource2OutStatements.get(root).stream().filter(st -> !st.equals(statementFromParent)).forEach(st -> {
				Node predicate = st.getPredicate().asNode();

				RDFNode data = st.getObject();

				// create the subtree
				RDFResourceTree subTree = new RDFResourceTree(nextNodeId(), data.asNode());
				tree.addChild(subTree, predicate);

				// if root of subtree is not a literal and current depth is < max depth recursive call
				if (!data.isLiteral() && (currentDepth + 1  < maxDepth)) {
					fillTree(data.asResource(), st, subTree, resource2InStatements, resource2OutStatements, currentDepth + 1, maxDepth);
				}
			});
		}
	}

	public static void main(String[] args) throws Exception {
		QueryTreeFactory factory = new QueryTreeFactoryBaseInv();
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
		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(
				SparqlEndpoint.getEndpointDBpedia());
		String resourceURI = "http://dbpedia.org/resource/Athens";
		Model cbd = cbdGen.getConciseBoundedDescription(resourceURI, 1);
		RDFResourceTree queryTree = factory.getQueryTree(resourceURI, cbd);
		System.out.println(queryTree.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQuery(queryTree));
	}

}
