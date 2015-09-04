package org.dllearner.algorithms.qtl.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.vocabulary.RDF;

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
			List<String> negExamples = new ArrayList<String>();
			
			// remove triple patterns as long as enough neg examples have been found
			Query query = QueryFactory.create(targetQuery);
			
			List<Query> queries = generateQueries(query);
			
			while(negExamples.size() < size && !queries.isEmpty()) {
				
				Query q = queries.remove(0);
				q.setLimit(size);
				logger.trace("Trying query\n" + q);
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
			return negExamples;
		}
		
		private ElementFilter getNotExistsFilter(Element el){
			return new ElementFilter(new E_NotExists(el));
		}
		
		private List<Query> generateQueries(Query query) {
			List<Query> queries = new ArrayList<Query>();
			
			// extract paths
			Node source = query.getProjectVars().get(0).asNode();
			List<List<Triple>> paths = getPaths(new ArrayList<Triple>(), query, source);
			
			int index = 0;
			for (List<Triple> path : paths) {
				if(path.size() == 1 && path.get(0).getPredicate().equals(RDF.type.asNode())) {
					index = paths.indexOf(path);
				}
			}
			List<Triple> path1 = paths.get(index == 0 ? 1 : 0);
			List<Triple> typePath = paths.get(index);
			
			// get last tp first
			if(path1.size() == 2) {
				// remove last edge
				ElementGroup eg = new ElementGroup();
				ElementTriplesBlock existsBlock = new ElementTriplesBlock();
//				existsBlock.addTriple(path1.get(0));
				existsBlock.addTriple(typePath.get(0));
				eg.addElement(existsBlock);
				
				ElementTriplesBlock notExistsBlock = new ElementTriplesBlock();
				notExistsBlock.addTriple(path1.get(0));
				notExistsBlock.addTriple(path1.get(1));
				ElementGroup notExistsGroup = new ElementGroup();
				notExistsGroup.addElement(notExistsBlock);
				eg.addElementFilter(getNotExistsFilter(notExistsGroup));
				
				Query newQuery = QueryFactory.create();
				newQuery.setQuerySelectType();
				newQuery.setQueryPattern(eg);
				newQuery.addProjectVars(query.getProjectVars());
				newQuery.setDistinct(true);
				queries.add(newQuery);
				
				//remove both edges
				eg = new ElementGroup();
				existsBlock = new ElementTriplesBlock();
				existsBlock.addTriple(typePath.get(0));
				eg.addElement(existsBlock);
				
				notExistsBlock = new ElementTriplesBlock();
				notExistsBlock.addTriple(path1.get(0));
				notExistsBlock.addTriple(path1.get(1));
				notExistsGroup = new ElementGroup();
				notExistsGroup.addElement(notExistsBlock);
				eg.addElementFilter(getNotExistsFilter(notExistsGroup));
				
				newQuery = QueryFactory.create();
				newQuery.setQuerySelectType();
				newQuery.setQueryPattern(eg);
				newQuery.addProjectVars(query.getProjectVars());
				newQuery.setDistinct(true);
				queries.add(newQuery);
				
			} else {
				//remove both edges
				ElementGroup eg = new ElementGroup();
				ElementTriplesBlock existsBlock = new ElementTriplesBlock();
				existsBlock.addTriple(typePath.get(0));
				eg.addElement(existsBlock);
				
				ElementTriplesBlock notExistsBlock = new ElementTriplesBlock();
				notExistsBlock.addTriple(path1.get(0));
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
			
			return queries;
		}
		
		private List<List<Triple>> getPaths(List<Triple> path, Query query, Node source) {
			List<List<Triple>> paths = new ArrayList<List<Triple>>();
			Set<Triple> outgoingTriplePatterns = QueryUtils.getOutgoingTriplePatterns(query, source);
			for (Triple tp : outgoingTriplePatterns) {
				List<Triple> newPath = new ArrayList<Triple>(path);
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
		 * @param query
		 */
		public Query generateSPARQLQuery(Query query){
			//choose a random triple for the modification
			List<Triple> triplePatterns = new ArrayList<Triple>(triplePatternExtractor.extractTriplePattern(query));
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
			for (Iterator<Element> iterator = new ArrayList<Element>(el.getElements()).iterator(); iterator.hasNext();) {
				Element e = iterator.next();
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
			for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
				Element e = iterator.next();
				e.visit(this);
			}
		}
		
		@Override
		public void visit(ElementFilter el) {
		}

	}