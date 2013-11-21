/**
 * 
 */
package org.dllearner.algorithms.isle.index;

/**
 * Compare the word types of two given words.
 * @author Lorenz Buehmann
 *
 */
public class WordTypeComparator {
	
	/**
	 * Returns TRUE if both POS tags are related to the same word type, i.e. whether both are NOUNS, VERBS, etc. ,
	 * else FALSE is returned.
	 * @param posTag1 the POS tag of the first word
	 * @param posTag2 the POS tag of the second word
	 * @return
	 */
	public static boolean sameWordType(String posTag1, String posTag2){
		if(posTag1.startsWith("NN") && posTag2.startsWith("NN") ||
				posTag1.startsWith("V") && posTag2.startsWith("V")){
			return true;
		}
		return false;
	}
}
