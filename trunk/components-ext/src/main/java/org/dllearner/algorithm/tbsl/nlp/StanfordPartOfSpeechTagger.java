package org.dllearner.algorithm.tbsl.nlp;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aliasi.tag.Tagging;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordPartOfSpeechTagger implements PartOfSpeechTagger{

	private static final String MODEL = "tbsl/models/bidirectional-distsim-wsj-0-18.tagger";
	
	private MaxentTagger tagger;
	
	public StanfordPartOfSpeechTagger(){
		try {
//			String modelPath = this.getClass().getClassLoader().getResource(MODEL).getPath();
			String modelPath = getClass().getResource("/tbsl/models/bidirectional-distsim-wsj-0-18.tagger").getPath(); 
//			String modelPath = Thread.currentThread().getContextClassLoader().getResource(MODEL).getFile();
			tagger = new MaxentTagger(modelPath);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getName() {
		return "Stanford POS Tagger";
	}

	@Override
	public String tag(String sentence) {
		String out = "";
		
		ArrayList<TaggedWord> tagged = new ArrayList<TaggedWord>(); 
		
		StringReader reader = new StringReader(sentence);
		List<List<HasWord>> text = MaxentTagger.tokenizeText(reader);
			
		if (text.size() == 1) {
			tagged = tagger.tagSentence(text.get(0));
		}
		
		for (TaggedWord t : tagged) {
			out += " " + t.toString();
		}
		return out.trim();
	}

	@Override
	public List<String> tagTopK(String sentence) {
		return Collections.singletonList(tag(sentence));
	}
	
	public List<String> getTags(String sentence){
		List<String> tags = new ArrayList<String>();
		
		ArrayList<TaggedWord> tagged = new ArrayList<TaggedWord>(); 
		
		StringReader reader = new StringReader(sentence);
		List<List<HasWord>> text = MaxentTagger.tokenizeText(reader);
			
		if (text.size() == 1) {
			tagged = tagger.tagSentence(text.get(0));
		}
		
		for(TaggedWord tW : tagged){
			tags.add(tW.tag());
		}
		
		return tags;
	}
	
	@Override
	public Tagging<String> getTagging(String sentence){
		ArrayList<TaggedWord> tagged = new ArrayList<TaggedWord>(); 
		
		StringReader reader = new StringReader(sentence);
		List<List<HasWord>> text = MaxentTagger.tokenizeText(reader);
			
		if (text.size() == 1) {
			tagged = tagger.tagSentence(text.get(0));
		}
		
		List<String> tokenList = new ArrayList<String>();
		List<String> tagList = new ArrayList<String>();
		
		for(TaggedWord tW : tagged){
			tokenList.add(tW.word());
			tagList.add(tW.tag());
		}
		
		return new Tagging<String>(tokenList, tagList);
	}

}
