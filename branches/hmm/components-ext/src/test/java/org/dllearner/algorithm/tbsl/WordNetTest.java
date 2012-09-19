package org.dllearner.algorithm.tbsl;

import java.util.Iterator;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;

import org.dllearner.algorithm.tbsl.nlp.WordNet;

public class WordNetTest {

	/**
	 * @param args
	 * @throws JWNLException 
	 */
	public static void main(String[] args) throws JWNLException {
		
		WordNet wordnet = new WordNet();
		
		System.out.println(wordnet.getBestSynonyms(POS.NOUN,"mayor"));

		PointerTargetNodeList relatedList;
		for (Synset syn : wordnet.dict.getIndexWord(POS.NOUN,"mayor").getSenses()) {
			relatedList = PointerUtils.getInstance().getSynonyms(syn);
			Iterator<PointerTargetNode> i = relatedList.iterator();
			while (i.hasNext()) {
			  PointerTargetNode related = i.next();
			  Synset s = related.getSynset();
			  System.out.println("-- " + s);
			}
		}
		
	}

}
