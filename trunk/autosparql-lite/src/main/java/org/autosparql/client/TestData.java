package org.autosparql.client;

import java.util.ArrayList;
import java.util.List;

import org.autosparql.shared.Example;

public class TestData {
	
	private static int SIZE = 20;
	
	public static List<Example> getDummyExamples(){
		List<Example> examples = new ArrayList<Example>();
		for(int i=1; i <= SIZE; i++){
			examples.add(new Example(String.valueOf(i), "label"+i, "", "comment"+i));
		}
		return examples;
	}
}
