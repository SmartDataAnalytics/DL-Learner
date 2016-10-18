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

import com.google.common.collect.Sets;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.sparql.vocabulary.FOAF;
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

/**
 * A factory for query trees that does materialization of the model in advance.
 *
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryMaterialized extends QueryTreeFactoryBase {

	Reasoner reasoner;

	public QueryTreeFactoryMaterialized() {
		initReasoner();
	}

	private void initReasoner() {
//		reasoner = ReasonerRegistry.getRDFSSimpleReasoner();
		reasoner = new GenericRuleReasoner(Rule.rulesFromURL(this.getClass().getClassLoader().getResource("rules/rdfs_simple.rules").getPath()));
	}

	@Override
	public RDFResourceTree getQueryTree(Resource resource, Model model, int maxDepth) {
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		return super.getQueryTree(resource, infModel, maxDepth);
	}
	
	public static void main(String[] args) throws Exception {
		QueryTreeFactory factory = new QueryTreeFactoryMaterialized();
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
