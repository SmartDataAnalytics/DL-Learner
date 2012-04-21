package org.dllearner.algorithm.tbsl.exploration.Index;

import java.sql.SQLException;
import java.util.ArrayList;

public class TestClassNewIndex {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		newSpecialSQliteIndex index = new newSpecialSQliteIndex();
		//System.out.println(index.getUriForIndex("381252"));
		
		ArrayList<String> test = new ArrayList<String>();
		
		test=index.getListOfUriSpecialIndex("female Russian astronauts");
		
		if(test!=null)for(String s:test) System.out.println(s);
	}

}
