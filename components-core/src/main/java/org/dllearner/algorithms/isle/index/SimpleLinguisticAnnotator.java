package org.dllearner.algorithms.isle.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.dllearner.algorithms.isle.StopWordFilter;

/**
 * 
 * @author Jens Lehmann
 * 
 */
public class SimpleLinguisticAnnotator implements LinguisticAnnotator {
	
	private StopWordFilter stopWordFilter = new StopWordFilter();
    NGramGeneratingAnnotator nGramAnnotator = new NGramGeneratingAnnotator(2);

	@Override
	public Set<Annotation> annotate(Document document) {
		String s = document.getContent().trim();
		System.out.println("Document:" + s);
//		s = stopWordFilter.removeStopWords(s);
		Set<Annotation> annotations = new HashSet<Annotation>();
		Pattern pattern = Pattern.compile("(\\u0020)+");
		Matcher matcher = pattern.matcher(s);
		// Check all occurrences
		int start = 0;
		while (matcher.find()) {
			int end = matcher.start();
			annotations.add(new Annotation(document, start, end - start));
			start = matcher.end();
		}
		if(start < s.length()-1){
			annotations.add(new Annotation(document, start, s.length() - start));
		}
        annotations.addAll(nGramAnnotator.annotate(document));
//		stopWordFilter.removeStopWordAnnotations(annotations);
		return annotations;
	}
	
	public static void main(String[] args) throws Exception {
		String s = "male person    least 1 child";
		Pattern pattern = Pattern.compile("(\\u0020)+");
		Matcher matcher = pattern.matcher(s);
		int start = 0;
		while (matcher.find()) {
			int end = matcher.start();
			System.out.println(s.substring(start, end));
			start = matcher.end();
		}
	}

}
