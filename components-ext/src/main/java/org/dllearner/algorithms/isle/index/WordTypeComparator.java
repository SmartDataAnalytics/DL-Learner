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
		if(posTag1.startsWith("NN") && posTag2.startsWith("NN") || //nouns
			posTag1.startsWith("V") && posTag2.startsWith("V") || //verbs
			posTag1.startsWith("JJ") && posTag2.startsWith("JJ") || //adjectives
			posTag1.startsWith("RB") && posTag2.startsWith("RB"))  //adverbs
		{
			return true;
		} else {
			return posTag1.equals(posTag2);
		}
	}
	
	public static int hashCode(String posTag){
		if(posTag.startsWith("NN")){//nouns
			return "NN".hashCode();
		} else if(posTag.startsWith("V")){//verbs
			return "V".hashCode();
		} else if(posTag.startsWith("JJ")){//adjectives
			return "JJ".hashCode();
		} else if(posTag.startsWith("RB")){//adverbs
			return "RB".hashCode();
		} else {
			return posTag.hashCode();
		}
	}
}
