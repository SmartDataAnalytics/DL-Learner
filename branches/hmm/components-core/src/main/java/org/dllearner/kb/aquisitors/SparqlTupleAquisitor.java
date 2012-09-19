/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.kb.aquisitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.jamonapi.Monitor;

/**
 * Can execute different queries.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlTupleAquisitor extends TupleAquisitor {
	
	
	private static Logger logger = Logger.getLogger(SparqlTupleAquisitor.class);
	protected static final String PREDICATE = "predicate";
	protected static final String OBJECT = "object";
	
	protected SparqlQueryMaker sparqlQueryMaker;
	protected SPARQLTasks sparqlTasks;
	
	
	
	

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
		disambiguateBlankNodes(uri, ret);
		return ret;
		
	}
	@Override
	public SortedSet<RDFNodeTuple> retrieveTuplesForClassesOnly(String uri){
		SortedSet<RDFNodeTuple> ret = retrieveTupel(uri);
		//the next line is not necessary
		//disambiguateBlankNodes(uri, ret);
		return ret;
	}
	
	@Override
	public SortedSet<RDFNodeTuple> getBlankNode(int id){
		return BlankNodeCollector.getBlankNode(id);
	}
	
	public void printHM(){
		
			for (int j = 0; j <  BlankNodeCollector.getBlankNodeMap().size(); j++) {
				System.out.println(j);
				for(RDFNodeTuple t :BlankNodeCollector.getBlankNodeMap().get(j)){
					System.out.println(t);
				}
			}
		
	}
	
	// main function for resolving blanknodes
	@Override
	protected void disambiguateBlankNodes(String uri, SortedSet<RDFNodeTuple> resultSet){
		if(!isDissolveBlankNodes()){
			return;
		}
		Monitor bnodeMonitor = JamonMonitorLogger.getTimeMonitor(SparqlTupleAquisitor.class, "blanknode time").start();
		try{
		for (RDFNodeTuple tuple : resultSet) {
			
			if(tuple.b.isAnon()){
				int currentId = BlankNodeCollector.getNextGlobalBNodeId();
				// replace the blanknode
				tuple.b = new RDFBlankNode(currentId, tuple.b);
				//System.out.println(uri+" replaced blanknode "+tuple.b);
				dissolveBlankNodes(currentId, uri, tuple);
				//System.out.println(BlankNodeCollector.getBlankNodeMap());
				
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}finally{
			bnodeMonitor.stop();
		}
		
	}
	
	// extends a sparql query as long as there are undissolved blanknodes
	private void dissolveBlankNodes(int currentId, String uri, RDFNodeTuple tuple){
		try{
			int currentDepth = 1;
			int lastDepth = 1;
			ResultSetRewindable rsw=null;
			do{
			String p = tuple.a.toString();
			if(p.equals(OWLVocabulary.RDFS_COMMENT) || p.equals(OWLVocabulary.RDFS_LABEL)  ){
				return ;
			}
			String q = BlankNodeCollector.makeQuery(uri, p, currentDepth);
//			System.out.println(q);
			rsw = sparqlTasks.queryAsResultSet(q);
			rsw.reset();
			lastDepth = currentDepth;
			}while (!BlankNodeCollector.testResultSet(rsw, currentDepth++));
			
			assignIds( currentId,  rsw, lastDepth);
		}catch (Exception e) {
			logger.info("An error occurred while dissolving blanknodes");
		}
	}
	
	//takes the resultset and assigns internal ids
	private void assignIds(int currentId, ResultSetRewindable rsw, int lastDepth){
		//prepare variables according to last depth
		List<String> vars = new ArrayList<String>();
		vars.add("o0");
		for (int i = 1; i <= lastDepth; i++) {
			vars.add("p"+i);
			vars.add("o"+i);
		}
		
		final List<String> tmpVars = new ArrayList<String>(); 
		
		Map<String, Integer> lastNodes = new HashMap<String, Integer>();
		// the resultset first variable is o0
		// iteration over each tuple of the set
		while (rsw.hasNext()){
			tmpVars.clear();
			tmpVars.addAll(vars);
			QuerySolution q = rsw.nextSolution();
			
			//skip all that do not start with a blanknode
			// could be two different blank nodes here, but unlikely
			if(!q.get("o0").isAnon()){
				lastNodes.put(q.get("o0").toString(), currentId);
				continue;
			}else{
				
				// remove the first node
				tmpVars.remove(0);
				assignIdRec(currentId, q, tmpVars,lastNodes);
			}
			
		
		}
		rsw.reset();
		
	}
	
	private void assignIdRec(int currentId, QuerySolution q, List<String> vars, Map<String, Integer> lastNodes ){
		if(vars.isEmpty()){return;}
		String pvar = vars.remove(0);
		String ovar = vars.remove(0);
		
		// the next node
		RDFNode n = q.get(ovar);
		if(n.isAnon()){
			int nextId;
			if(lastNodes.get(n.toString())==null){
				nextId = BlankNodeCollector.getNextGlobalBNodeId();
				lastNodes.put(n.toString(), nextId);
				//System.out.println(n.toString());
			}else{
				nextId = lastNodes.get(n.toString());
			}
			RDFNodeTuple tuple = new RDFNodeTuple(q.get(pvar), new RDFBlankNode(nextId,n));
			BlankNodeCollector.addBlankNode(currentId, tuple);
			assignIdRec(nextId, q, vars, lastNodes);
		}else{
			BlankNodeCollector.addBlankNode(currentId, new RDFNodeTuple(q.get(pvar), n));
		}
		
		
	}
	
	
	


	
	
	
	
	
	


}
