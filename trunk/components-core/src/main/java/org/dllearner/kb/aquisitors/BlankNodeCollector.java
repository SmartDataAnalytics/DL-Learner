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
import java.util.TreeSet;

import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class BlankNodeCollector {

	private static int globalBNodeId = 0;
	public static synchronized int getNextGlobalBNodeId(){
		int ret = globalBNodeId;
		globalBNodeId++;
		return ret;
	}
	
	private static Map<Integer, SortedSet<RDFNodeTuple>> blankNodes = new HashMap<Integer, SortedSet<RDFNodeTuple>>();
	
	public static void addBlankNode(int id, RDFNodeTuple t){
		if(blankNodes.get(id)==null){
			blankNodes.put(id, new TreeSet<RDFNodeTuple>());
			}
		blankNodes.get(id).add(t);
		//System.out.println("added: "+id+" "+t);
		//System.out.println();
	}
	
	public static  SortedSet<RDFNodeTuple> getBlankNode(int id){
		return blankNodes.get(id);
	}
	
	public static  Map<Integer, SortedSet<RDFNodeTuple>> getBlankNodeMap(){
		return blankNodes;
	}
	
	
	/**
	 * @param rsw
	 * @param depth
	 * @return true if there are more blanknodes
	 */
	public static boolean testResultSet(ResultSetRewindable rsw, int depth){
		List<String> vars = new ArrayList<String>();
		vars.add("o0");
		for (int i = 1; i <= depth; i++) {
			vars.add("o"+i);
		}
		
		while (rsw.hasNext()){
			QuerySolution q = rsw.nextSolution();
			if(!q.get("o0").isAnon()){
				continue;
			}else{
				if(!testOneQuerySolution(vars, q)){
					rsw.reset();
					return false;
				}
		
			}
		}
		rsw.reset();
		return true;
	}
	
	//true to stop expansion
	private static boolean testOneQuerySolution(List<String> vars, QuerySolution q){
		
//		System.out.println(q);
//		System.out.println(vars);
//		for (String v : vars) {
//			RDFNode n = q.get(v);
//			if(n==null){
//				System.out.println("returning true");
//				return true;
//			}
//			
//		}
		
		for (String v : vars) {
			RDFNode n = q.get(v);
			if(!n.isAnon()){
//				System.out.println(n);
				return true;
			}
		}
		return false;
	}
	
	public static String makeQuery(String uri, String predicate, int maxDepth){
		int currentDepth = 0;
		StringBuffer sq = new StringBuffer();
		String init = "SELECT * WHERE { "+
				"<"+uri+"> <"+predicate+"> ?o"+currentDepth+". ";
		
		sq.append(init);
		
		for (; currentDepth < maxDepth; currentDepth++) {
			String currentO = "?o"+currentDepth;
			String nextP = "?p"+(currentDepth+1);
			String nextO = "?o"+(currentDepth+1);
			sq.append("  OPTIONAL { "+currentO+" "+nextP+" "+nextO+". }");
		}
		
		
		sq.append("  }");
		return sq.toString();
	}
	
}
