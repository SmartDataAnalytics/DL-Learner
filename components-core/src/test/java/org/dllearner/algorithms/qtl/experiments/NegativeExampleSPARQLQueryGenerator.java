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
package org.dllearner.algorithms.qtl.experiments;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

public class NegativeExampleSPARQLQueryGenerator extends ElementVisitorBase{
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(NegativeExampleSPARQLQueryGenerator.class);
		
		private boolean inOptionalClause;
		private Stack<ElementGroup> parentGroup = new Stack<>();
		private QueryUtils triplePatternExtractor = new QueryUtils();
		private Triple triple;
		Random randomGen = new Random(123);

		private QueryExecutionFactory qef;
		
		public NegativeExampleSPARQLQueryGenerator(QueryExecutionFactory qef) {
			this.qef = qef;
		}
		
		public List<String> getNegativeExamples(String targetQuery, int size) {
			logger.trace("Generating neg. examples...");
			Set<String> negExamples = new HashSet<>();
			
			Query query = QueryFactory.create(targetQuery);
			
			// generate queries that return neg. examples
			List<Query> queries = new ArrayList<>();
			queries.addAll(generateQueriesByReplacement(query));
			queries.addAll(generateQueriesByRemoval(query));
			
			// get a list of resources for each query
			for (Query q : queries) {
				q.setLimit(size);
				logger.info("Trying query\n" + q);
				QueryExecution qe = qef.createQueryExecution(q);
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()) {
					QuerySolution qs = rs.next();
					String example = qs.getResource(query.getProjectVars().get(0).getName()).getURI();
					negExamples.add(example);
				}
				qe.close();
			}
			logger.trace("...finished generating neg. examples.");

			// sanity check: remove all pos. examples
			QueryExecution qe = qef.createQueryExecution(targetQuery);
			ResultSet rs = qe.execSelect();
			Iterable<QuerySolution> iter = () -> rs;
			final Var targetVar = query.getProjectVars().get(0);
			List<String> posExamples = StreamSupport.stream(iter.spliterator(), false)
											.map(qs -> qs.getResource(targetVar.getName()).getURI())
											.collect(Collectors.toList());
			negExamples.removeAll(posExamples);

