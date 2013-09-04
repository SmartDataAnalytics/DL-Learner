package org.dllearner.algorithms.isle.index;

import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.DefaultLemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.Lemmatizer;
import net.didion.jwnl.data.POS;
import org.dllearner.algorithms.isle.WordNet;

import java.util.ArrayList;

/**
 * Provides shortcuts to commonly used linguistic operations
 * @author Daniel Fleischhacker
 */
public class LinguisticUtil {
    private static final WordNet wn = new WordNet();
    private static POS[] RELEVANT_POS = new POS[]{POS.NOUN, POS.VERB};
    private static Lemmatizer lemmatizer;

    static {
        try {
            lemmatizer = new DefaultLemmatizer();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the given string and puts camelCased words into single words.
     * @param camelCase    the word containing camelcase to split
     * @return all words as camelcase contained in the given word
     */
    public static String[] getWordsFromCamelCase(String camelCase) {
        ArrayList<String> resultingWords = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            // we just ignore characters not matching the defined pattern
            char curChar = camelCase.charAt(i);
            if (!Character.isLetter(curChar)) {
                continue;
            }
            if (Character.isUpperCase(curChar)) { // found a new upper case letter
                resultingWords.add(sb.toString());
                sb = new StringBuilder();
                sb.append(Character.toLowerCase(curChar));
            }
            else { // lower case letter
                sb.append(curChar);
            }
        }

        if (sb.length() > 0) {
            resultingWords.add(sb.toString());
        }

        return resultingWords.toArray(new String[resultingWords.size()]);
    }

    /**
     * Split word into words it contains divided by underscores.
     *
     * @param underScored    word to split at underscores
     * @return words contained in given word
     */
    public static String[] getWordsFromUnderscored(String underScored) {
        return underScored.split("_");
    }

    /**
     * Returns an array of all synonyms for the given word. Only synonyms for the POS in {@link #RELEVANT_POS} are
     * returned.
     *
     * @param word the word to retrieve synonyms for
     * @return synonyms for the given word
     */
    public static String[] getSynonymsForWord(String word) {
        ArrayList<String> synonyms = new ArrayList<String>();

        for (POS pos : RELEVANT_POS) {
            synonyms.addAll(wn.getAllSynonyms(pos, word));
        }
        return synonyms.toArray(new String[synonyms.size()]);
    }

    /**
     * Returns the normalized form of the given word. This method is only able to work with single words! If there is an
     * error normalizing the given word, the word itself is returned.
     *
     * @param word the word to get normalized form for
     * @return normalized form of the word or the word itself on an error
     */
    public static String getNormalizedForm(String word) {
        try {
            if (lemmatizer == null) {
                return word;
            }
            return lemmatizer.lemmatize(word);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return word;
    }

    public static void main(String[] args) {
        System.out.println(getNormalizedForm("going"));
        for (String s : getWordsFromCamelCase("thisIsAClassWith1Name123")) {
            System.out.println(s);
            for (String w : getSynonymsForWord(s)) {
                System.out.println(" --> " + w);
            }
        }
    }
}
