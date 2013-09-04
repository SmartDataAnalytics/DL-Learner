package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Jens Lehmann
 * 
 */
public class SimpleLinguisticAnnotator implements LinguisticAnnotator {

	@Override
	public Set<Annotation> annotate(Document document) {
		String s = document.getRawContent().trim();
		Set<Annotation> annotations = new HashSet<Annotation>();
		Pattern pattern = Pattern.compile("\\u0020+");
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
		return annotations;
	}

}
