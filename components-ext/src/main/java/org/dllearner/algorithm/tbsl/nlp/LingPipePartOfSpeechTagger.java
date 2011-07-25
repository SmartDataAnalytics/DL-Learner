package org.dllearner.algorithm.tbsl.nlp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.Streams;

public class LingPipePartOfSpeechTagger implements PartOfSpeechTagger{
	
	private static final String MODEL_PATH = "tbsl/models/lingpipe/pos-en-general-brown.HiddenMarkovModel";
	
	private static final int TOP_K = 5;
	
	private HmmDecoder tagger;

	public LingPipePartOfSpeechTagger() {
		try {
			InputStream fileIn = this.getClass().getClassLoader().getResourceAsStream(MODEL_PATH);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeQuietly(objIn);
			tagger = new HmmDecoder(hmm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public String tag(String sentence) {
		com.aliasi.tokenizer.Tokenizer tokenizer = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(sentence.toCharArray(), 0, sentence.length());
//		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
	    String[] tokens = tokenizer.tokenize();
	    List<String> tokenList = Arrays.asList(tokens);
		Tagging<String> tagging = tagger.tag(tokenList);
		
		return tagging.toString();
	}

	@Override
	public List<String> tagTopK(String sentence) {
		List<String> taggedSentences = new ArrayList<String>();
		
		com.aliasi.tokenizer.Tokenizer tokenizer = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(sentence.toCharArray(), 0, sentence.length());
//		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
	    String[] tokens = tokenizer.tokenize();
	    List<String> tokenList = Arrays.asList(tokens);
		Iterator<ScoredTagging<String>> taggingIter = tagger.tagNBest(tokenList, TOP_K);
		while(taggingIter.hasNext()){
			taggedSentences.add(taggingIter.next().toString());
		}
		return taggedSentences;
	}

}
