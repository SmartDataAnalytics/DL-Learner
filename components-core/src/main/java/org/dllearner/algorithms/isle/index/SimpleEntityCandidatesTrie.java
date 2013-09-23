package org.dllearner.algorithms.isle.index;

import org.apache.commons.lang.StringUtils;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.datastructures.PrefixTrie;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

public class SimpleEntityCandidatesTrie implements EntityCandidatesTrie {

	PrefixTrie<Set<Entity>> trie;
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
		this.trie = new PrefixTrie<Set<Entity>>();
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
                    addEntry(alternativeText, entity);
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
            Set<String> allPossibleSubsequences = getAllPossibleSubsequences(wordnetTokens);

            for (String s : allPossibleSubsequences) {
                addEntry(s, entity);
            }
        }
    }

    private static Set<String> getAllPossibleSubsequences(List<String>[] wordnetTokens) {
        ArrayList<String> res = new ArrayList<String>();

        for (int size = 1; size < wordnetTokens.length + 1; size++) {
            for (int start = 0; start < wordnetTokens.length - size + 1; start++) {
                getPossibleSubsequencesRec(res, new ArrayList<String>(), wordnetTokens, 0, size);
            }
        }

        return new HashSet<String>(res);
    }

    private static void getPossibleSubsequencesRec(List<String> allSubsequences, List<String> currentSubsequence, List<String>[] wordnetTokens,
                                            int curStart, int maxLength) {
        if (currentSubsequence.size() == maxLength) {
            allSubsequences.add(StringUtils.join(currentSubsequence, " "));
            return;
        }
        for (String w : wordnetTokens[curStart]) {
            ArrayList<String> tmpSequence = new ArrayList<String>(currentSubsequence);
            tmpSequence.add(w);
            getPossibleSubsequencesRec(allSubsequences, tmpSequence, wordnetTokens, curStart + 1, maxLength);
        }
    }

    @Override
	public void addEntry(String s, Entity e) {
		Set<Entity> candidates;
		if (trie.contains(s)) 
			candidates = trie.get(s);
		else
			candidates = new HashSet<Entity>();
		
		candidates.add(e);
		
		trie.put(s, candidates);
	}

	@Override
	public Set<Entity> getCandidateEntities(String s) {
		return trie.get(s);
	}

	@Override
	public String getLongestMatch(String s) {
		CharSequence match = trie.getLongestMatch(s);
		return (match!=null) ? match.toString() : null;
	}
	
	public String toString() {
		String output = "";
		Map<String,Set<Entity>> trieMap = trie.toMap();
		List<String> termsList = new ArrayList<String>(trieMap.keySet());
		Collections.sort(termsList);
		for (String key : termsList) {
			output += key + ":\n";
			for (Entity candidate: trieMap.get(key)) {
				output += "\t"+candidate+"\n";
			}
		}
		return output;
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
        Set<String> allPossibleSubsequences = getAllPossibleSubsequences(wordnetTokens);

        for (String s : allPossibleSubsequences) {
            System.out.println(s);
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
}
