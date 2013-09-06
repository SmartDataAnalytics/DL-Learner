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

    /**
     * Initialize the trie with strings from the provided ontology using a no-op name generator, i.e., only the
     * actual ontology strings are added and no expansion is done.
     *
     * @param entityTextRetriever the text retriever to use
     * @param ontology the ontology to get strings from
     */
	public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever, OWLOntology ontology) {
        this(entityTextRetriever, ontology, new DummyNameGenerator());
	}

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
                text = StringUtils.join(LinguisticUtil.getWordsFromCamelCase(text), " ");
                text = StringUtils.join(LinguisticUtil.getWordsFromUnderscored(text), " ");
                if (text.trim().isEmpty()) {
                    continue;
                }
                addEntry(text, entity);
                for (String alternativeText : nameGenerator.getAlternativeText(text)) {
//                    System.out.println("New alternative text for " + text + " --> " + alternativeText);
                    addEntry(alternativeText, entity);
                }
                // Adds also composing words, e.g. for "has child", "has" and "child" are also added
                if (text.contains(" ")) {
                    for (String subtext : text.split(" ")) {
                        addEntry(subtext, entity);
                        for (String alternativeText : nameGenerator.getAlternativeText(subtext)) {
//                            System.out.println("New alternative text for " + subtext + " --> " + alternativeText);
                            addEntry(alternativeText, entity);
                        }
                        //System.out.println("trie.add("+subtext+","++")");
                    }
                }
            }
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
            return Arrays.asList(LinguisticUtil.getTopSynonymsForWord(word, maxNumberOfSenses));
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
            res.add(LinguisticUtil.getNormalizedForm(word));

            for (String w : LinguisticUtil
                    .getTopSynonymsForWord(LinguisticUtil.getNormalizedForm(word), maxNumberOfSenses)) {
                res.add(w.replaceAll("_", " "));
            }

            return res;
        }
    }
}
