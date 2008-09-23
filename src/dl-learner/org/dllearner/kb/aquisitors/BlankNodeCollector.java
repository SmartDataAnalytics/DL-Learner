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
	}
	
	public static  SortedSet<RDFNodeTuple> getBlankNode(int id){
		return blankNodes.get(id);
	}
	
	public static  Map<Integer, SortedSet<RDFNodeTuple>> getBlankNodeMap(){
		return blankNodes;
	}
	
	
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
	private static boolean testOneQuerySolution(List<String> vars, QuerySolution q){
		for (String v : vars) {
			if(!q.get(v).isAnon()){
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
			sq.append(" { OPTIONAL { "+currentO+" "+nextP+" "+nextO+". }}");
		}
		
		
		sq.append(" } ");
		return sq.toString();
	}
	
}
