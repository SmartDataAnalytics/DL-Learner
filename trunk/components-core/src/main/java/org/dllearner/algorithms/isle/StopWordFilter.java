/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.algorithms.isle.index.Annotation;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author Lorenz Buehmann
 *
 */
public class StopWordFilter {
	
	private Set<String> stopWords;
	private static final String stopWordfile = "src/main/resources/stopwords.txt";
	
	public StopWordFilter() {
		try {
			stopWords = new HashSet<String>(Files.readLines(new File(stopWordfile), Charsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String removeStopWords(String input) {
	    for (String s : stopWords) {
			input = input.replaceAll("\\b" + s + "\\b", "");
		}
	    return input;
	}
	
	public void removeStopWords(Set<String> words) {
	    words.removeAll(stopWords);
	}
	
	public void removeStopWordAnnotations(Set<Annotation> annotations) {
		for (Iterator<Annotation> iter = annotations.iterator(); iter.hasNext();) {
			Annotation annotation = iter.next();
			String content = annotation.getGetReferencedDocument().getContent();
			String token = content.substring(annotation.getOffset(), annotation.getOffset()+annotation.getLength());
			if(stopWords.contains(token)){
				iter.remove();
			}
		}
	}

}
