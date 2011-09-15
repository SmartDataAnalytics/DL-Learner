package org.dllearner.algorithm.tbsl.nlp;

import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNet {
	
	private Dictionary dict;	
	
	public WordNet() {
		try {
			JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream("tbsl/wordnet_properties.xml"));
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getBestSynonyms(POS pos, String s) {
		
		List<String> synonyms = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				Word[] words = synsets[0].getWords();
				for(Word w : words){
					String c = w.getLemma();
					if (!c.equals(s) && !c.contains(" ") && synonyms.size() < 4) {
						synonyms.add(c);
					}
				}
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return synonyms;
	}
	
	public List<String> getAttributes(String s) {
		
		List<String> result = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(POS.ADJECTIVE, s);			
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				Word[] words = synsets[0].getWords();
				for(Word w : words){
					String c = w.getLemma();
					if (!c.equals(s) && !c.contains(" ") && result.size() < 4) {
						result.add(c);
					}
				}
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println(new WordNet().getBestSynonyms(POS.VERB, "learn"));
	}

}
