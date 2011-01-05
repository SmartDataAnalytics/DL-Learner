package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.server.QueryTreeChange.ChangeType;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class GeneralisedQueryTree<N> {
	
	private QueryTree<N> tree;
	private List<QueryTreeChange> changes;
	
	public GeneralisedQueryTree(QueryTree<N> tree){
		this.tree = tree;
		changes = new ArrayList<QueryTreeChange>();
	}
	
	public GeneralisedQueryTree(QueryTree<N> tree, List<QueryTreeChange> changes){
		this.tree = tree;
		this.changes = changes;
	}
	
	public void setQueryTree(QueryTree<N> tree){
		this.tree = tree;
	}
	
	public QueryTree<N> getQueryTree(){
		return tree;
	}
	
	public void addChange(QueryTreeChange change){
		changes.add(change);
	}
	
	public void addChanges(List<QueryTreeChange> changes){
		this.changes.addAll(changes);
	}
	
	public List<QueryTreeChange> getChanges(){
		return changes;
	}
	
	public QueryTreeChange getLastChange(){
		if(changes.isEmpty()){
			return new QueryTreeChange(0, ChangeType.REPLACE_LABEL);
		}
		return changes.get(changes.size()-1);
	}

}
