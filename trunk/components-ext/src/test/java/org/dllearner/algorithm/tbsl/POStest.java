package org.dllearner.algorithm.tbsl;

import java.io.IOException;
import java.util.List;

import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;

public class POStest {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		String sentence = "When did Nirvana record Nevermind?";
		
		PartOfSpeechTagger tagger = new StanfordPartOfSpeechTagger();
		long startTime = System.currentTimeMillis();
		String tagged = tagger.tag(sentence);
		System.out.format("Tagged sentence with Stanford tagger (%d ms):\n", System.currentTimeMillis()-startTime);
		System.out.println(tagged + "\n");
		
		
		tagger = new ApachePartOfSpeechTagger();
		startTime = System.currentTimeMillis();
		tagged = tagger.tag(sentence);
		System.out.format("Tagged sentence with Apache OpenNLP (%d ms):\n", System.currentTimeMillis()-startTime);
		startTime = System.currentTimeMillis();
		System.out.println(tagged + "\n");
		
		
		startTime = System.currentTimeMillis();
		List<String> topKTaggedSentences = tagger.tagTopK(sentence);
		System.out.format("Top k tags with Apache OpenNLP (%d ms):\n", System.currentTimeMillis()-startTime);
		for(String t : topKTaggedSentences){
			System.out.println(t);
		}
	}

}
