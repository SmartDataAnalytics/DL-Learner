package org.dllearner.algorithm.tbsl;

import org.dllearner.algorithm.tbsl.templator.WordNet;

public class WordNetTest {

	public static void main(String[] args) {
		
		WordNet wordnet = new WordNet();

		System.out.println(wordnet.getBestSynonyms("city"));
		System.out.println(wordnet.getAttributes("biggest"));
	}

}
