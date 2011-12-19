package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.nlp.DBpediaSpotlightNER;
import org.dllearner.algorithm.tbsl.nlp.LingPipeNER;
import org.dllearner.algorithm.tbsl.nlp.NER;
import org.dllearner.algorithm.tbsl.templator.Templator;

public class Preprocessor {
	
	private static final Logger logger = Logger.getLogger(Preprocessor.class);

	static final String[] genericReplacements = { "[!?.,;]", "" };
	static final String[] englishReplacements = { "don't", "do not", "doesn't", "does not" };
	static boolean USE_NER;
	static boolean VERBOSE;
	static NER ner;
	
	public Preprocessor(boolean n) {
		USE_NER = n;
		VERBOSE = true;
		if (USE_NER) {
//			ner = new LingPipeNER(true); //not case sensitive best solution?
			ner = new DBpediaSpotlightNER();
		}
	}
	
	public void setVERBOSE(boolean b) {
		VERBOSE = b;
	}
	
	public String normalize(String s) {
		return normalize(s, new String[0]);
	}

	public String normalize(String s, String... repl) {

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
	
	public String condense(String taggedstring) {
		
		/* condense: 
		 * x/RBR adj/JJ > adj/JJR, x/RBS adj/JJ > adj/JJS, x/WRB adj/JJ > x/JJH
		 * nn/RBR of/IN > nn/NPREP
		 * usw. 
		 * */ 
		String condensedstring = taggedstring.replaceAll("``/``","").replaceAll("''/''","").replaceAll("  "," ");
		Matcher m;
		
		Pattern compAdjPattern    = Pattern.compile("(\\w+/RBR.(\\w+)/JJ)");
//		Pattern superAdjPattern   = Pattern.compile("(\\w+/RBS.(\\w+)/JJ)"); // TODO "(the most) official languages" vs "the (most official) languages"
		Pattern howManyPattern    = Pattern.compile("(how/WRB.many/JJ)"); 
		Pattern howAdjPattern     = Pattern.compile("(\\w+/WRB.(\\w+)(?<!many)/JJ)"); 
		Pattern thesameasPattern  = Pattern.compile("(the/DT.same/JJ.(\\w+)/NN.as/IN)");
		Pattern nprepPattern      = Pattern.compile("\\s((\\w+)/NNS?.of/IN)");
		Pattern didPattern        = Pattern.compile("(?i)(\\s((did)|(do)|(does))/VB.?)\\s"); 
		Pattern prepfrontPattern  = Pattern.compile("(\\A\\w+/((TO)|(IN)).)\\w+/WDT"); // TODO (Nicht ganz sauber. Bei P-Stranding immer zwei Querys, hier nur eine.)
		Pattern passivePattern1a  = Pattern.compile("(((has)|(have)|(had))/VB[A-Z]?.been/VBN.(\\w+)/VBN.by/IN)");
		Pattern passivePattern1b  = Pattern.compile("(\\s((has)|(have)|(had))/VB[A-Z]?(.+\\s)been/VBN\\s(\\w+)/VB(N|D))");
		Pattern passivePattern2a  = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(\\w+)/VBN.by/IN)");
		Pattern pseudopassPattern = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(\\w+)/VBN.\\w+/((TO)|(IN)))");
		Pattern pseudopwhPattern  = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(.+)\\s(\\w+)/VB(N|D).\\w+/((TO)|(IN)))");
		Pattern saveIsThere       = Pattern.compile("((is)|(are))/(VB[A-Z]?).there/(RB)");
		Pattern passivePattern2b  = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.((.+)\\s\\w+)/VB(N|D))");
		Pattern passpartPattern   = Pattern.compile("\\s((\\w+)/VBN.by/IN)");
		Pattern vpassPattern      = Pattern.compile("\\s(\\w+/VBD.(\\w+)/VBN)");
		Pattern vpassinPattern    = Pattern.compile("\\s((\\w+)/VPASS.\\w+/IN)");
		Pattern gerundinPattern   = Pattern.compile("\\s((\\w+)/((VBG)|(VBN)).\\w+/IN)");
		Pattern vprepPattern      = Pattern.compile("\\s((\\w+)/V[A-Z]+\\s\\w+/(IN|TO))");
		Pattern whenPattern       = Pattern.compile("(?i)(when/WRB\\s(.+\\s)(\\w+)/((V[A-Z]+)|(PASS[A-Z]+)))");
		Pattern wherePattern      = Pattern.compile("(?i)(where/WRB\\s(.+\\s)(\\w+)/((V[A-Z]+)|(PASS[A-Z]+)))");
		Pattern adjsPattern       = Pattern.compile("((\\w+)/JJ.(\\w+)/JJ)");
		Pattern adjnounPattern    = Pattern.compile("((\\w+)(?<!many)/JJ.(\\w+)/NN(S)?)");
		Pattern adjnprepPattern   = Pattern.compile("((\\w+)(?<!many)/JJ.(\\w+)/NPREP)");
		
		m = compAdjPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/JJR");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJR");
		}
