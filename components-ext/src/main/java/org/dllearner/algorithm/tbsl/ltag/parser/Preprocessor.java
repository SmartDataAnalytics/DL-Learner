package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.algorithm.tbsl.sem.util.Pair;

public class Preprocessor {

	static final String[] genericReplacements = { "\"", "", "'", "", "[!?.,;]", "" };
	static final String[] englishReplacements = { "don't", "do not", "doesn't", "does not" };
	
	public Preprocessor() {
	}
	
	public static String normalize(String s) {
		return normalize(s, new String[0]);
	}

	public static String normalize(String s, String... repl) {

		if (repl.length % 2 != 0 || genericReplacements.length % 2 != 0 || englishReplacements.length % 2 != 0) {
			throw new IllegalArgumentException();
		}

		List<String> replacements = new ArrayList<String>();
		replacements.addAll(Arrays.asList(repl));
		replacements.addAll(Arrays.asList(englishReplacements));
		replacements.addAll(Arrays.asList(genericReplacements));

		for (int i = 0; i < replacements.size(); i += 2) {
			s = s.replaceAll(replacements.get(i), replacements.get(i + 1));
		}

		return s;
	}
	
	public static String condense(String taggedstring) {
		
		/* condense: 
		 * x/RBR adj/JJ > adj/JJR, x/RBS adj/JJ > adj/JJS, x/WRB adj/JJ > x/JJH
		 * nn/RBR of/IN > nn/NPREP
		 * */ 
		String condensedstring = taggedstring;
		Matcher m;
		
		Pattern compAdjPattern  = Pattern.compile("\\s(\\w+/RBR.([a-zA-Z_0-9]+)/JJ)");
		Pattern superAdjPattern = Pattern.compile("\\s(\\w+/RBS.([a-zA-Z_0-9]+)/JJ)");
		Pattern howAdjPattern   = Pattern.compile("\\s(\\w+/WRB.([a-zA-Z_0-9]+)/JJ)"); 
		Pattern nprepPattern    = Pattern.compile("\\s((\\w+)/NNS?.of/IN)");
		Pattern passivePattern1 = Pattern.compile("(((has)|(have)|(had))/VB[A-Z]?.been/VBN.(\\w+)/VBN.by/IN)");
		Pattern passivePattern2 = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(\\w+)/VBN.by/IN)");
		Pattern passpartPattern = Pattern.compile("\\s((\\w+)/VBN.by/IN)");
		Pattern vpassPattern    = Pattern.compile("\\s(\\w+/VBD.(\\w+)/VBN)");
		Pattern vpassinPattern  = Pattern.compile("\\s((\\w+)/VPASS.\\w+/IN)");
		Pattern vprepPattern    = Pattern.compile("\\s((\\w+)/V[A-Z]+\\s\\w+/IN)");
		
		m = compAdjPattern.matcher(condensedstring); 
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJR");
		}
		m = superAdjPattern.matcher(condensedstring); 
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJS");
		}
		m = howAdjPattern.matcher(condensedstring); 
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJH");
		}
		m = nprepPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/NPREP");
		}
		m = passivePattern1.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(6)+"/PASSIVE");
		}
		m = passivePattern2.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+"/PASSIVE");
		}
		m = passpartPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/PASSPART");
		}
		m = vpassPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPASS");
		}
		m = vpassinPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPASSIN");
		}
		m = vprepPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPREP");
		}
		
		return condensedstring;
	}

	public static List<Pair<String,String>> condenseNominalPhrases(List<Pair<String,String>> tokenPOSpairs){
		List<Pair<String,String>> test = new ArrayList<Pair<String,String>>();
		
		String nounPhrase = "";
		String phraseTag = "";
		for(Pair<String,String> pair : tokenPOSpairs){
			if(pair.snd.startsWith("NNP")){
				if(phraseTag.equals("NN")){
					if(!nounPhrase.isEmpty()){
						test.add(new Pair<String, String>(phraseTag.trim(), "NN"));
						nounPhrase = "";
					}
				}
				phraseTag = "NNP";
	    		nounPhrase += " " + pair.fst;
			} else if(pair.snd.startsWith("NN")){
				if(phraseTag.equals("NNP")){
					if(!nounPhrase.isEmpty()){
						test.add(new Pair<String, String>(phraseTag.trim(), "NNP"));
						nounPhrase = "";
					}
				}
				phraseTag = "NN";
	    		nounPhrase += " " + pair.fst;
			} else {
				if(!nounPhrase.isEmpty()){
	    			test.add(new Pair<String, String>(nounPhrase.trim(), phraseTag));
	    			nounPhrase = "";
	    		}
				test.add(pair);
			}
		}
		if(!nounPhrase.isEmpty()){
			test.add(new Pair<String, String>(nounPhrase.trim(), phraseTag));
			nounPhrase = "";
		}
		
		return test;
	}
}
