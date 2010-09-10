package org.dllearner.scripts.tiger;

import org.dllearner.utilities.experiments.Table;

public class TableSorter {

	
	public static void main(String[] args) {
		Table t = new Table();
		t.addTable(Table.deserialize("/home/sebastian/work/papers/2010/ACL_Corpus_Navigation/results/NO_ZU/baseline_5_5.ser"));
		t.addTable(Table.deserialize("/home/sebastian/work/papers/2010/ACL_Corpus_Navigation/results/NO_ZU/fixRuntime_20s.ser"));
		t.addTable(Table.deserialize("/home/sebastian/work/papers/2010/ACL_Corpus_Navigation/results/NO_ZU/reducedExamples_2_2.ser"));
		t.addTable(Table.deserialize("/home/sebastian/work/papers/2010/ACL_Corpus_Navigation/results/NO_ZU/useLemma_false.ser"));
		t.sortByLabel();
		t.write("/tmp/noZU/", "nozumaster");
		
		
	}
}
