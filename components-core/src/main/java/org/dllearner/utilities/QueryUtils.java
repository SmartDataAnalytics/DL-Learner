package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.ListUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.util.VarUtils;

public class QueryUtils extends ElementVisitorBase {
	
	private Set<Triple> triplePattern;
	private Set<Triple> optionalTriplePattern;
	
	private boolean inOptionalClause = false;
	
	private int unionCount = 0;
	private int optionalCount = 0;
	private int filterCount = 0;
	
	private Map<Triple, ElementGroup> triple2Parent = new HashMap<Triple, ElementGroup>();
	
	Stack<ElementGroup> parents = new Stack<ElementGroup>();
	
	public static String addPrefix(String queryString, Map<String, String> prefix2Namespace){
		Query query = QueryFactory.create(queryString);
		for (Entry<String, String> entry : prefix2Namespace.entrySet()) {
			String prefix = entry.getKey();
			String namespace = entry.getValue();
			query.setPrefix(prefix, namespace);
		}
		return query.toString();
	}
	
	public static String addPrefixes(String queryString, String prefix, String namespace){
		Query query = QueryFactory.create(queryString);
		query.setPrefix(prefix, namespace);
		return query.toString();
	}
	
	/**
	 * Returns all variables that occur in a triple pattern of the SPARQL query.
	 * @param query
	 * @return
	 */
	public Set<Var> getVariables(Query query){
		Set<Var> vars = new HashSet<Var>();
		
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		
		for (Triple tp : triplePatterns) {
			if(tp.getSubject().isVariable()){
				vars.add(Var.alloc(tp.getSubject()));
			} else if(tp.getObject().isVariable()){
				vars.add(Var.alloc(tp.getObject()));
			} else if(tp.getPredicate().isVariable()){
				vars.add(Var.alloc(tp.getPredicate()));
			}
		}
		
		return vars;
	}
	
	/**
	 * Returns all variables that occur as subject in a triple pattern of the SPARQL query.
	 * @param query
	 * @return
	 */
	public Set<Var> getSubjectVariables(Query query){
		Set<Var> vars = new HashSet<Var>();
		
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		
		for (Triple tp : triplePatterns) {
			if(tp.getSubject().isVariable()){
				vars.add(Var.alloc(tp.getSubject()));
			} 
		}
		
		return vars;
	}
	
