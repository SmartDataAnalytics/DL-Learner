package org.dllearner.autosparql.server.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class QuestionProcessor {
	
	private final Logger logger = Logger.getLogger(QuestionProcessor.class);
	
	private MaxentTagger tagger;
	private final List<String> stopWords = Arrays.asList(
		      "a", "all", "an", "and", "are", "as", "at", "be", "but", "by", "do",
		      "for", "has", "have", "he",  "if", "in", "into", "is", "it", "me",
		      "no", "not", "of", "on", "or", "she", "such",
		      "that", "the", "their", "then", "there", "these",
		      "they", "this", "to", "was", "were", "which", "will", "with"
		    );
	
	public QuestionProcessor(){
		try {
			tagger = new MaxentTagger(this.getClass().getClassLoader().getResource("de/simba/ner/models/left3words-wsj-0-18.tagger").getPath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public List<String> getRelevantWords(String question){
		logger.info("Processing question \"" + question + "\"...");
		//tokenize question
		List<String> words = getWords(question);
		logger.info("Extracted words: " + words);
		//remove stop words
		removeStopWords(words);
		logger.info("After removed stop words: " + words);
		//stem words
		words = getStemmedWords(words);
		removeStopWords(words);
		logger.info("After stemming: " + words);
		
		
		return words;
	}
	
	private List<String> getWords(String question){
		List<String> words = new ArrayList<String>();
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(question)));
		for (List<HasWord> sentence : sentences) {
		    ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);System.out.println(tSentence);
		    String nounPhrase = "";
		    boolean firstWord = true;
		    String phraseTag = "";
		    for(TaggedWord tWord : tSentence){
		    	//ignore first word if it is a verb
		    	if(firstWord){
		    		if(tWord.tag().startsWith("V")){
		    			continue;
		    		}
		    		firstWord = false;
		    	}
		    	if(tWord.tag().startsWith("NNP")){
		    		if(phraseTag.equals("NN")){
		    			if(!nounPhrase.isEmpty()){
			    			words.add(nounPhrase.trim());
			    			nounPhrase = "";
			    		}
		    		} 
		    		phraseTag = "NNP";
		    		nounPhrase += " " + tWord.word();
		    	} else if(tWord.tag().equals("NN") || tWord.tag().equals("NNS")){
		    		if(phraseTag.equals("NNP")){
		    			if(!nounPhrase.isEmpty()){
			    			words.add(nounPhrase.trim());
			    			nounPhrase = "";
			    		}
		    		} 
		    		phraseTag = "NN";
		    		nounPhrase += " " + tWord.word();
		    	} else {
		    		if(!nounPhrase.isEmpty()){
		    			words.add(nounPhrase.trim());
		    			nounPhrase = "";
		    		}
		    		//ignore punctuation signs
		    		if(!tWord.tag().equals(".")){
		    			words.add(tWord.word());
		    		}
		    	}
//		    	//if words belongs to noun phrase treat them as one single term
//		    	if(tWord.tag().equals("NNP") || tWord.tag().startsWith("NN")){
//		    		nounPhrase += " " + tWord.word();
//		    	} else {
//		    		if(!nounPhrase.isEmpty()){
//		    			words.add(nounPhrase.trim());
//		    			nounPhrase = "";
//		    		}
//		    		//ignore punctuation signs
//		    		if(!tWord.tag().equals(".")){
//		    			words.add(tWord.word());
//		    		}
//		    	}
		    	
		    }
		    if(!nounPhrase.isEmpty()){
    			words.add(nounPhrase.trim());
    			nounPhrase = "";
    		}
		}
		return words;
	}
	
	private void removeStopWords(List<String> words){
		words.removeAll(stopWords);
	}
	
	private List<String> getStemmedWords(List<String> words) {
		List<String> stemmedWords = new ArrayList<String>();
        Morphology morpho = new Morphology();
        for (String w : words) {
        	if(!(w.indexOf(" ") > 0) && !Character.isUpperCase(w.charAt(0))){
        		stemmedWords.add(morpho.stem(w));
        	} else {
        		stemmedWords.add(w);
        	}
        }
        return stemmedWords;
    }

}
