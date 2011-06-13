package org.dllearner.algorithm.tbsl.templator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.smu.tspell.wordnet.*;

public class WordNet {

	public String path = "tbsl/dict/";
	public WordNetDatabase database;
	
	public WordNet(String s) {
		path = s;
		
	}
	public WordNet() {
		path = this.getClass().getClassLoader().getResource(path).getPath();
	}
	
	public void setWordNetPath(String s) {
		path = s;
	}	
	
	public void init() {	
		System.setProperty("wordnet.database.dir",path);
		database = WordNetDatabase.getFileInstance();
	}
	

	public List<String> getBestSynonyms(String s) {
		
		List<String> synonyms = new ArrayList<String>();
		
		Synset[] synsets = database.getSynsets(s);
		if (synsets.length != 0) {
			String[] candidates = synsets[0].getWordForms();
			for (String c : candidates) {
				if (!c.equals(s) && !c.contains(" ") && synonyms.size() < 4) {
					synonyms.add(c);
				}
			}
		}
		return synonyms;
	}
	
	public List<String> getHypernyms(String s) {
		
		List<String> hypernyms = new ArrayList<String>();
		
		Synset[] synsets = database.getSynsets(s);
		Synset[] hypsets = {};
		for(int i = 0; i < synsets.length; i++){
			if(synsets[i].getType() == SynsetType.NOUN){
				hypsets = ((NounSynset)synsets[i]).getHypernyms();
			} else if(synsets[i].getType() == SynsetType.VERB){
				hypsets = ((VerbSynset)synsets[i]).getHypernyms();
			}
			for(Synset hypset : hypsets){
				hypernyms.addAll(Arrays.asList(hypset.getWordForms()));
			}
		}
		return hypernyms;
	}
	
	public List<String> getAttributes(String s) {
		
		List<String> result = new ArrayList<String>();
		
		Synset[] synsets = database.getSynsets(s);
		if (synsets.length > 0) {
			Synset synset = synsets[0];
			if (synset.getType().equals(SynsetType.ADJECTIVE)) {
				NounSynset[] attributes = ((AdjectiveSynset) synset).getAttributes();
				for (int i = 0; i < attributes.length; i++) {
					result.add(attributes[i].getWordForms()[0]);
				}
			}
		}
		
		return result;
	}
	
}
