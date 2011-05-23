package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.Category;
import org.dllearner.algorithm.tbsl.ltag.data.LTAG_Tree_Constructor;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.ltag.reader.ParseException;
import org.dllearner.algorithm.tbsl.sem.util.Pair;
import org.dllearner.algorithm.tbsl.templator.SlotBuilder;

/**
 * GrammarFilter implements the grammar filtering of an LTAG parser. The input
 * string is iteratively partitioned into shrinking n-grams and compared to the
 * existing anchors of the trees in the grammar. For example, the input string
 * "a b c" is compared to the grammar with the following n-grams: "a b c",
 * "a b", "a", "b c", "b", "c"; If no trees are found for the exact n-gram, the
 * grammar filter supports the .+ wildcard. If an anchor of a tree in the
 * grammar contains the .+ wildcard the input n-gram "a b x y c" matches the
 * anchor "a b .+ c".
 */
class GrammarFilter {

	final static String[] NAMED_Strings = {"named", "called"};
	// DISAM
	static List<Integer> usedInts = new ArrayList<Integer>();
	static ArrayList<String> doubles = new ArrayList<String>();
	
	static ParseGrammar filter(String taggedinput,LTAGLexicon grammar,List<Integer> temps) {
		
		// DISAM: CLEAR 
		usedInts = new ArrayList<Integer>();
		doubles = new ArrayList<String>();
		
		SlotBuilder slotbuilder = new SlotBuilder();
		
		List<String> input = getWordList(taggedinput.trim());
		input.add(0,"#");  // This is important. Don't mess with the parser!
		
		ParseGrammar parseG = new ParseGrammar(input.size());
		LTAG_Tree_Constructor c = new LTAG_Tree_Constructor();

		short localID = 0;
		int start = 1; // Because # does not have a tree.
		int end = input.size();
		
		List<String> unknownTokens = new ArrayList<String>();
		List<String> coveredTokens= new ArrayList<String>(); 
		
		while (start <= input.size()) {

			end = input.size();

			boolean foundCandidates = false;
			
			while (end > start) {

				String token = join(input.subList(start,end)," ").trim().toLowerCase();

				// check for trees in the grammar with token as anchor
				List<Pair<Integer,TreeNode>> candidates = grammar.getAnchorToTrees().get(token); 
				
				/* 
				 * check for a token that matches a pattern like "named <STRING>+";
				 * if so, the function checkForNamedString returns the appropriate tree.
				 * The tree and the corresponding semantics are added below.
				 */
				List<Pair<String,String>> named = checkForNamedString(token);

				if (candidates != null) {
					foundCandidates = true;
					coveredTokens.add(token);
					
					// DISAM 
					String[] tokenParts = token.split(" ");
					String[] newTokenParts = new String[tokenParts.length];
					int fresh = createFresh();
					for (int i = 0; i < tokenParts.length; i++) {
						newTokenParts[i] = tokenParts[i] + fresh;
					} //
					
					for (Pair<Integer,TreeNode> p : candidates) {
						
						// DISAM
						TreeNode new_p_second = p.getSecond().clone();
						if (doubles.contains(token)) {	
							for (int i = 0; i < tokenParts.length; i++) {
								new_p_second.setAnchor(tokenParts[i],newTokenParts[i]);
							} 
						} //
						
						add(parseG, new_p_second, p.getFirst(), localID);
						localID++;
					}
					doubles.add(token); // DISAM

				} else if (named != null) {
					
					for (Pair<String,String> p : named) {
						try {
							TreeNode tree = c.construct(p.getFirst().replaceAll("_"," "));
							
							int gid = grammar.addTree(grammar.size(), new Pair<String,TreeNode>(token,tree), Collections.singletonList(p.getSecond()));
							add(parseG, tree, gid-1, localID);
							temps.add(gid-1);
							localID++;
							
							foundCandidates = true;
							coveredTokens.add(token);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					
				
				} else if (token.matches("[0-9]+[,\\.]?[0-9]*")) {
					/*
					 * The following if clause adds support for numbers. If the
					 * input token matches the regular expression "\d", a new
					 * auxiliary tree is added to the parseGrammar.
					 */

					try {
						TreeNode tree = c.construct("NUM:'" + token + "'");

						int gid = grammar.addTree(grammar.size(), new Pair<String,TreeNode>(token,tree), 
								Collections.singletonList("<x,l1,e,[l1:[ x | equal(x," + token + ")]],[],[],[]>"));
						add(parseG, tree, gid-1, localID);
						localID++;
						
						foundCandidates = true;
						coveredTokens.add(token);

					} catch (ParseException e) {
						e.printStackTrace();
					}
					
				} else {
					/*
					 * Check if the grammar contains an anchor with a wildcard
					 */
					String[] tokenParts = token.split(" ");
					if (tokenParts.length > 2) {
						
						for (String anchor : grammar.getWildCardAnchors()) {
							
							if (token.matches(anchor)) {
								
								foundCandidates = true;
								coveredTokens.add(token);
								
								// DISAM 
								String[] newTokenParts = new String[tokenParts.length];
								int fresh = createFresh();
								for (int i = 0; i < tokenParts.length; i++) {
									newTokenParts[i] = tokenParts[i] + fresh;
								} //
								
								for (Pair<Integer, TreeNode> p : grammar.getAnchorToTrees().get(anchor)) {
									
									// DISAM
									TreeNode new_p_second = p.getSecond();
									if (doubles.contains(token)) {	
										for (int i = 0; i < tokenParts.length; i++) {
											new_p_second.setAnchor(tokenParts[i],newTokenParts[i]);
										} 
									} //
									
									add(parseG, new_p_second, p.getFirst(),localID);
									localID++;
								}
								doubles.add(token); // DISAM
							}
						}
					} 			
					else if (!foundCandidates) {						
						unknownTokens.add(token);
					}
				}
				end--;
			}			
			foundCandidates = false;
			start++;
		}
		
		System.out.println("\ncovered tokens: " + coveredTokens);

		/* construct slots for all unknown tokens */
		
		List<String> coveredWords = new ArrayList<String>();
		for (String	ct : coveredTokens) {
			for (String ctPart : ct.split(" ")) {
				coveredWords.add(ctPart.trim());
			}
		}
		
		List<String> unknownWords = new ArrayList<String>();	
		for (String t : unknownTokens) {	
			String[] tParts = t.split(" ");
			for (String s : tParts) {
				if (!coveredWords.contains(s) && !unknownWords.contains(s)) {
					unknownWords.add(s);
				}
			}
		}
		System.out.println("unknown words:  " + unknownWords);
		
		List<Pair<String,String>> buildSlotFor = new ArrayList<Pair<String,String>>();
		
		// remove known parts 
		String newtaggedstring = "";
		String[] condensedparts = taggedinput.split(" ");
		for (String part : condensedparts) {
			if (unknownWords.contains(part.substring(0,part.indexOf("/")).toLowerCase())) {
				newtaggedstring += part + " ";
			}
		}
		
		// build token-POStag-pairs 
		String[] newparts = newtaggedstring.trim().split(" ");
		for (String s : newparts) {
			if (s.contains("/")) {
				String word = s.trim().substring(0,s.indexOf("/"));
				if (doubles.contains(word)) {
					word += createFresh();
				}
				buildSlotFor.add(new Pair<String,String>(word,s.trim().substring(s.indexOf("/")+1)));
				doubles.add(word);
			} else {
				System.out.println("Oh no, " + s + " has no POS tag!");
			}
		}	
		System.out.println("build slot for: " + buildSlotFor + "\n");
			
		List<String[]> entries = slotbuilder.build(taggedinput,buildSlotFor);
				
		try {	
				for (String[] entry : entries) {
					String anchor = entry[0];
					String treestring = entry[1];					
					List<String> dudeStrings = new ArrayList<String>();
					for (String s : entry[2].trim().split(";;")) {
						if (!s.equals("")) {
							dudeStrings.add(s.trim());
						}
					}
							
					TreeNode tree = c.construct(treestring);
					int gid = grammar.addTree(grammar.size(), new Pair<String,TreeNode>(anchor,tree), dudeStrings);
					add(parseG, tree, gid-1, localID); 
					temps.add(gid-1);
					localID++;
				}
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return parseG;
	}

	private static List<Pair<String,String>> checkForNamedString(String token) {

		String[] split = token.split(" ");
		
		if (split.length > 1 && split.length < 5) {
			
			for (String w : NAMED_Strings) {
				
				if (split[0].trim().equals(w)) {
					
					List<Pair<String,String>> out = new ArrayList<Pair<String,String>>(2);
					String rawNames = "";
					String semName = "";
					for (int i=1;i<split.length;i++) {
						semName += "_" + split[i];
						rawNames += "DP:'" + split[i] + "' ";
					}
					semName = semName.substring(1);
					out.add(new Pair<String,String>("(NP NP* ADJ:'"+ w +"' " + rawNames + ")", "<x,l1,<e,t>,[ l1:[ | SLOT_title(x,'" + semName + "') ] ], [],[],[ SLOT_title/PROPERTY/title^name ]>"));
					out.add(new Pair<String,String>("(DP DP* ADJ:'"+ w +"' " + rawNames + ")", "<x,l1,<<e,t>,t>,[ l1:[ | SLOT_title(x,'" + semName + "') ] ], [],[],[ SLOT_title/PROPERTY/title^name ]>"));
					out.add(new Pair<String,String>("(ADJ ADJ:'"+ w +"' " + rawNames + ")", "<x,l1,<e,t>,[ l1:[ | SLOT_title(x,'" + semName + "') ] ], [],[],[ SLOT_title/PROPERTY/title^name ]>"));
					
					return out;
					
				}
				
			}
			
		}
		
		return null;
	}

	private static void add(ParseGrammar parseG, TreeNode t, int globalID,
			short localID) {

		t = t.clone();

		// add to parseGrammar
		parseG.add(new Pair<TreeNode, Short>(t, localID));

		if (t.isAuxTree()) {
			// add to list of auxiliary trees
			parseG.getAuxTrees().add(new Pair<TreeNode, Short>(t, localID));

		} else if ((t.getCategory() == Category.S)) {
			// add to list of init trees
			parseG.getInitTrees().add(new Pair<TreeNode, Short>(t, localID));
		}

		parseG.getIndex().put(localID, t);

		parseG.getLocalIdsToGlobalIds().put(localID, globalID);

	}

	public static String join(List<String> list, String delimiter) {
		if (list.isEmpty())
			return "";
		Iterator<String> iter = list.iterator();
		StringBuffer buffer = new StringBuffer(iter.next());
		while (iter.hasNext())
			buffer.append(delimiter).append(iter.next());
		return buffer.toString();
	}
	
	private static List<String> getWordList(String string) {
		
		List<String> result = new ArrayList<String>();
		
		for (String s : string.split(" ")) {
				result.add(s.substring(0,s.indexOf("/")));
		}
		
		return result;
	}
	
	private static int createFresh() {
		
		int fresh = 0;
		for (int i = 0; usedInts.contains(i); i++) {
			fresh = i+1 ;
		}
		usedInts.add(fresh);
		return fresh;
	}

}
