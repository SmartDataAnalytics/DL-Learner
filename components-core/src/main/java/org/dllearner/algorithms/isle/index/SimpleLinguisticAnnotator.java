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
		String s = document.getRawContent();
		Set<Annotation> annotations = new HashSet<Annotation>();
		Pattern pattern = Pattern.compile(" ");
		Matcher matcher = pattern.matcher(s);
		// Check all occurrences
		while (matcher.find()) {
			annotations.add(new Annotation(document, matcher.start(), 
					matcher.end() - matcher.start()));
		}
		return annotations;
	}

}