//		m = superAdjPattern.matcher(condensedstring); 
//		while (m.find()) {
//			logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/JJS");
//			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJS");
//		}
		m = howManyPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by how/WLEX many/WLEX");
			condensedstring = condensedstring.replaceFirst(m.group(1),"how/WLEX many/WLEX");
		}
		m = howAdjPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/JJH");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/JJH");
		}
		m = thesameasPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/NNSAME");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/NNSAME");
		}
		m = nprepPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/NPREP");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/NPREP");
		}
		m = didPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by \"\"");
			condensedstring = condensedstring.replaceFirst(m.group(1),"");
		}
		m = prepfrontPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by \"\"");
			condensedstring = condensedstring.replaceFirst(m.group(1),"");
		}
		m = passivePattern1a.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(6)+"/PASSIVE");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(6)+"/PASSIVE");
		}
		m = passivePattern1b.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(6)+m.group(7)+"/PASSIVE");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(6) + m.group(7)+"/PASSIVE");
		}
		m = passivePattern2a.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(7)+"/PASSIVE");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+"/PASSIVE");
		}
		m = pseudopassPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(7)+"/VPREP");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+"/VPREP");
		}
		m = pseudopwhPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(7)+m.group(8)+"/VPREP");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+" "+m.group(8)+"/VPREP");
		}
		m = saveIsThere.matcher(condensedstring);
		while (m.find()) {
			condensedstring = condensedstring.replaceFirst(m.group(4),"LEX").replaceFirst(m.group(5),"LEX"); // TODO what a dirty hack!
		}
		m = passivePattern2b.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(7)+"/PASSIVE");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(7)+"/PASSIVE");
		}
		m = passpartPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/PASSPART");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/PASSPART");
		}
		m = vpassPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/VPASS");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPASS");
		}
		m = vpassinPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/VPASSIN");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPASSIN");
		}
		m = gerundinPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/GERUNDIN");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/GERUNDIN");
		}
		m = vprepPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"/VPREP");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"/VPREP");
		}
		m = whenPattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+m.group(3)+"/WHEN");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2) + m.group(3)+"/WHEN");
		}
		m = wherePattern.matcher(condensedstring);
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+m.group(3)+"/WHERE");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2) + m.group(3)+"/WHERE");
		}
		m = adjsPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"_"+m.group(3)+"/JJ");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"_"+m.group(3)+"/JJ");
		}		
		m = adjnounPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"_"+m.group(3)+"/JJNN");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"_"+m.group(3)+"/JJNN");
		}
		m = adjnprepPattern.matcher(condensedstring); 
		while (m.find()) {
			if (VERBOSE) logger.trace("Replacing " + m.group(1) + " by " + m.group(2)+"_"+m.group(3)+"/JJNPREP");
			condensedstring = condensedstring.replaceFirst(m.group(1),m.group(2)+"_"+m.group(3)+"/JJNPREP");
		}
		
		return condensedstring;
	}

	public String condenseNominals(String s) {
		
		String flat = s;
		
		Matcher m;
		Pattern quotePattern1 = Pattern.compile("``/``(\\s)?(\\w+(/\\w+\\s)).*''/''");
		Pattern quotePattern2 = Pattern.compile("(``/``((.*)_)''/'')");
		Pattern nnpPattern    = Pattern.compile("\\s?((\\w+)/NNP[S]?\\s(\\w+))/NNP[S]?(\\W|$)");
		Pattern nnPattern     = Pattern.compile("\\s?((\\w+)/NN[S]?\\s(\\w+))/NN[S]?(\\W|$)");
		Pattern nnnnpPattern  = Pattern.compile("\\s?((\\w+)/NNP[S]?)\\s(\\w+)/NN[S]?(\\W|$)");

		m = quotePattern1.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(3),"_");
			m = quotePattern1.matcher(flat);
		}
		m = quotePattern2.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(2),m.group(3)+"/NNP");
		}
		
		m = nnpPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
			m = nnpPattern.matcher(flat);
		}
		m = nnpPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
			m = nnpPattern.matcher(flat);
		}
		m = nnPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
			m = nnPattern.matcher(flat);
		}
		m = nnnnpPattern.matcher(flat);
		while (m.find()) {
			flat = flat.replaceFirst(m.group(1),m.group(2) + "/JJ");
			m = nnnnpPattern.matcher(flat);
		}
		
		return flat;
	}
	
	public String findNEs(String tagged,String untagged) {
		
		String out = tagged;
		
		String[] postags = {"NN","NNS","NNP","NNPS","NPREP","JJ","JJR","JJS","JJH",
				"VB","VBD","VBG","VBN","VBP","VBZ","PASSIVE","PASSPART","VPASS","VPASSIN",
				"GERUNDIN","VPREP","WHEN","WHERE","IN","TO","DT"};
		
		List<String> namedentities = ner.getNamedEntitites(untagged);
		List<String> usefulnamedentities = new ArrayList<String>();
		
		if (VERBOSE) logger.trace("Proposed NEs: " + namedentities);
		
		// keep only longest matches (e.g. keep 'World of Warcraft' and forget about 'Warcraft') 
		// containing at least one upper case letter (in order to filter out errors like 'software')
		for (String s1 : namedentities) {
			if (s1.matches(".*[A-Z].*") && !Arrays.asList(postags).contains(s1)) { 
				boolean isLongestMatch = true;
				for (String s2 : namedentities) {
					if (!s2.equals(s1) && s2.contains(s1)) {
						isLongestMatch = false;
					}
				}
				if (isLongestMatch) {
					usefulnamedentities.add(s1);
				}
			}
		}
		
		if (VERBOSE) logger.trace("Accepted NEs: " + usefulnamedentities);
		
		// replace POS tags accordingly
		for (String ne : usefulnamedentities) {
			String[] neparts = ne.split(" ");
			Pattern p; Matcher m;
			for (String nep : neparts) {
				p = Pattern.compile("(\\s)?(" + nep + "/([A-Z]+))(\\s)?");
				m = p.matcher(out);
				while (m.find()) {
					out = out.replaceFirst(m.group(2),nep+"/NNP");
				}
			}
		}
		
		return out;
	}
	
}
