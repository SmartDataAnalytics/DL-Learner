package org.dllearner.algorithms.isle.index;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;

import java.util.*;

/**
 * Tree for finding longest matching Token sequence
 *
 * @author Daniel Fleischhacker
 */
public class TokenTree {
    private LinkedHashMap<Token, TokenTree> children;
    private Set<Entity> entities;
    private List<Token> originalTokens;
    private boolean ignoreStopWords = true;

    public TokenTree() {
        this.children = new LinkedHashMap<>();
        this.entities = new HashSet<>();
        this.originalTokens = new ArrayList<>();
    }
    
    /**
     * If set to TRUE, stopwords like 'of, on' are ignored during creation and retrieval operations.
	 * @param ignoreStopWords the ignoreStopWords to set
	 */
	public void setIgnoreStopWords(boolean ignoreStopWords) {
		this.ignoreStopWords = ignoreStopWords;
	}

    /**
     * Adds all given entities to the end of the path resulting from the given tokens.
     *
     * @param tokens   tokens to locate insertion point for entities
     * @param entities entities to add
     */
    public void add(List<Token> tokens, Set<Entity> entities, List<Token> originalTokens) {
        TokenTree curNode = this;
        for (Token t : tokens) {
        	if(!ignoreStopWords || (ignoreStopWords && !t.isStopWord())){
        		TokenTree nextNode = curNode.children.get(t);
                if (nextNode == null) {
                    nextNode = new TokenTree();
                    curNode.children.put(t, nextNode);
                }
                curNode = nextNode;
        	} 
        }
        curNode.entities.addAll(entities);
        curNode.originalTokens = new ArrayList<>(originalTokens);
    }

    public void add(List<Token> tokens, Set<Entity> entities) {
        add(tokens, entities, tokens);
    }

    /**
     * Adds the given entity to the tree.
     *
     * @param tokens tokens to locate insertion point for entities
     * @param entity entity to add
     */
    public void add(List<Token> tokens, Entity entity) {
        add(tokens, Collections.singleton(entity));
    }

    public void add(List<Token> tokens, Entity entity, List<Token> originalTokens) {
        add(tokens, Collections.singleton(entity), originalTokens);
    }

    /**
     * Returns the set of entities located by the given list of tokens. This method does not consider alternative forms.
     *
     * @param tokens tokens to locate the information to get
     * @return located set of entities or null if token sequence not contained in tree
     */
    public Set<Entity> get(List<Token> tokens) {
        TokenTree curNode = this;
        for (Token t : tokens) {
            TokenTree nextNode = curNode.children.get(t);
            if (nextNode == null) {
                return null;
            }
            curNode = nextNode;
        }
        return curNode.entities;
    }

    /**
     * Returns the list of tokens which are the longest match with entities assigned in this tree.
     *
     * @param tokens list of tokens to check for longest match
     * @return list of tokens being the longest match, sublist of {@code tokens} anchored at the first token
     */
    public List<Token> getLongestMatch(List<Token> tokens) {
        List<Token> fallbackTokenList = new ArrayList<>();
        TokenTree curNode = this;

        for (Token t : tokens) {
            TokenTree nextNode = getNextTokenTree(curNode, t);
            if (nextNode == null) {
                return fallbackTokenList;
            }
            curNode = nextNode;
            fallbackTokenList.add(t);
        }
        return fallbackTokenList;
    }

    private TokenTree getNextTokenTree(TokenTree current, Token t) {
        TokenTree next = current.children.get(t);
        if (next != null) {
            return next;
        }
        for (Map.Entry<Token, TokenTree> child : current.children.entrySet()) {
            if (child.getKey().equalsWithAlternativeForms(t)) {
                return child.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the set of entities assigned to the longest matching token subsequence of the given token sequence.
     * @param tokens    token sequence to search for longest match
     * @return set of entities assigned to the longest matching token subsequence of the given token sequence
     */
    public Set<Entity> getEntitiesForLongestMatch(List<Token> tokens) {
        TokenTree fallback = this.entities.isEmpty() ? null : this;
        TokenTree curNode = this;

        for (Token t : tokens) {
            TokenTree nextNode = getNextTokenTree(curNode, t);
            if (nextNode == null) {
                return fallback == null ? null : fallback.entities;
            }
            curNode = nextNode;
            if (!curNode.entities.isEmpty()) {
                fallback = curNode;
            }
        }

        return fallback == null ? Collections.<Entity>emptySet() : fallback.entities;
    }

    /**
     * Returns the original token for the longest match
     */
    public List<Token> getOriginalTokensForLongestMatch(List<Token> tokens) {
        TokenTree fallback = this.entities.isEmpty() ? null : this;
        TokenTree curNode = this;

        for (Token t : tokens) {
            TokenTree nextNode = getNextTokenTree(curNode, t);
            if (nextNode == null) {
                return fallback == null ? null : fallback.originalTokens;
            }
            curNode = nextNode;
            if (!curNode.entities.isEmpty()) {
                fallback = curNode;
            }
        }

        return fallback == null ? Collections.<Token>emptyList() : fallback.originalTokens;
    }

    public static void main(String[] args) throws Exception {
    	List<Token> tokens1 = Lists.newLinkedList();
    	for (String s : Splitter.on(" ").split("this is a token tree")) {
			tokens1.add(new Token(s, s, s, false, false));
		};
		
		List<Token> tokens2 = Lists.newLinkedList();
    	for (String s : Splitter.on(" ").split("this is a tokenized tree")) {
			tokens2.add(new Token(s, s, s, false, false));
		};
		
		TokenTree tree = new TokenTree();
		tree.add(tokens1, new NamedClass("TokenTree"));
		tree.add(tokens2, new NamedClass("TokenizedTree"));
        System.out.println(tree);
        
        System.out.println(tree.getEntitiesForLongestMatch(tokens1));
        System.out.println(tree.getLongestMatch(tokens1));
        
        List<Token> tokens3 = Lists.newLinkedList();
    	for (String s : Splitter.on(" ").split("this is a very nice tokenized tree")) {
			tokens3.add(new Token(s, s, s, false, false));
		};
        System.out.println(tree.getLongestMatch(tokens3));
    }

    
    public String toString() {
        return "TokenTree\n"+ toString(0);
    }

    public String toString(int indent) {
        StringBuilder indentStringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStringBuilder.append(" ");
        }
        String indentString = indentStringBuilder.toString();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Token, TokenTree> e : new TreeMap<>(children).entrySet()) {
            sb.append(indentString).append(e.getKey().toString());
            sb.append("\n");
            sb.append(e.getValue().toString(indent + 1));
        }
        return sb.toString();
    }

    
}
