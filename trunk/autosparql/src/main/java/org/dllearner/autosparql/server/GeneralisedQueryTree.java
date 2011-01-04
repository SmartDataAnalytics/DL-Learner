package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class GeneralisedQueryTree<N> {
	
	private QueryTree<N> tree;
	private List<QueryTreeChange> changes;
	
	public GeneralisedQueryTree(QueryTree<N> tree){
		this.tree = tree;
		changes = new ArrayList<QueryTreeChange>();
	}
	
	public QueryTree<N> getTree(){
		return tree;
	}
	
	public void addChange(QueryTreeChange change){
		changes.add(change);
	}
	
	public List<QueryTreeChange> getChanges(){
		return changes;
	}

}
