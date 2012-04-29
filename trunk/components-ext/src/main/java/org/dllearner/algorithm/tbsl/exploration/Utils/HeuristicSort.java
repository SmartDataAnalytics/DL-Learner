package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;

public class HeuristicSort {

	/*
	 * TODO: test if the sorted queries are given back properly
	 */
	public static ArrayList<QueryPair> doSort(ArrayList<QueryPair> qp, String question){
		
		boolean change=true;
		//while(change){
			//change=false;
			for(int i = 0; i<qp.size()-1;i++){
				if(qp.get(i).getRank()==qp.get(i+1).getRank()&&question.contains("of")){
					//change=true;
					QueryPair one = qp.get(i);
					QueryPair two = qp.get(i+1);
					String string = one.getQuery();
				    //Pattern p = Pattern.compile (".*\\<http://dbpedia.org/resource/.*\\> \\<http://dbpedia.org/.*\\> \\?.*");
				    //Matcher m = p.matcher (string);
				    if(string.matches(".*\\<http://dbpedia.org/resource/.*\\> \\<http://dbpedia.org/.*\\> \\?.*")){
				    	qp.set(i, one);
						qp.set(i+1, two);
				    	
				    } 
				    else{
				    	qp.set(i, two);
						qp.set(i+1, one);
				    }
				   
				}
			}
		//}
		
		if(Setting.isDebugModus())DebugMode.printQueryPair(qp);
		
		return qp;

	}
	
}
