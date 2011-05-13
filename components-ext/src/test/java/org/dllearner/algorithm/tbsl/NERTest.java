package org.dllearner.algorithm.tbsl;

import java.util.List;

import org.dllearner.algorithm.tbsl.nlp.DBpediaSpotlightNER;
import org.dllearner.algorithm.tbsl.nlp.LingPipeNER;
import org.dllearner.algorithm.tbsl.nlp.NER;

public class NERTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sentence = "When did Nirvana record Nevermind?";
		
		NER ner = new DBpediaSpotlightNER();
		long startTime = System.currentTimeMillis();
		List<String> namedEntities = ner.getNamedEntitites(sentence);
		System.out.format("Using DBpedia Spotlight WebService (%d ms):\n", System.currentTimeMillis()-startTime);
		System.out.println(namedEntities + "\n");
		
		ner = new LingPipeNER();
		startTime = System.currentTimeMillis();
		namedEntities = ner.getNamedEntitites(sentence);
		System.out.format("Using Lingpipe API with local DBpedia dictionary (%d ms):\n", System.currentTimeMillis()-startTime);
		System.out.println(namedEntities);
		
	}

}
