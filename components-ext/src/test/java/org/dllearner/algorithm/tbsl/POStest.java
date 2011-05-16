package org.dllearner.algorithm.tbsl;

import java.io.IOException;
import java.util.List;

import org.annolab.tt4j.TreeTaggerException;
import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.TreeTagger;

public class POStest {

	public static void main(String[] args) throws IOException, ClassNotFoundException, TreeTaggerException {
		
		String sentence = "Which rivers does the Brooklyn Bridge cross ?";
		
		PartOfSpeechTagger tagger = new StanfordPartOfSpeechTagger();
		long startTime = System.currentTimeMillis();
		String tagged = tagger.tag(sentence);
		System.out.format("Tagged sentence with Stanford tagger (%d ms):\n", System.currentTimeMillis()-startTime);
		System.out.println(tagged + "\n");
	
		TreeTagger tt = new TreeTagger(); 
		tt.tag(sentence);
		
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
