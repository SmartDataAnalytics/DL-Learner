package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Preprocessor {

	static final String[] genericReplacements = { "\"", "", "'", "", "[!?.,;]", "" };
	static final String[] englishReplacements = { "don't", "do not", "doesn't", "does not" };
	
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

}
