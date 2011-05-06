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
		
		Pattern compAdjPattern   = Pattern.compile("\\s(\\w+/RBR.([a-zA-Z_0-9]+)/JJ)");
		Pattern superAdjPattern  = Pattern.compile("\\s(\\w+/RBS.([a-zA-Z_0-9]+)/JJ)");
		Pattern howAdjPattern    = Pattern.compile("\\s(\\w+/WRB.([a-zA-Z_0-9]+)/JJ)"); 
		Pattern nprepPattern     = Pattern.compile("\\s((\\w+)/NNS?.of/IN)");
		Pattern didPattern       = Pattern.compile("(?i)(\\s((did)|(do)|(does))/VB.?)\\s"); 
		Pattern passivePattern1a = Pattern.compile("(((has)|(have)|(had))/VB[A-Z]?.been/VBN.(\\w+)/VBN.by/IN)");
		Pattern passivePattern1b = Pattern.compile("(\\s((has)|(have)|(had))/VB[A-Z]?(.+\\s)been/VBN\\s(\\w+)/VB(N|D))");
		Pattern passivePattern2a = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(\\w+)/VBN.by/IN)");
		Pattern passivePattern2b = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(.+)(\\s\\w+)/VB(N|D))");
		Pattern passpartPattern  = Pattern.compile("\\s((\\w+)/VBN.by/IN)");
		Pattern vpassPattern     = Pattern.compile("\\s(\\w+/VBD.(\\w+)/VBN)");
		Pattern vpassinPattern   = Pattern.compile("\\s((\\w+)/VPASS.\\w+/IN)");
		Pattern gerundinPattern  = Pattern.compile("\\s((\\w+)/((VBG)|(VBN)).\\w+/IN)");
		Pattern vprepPattern     = Pattern.compile("\\s((\\w+)/V[A-Z]+\\s\\w+/(IN|TO))");
		Pattern whenPattern      = Pattern.compile("(?i)(when/WRB\\s(.+\\s)(\\w+)/((V[A-Z]+)|(PASS[A-Z]+)))");
		Pattern wherePattern     = Pattern.compile("(?i)(where/WRB\\s(.+\\s)(\\w+)/((V[A-Z]+)|(PASS[A-Z]+)))");
		
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
		m = didPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),"");
		}
		m = passivePattern1a.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(6)+"/PASSIVE");
		}
		m = passivePattern1b.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(6) + m.group(7)+"/PASSIVE");
		}
		m = passivePattern2a.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+"/PASSIVE");
		}
		m = passivePattern2b.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7) + m.group(8)+"/PASSIVE");
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
		m = gerundinPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/GERUNDIN");
		}
		m = vprepPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPREP");
		}
		m = whenPattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2) + m.group(3)+"/WHEN");
		}
		m = wherePattern.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2) + m.group(3)+"/WHERE");
		}
		
		return condensedstring;
	}

	public static List<Pair<String,String>> condenseNominalPhrases(List<Pair<String,String>> tokenPOSpairs) {
		
		List<Pair<String,String>> out = new ArrayList<Pair<String,String>>();

		String flat = "";
		for (Pair<String,String> p : tokenPOSpairs) {
			flat += " " + p.fst.trim() + "/" + p.snd.trim();
		}
		flat = flat.trim();
		
		Matcher m;
		Pattern nnpPattern = Pattern.compile("\\s?((\\w+)/NNP[S]?\\s(\\w+))/NNP[S]?");
		Pattern nnPattern  = Pattern.compile("\\s?((\\w+)/NN[S]?\\s(\\w+))/NN[S]?");
		
		m = nnpPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
		}
		m = nnPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
		}
		
		System.out.println("NNP stuff: " + flat);
		
		String[] flatParts = flat.split(" ");
		for (String part : flatParts) {
			System.out.println(part);
			out.add(new Pair<String,String>(part.substring(0,part.indexOf("/")).replaceAll("_"," "), part.substring(part.indexOf("/")+1)));
		}

		return out;
	}
}
