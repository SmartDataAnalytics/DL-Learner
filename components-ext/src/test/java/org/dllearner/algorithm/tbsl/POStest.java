package org.dllearner.algorithm.tbsl;

import java.io.IOException;

import org.dllearner.algorithm.tbsl.templator.POStagger;

public class POStest {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		POStagger tagger = new POStagger();
		
		String sentence = "give me all cities in Germany";
		
		String tagged = tagger.tag(sentence);
		
		System.out.println(tagged);
	}

}
