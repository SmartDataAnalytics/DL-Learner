package org.autosparql.client;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
	
	public static void main(String[] args) {
		try {
			NumberFormat.getNumberInstance(Locale.GERMAN).parse("9,2");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
