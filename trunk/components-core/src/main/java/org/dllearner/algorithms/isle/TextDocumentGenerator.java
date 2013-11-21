package org.dllearner.algorithms.isle;

import java.util.List;
import java.util.Properties;

import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.Token;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class TextDocumentGenerator {

	private static TextDocumentGenerator instance;
	private StanfordCoreNLP pipeline;
	
	private TextDocumentGenerator(){
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public static synchronized TextDocumentGenerator getInstance(){
		if(instance == null){
			instance = new TextDocumentGenerator();
		}
		return instance;
	}

	public TextDocument tag(String text) {
		TextDocument document = new TextDocument();
	    // create an empty Annotation just with the given text
	    Annotation annotatedDocument = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(annotatedDocument);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel label: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = label.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = label.get(PartOfSpeechAnnotation.class);
	            //this is the POS tag of the token
	            String lemma = label.get(LemmaAnnotation.class);
	           
	            Token token = new Token(word);
	            token.setPOSTag(pos);
	            token.setStemmedForm(lemma);
	            document.add(token);
	          }
	    }
		
		return document;
	}
}
