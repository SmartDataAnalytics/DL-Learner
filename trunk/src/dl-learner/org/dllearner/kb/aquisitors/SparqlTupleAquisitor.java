/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.kb.aquisitors;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlTupleAquisitor extends TupleAquisitor {
	
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SparqlTupleAquisitor.class);
	protected static final String PREDICATE = "predicate";
	protected static final String OBJECT = "object";
	
	protected SparqlQueryMaker sparqlQueryMaker;
	protected SPARQLTasks sparqlTasks;
	//RBC
	
	

	public SparqlTupleAquisitor(SparqlQueryMaker sparqlQueryMaker, SPARQLTasks sparqlTasks) {
		
		this.sparqlQueryMaker = sparqlQueryMaker;
		this.sparqlTasks = sparqlTasks;
	}
	
	@Override
	public SortedSet<RDFNodeTuple> retrieveTupel(String uri){
		// getQuery
		String sparqlQueryString = sparqlQueryMaker.makeSubjectQueryUsingFilters(uri);
		SortedSet<RDFNodeTuple> ret = sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, PREDICATE, OBJECT);
		disambiguateBlankNodes(uri, ret);
		return ret;
	}
	@Override
	public SortedSet<RDFNodeTuple> retrieveClassesForInstances(String uri){
		// getQuery
		String sparqlQueryString = sparqlQueryMaker.makeClassQueryUsingFilters(uri);
		SortedSet<RDFNodeTuple> ret = sparqlTasks.queryAsRDFNodeTuple(sparqlQueryString, PREDICATE, OBJECT);
		return ret;
		
	}
	@Override
	public SortedSet<RDFNodeTuple> retrieveTuplesForClassesOnly(String uri){
		SortedSet<RDFNodeTuple> ret = retrieveTupel(uri);
		return ret;
	}
	
	private void disambiguateBlankNodes(String uri, SortedSet<RDFNodeTuple> resultSet){
		try{
		for (RDFNodeTuple tuple : resultSet) {
			if(tuple.b.isAnon()){
				int currentId = BlankNodeCollector.getNextGlobalBNodeId();
				tuple.b = new RDFBlankNode(currentId, tuple.b);
				System.out.println(uri+" replaced blanknode "+tuple.b);
				dissolveBlankNodes(currentId, uri, tuple);
				System.out.println(BlankNodeCollector.getBlankNodeMap());
				
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void dissolveBlankNodes(int currentId, String uri, RDFNodeTuple tuple){
		int currentDepth = 1;
		int lastDepth = 1;
		ResultSetRewindable rsw=null;
		do{
		String q = BlankNodeCollector.makeQuery(uri, tuple.a.toString(), currentDepth);
	
		rsw = sparqlTasks.queryAsResultSet(q);
		lastDepth = currentDepth;
		}while (!BlankNodeCollector.testResultSet(rsw, currentDepth++));
		
		assignIds( currentId,  rsw, lastDepth);
		
	}
	
	private void assignIds(int currentId, ResultSetRewindable rsw, int lastDepth){
		List<String> vars = new ArrayList<String>();
		vars.add("o0");
		for (int i = 1; i <= lastDepth; i++) {
			vars.add("p"+i);
			vars.add("o"+i);
		}
		
		final List<String> tmpVars = new ArrayList<String>(); 
		
		while (rsw.hasNext()){
			tmpVars.clear();
			tmpVars.addAll(vars);
			QuerySolution q = rsw.nextSolution();
			
			if(!q.get("o0").isAnon()){
				continue;
			}else{
				tmpVars.remove(0);
				assignIdRec(currentId, q, tmpVars);
			}
			
		
		}
		rsw.reset();
		
	}
	
	private void assignIdRec(int currentId, QuerySolution q, List<String> vars ){
		if(vars.isEmpty()){return;}
		String pvar = vars.remove(0);
		String ovar = vars.remove(0);
		
		RDFNode n = q.get(ovar);
		if(n.isAnon()){
			int nextId = BlankNodeCollector.getNextGlobalBNodeId();
			RDFNodeTuple tuple = new RDFNodeTuple(q.get(pvar), new RDFBlankNode(nextId,n));
			BlankNodeCollector.addBlankNode(currentId, tuple);
			assignIdRec(nextId, q, vars);
		}else{
			BlankNodeCollector.addBlankNode(currentId, new RDFNodeTuple(q.get(pvar), n));
		}
		
		
	}
	
	
	


	
	
	
	
	
	


}
