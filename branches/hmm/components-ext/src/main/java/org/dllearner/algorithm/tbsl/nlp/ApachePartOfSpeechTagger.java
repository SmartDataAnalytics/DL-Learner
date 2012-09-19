package org.dllearner.algorithm.tbsl.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Sequence;

import com.aliasi.tag.Tagging;

public class ApachePartOfSpeechTagger implements PartOfSpeechTagger{
	
	private POSTaggerME tagger;
	private static final String MODEL_PATH = "tbsl/models/en-pos-maxent.bin";
	
	private Tokenizer tokenizer;
	
	public ApachePartOfSpeechTagger() {
		
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream(MODEL_PATH);
		POSModel model = null;
		try {
		  model = new POSModel(modelIn);
		}
		catch (IOException e) {
		  // Model loading failed, handle the error
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		
		tagger = new POSTaggerME(model);
		
		tokenizer = new ApacheTokenizer();
	}
	
	@Override
	public String getName() {
		return "Apache Open NLP POS Tagger";
	}

	@Override
	public String tag(String sentence) {
		String[] tokens = tokenizer.tokenize(sentence);
		String[] tags =  tagger.tag(tokens);
		
		return convert2TaggedSentence(tokens, tags);
	}
	
	public List<String> getTags(String sentence){
		String[] tokens = tokenizer.tokenize(sentence);
		String[] tags =  tagger.tag(tokens);
		
		return Arrays.asList(tags);
	}

	@Override
	public List<String> tagTopK(String sentence) {
		List<String> taggedSentences = new ArrayList<String>();
		String[] tokens = tokenizer.tokenize(sentence);
		Sequence[] sequences = tagger.topKSequences(tokens);
		for(Sequence s : sequences){
			taggedSentences.add(convert2TaggedSentence(tokens, (String[])s.getOutcomes().toArray(new String[s.getOutcomes().size()])));
		}
		return taggedSentences;
	}
	
	@Override
	public Tagging<String> getTagging(String sentence){
		String[] tokens = tokenizer.tokenize(sentence);
		String[] tags =  tagger.tag(tokens);
		
		return new Tagging<String>(Arrays.asList(tokens), Arrays.asList(tags));
	}
	
	private String convert2TaggedSentence(String[] words, String[] tags){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < words.length; i++){
			sb.append(words[i]).append("/").append(tags[i]);
			if(i < words.length-1){
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}

}
