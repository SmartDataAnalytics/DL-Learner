package org.dllearner.algorithm.tbsl;

import org.dllearner.algorithm.tbsl.templator.WordNet;

import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WordNetTest {

	public static void main(String[] args) {
		
		System.setProperty("wordnet.database.dir", System.getProperty("user.dir") + "/src/main/resources/tbsl/dict/");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		
		System.out.println(database.getBaseFormCandidates("cities",SynsetType.NOUN)[1]);
		
		WordNet wordnet = new WordNet();
		
		System.out.println(wordnet.getAttributes("bigger"));
		System.out.println(wordnet.getBestSynonyms("city","NN"));
		System.out.println(wordnet.getAttributes("biggest"));
		System.out.println(wordnet.getBestSynonyms("biggest","JJR"));
		System.out.println(wordnet.getAttributes("city"));
	}

}
