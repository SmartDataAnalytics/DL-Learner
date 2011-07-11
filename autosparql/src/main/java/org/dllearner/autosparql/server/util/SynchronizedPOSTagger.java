package org.dllearner.autosparql.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SynchronizedPOSTagger {
	
	private static final String MODEL_PATH = "de/simba/ner/models/left3words-wsj-0-18.tagger";
	private MaxentTagger tagger;
	
	public SynchronizedPOSTagger() {
		try {
			tagger = new MaxentTagger(this.getClass().getClassLoader().getResource(MODEL_PATH).getPath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized ArrayList<TaggedWord> tagSentence(List<HasWord> sentence){
		return tagger.tagSentence(sentence);
	}
	
	

}
