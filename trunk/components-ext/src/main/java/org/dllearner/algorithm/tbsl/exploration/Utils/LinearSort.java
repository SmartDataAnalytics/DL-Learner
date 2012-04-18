package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;

public class LinearSort {

	/*
	 * TODO: test if the sorted queries are given back properly
	 */
	public static ArrayList<QueryPair> doSort(ArrayList<QueryPair> qp){
		
		boolean change=true;
		while(change){
			change=false;
			for(int i = 0; i<qp.size()-1;i++){
				if(qp.get(i).getRank()<qp.get(i+1).getRank()){
					change=true;
					QueryPair one = qp.get(i);
					QueryPair two = qp.get(i+1);
					qp.set(i, two);
					qp.set(i+1, one);
				}
			}
		}
		
		if(Setting.isDebugModus())DebugMode.printQueryPair(qp);
		
		return qp;

	}
	
}
