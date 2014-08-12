package org.dllearner.algorithms.isle;

import java.util.ArrayList;
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
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class TextDocumentGenerator {

	private static TextDocumentGenerator instance;
	
	private StanfordCoreNLP pipeline;
	private StanfordCoreNLP pipelineSimple;
	private final String punctuationPattern = "\\p{Punct}";
	private final StopWordFilter stopWordFilter = new StopWordFilter();
	
	private TextDocumentGenerator(){
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");//, pos, lemma, parse");
	    pipelineSimple = new StanfordCoreNLP(props);
	    
	    props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public static synchronized TextDocumentGenerator getInstance(){
		if(instance == null){
			instance = new TextDocumentGenerator();
		}
		return instance;
	}

	public TextDocument generateDocument(String text) {
		return generateDocument(text, false);
	}
	
	public TextDocument generateDocument(String text, boolean determineHead) {
		TextDocument document = new TextDocument();
	    // create an empty Annotation just with the given text
	    Annotation annotatedDocument = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(annotatedDocument);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	
	    	//determine the head noun
	    	String head = null;
	    	if(determineHead){
	    		//if phrase only contains one single token, the task is trivial
	    		if(sentence.get(TokensAnnotation.class).size() == 1){
	    			head = sentence.get(TokensAnnotation.class).get(0).get(TextAnnotation.class);
	    		} else {
	    			Tree tree = sentence.get(TreeAnnotation.class);
		            CollinsHeadFinder headFinder = new CollinsHeadFinder();
//		            Tree head = headFinder.determineHead(tree);
//		            System.out.println(sentence);
//		            System.out.println(tree.headTerminal(headFinder));
		            head = tree.headTerminal(headFinder).toString();
		            
//		            // Create a reusable pattern object 
//		            TregexPattern patternMW = TregexPattern.compile("__ >># NP"); 
//		            // Run the pattern on one particular tree 
//		            TregexMatcher matcher = patternMW.matcher(tree); 
//		            // Iterate over all of the subtrees that matched 
//		            while (matcher.findNextMatchingNode()) { 
//		              Tree match = matcher.getMatch(); 
//		              // do what we want to with the subtree 
//		            }
	    		}
	    	}
           
	    	for (CoreLabel label: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = label.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = label.get(PartOfSpeechAnnotation.class);
	            //this is the POS tag of the token
	            String lemma = label.get(LemmaAnnotation.class);
	            //check if token is punctuation
	            boolean isPunctuation = word.matches(punctuationPattern) 
	            		|| (pos != null && (pos.equalsIgnoreCase("-lrb-") || pos.equalsIgnoreCase("-rrb-")))
	            		|| word.startsWith("'")
	            		;
	            //check if it is a stop word
	            boolean isStopWord = stopWordFilter.isStopWord(word.toLowerCase());
	           
	            Token token = new Token(word, lemma, pos, isPunctuation, isStopWord);
	            
	            if(determineHead && word.equals(head)){
	            	token.setIsHead(true);
	            }
	            
	            document.add(token);
	          }
	    }
		
		return document;
	}
	
	public List<String> generateDocumentSimple(String text) {
		List<String> tokens = new ArrayList<>();
		
	    // create an empty Annotation just with the given text
	    Annotation annotatedDocument = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipelineSimple.annotate(annotatedDocument);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	
           
	    	for (CoreLabel label: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = label.get(TextAnnotation.class);
	            
	            tokens.add(word);
	        }
	    }
		
		return tokens;
	}
	
	public static void main(String[] args) throws Exception {
		TextDocument document = TextDocumentGenerator.getInstance().generateDocument("And he said, Amos, what seest thou? And I said, A basket of summer fruit. Then said the LORD unto me, The end is come upon my people of Israel; I will not again pass by them any more. ");
		System.out.println(document);
	}
}
