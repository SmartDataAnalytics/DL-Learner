package org.dllearner.algorithms.isle.index;

import net.didion.jwnl.data.POS;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;
import java.util.Map.Entry;

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
		Map<Entity, Set<List<Token>>> entity2TokenSet = entityTextRetriever.getRelevantText(ontology);
		
		
		for (Entry<Entity, Set<List<Token>>> entry : entity2TokenSet.entrySet()) {
			Entity entity = entry.getKey();
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
    private void addSubsequences(Entity entity, List<Token> tokens) {
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
            String[] synonyms = LinguisticUtil.getInstance().getSynonymsForWord(t.getRawForm(), wordnetPos);

            for (String synonym : synonyms) {
                t.addAlternativeForm(LinguisticUtil.getInstance().getNormalizedForm(synonym));
            }
        }
    }

    @Override
	public void addEntry(List<Token> s, Entity e) {
        tree.add(s, e);
	}

    public void addEntry(List<Token> s, Entity e, List<Token> originalTokens) {
        tree.add(s, e, originalTokens);
    }

	@Override
	public Set<Entity> getCandidateEntities(List<Token> tokens) {
        return tree.get(tokens);
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
            wordnetTokens[i] = new ArrayList<String>();
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
