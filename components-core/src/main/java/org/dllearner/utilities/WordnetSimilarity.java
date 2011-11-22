package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordnetSimilarity {
	
	public Dictionary dict;	
	
	public WordnetSimilarity(){
		try {
			JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public double computeSimilarity(String s1, String s2, POS pos){
		List<String> synonyms = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(pos, s1);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				for(Synset s : synsets){
					System.out.println(s);
					PointerTarget[] targets = s.getTargets(PointerType.HYPERNYM);
					for (PointerTarget target : targets)
		               {
		                  Word[] words = ((Synset) target).getWords();
		                  for (Word word : words)
		                  {
		                     System.out.println(word);
		                  }
		               }
				}
//				Word[] words = synsets[0].getWords();
//				for(Word w : words){
//					String c = w.getLemma();
//					System.out.println(c);
//				}
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
		
		return -1;
	}
	
	public static void main(String[] args) {
		System.out.println(new WordnetSimilarity().computeSimilarity("writer", "teacher", POS.NOUN));
	}

}
