package org.dllearner.scripts.matching;

import java.util.SortedSet;

import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.datastructures.StringTuple;

public class SameCollect {

	String ld;
	String db;
	
	SortedSet<RDFNodeTuple> dbdata;
	SortedSet<StringTuple> lddata;
	
	public SameCollect(String ld, String db) {
		super();
		this.ld = ld;
		this.db = db;
	}
	
	
}
