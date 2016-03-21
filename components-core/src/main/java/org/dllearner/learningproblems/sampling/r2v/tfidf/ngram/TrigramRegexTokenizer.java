package org.dllearner.learningproblems.sampling.r2v.tfidf.ngram;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class TrigramRegexTokenizer extends RegularExpressionTokenizer {

	@Override
	public List<String> tokenize(String text) {
		List<String> list = new ArrayList<>();
		// head
		for(int i=1; i<3; i++)
			list.add(text.substring(0, i));
		// actual trigrams
		for(int i=0; i<=text.length() - 3; i++)
			list.add(text.substring(i, i + 3));
		// tail
		for(int i=text.length() - 2; i<text.length(); i++)
			list.add(text.substring(i));
		
		// add from regex
		list.addAll(super.tokenize(text));
		
		return list;
	}

}
