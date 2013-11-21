package org.dllearner.algorithms.isle;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordPartOfSpeechTagger {

	private static StanfordPartOfSpeechTagger instance;
	private StanfordCoreNLP pipeline;
	
	private StanfordPartOfSpeechTagger(){
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public static synchronized StanfordPartOfSpeechTagger getInstance(){
		if(instance == null){
			instance = new StanfordPartOfSpeechTagger();
		}
		return instance;
	}

	public String tag(String text) {
		String out = "";
		
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = token.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = token.get(PartOfSpeechAnnotation.class);
	           
	            out += " " + word + "/" + pos;
	          }
	    }
		
		return out.trim();
	}
}
