package org.dllearner.algorithm.tbsl;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;

import org.dllearner.algorithm.tbsl.nlp.WordNet;

public class WordNetTest {

	/**
	 * @param args
	 * @throws JWNLException 
	 */
	public static void main(String[] args) throws JWNLException {
		
		WordNet wordnet = new WordNet();
		
		System.out.println(wordnet.getBestSynonyms(POS.NOUN,"mayor"));

	}

}
