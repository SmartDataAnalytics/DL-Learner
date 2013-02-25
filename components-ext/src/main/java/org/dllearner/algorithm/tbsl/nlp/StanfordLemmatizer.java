package org.dllearner.algorithm.tbsl.nlp;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.process.Morphology;

/*
 * Morphology computes the base form of English words, by removing just inflections (not derivational morphology). 
 * That is, it only does noun plurals, pronoun case, and verb endings, and not things like comparative adjectives or 
 * derived nominals. It is based on a finite-state transducer implemented by John Carroll et al., written in flex 
 * and publicly available. See: http://www.informatics.susx.ac.uk/research/nlp/carroll/morph.html .
 */
public class StanfordLemmatizer implements Lemmatizer{
	
	private Morphology stemmer;
	
	public StanfordLemmatizer(){
		stemmer = new Morphology();
	}

	@Override
	public String stem(String word) {
		return stemmer.stem(word);
	}

	@Override
	public String stem(String word, String tag) {
		return stemmer.lemma(word, tag);
	}

	@Override
	public List<String> stem(List<String> words) {
		List<String> stemmedWords = new ArrayList<String>();
		for(String word : words){
			stemmedWords.add(stem(word));
		}
		return stemmedWords;
	}


}
