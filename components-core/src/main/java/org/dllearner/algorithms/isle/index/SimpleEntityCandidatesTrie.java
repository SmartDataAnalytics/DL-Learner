package org.dllearner.algorithms.isle.index;

import org.apache.commons.lang.StringUtils;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.MapUtils;
import org.dllearner.utilities.datastructures.PrefixTrie;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;
import java.util.Map.Entry;

public class SimpleEntityCandidatesTrie implements EntityCandidatesTrie {

	PrefixTrie<FullTokenEntitySetPair> trie;
	EntityTextRetriever entityTextRetriever;

//    /**
//     * Initialize the trie with strings from the provided ontology using a no-op name generator, i.e., only the
//     * actual ontology strings are added and no expansion is done.
//     *
//     * @param entityTextRetriever the text retriever to use
//     * @param ontology the ontology to get strings from
//     */
//	public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever, OWLOntology ontology) {
//        this(entityTextRetriever, ontology, new DummyNameGenerator());
//	}

    /**
     * Initialize the trie with strings from the provided ontology and use the given entity name generator
     * for generating alternative words.
     *
     * @param entityTextRetriever the text retriever to use
     * @param ontology the ontology to get strings from
     * @param nameGenerator the name generator to use for generating alternative words
     */
    public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever, OWLOntology ontology,
                                      NameGenerator nameGenerator) {
        this.entityTextRetriever = entityTextRetriever;
        buildTrie(ontology, nameGenerator);
    }
	
	public void buildTrie(OWLOntology ontology, NameGenerator nameGenerator) {
		this.trie = new PrefixTrie<FullTokenEntitySetPair>();
		Map<Entity, Set<String>> relevantText = entityTextRetriever.getRelevantText(ontology);
		
		for (Entity entity : relevantText.keySet()) {

			for (String text : relevantText.get(entity)) {
                text = StringUtils.join(LinguisticUtil.getInstance().getWordsFromCamelCase(text), " ");
                text = StringUtils.join(LinguisticUtil.getInstance().getWordsFromUnderscored(text), " ");
                if (text.trim().isEmpty()) {
                    continue;
                }
                
                addEntry(text, entity);
                addSubsequencesWordNet(entity, text);
                
                for (String alternativeText : nameGenerator.getAlternativeText(text)) {
                    addEntry(alternativeText.toLowerCase(), entity, text);
                }
            }
        }
		
	}
	
	/**
	 * Adds the subsequences of a test
	 * @param entity
	 * @param text
	 */
	private void addSubsequences(Entity entity, String text) {
        if (text.contains(" ")) {
        	String[] tokens = text.split(" ");
        	for (int size=1; size<tokens.length; size++) {
        		
        		for (int start=0; start<tokens.length-size+1; start++) {
        			String subsequence = "";
        			for (int i=0; i<size; i++) {
        				subsequence += tokens[start+i] + " ";
        			}
        			subsequence = subsequence.trim();
        			
            		addEntry(subsequence, entity);
        		}
        		
        	}
        }
	}

    private void addSubsequencesWordNet(Entity entity, String text) {
        if (text.contains(" ")) {
            String[] tokens = text.split(" ");

            List<String>[] wordnetTokens = (ArrayList<String>[]) new ArrayList[tokens.length];

            // generate list of lemmatized wordnet synonyms for each token
            for (int i = 0; i < tokens.length; i++) {
                wordnetTokens[i] = new ArrayList<String>();
                wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(tokens[i].toLowerCase()));
                for (String w : LinguisticUtil.getInstance().getTopSynonymsForWord(tokens[i], 5)) {
                    wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(w).toLowerCase());
                }
            }

            // generate subsequences starting at the given start index of the given size
            Set<String[]> allPossibleSubsequences = getAllPossibleSubsequences(tokens, wordnetTokens);

            for (String[] s : allPossibleSubsequences) {
                addEntry(s[0], entity, s[1]);
            }
        }
    }

    private static Set<String[]> getAllPossibleSubsequences(String[] originalTokens, List<String>[] wordnetTokens) {
        ArrayList<String[]> res = new ArrayList<String[]>();

        for (int size = 1; size < wordnetTokens.length + 1; size++) {
            for (int start = 0; start < wordnetTokens.length - size + 1; start++) {
                getPossibleSubsequencesRec(originalTokens, res, new ArrayList<String>(), new ArrayList<String>(),
                        wordnetTokens, 0, size);
            }
        }

        return new HashSet<String[]>(res);
    }


    private static void getPossibleSubsequencesRec(String[] originalTokens, List<String[]> allSubsequences,
                                                   List<String> currentSubsequence,
                                                   List<String> currentOriginalSubsequence,
                                                   List<String>[] wordnetTokens,
                                                   int curStart, int maxLength) {

        if (currentSubsequence.size() == maxLength) {
            allSubsequences.add(new String[]{StringUtils.join(currentSubsequence, " ").toLowerCase(), StringUtils
                    .join(currentOriginalSubsequence, " ").toLowerCase()});
            return;
        }
        for (String w : wordnetTokens[curStart]) {
            ArrayList<String> tmpSequence = new ArrayList<String>(currentSubsequence);
            ArrayList<String> tmpOriginalSequence = new ArrayList<String>(currentOriginalSubsequence);
            tmpSequence.add(w);
            tmpOriginalSequence.add(originalTokens[curStart]);
            getPossibleSubsequencesRec(originalTokens, allSubsequences, tmpSequence, tmpOriginalSequence, wordnetTokens,
                    curStart + 1, maxLength);
        }
    }

    @Override
	public void addEntry(String s, Entity e) {
    	s = s.trim();
		FullTokenEntitySetPair candidates;
		if (trie.contains(s)) 
			candidates = trie.get(s);
		else
			candidates = new FullTokenEntitySetPair(s);
		
		candidates.addEntity(e);
		
		trie.put(s, candidates);
	}

    public void addEntry(String s, Entity e, String originalString) {
    	s = s.trim();
        FullTokenEntitySetPair candidates;
        if (trie.contains(s))
            candidates = trie.get(s);
        else
            candidates = new FullTokenEntitySetPair(originalString);

        candidates.addEntity(e);

        trie.put(s, candidates);
    }

	@Override
	public Set<Entity> getCandidateEntities(String s) {
        FullTokenEntitySetPair res = trie.get(s);
		return res == null ? new HashSet<Entity>() : trie.get(s).getEntitySet();
	}

	@Override
	public String getGeneratingStringForLongestMatch(String s) {
		CharSequence match = trie.getLongestMatch(s);
		return (match!=null) ? trie.get(match).getFullToken() : null;
	}

    @Override
    public String getLongestMatchingText(String s) {
        CharSequence match = trie.getLongestMatch(s);
        return (match!=null) ? match.toString() : null;
    }
	
	public String toString() {
		StringBuilder output = new StringBuilder();
		Map<String,FullTokenEntitySetPair> trieMap = trie.toMap();
		
		for (Entry<String, FullTokenEntitySetPair> entry : trieMap.entrySet()) {
			String key = entry.getKey();
			FullTokenEntitySetPair pair = entry.getValue();
			output.append(key + " (" + pair.getFullToken() + ") :\n");
			for (Entity candidate: pair.getEntitySet()) {
				output.append("\t"+candidate+"\n");
			}
		}
		return output.toString();
	}

    public static void main(String[] args) {
        String[] tokens = "this is a long and very complex text".split(" ");

        List<String>[] wordnetTokens = (ArrayList<String>[]) new ArrayList[tokens.length];

        // generate list of lemmatized wordnet synonyms for each token
        for (int i = 0; i < tokens.length; i++) {
            wordnetTokens[i] = new ArrayList<String>();
            wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(tokens[i]));
            for (String w : LinguisticUtil.getInstance().getTopSynonymsForWord(tokens[i], 5)) {
                System.out.println("Adding: " + LinguisticUtil.getInstance().getNormalizedForm(w));
                wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(w).replaceAll("_", " "));
            }
        }

        // generate subsequences starting at the given start index of the given size
        Set<String[]> allPossibleSubsequences = getAllPossibleSubsequences(tokens, wordnetTokens);

        for (String[] s : allPossibleSubsequences) {
            System.out.println(String.format("%s - %s", s[0], s[1]));
        }
    }

    public void printTrie() {
		System.out.println(this.toString());
		
	}

    public static interface NameGenerator {
        /**
         * Returns a list of possible alternative words for the given word
         *
         * @param text    the text to return alternative words for
         * @return alternative words for given word
         */
        List<String> getAlternativeText(String text);
    }

    public static class DummyNameGenerator implements NameGenerator {
        @Override
        public List<String> getAlternativeText(String word) {
            return Collections.singletonList(word);
        }
    }

    /**
     * Generates alternative texts by using WordNet synonyms.
     */
    public static class WordNetNameGenerator implements NameGenerator {
        private int maxNumberOfSenses = 5;

        /**
         * Sets up the generator for returning the lemmas of the top {@code maxNumberOfSenses} senses.
         * @param maxNumberOfSenses the maximum number of senses to aggregate word lemmas from
         */
        public WordNetNameGenerator(int maxNumberOfSenses) {
            this.maxNumberOfSenses = maxNumberOfSenses;
        }

        @Override
        public List<String> getAlternativeText(String word) {
            return Arrays.asList(LinguisticUtil.getInstance().getTopSynonymsForWord(word, maxNumberOfSenses));
        }
    }

    /**
     * Generates alternative texts by using WordNet synonym and lemmatizing of the original words
     */
    public static class LemmatizingWordNetNameGenerator implements NameGenerator {
        private int maxNumberOfSenses = 5;

        /**
         * Sets up the generator for returning the lemmas of the top {@code maxNumberOfSenses} senses.
         * @param maxNumberOfSenses the maximum number of senses to aggregate word lemmas from
         */
        public LemmatizingWordNetNameGenerator(int maxNumberOfSenses) {
            this.maxNumberOfSenses = maxNumberOfSenses;
        }

        @Override
        public List<String> getAlternativeText(String word) {
            ArrayList<String> res = new ArrayList<String>();
            res.add(LinguisticUtil.getInstance().getNormalizedForm(word));

            for (String w : LinguisticUtil.getInstance().getTopSynonymsForWord(word, maxNumberOfSenses)) {
                res.add(LinguisticUtil.getInstance().getNormalizedForm(w.replaceAll("_", " ")));
            }

            return res;
        }
    }

    /**
     * Pair of the actual word and the word after processing.
     */
    public static class ActualModifiedWordPair {
        private String actualString;
        private String modifiedString;

        public String getActualString() {
            return actualString;
        }

        public void setActualString(String actualString) {
            this.actualString = actualString;
        }

        public String getModifiedString() {
            return modifiedString;
        }

        public void setModifiedString(String modifiedString) {
            this.modifiedString = modifiedString;
        }

        public ActualModifiedWordPair(String actualString, String modifiedString) {

            this.actualString = actualString;
            this.modifiedString = modifiedString;
        }
    }
}
