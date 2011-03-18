package org.dllearner.algorithm.tbsl.templator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithm.tbsl.sem.util.Pair;

public class SlotBuilder {
	
	private WordNet wordnet;
	private String[] noun = {"NN","NNS","NNP","NNPS"};
	private String[] adjective = {"JJ","JJR","JJS","JJH"};
	private String[] verb = {"VB","VBD","VBG","VBN","VBP","VBZ"};
	private String[] preps = {"IN"};
	
	public SlotBuilder() {
		
		wordnet = new WordNet();
		wordnet.init();
	}
	
	/**
	 *  gets synonyms, attribute etc. from WordNet and construct grammar entries 
	 *  INPUT:  array of tokens and array of POStags, from which preprocessor constructs a list of pairs (token,pos)
	 *  OUTPUT: list of (treestring,dude) 
	 **/
	public List<String[]> build(String taggedstring,List<Pair<String,String>> tokenPOSpairs) {
		
		List<String[]> result = new ArrayList<String[]>();
		
		for (Pair<String,String> pair : tokenPOSpairs) {
		
			String token = pair.fst;
			String pos = pair.snd;
			
			String type = "UNSPEC";
			
			/* NOUNS */
			if (equalsOneOf(pos,noun)) {
				
				if (pos.equals("NN") || pos.equals("NNS")) {
					type = "CLASS";
				}
				else if (pos.equals("NNP") || pos.equals("NNPS")) {
					type = "RESOURCE";
				}
				List<String> words = wordnet.getBestSynonyms(token);
				words.add(0,token);
				
				String slotX = "x/" + type + "/";
				String slotP = "SLOT_" +token + "/" + type + "/";
				for (Iterator<String> i = words.iterator(); i.hasNext();) {
					String next = i.next();
					slotX += next; slotP += next;
					if (i.hasNext()) { slotX += "^"; slotP += "^"; }
				}
				if (pos.equals("NN") || pos.equals("NNS")) {
					/* DP */
					String[] dpEntry = {token,
							"(DP (NP N:'" + token.toLowerCase() + "'))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + token + "(x) ] ],[],[],[" + slotP + "]>"};
					result.add(dpEntry);
					/* NP */
					String[] npEntry = {token,
							"(NP N:'" + token.toLowerCase() + "')",
							"<x,l1,<e,t>,[ l1:[ | SLOT_" + token + "(x) ] ],[],[],[" + slotP + "]>"};
					result.add(npEntry);	
				} 
				else if (pos.equals("NNP") || pos.equals("NNPS")) {
					/* DP */
					String[] dpEntry = {token,
							"(DP (NP N:'" + token.toLowerCase() + "'))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | ] ],[],[],[" + slotX + "]>"};
					result.add(dpEntry);
				}
						
			}
			/* VERBS */
			else if (equalsOneOf(pos,verb)) {
				
			}
			/* ADJECTIVES */
			else if (equalsOneOf(pos,adjective)) {
				
				String slot = "SLOT_" + token + "/PROPERTY/";
				List<String> preds = wordnet.getAttributes(token);
				for (Iterator<String> i = preds.iterator(); i.hasNext();) {
					slot += i.next();
					if (i.hasNext()) {
						slot += "^";
					}
				}
				/* ADJECTIVE */
				if (pos.equals("JJ")) {
					String[] adjEntry = {token,
							"(NP ADJ:'" + token.toLowerCase() + "' NP*)",
							"<x,l1,<e,t>,[ l1:[ j | SLOT_" + token + "(x,j) ] ],[],[],["+slot+"]>"};			
					result.add(adjEntry);
				}
				if (pos.equals("JJH")) {
					String[] howEntry = {"how "+token,
							"(DP WH:'how' ADJ:'" + token.toLowerCase() + "')",
							"<x,l1,<<e,t>,t>,[ l1:[ ?j,x | SLOT_" + token + "(x,j) ] ],[],[],["+slot+"]>"};
					result.add(howEntry);
				}
				/* COMPARATIVE */
				else if (pos.equals("JJR")) {
					// TODO polarity not given, reference value not determinable	
				}
				/* SUPERLATIVE */
				else if (pos.equals("JJS")) {
					// ditto
				}
			}
			/* PREPOSITIONS */
			else if (equalsOneOf(pos,preps)) {
				String slot = "SLOT_" + token + "/PROPERTY/";
				String[] npAdjunct = {token,
						"(NP NP* (PP P:'" + token.toLowerCase() + "' DP[pobj]))",
						"<x,l1,<e,t>,[ l1:[ | SLOT_" + token + "(x,y) ] ],[(l2,y,pobj,<<e,t>,t>)],[l2=l1],["+slot+"]>"};
				result.add(npAdjunct);
			}
		}
		
		return result;
	}
	
	private boolean equalsOneOf(String string,String[] strings) {
		for (String s : strings) {
			if (string.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	private List<Pair<String,String>> extractNominalPhrases(List<Pair<String,String>> tokenPOSpairs){
		List<Pair<String,String>> test = new ArrayList<Pair<String,String>>();
		
		String nounPhrase = "";
		String phraseTag = "";
		for(Pair<String,String> pair : tokenPOSpairs){
			if(pair.snd.startsWith("NNP")){
				if(phraseTag.equals("NN")){
					
				}
				phraseTag = "NNP";
	    		nounPhrase += " " + pair.snd;
			} else if(pair.snd.startsWith("NN")){
				
			} else {
				test.add(pair);
			}
		}
		
		
		return test;
	}

}
