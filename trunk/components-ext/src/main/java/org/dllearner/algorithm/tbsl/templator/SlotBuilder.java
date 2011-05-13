package org.dllearner.algorithm.tbsl.templator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithm.tbsl.sem.util.Pair;

public class SlotBuilder {
	
	private WordNet wordnet;
	private String[] noun = {"NN","NNS","NNP","NNPS","NPREP"};
	private String[] adjective = {"JJ","JJR","JJS","JJH"};
	private String[] verb = {"VB","VBD","VBG","VBN","VBP","VBZ","PASSIVE","PASSPART","VPASS","VPASSIN","GERUNDIN","VPREP","WHEN","WHERE"};
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
				else if (pos.equals("NPREP")) {
					type = "PROPERTY";
				}
				
				List<String> words = new ArrayList<String>();
				words.add(token); 
				if (!pos.equals("NNP") && !pos.equals("NNPS")) {
					words.addAll(wordnet.getBestSynonyms(token));
				}
				
				String tokenfluent = token.replaceAll(" ","");
				String slotX = "x/" + type + "/";
				String slotP = "SLOT_" + tokenfluent + "/" + type + "/";
				for (Iterator<String> i = words.iterator(); i.hasNext();) {
					String next = i.next().replaceAll(" ","_");
					slotX += next; slotP += next;
					if (i.hasNext()) { slotX += "^"; slotP += "^"; }
				}
				String treetoken = "N:'" + token.toLowerCase() + "'";
				if (token.trim().contains(" ")) {
					String[] tokenParts = token.split(" ");
					treetoken = "";
					for (String t : tokenParts) {
						treetoken += " N:'" + t.toLowerCase() + "'";
					}
					treetoken = treetoken.trim();
				}
				if (pos.equals("NN") || pos.equals("NNS")) {
					/* DP */
					String[] dpEntry = {token,
							"(DP (NP " + treetoken + "))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + tokenfluent + "(x) ] ],[],[],[" + slotP + "]>"};
					result.add(dpEntry);
					/* NP */
					String[] npEntry = {token,
							"(NP " + treetoken + ")",
							"<x,l1,<e,t>,[ l1:[ | SLOT_" + tokenfluent + "(x) ] ],[],[],[" + slotP + "]>"};
					result.add(npEntry);	
				} 
				else if (pos.equals("NNP") || pos.equals("NNPS")) {
					/* DP */
					String[] dpEntry1 = {token,
							"(DP (NP " + treetoken + "))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | ] ],[],[],[" + slotX + "]>"};
					String[] dpEntry2 = {token,
							"(DP DET[det] (NP " + treetoken + "))",
							"<x,l1,<<e,t>,t>,[ l1:[ | ] ],[(l2,x,det,e)],[l2=l1],[" + slotX + "]>"};					
					result.add(dpEntry1);
					result.add(dpEntry2);
				}
				else if (pos.equals("NPREP")) {
					/* DP */
					String[] dpEntry1a = {token,
							"(DP (NP " + treetoken + " DP[pobj]))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + tokenfluent + "(y,x) ] ],[(l2,y,pobj,<<e,t>,t>)],[l2=l1],[" + slotP + "]>"};
					String[] dpEntry1b = {token,
							"(DP (NP " + treetoken + " DP[pobj]))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + tokenfluent + "(x), SLOT_of(x,y) ] ],[(l2,y,pobj,<<e,t>,t>)],[l2=l1],[" + slotP + "," + "SLOT_of/PROPERTY/" + "]>"};
					String[] dpEntry2a = {token,
							"(DP DET[det] (NP " + treetoken + " DP[pobj]))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + tokenfluent + "(y,x) ] ],[(l2,y,pobj,<<e,t>,t>),(l3,x,det,e)],[l2=l1,l3=l1],[" + slotP + "]>"};
					String[] dpEntry2b = {token,
							"(DP DET[det] (NP " + treetoken + " DP[pobj]))",
							"<x,l1,<<e,t>,t>,[ l1:[ x | SLOT_" + tokenfluent + "(x), SLOT_of(x,y) ] ],[(l2,y,pobj,<<e,t>,t>),(l3,x,det,e)],[l2=l1,l3=l1],[" + slotP + "," + "SLOT_of/PROPERTY/" + "]>"};
					String[] npEntry1 = {token,
							"(NP " + treetoken + " DP[pobj])",
							"<x,l1,<e,t>,[ l1:[ | SLOT_" + tokenfluent + "(y,x) ] ],[(l2,y,pobj,<<e,t>,t>)],[l2=l1],[" + slotP + "]>"};
					String[] npEntry2 = {token,
							"(NP " + treetoken + " DP[pobj])",
							"<x,l1,<e,t>,[ l1:[ | SLOT_" + tokenfluent + "(x), SLOT_of(x,y) ] ],[(l2,y,pobj,<<e,t>,t>)],[l2=l1],[" + slotP + "," + "SLOT_of/PROPERTY/" + "]>"};
					result.add(dpEntry1a);
					result.add(dpEntry1b);
					result.add(dpEntry2a);
					result.add(dpEntry2b);
					result.add(npEntry1);
					result.add(npEntry2);
				}
						
			}
			/* VERBS */
			else if (equalsOneOf(pos,verb)) {
				
				String slot = "SLOT_" + token + "/PROPERTY/" + token; 				
				List<String> preds = wordnet.getAttributes(token);
				for (Iterator<String> i = preds.iterator(); i.hasNext();) {
					slot += i.next();
					if (i.hasNext()) {
						slot += "^";
					}
				}
				if (pos.equals("PASSIVE")) {
					String[] passEntry1 = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(y,x) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					String[] passEntry2 = {token,
							"(S DP[wh] (VP DP[dp] V:'" + token + "'))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(y,x) ] ],[(l2,x,wh,<<e,t>,t>),(l3,y,dp,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					result.add(passEntry1);
					result.add(passEntry2);
				}
				else if (pos.equals("PASSPART")) {
					String[] passpartEntry = {token,
							"(NP NP* (VP V:'" + token + "' DP[dp]))",
							"<x,l1,t,[ l1:[ | SLOT_" + token + "(y,x) ] ],[(l2,y,dp,<<e,t>,t>)],[ l2=l1 ],[" + slot + "]>"};
					result.add(passpartEntry);
				}
				else if (pos.equals("VPASS")) {
					String[] passEntry = {token,
							"(S DP[subj] (VP V:'" + token + "'))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(y,x) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					result.add(passEntry);
				}
				else if (pos.equals("VPASSIN")) {
					String[] passEntry = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(x,y) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					result.add(passEntry);
				}
				else if (pos.equals("GERUNDIN")) {
					String[] gerundinEntry1 = {token,
							"(NP NP* V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[ | SLOT_" + token + "(x,y) ] ],[(l2,y,obj,<<e,t>,t>)],[ l2=l1 ],[" + slot + "]>"};
					String[] gerundinEntry2 = {token,
							"(ADJ V:'" + token + "' DP[obj]))",
							"<x,l1,<e,t>,[ l1:[ | SLOT_" + token + "(x,y) ] ],[(l2,y,obj,<<e,t>,t>)],[ l2=l1 ],[" + slot + "]>"};
					result.add(gerundinEntry1);
					result.add(gerundinEntry2);
				}
				else if (pos.equals("VPREP")) {
					String[] passEntry = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(x,y) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					result.add(passEntry);
				}
				else if (pos.equals("VBD") || pos.equals("VBZ") || pos.equals("VBP") || pos.equals("VB")) {
					String[] vEntry = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ | SLOT_" + token + "(x,y) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ],[" + slot + "]>"};
					result.add(vEntry);
				} 
				else if (pos.equals("VBG") || pos.equals("VBN")) {
					String[] gerEntry = {token,
							"(NP NP* (VP V:'" + token + "' DP[dp]))",
							"<x,l1,t,[ l1:[ | SLOT_" + token + "(x,y) ] ],[(l2,y,dp,<<e,t>,t>)],[ l2=l1 ],[" + slot + "]>"};
					result.add(gerEntry);
				}
				else if (pos.equals("WHEN")) {
					String dateSlot = "SLOT_" + token + "/PROPERTY/" + token + "Date";
					String tokenSlot = "SLOT_" + token + "/PROPERTY/" + token;
					String[] whenEntry1 = {token,
							"(S DP[subj] (VP V:'" + token + "'))",
							"<x,l1,t,[ l1:[ ?y | SLOT_" + token + "(x,y) ] ],[(l2,x,subj,<<e,t>,t>)],[ l2=l1 ],[ " + dateSlot + " ]>"};
					String[] whenEntry2 = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ ?z | SLOT_" + token + "(x,y), SLOT_date(x,z) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ]," +
									"[" + tokenSlot + ", SLOT_date/PROPERTY/date ]>"};
					result.add(whenEntry1);
					result.add(whenEntry2);
				}
				else if (pos.equals("WHERE")) {
					String placeSlot = "SLOT_" + token + "/PROPERTY/" + token + "Place";
					String tokenSlot = "SLOT_" + token + "/PROPERTY/" + token;
					String[] whereEntry1 = {token,
							"(S DP[subj] (VP V:'" + token + "'))",
							"<x,l1,t,[ l1:[ ?y | SLOT_" + token + "(x,y) ] ],[(l2,x,subj,<<e,t>,t>)],[ l2=l1 ],[ " + placeSlot + " ]>"};
					String[] whereEntry2 = {token,
							"(S DP[subj] (VP V:'" + token + "' DP[obj]))",
							"<x,l1,t,[ l1:[|], l4:[ ?z | SLOT_" + token + "(x,y), SLOT_place(x,z) ] ],[(l2,x,subj,<<e,t>,t>),(l3,y,obj,<<e,t>,t>)],[ l2<l1,l3<l1,l4<scope(l2),l4<scope(l3) ]," +
									"[" + tokenSlot + ", SLOT_place/PROPERTY/place ]>"};
					result.add(whereEntry1);
					result.add(whereEntry2);
				}
				
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
	


}
