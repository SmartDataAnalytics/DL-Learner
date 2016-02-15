package org.dllearner.algorithms.isle.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.didion.jwnl.data.POS;

import org.dllearner.algorithms.isle.WordNet;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleEntityCandidatesTrie implements EntityCandidatesTrie {
    TokenTree tree;
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
     */
    public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever, OWLOntology ontology) {
        this.entityTextRetriever = entityTextRetriever;
        buildTrie(ontology);
    }
	
	public void buildTrie(OWLOntology ontology) {
		this.tree = new TokenTree();
		Map<OWLEntity, Set<List<Token>>> entity2TokenSet = entityTextRetriever.getRelevantText(ontology);
		
		
		for (Entry<OWLEntity, Set<List<Token>>> entry : entity2TokenSet.entrySet()) {
			OWLEntity entity = entry.getKey();
			Set<List<Token>> tokenSet = entry.getValue();
			for (List<Token> tokens : tokenSet) {
                addAlternativeFormsFromWordNet(tokens);
				addEntry(tokens, entity);
                addSubsequences(entity, tokens);
			}
		}
	}
	
	/**
	 * Adds the subsequences of a test
	 * @param entity
     * @param tokens
	 */
    private void addSubsequences(OWLEntity entity, List<Token> tokens) {
        tree.add(tokens, entity);
        for (int size = 1; size < tokens.size(); size++) {
            for (int start = 0; start < tokens.size() - size + 1; start++) {
                ArrayList<Token> subsequence = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    subsequence.add(tokens.get(start + i));
                }
                addEntry(subsequence, entity);
            }
        }
    }

    private void addAlternativeFormsFromWordNet(List<Token> tokens) {
        for (Token t : tokens) {
            POS wordnetPos = null;
            String posTag = t.getPOSTag();
            if (posTag.startsWith("N")) {//nouns
                wordnetPos = POS.NOUN;
            }
            else if (posTag.startsWith("V")) {//verbs
                wordnetPos = POS.VERB;
            }
            else if (posTag.startsWith("J")) {//adjectives
                wordnetPos = POS.ADJECTIVE;
            }
            else if (posTag.startsWith("R")) {//adverbs
                wordnetPos = POS.ADVERB;
            }
            if (wordnetPos == null) {
                continue;
            }
            //String[] synonyms = LinguisticUtil.getInstance().getSynonymsForWord(t.getRawForm(), wordnetPos);
            Set<WordNet.LemmaScorePair> alternativeFormPairs = LinguisticUtil.getInstance()
                    .getScoredHyponyms(t.getRawForm(), wordnetPos);

            for (WordNet.LemmaScorePair synonym : alternativeFormPairs) {
                // ignore all multi word synonyms
                if (synonym.getLemma().contains("_")) {
                    continue;
                }
                //t.addAlternativeForm(LinguisticUtil.getInstance().getNormalizedForm(synonym));
                t.addAlternativeForm(synonym.getLemma(), synonym.getScore());
            }
        }
    }

    @Override
	public void addEntry(List<Token> s, OWLEntity e) {
        tree.add(s, e);
	}

    public void addEntry(List<Token> s, OWLEntity e, List<Token> originalTokens) {
        tree.add(s, e, originalTokens);
    }

	@Override
	public Set<EntityScorePair> getCandidateEntities(List<Token> tokens) {
        Set<EntityScorePair> res = tree.getAllEntitiesScored(tokens);
        return res;
    }

	@Override
	public List<Token> getGeneratingStringForLongestMatch(List<Token> tokens) {
		return tree.getOriginalTokensForLongestMatch(tokens);
	}

    @Override
    public List<Token> getLongestMatchingText(List<Token> tokens) {
        return tree.getLongestMatch(tokens);
    }
	
	public String toString() {
		return tree.toString();
	}

    public static void main(String[] args) {
        String[] tokens = "this is a long and very complex text".split(" ");

        List<String>[] wordnetTokens = (ArrayList<String>[]) new ArrayList[tokens.length];

        // generate list of lemmatized wordnet synonyms for each token
        for (int i = 0; i < tokens.length; i++) {
            wordnetTokens[i] = new ArrayList<>();
            wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(tokens[i]));
            for (String w : LinguisticUtil.getInstance().getTopSynonymsForWord(tokens[i], 5)) {
                System.out.println("Adding: " + LinguisticUtil.getInstance().getNormalizedForm(w));
                wordnetTokens[i].add(LinguisticUtil.getInstance().getNormalizedForm(w).replaceAll("_", " "));
            }
        }
    }

    public void printTrie() {
		System.out.println(this.toString());

	}
}
