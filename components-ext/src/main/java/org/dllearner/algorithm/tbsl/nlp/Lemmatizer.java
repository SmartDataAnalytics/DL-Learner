package org.dllearner.algorithm.tbsl.nlp;

import java.util.List;

public interface Lemmatizer {
	
	/**
	 * Lemmatize the word.
	 * @param word the word to lemmatize
	 * @return the stemmed word
	 */
	String stem(String word);
	
	/**
	 * Lemmatize the word, being sensitive to the tag.
	 * @param word the word to lemmatize
	 * @param tag 
	 * @return the stemmed word
	 */
	String stem(String word, String tag);
	
	/**
	 * Lemmatize a list of words. The result will be in the same order as the input.
	 * @param words the words to lemmatize
	 * @return a list of stemmed words in the same order as the input list
	 */
	List<String> stem(List<String> words);

}
