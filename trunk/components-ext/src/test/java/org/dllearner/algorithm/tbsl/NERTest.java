package org.dllearner.algorithm.tbsl;

import org.dllearner.algorithm.tbsl.nlp.DBpediaSpotlightNER;
import org.dllearner.algorithm.tbsl.nlp.NER;

public class NERTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sentence = "When did Nirvana record Nevermind?";
		
		NER ner = new DBpediaSpotlightNER();
		System.out.println(ner.getNamedEntitites(sentence));
		
	}

}
