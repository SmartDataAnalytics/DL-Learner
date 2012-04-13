package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Template;

public class DebugMode {
	
	public static void waitForButton() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		System.out.println("\n\n");
		System.out.println("Press Any Key to continue");
		line = in.readLine();
	}
	
	
	public static void printHypothesen(ArrayList<Hypothesis> list_of_hypothesis, String string){
		System.out.println(string);
		for(Hypothesis x : list_of_hypothesis){
			x.printAll();
		}
		System.out.println(string +" Done \n\n");
		try {
			waitForButton();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public static void printHypothesenSet(ArrayList<ArrayList<Hypothesis>> set_hypothesis, String string){
		System.out.println(string);
		for(ArrayList<Hypothesis> lh : set_hypothesis){
			for(Hypothesis x : lh){
				x.printAll();
			}
		}
		System.out.println(string +" Done \n\n");
		try {
			waitForButton();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public static void printTemplateList(ArrayList<Template> templateList, String string){
		System.out.println(string);
		for(Template t : templateList) t.printAll();
		System.out.println(string +" Done \n\n");
		try {
			waitForButton();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public static void debugPrint(String string){
		System.out.println(string);
	}
	
	public static void debugErrorPrint(String string){
		System.err.println(string);
		try {
			waitForButton();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printQueryPair(ArrayList<QueryPair> qp){
		System.out.println("All constructed Queries with Rank");
		for(QueryPair p : qp){
			p.printAll();
		}
		
		try {
			waitForButton();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

}
