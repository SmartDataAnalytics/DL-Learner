package org.dllearner.algorithms.isle.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.didion.jwnl.data.POS;

import org.dllearner.algorithms.isle.WordNet;

import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.DefaultLemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.Lemmatizer;

/**
 * Provides shortcuts to commonly used linguistic operations
 * @author Daniel Fleischhacker
 */
public class LinguisticUtil {
    private static LinguisticUtil instance;

    private static final WordNet wn = new WordNet();
    private static POS[] RELEVANT_POS = new POS[]{POS.NOUN, POS.VERB};
    private static Lemmatizer lemmatizer;

    public static LinguisticUtil getInstance() {
        if (instance == null) {
            instance = new LinguisticUtil();
        }
        return instance;
    }

    public LinguisticUtil() {
        try {
            lemmatizer = new DefaultLemmatizer();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<WordNet.LemmaScorePair> getScoredHyponyms(String word, POS pos) {
        List<WordNet.LemmaScorePair> pairs = wn.getHyponymsScored(pos, word);
        HashMap<String, Double> lemmaScores = new HashMap<>();
        for (WordNet.LemmaScorePair p : pairs) {
            if (!lemmaScores.containsKey(p.getLemma())) {
                lemmaScores.put(p.getLemma(), p.getScore());
            }
            else {
                lemmaScores.put(p.getLemma(), Math.max(p.getScore(), lemmaScores.get(p.getLemma())));
            }
        }

        TreeSet<WordNet.LemmaScorePair> scoredPairs = new TreeSet<>();
        for (Map.Entry<String, Double> e : lemmaScores.entrySet()) {
            scoredPairs.add(new WordNet.LemmaScorePair(e.getKey(), e.getValue()));
        }

        return scoredPairs;
    }

    /**
     * Processes the given string and puts camelCased words into single words.
     * @param camelCase    the word containing camelcase to split
     * @return all words as camelcase contained in the given word
     */
    public String[] getWordsFromCamelCase(String camelCase) {
        ArrayList<String> resultingWords = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            // we just ignore characters not matching the defined pattern
            char curChar = camelCase.charAt(i);
            if (Character.isWhitespace(curChar)) {
                sb.append(" ");
                continue;
            }
            else if (!Character.isLetter(curChar)) {
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
    public String[] getWordsFromUnderscored(String underScored) {
        return underScored.split("_");
    }

    /**
     * Returns an array of all synonyms for the given word. Only synonyms for the POS in {@link #RELEVANT_POS} are
     * returned.
     *
     * @param word the word to retrieve synonyms for
     * @return synonyms for the given word
     */
    public String[] getSynonymsForWord(String word) {
        ArrayList<String> synonyms = new ArrayList<>();

        for (POS pos : RELEVANT_POS) {
            synonyms.addAll(wn.getAllSynonyms(pos, word));
        }
        return synonyms.toArray(new String[synonyms.size()]);
    }

    /**
     * Iterates through the hypernym tree for the given word at the given POS and returns a list of all lemmas of the
     * most frequent synsets visited during traversing the tree.
     * @param word word to get hypernyms for
     * @param pos POS to get hypernyms for
     * @return list of all lemmas of all hypernyms for the given word
     */
    public String[] getAllHyponymsForWord(String word, POS pos) {
        ArrayList<String> hyponyms = new ArrayList<>();

        hyponyms.addAll(wn.getHyponyms(pos, word));

        return hyponyms.toArray(new String[hyponyms.size()]);
    }

    /**
     * Returns an array of all synonyms for the given word for the given POS.
     *
     * @param word the word to retrieve synonyms for
     * @param pos  POS to retrieve synonyms for
     * @return synonyms for the given word
     */
    public String[] getSynonymsForWord(String word, POS pos) {
        ArrayList<String> synonyms = new ArrayList<>();

        synonyms.addAll(wn.getAllSynonyms(pos, word));
        return synonyms.toArray(new String[synonyms.size()]);
    }

    /**
     * Returns an array of the lemmas of the top {@code n} synonyms for the given word. Only synonyms for the POS in
     * {@link #RELEVANT_POS} are returned.
     *
     * @param word the word to retrieve synonyms for
     * @param n the number of senses to get lemmas for
     * @return synonyms for the given word
     */
    public String[] getTopSynonymsForWord(String word, int n) {
        ArrayList<String> synonyms = new ArrayList<>();

        for (POS pos : RELEVANT_POS) {
            synonyms.addAll(wn.getTopSynonyms(pos, word, n));
        }
        return synonyms.toArray(new String[synonyms.size()]);
    }

    /**
     * Returns the normalized form of the given word. If the word contains spaces, each part separated by spaces is
     * normalized independently and joined afterwards. If there is an error normalizing the given word, the word itself
     * is returned.
     *
     * @param word the word to get normalized form for
     * @return normalized form of the word or the word itself on an error
     */
    public String getNormalizedForm(String word) {
        StringBuilder res = new StringBuilder();

        boolean first = true;

        ArrayList<String> singleWords = new ArrayList<>();
        Collections.addAll(singleWords, word.trim().split(" "));

        for (String w : singleWords) {
            try {
                if (first) {
                    first = false;
                }
                else {
                    res.append(" ");
                }
                res.append(lemmatizeSingleWord(w));
            }
            catch (Exception e) {
               throw new RuntimeException(e);
            }
        }
        return res.toString();
    }

    private String lemmatizeSingleWord(String word) {
        try {
            if (lemmatizer == null) {
                return word;
            }
            else {
                return lemmatizer.lemmatize(word);
            }
        }
        catch (NullPointerException e) {
            return word;
        }
    }

    public static void main(String[] args) {
        System.out.println(LinguisticUtil.getInstance().getNormalizedForm("going"));
        for (String s : LinguisticUtil.getInstance().getWordsFromCamelCase("thisIsAClassWith1Name123")) {
            System.out.println(s);
            for (String w : LinguisticUtil.getInstance().getSynonymsForWord(s)) {
                System.out.println(" --> " + w);
            }
        }
    }
}
