package org.dllearner.algorithm.tbsl.templator;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POStagger {
	
	String taggermodel;
	MaxentTagger tagger;
	
	public POStagger(String s) throws IOException, ClassNotFoundException {
		taggermodel = s;
		tagger = new MaxentTagger(taggermodel);
	}
	public POStagger() throws IOException, ClassNotFoundException {
		taggermodel = "src/main/resources/tbsl/models/bidirectional-distsim-wsj-0-18.tagger";
		tagger = new MaxentTagger(taggermodel);
	}
	
	public void setPOStaggerModel(String s) throws IOException, ClassNotFoundException {
		taggermodel = s;
		tagger = new MaxentTagger(taggermodel);
	}
	
	public String tag(String s) {
		
		String out = "";
		
		ArrayList<TaggedWord> tagged = new ArrayList<TaggedWord>(); 
		
		StringReader reader = new StringReader(s);
		List<ArrayList<? extends HasWord>> text = tagger.tokenizeText(reader);
			
		if (text.size() == 1) {
			tagged = tagger.processSentence(text.get(0));
		}

		
		for (TaggedWord t : tagged) {
			out += " " + t.toString();
		}
		
		return out.trim();

	}

}