	/**
	 * Returns all variables that occur as subject in a triple pattern of the SPARQL query.
	 * @param query
	 * @return
	 */
	public Set<Var> getObjectVariables(Query query){
		Set<Var> vars = new HashSet<Var>();
		
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		
		for (Triple tp : triplePatterns) {
			if(tp.getObject().isVariable()){
				vars.add(Var.alloc(tp.getObject()));
			} 
		}
		
		return vars;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node in subject position, i.e. the outgoing
	 * triple patterns.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Set<Triple> extractOutgoingTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		//remove triple patterns not containing triple patterns with given node in subject position
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if(!triple.subjectMatches(node)){
				iterator.remove();
			}
		}
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node in object position, i.e. the incoming
	 * triple patterns.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Set<Triple> extractIncomingTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		//remove triple patterns not containing triple patterns with given node in subject position
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if(!triple.objectMatches(node)){
				iterator.remove();
			}
		}
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node in object position, i.e. the ingoing
	 * triple patterns.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Set<Triple> extractIngoingTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		//remove triple patterns not containing triple patterns with given node in object position
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if(!triple.objectMatches(node)){
				iterator.remove();
			}
		}
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node either in subject or in object position, i.e. 
	 * the ingoing and outgoing triple patterns.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Set<Triple> extractTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = new HashSet<Triple>();
		triplePatterns.addAll(extractIngoingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		return triplePatterns;
	}
	
	/**
	 * Returns all triple patterns in given SPARQL query that have the given node either in subject or in object position, i.e. 
	 * the ingoing and outgoing triple patterns.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Set<Triple> extractNonOptionalTriplePatterns(Query query, Node node){
		Set<Triple> triplePatterns = new HashSet<Triple>();
		triplePatterns.addAll(extractIngoingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		triplePatterns.removeAll(optionalTriplePattern);
		return triplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is either in subject or object position.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Map<Var,Set<Triple>> extractTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<Var,Set<Triple>>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<Triple>();
			triplePatterns.addAll(extractIngoingTriplePatterns(query, var));
			triplePatterns.addAll(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in subject position.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Map<Var,Set<Triple>> extractOutgoingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<Var,Set<Triple>>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<Triple>();
			triplePatterns.addAll(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * @return the optionalTriplePattern
	 */
	public Set<Triple> getOptionalTriplePatterns() {
		return optionalTriplePattern;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in subject position.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Map<Var,Set<Triple>> extractIncomingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<Var,Set<Triple>>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<Triple>();
			triplePatterns.addAll(extractIncomingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	/**
	 * Returns triple patterns for each projection variable v such that v is in object position.
	 * @param query The SPARQL query.
	 * @param node
	 * @return
	 */
	public Map<Var,Set<Triple>> extractIngoingTriplePatternsForProjectionVars(Query query){
		Map<Var,Set<Triple>> var2TriplePatterns = new HashMap<Var,Set<Triple>>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<Triple>();
			triplePatterns.addAll(extractIngoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}
	
	public Set<Triple> extractTriplePattern(Query query){
		return extractTriplePattern(query, false);
	}
	
	public Set<Triple> extractTriplePattern(Query query, boolean ignoreOptionals){
		triplePattern = new HashSet<Triple>();
		optionalTriplePattern = new HashSet<Triple>();
		
		query.getQueryPattern().visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			if(query.isSelectType()){
				for(Triple t : optionalTriplePattern){
					if(!ListUtils.intersection(new ArrayList<Var>(VarUtils.getVars(t)), query.getProjectVars()).isEmpty()){
						triplePattern.add(t);
					}
				}
			}
		}
		
		return triplePattern;
	}
	
	public boolean isOptional(Triple triple){
		return optionalTriplePattern.contains(triple);
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group){
		return extractTriplePattern(group, false);
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group, boolean ignoreOptionals){
		triplePattern = new HashSet<Triple>();
		optionalTriplePattern = new HashSet<Triple>();
		
		group.visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			for(Triple t : optionalTriplePattern){
				triplePattern.add(t);
			}
		}
		
		return triplePattern;
	}
	
	@Override
	public void visit(ElementGroup el) {
		parents.push(el);
		for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
			Element e = iterator.next();
			e.visit(this);
		}
		parents.pop();
	}

	@Override
	public void visit(ElementOptional el) {
		optionalCount++;
		inOptionalClause = true;
		el.getOptionalElement().visit(this);
		inOptionalClause = false;
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if(inOptionalClause){
				optionalTriplePattern.add(t);
			} else {
				triplePattern.add(t);
			}
			if(!parents.isEmpty()){
				triple2Parent.put(t, parents.peek());
			}
		}
	}

	@Override
	public void visit(ElementPathBlock el) {
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath tp = iterator.next();
			if(inOptionalClause){
				if(tp.isTriple()){
					optionalTriplePattern.add(tp.asTriple());
					if(!parents.isEmpty()){
						triple2Parent.put(tp.asTriple(), parents.peek());
					}
				}
			} else {
				if(tp.isTriple()){
					triplePattern.add(tp.asTriple());
					if(!parents.isEmpty()){
						triple2Parent.put(tp.asTriple(), parents.peek());
					}
				}
			}
			
		}
	}

	@Override
	public void visit(ElementUnion el) {
		unionCount++;
		for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
			Element e = iterator.next();
			e.visit(this);
		}
	}
	
	@Override
	public void visit(ElementFilter el) {
		filterCount++;
	}

	public int getUnionCount() {
		return unionCount;
	}

	public int getOptionalCount() {
		return optionalCount;
	}

	public int getFilterCount() {
		return filterCount;
	}
	
	/**
	 * Returns the ElementGroup object containing the triple pattern.
	 * @param triple
	 * @return
	 */
	public ElementGroup getElementGroup(Triple triple){
		return triple2Parent.get(triple);
	}
	
	public static void main(String[] args) throws Exception {
		Query q = QueryFactory.create(
				"PREFIX  dbp:  <http://dbpedia.org/resource/>\n" + 
				"PREFIX  dbo: <http://dbpedia.org/ontology/>\n" + 
				"SELECT  ?thumbnail\n" + 
				"WHERE\n" + 
				"  { dbp:total !dbo:thumbnail ?thumbnail }");
		QueryUtils triplePatternExtractor = new QueryUtils();
		triplePatternExtractor.extractIngoingTriplePatterns(q, q.getProjectVars().get(0));
	}
}
