package org.dllearner.algorithm.tbsl;

import java.io.IOException;

import org.dllearner.algorithm.tbsl.templator.POStagger;

public class POStest {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		
		POStagger tagger = new POStagger();
		
		String sentence = "When did Nirvana record Nevermind?";
		
		String tagged = tagger.tag(sentence);
		
		System.out.println(tagged);
		
//		Tagger tagger = new Tagger("en");
//
//		String s = "";
//		
//		String[] words = s.split(" ");
//		String[] tagged;
//
//		tagged = tagger.tag(words); 
//		
//		for (String string : tagged) {
//			System.out.println(string);
//		}
	}

}