			return new ArrayList<>(negExamples);
		}
		
		private ElementFilter getNotExistsFilter(Element el){
			return new ElementFilter(new E_NotExists(el));
		}

		private List<Query> generateQueriesByReplacement(Query query) {
			List<Query> queries = new ArrayList<>();

			QueryUtils utils = new QueryUtils();

			List<Triple> triplePatterns = new ArrayList<>(utils.extractTriplePattern(query));

			for (Triple tp : triplePatterns) {
				List<Triple> remainingTriplePatterns = new ArrayList<>(triplePatterns);
				remainingTriplePatterns.remove(tp);

				List<List<Integer>> positions2Replace = new ArrayList<>();
				if(tp.getSubject().isURI()) { // s
					if(tp.getPredicate().isURI()) { // s p ?var
						positions2Replace.add(Lists.newArrayList(0));
						positions2Replace.add(Lists.newArrayList(1));
					} else if(tp.getObject().isConcrete()) { // s ?var ?o
						positions2Replace.add(Lists.newArrayList(0));
						positions2Replace.add(Lists.newArrayList(2));
					}
				} else {
					if(tp.getPredicate().isURI()) {
						 if(tp.getObject().isConcrete()) { // ?var p o
							 positions2Replace.add(Lists.newArrayList(1));
							 positions2Replace.add(Lists.newArrayList(2));
						 }
					}
				}

				for (List<Integer> positions : positions2Replace) {
					Node s = tp.getSubject();
					Node p = tp.getPredicate();
					Node o = tp.getObject();
					List<ElementFilter> filters = new ArrayList<>();
					for (Integer pos : positions) {
						switch (pos) {
							case 0: {
								Node var = NodeFactory.createVariable("s_var");
								s = var;
								filters.add(new ElementFilter(new E_LogicalNot(new E_Equals(new ExprVar(var), NodeValue.makeNode(tp.getSubject())))));
								break;
							}
							case 1: {
								Node var = NodeFactory.createVariable("p_var");
								p = var;
								// ?var != <p> && ?var != rdf:type
								filters.add(new ElementFilter(new E_LogicalNot(new E_Equals(new ExprVar(var), NodeValue.makeNode(tp.getPredicate())))));
								filters.add(new ElementFilter(new E_LogicalNot(new E_Equals(new ExprVar(var), NodeValue.makeNode(RDF.type.asNode())))));
								break;
							}
							case 2: {
								Node var = NodeFactory.createVariable("o_var");
								o = var;
								filters.add(new ElementFilter(new E_LogicalNot(new E_Equals(new ExprVar(var), NodeValue.makeNode(tp.getObject())))));
								break;
							}
						}
					}
					// FILTER(isIRI(?uri))
					Var projectVar = query.getProjectVars().get(0);
					filters.add(new ElementFilter(new E_IsIRI(NodeValue.makeNode(projectVar.asNode()))));

					Triple newTp = Triple.create(s, p, o);

					List<Triple> newTriplePatterns = new ArrayList<>(remainingTriplePatterns);
					newTriplePatterns.add(triplePatterns.indexOf(tp), newTp);

					Query newQuery = QueryFactory.create();
					newQuery.setQuerySelectType();
					newQuery.addProjectVars(query.getProjectVars());
					newQuery.setDistinct(true);

					ElementTriplesBlock bgp = new ElementTriplesBlock();
					newTriplePatterns.forEach(bgp::addTriple);
					ElementGroup eg = new ElementGroup();
					eg.addElement(bgp);
					filters.forEach(eg::addElementFilter);

					Concept attrConcept = new Concept(bgp, query.getProjectVars().get(0));
					Concept filterConcept = new Concept(query.getQueryPattern(), query.getProjectVars().get(0));

					Map<Var, Var> varMap = ConceptUtils.createVarMap(attrConcept, filterConcept);
					Concept renamedConcept = ConceptUtils.createRenamedConcept(filterConcept, varMap);

					E_NotExists notExists = new E_NotExists(renamedConcept.getElement());
					eg.addElementFilter(new ElementFilter(notExists));

					newQuery.setQueryPattern(eg);

					queries.add(newQuery);
				}
			}

			return queries;
		}
		
		private List<Query> generateQueriesByRemoval(Query query) {
			List<Query> queries = new ArrayList<>();
			
			// extract paths
			Node source = query.getProjectVars().get(0).asNode();
			Set<List<Triple>> paths = getPaths(new ArrayList<>(), query, source);
			
			// for each path create query which excludes the path by FILTER NOT EXISTS
			Set<Set<List<Triple>>> pathSubsets = Sets.powerSet(paths);
			
			for (Set<List<Triple>> pathSubset : pathSubsets) {
				if(!pathSubset.isEmpty() && pathSubset.size() < paths.size()) {
					
					ElementGroup eg = new ElementGroup();
					
					// keep other paths
					ElementTriplesBlock existsBlock = new ElementTriplesBlock();
					eg.addElement(existsBlock);
					SetView<List<Triple>> difference = Sets.difference(paths, pathSubset);
					for(List<Triple> otherPath : difference) {
						for (Triple tp : otherPath) {
							existsBlock.addTriple(tp);
						}
					}
					
					// not exists current path
					ElementTriplesBlock notExistsBlock = new ElementTriplesBlock();
					for(List<Triple> path : pathSubset) {
						for (Triple tp : path) {
							notExistsBlock.addTriple(tp);
						}
					}
					ElementGroup notExistsGroup = new ElementGroup();
					notExistsGroup.addElement(notExistsBlock);
					eg.addElementFilter(getNotExistsFilter(notExistsGroup));
					
					Query newQuery = QueryFactory.create();
					newQuery.setQuerySelectType();
					newQuery.setQueryPattern(eg);
					newQuery.addProjectVars(query.getProjectVars());
					newQuery.setDistinct(true);
					queries.add(newQuery);
				}
			}
			
			return queries;
		}
		
		private Set<List<Triple>> getPaths(List<Triple> path, Query query, Node source) {
			Set<List<Triple>> paths = new LinkedHashSet<>();
			Set<Triple> outgoingTriplePatterns = QueryUtils.getOutgoingTriplePatterns(query, source);
			for (Triple tp : outgoingTriplePatterns) {
				List<Triple> newPath = new ArrayList<>(path);
				newPath.add(tp);
				if(tp.getObject().isVariable()) {
					paths.addAll(getPaths(newPath, query, tp.getObject()));
				} else {
					paths.add(newPath);
				}
			}
			return paths;
		}

		/**
		 * Returns a modified SPARQL query such that it is similar but different by choosing one of the triple patterns and use
		 * the negation of its existence.
		 * @param query the SPARQL query
		 */
		public Query generateSPARQLQuery(Query query){
			//choose a random triple for the modification
			List<Triple> triplePatterns = new ArrayList<>(triplePatternExtractor.extractTriplePattern(query));
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
			for (Element e : new ArrayList<>(el.getElements())) {
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
			for (Element e : el.getElements()) {
				e.visit(this);
			}
		}
		
		@Override
		public void visit(ElementFilter el) {
		}

	}