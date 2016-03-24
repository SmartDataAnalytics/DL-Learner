package org.dllearner.learningproblems.sampling.r2v.tfidf.ngram;

import java.util.List;
import java.util.TreeSet;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class TrigramRegexTokenizer extends RegularExpressionTokenizer {
	
	private final static int Q = 3;

	@Override
	public List<String> tokenize(String text) {
		// add from regex
		List<String> list = super.tokenize(text);
		
		TreeSet<String> set = new TreeSet<>();
		// head
		for(int i=1; i<Q && i<=text.length(); i++)
			set.add(nobreak(text.substring(0, i)));
		// actual trigrams
		for(int i=0; i<=text.length() - Q; i++)
			set.add(nobreak(text.substring(i, i + 3)));
		// tail
		for(int i=text.length() - Q + 1; i<text.length() && i>=0; i++)
			set.add(nobreak(text.substring(i)));

		list.addAll(set);
		
		return list;
	}

	private String nobreak(String substring) {
		return substring.replaceAll("\n", " ").replaceAll("\r", " ");
	}

}
