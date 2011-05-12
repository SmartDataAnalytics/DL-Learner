package org.dllearner.algorithm.tbsl.nlp;

import java.util.List;

public interface PartOfSpeechTagger {
	
	String tag(String sentence);
	
	List<String> tagTopK(String sentence);

}
