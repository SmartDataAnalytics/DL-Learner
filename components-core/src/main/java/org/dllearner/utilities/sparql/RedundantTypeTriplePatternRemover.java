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
package org.dllearner.utilities.sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class RedundantTypeTriplePatternRemover extends ElementVisitorBase{
	
	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"SELECT ?sup WHERE {?sub rdfs:subClassOf+ ?sup .}");
	
	
	private QueryExecutionFactory qef;

	public RedundantTypeTriplePatternRemover(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	/**
	 * Returns a pruned copy of the given query.
	 * @param query the query
	 * @return a pruned copy of the given query
	 */
	public Query pruneQuery(Query query) {
		Query copy = query.cloneQuery();
		copy.getQueryPattern().visit(this);
		return copy;
	}
	
	private Set<Node> getSuperClasses(Node cls){
		Set<Node> superClasses = new HashSet<>();
		
		superClassesQueryTemplate.setIri("sub", cls.getURI());
		
		String query = superClassesQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			superClasses.add(qs.getResource("sup").asNode());
		}
		qe.close();
		
		return superClasses;
	}
	
	@Override
	public void visit(ElementGroup el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	@Override
	public void visit(ElementOptional el) {
		el.getOptionalElement().visit(this);
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// get all rdf:type triple patterns
		Multimap<Node, Triple> subject2TypeTriples = HashMultimap.create();
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if(t.getPredicate().matches(RDF.type.asNode())) {
				subject2TypeTriples.put(t.getSubject(), t);
			}
		}
		
		// check for semantically redundant triple patterns
		Set<Triple> redundantTriples = new HashSet<>();
		for (Entry<Node, Collection<Triple>> entry : subject2TypeTriples.asMap().entrySet()) {
			Collection<Triple> triples = entry.getValue();
			
			// get all super classes
			Set<Node> superClasses = new HashSet<>();
			for (Triple triple : triples) {
				Node cls = triple.getObject();
				superClasses.addAll(getSuperClasses(cls));
			}
			
			for (Triple triple : triples) {
				Node cls = triple.getObject();
				if(superClasses.contains(cls)) {
					redundantTriples.add(triple);
				}
			}
		}
		
		// remove redundant triple patterns
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if(redundantTriples.contains(t)) {
				iterator.remove();
			}
		}
	}

	@Override
	public void visit(ElementPathBlock el) {
		// get all rdf:type triple patterns
		Multimap<Node, Triple> subject2TypeTriples = HashMultimap.create();
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath t = iterator.next();
			if (t.isTriple() && t.getPredicate().matches(RDF.type.asNode())) {
				subject2TypeTriples.put(t.getSubject(), t.asTriple());
			}
		}

		// check for semantically redundant triple patterns
		Set<Triple> redundantTriples = new HashSet<>();
		for (Entry<Node, Collection<Triple>> entry : subject2TypeTriples.asMap().entrySet()) {
			Collection<Triple> triples = entry.getValue();

			// get all super classes
			Set<Node> superClasses = new HashSet<>();
			for (Triple triple : triples) {
				Node cls = triple.getObject();
				superClasses.addAll(getSuperClasses(cls));
			}

			for (Triple triple : triples) {
				Node cls = triple.getObject();
				if (superClasses.contains(cls)) {
					redundantTriples.add(triple);
				}
			}
		}

		// remove redundant triple patterns
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath t = iterator.next();
			if (t.isTriple() && redundantTriples.contains(t.asTriple())) {
				iterator.remove();
			}
		}
	}

	@Override
	public void visit(ElementUnion el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}
	
	public static void main(String[] args) throws Exception {
		String query = "SELECT DISTINCT  ?x0\n" + 
				"WHERE\n" + 
				"  { ?x0  <http://dbpedia.org/ontology/capital>  ?x7 ;\n" + 
				"         <http://dbpedia.org/ontology/currency>  <http://dbpedia.org/resource/West_African_CFA_franc> ;\n" + 
				"         <http://dbpedia.org/ontology/foundingDate>  ?x12 ;\n" + 
				"         <http://dbpedia.org/ontology/governmentType>  ?x13 ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Country> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Place> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/PopulatedPlace> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Wikidata:Q532> .\n" + 
				"    ?x7  <http://dbpedia.org/ontology/country>  ?x8 ;\n" + 
				"         <http://dbpedia.org/ontology/elevation>  ?x9 ;\n" + 
				"         <http://dbpedia.org/ontology/isPartOf>  ?x10 ;\n" + 
				"         <http://dbpedia.org/ontology/populationTotal>  ?x11 ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Place> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/PopulatedPlace> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Settlement> ;\n" + 
				"         <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Wikidata:Q532> .\n" + 
				"  }";
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		RedundantTypeTriplePatternRemover remover = new RedundantTypeTriplePatternRemover(qef);
		System.out.println(remover.pruneQuery(QueryFactory.create(query)));
	}
	
	

}
