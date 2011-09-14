package org.dllearner.algorithm.tbsl.templator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import edu.smu.tspell.wordnet.*;

public class WordNet {

	private WordNetDatabase database;
	
	private String[] noun = {"NN","NNS","NNP","NNPS","NPREP","JJNN","JJNPREP"};
	private String[] adjective = {"JJ","JJR","JJS","JJH"};
	private String[] verb = {"VB","VBD","VBG","VBN","VBP","VBZ","PASSIVE","PASSPART","VPASS","VPASSIN","GERUNDIN","VPREP","WHEN","WHERE"};
	private String[] preps = {"IN","TO"};
	
	public WordNet() {
		System.setProperty("wordnet.database.dir", System.getProperty("user.dir") + "/src/main/resources/tbsl/dict/");
		database = WordNetDatabase.getFileInstance();
	}
	
	public Set<String> getBestSynonyms(String s,String pos) {
		
		Set<String> synonyms = new HashSet<String>();
		
		SynsetType type = null;
		if (equalsOneOf(pos,noun)) {
			type = SynsetType.NOUN;
		}
		else if (equalsOneOf(pos,adjective)) {
			type = SynsetType.ADJECTIVE;
		}
		else if (equalsOneOf(pos,verb)) {
			type = SynsetType.VERB;
		}
		
		String[] basecandidates;
		if (type != null) {
			String[] bfc = database.getBaseFormCandidates(s,type);
			basecandidates = new String[bfc.length + 1];
			basecandidates[0] = s;
			for (int i = 0; i < bfc.length; i++) {
				basecandidates[i+1] = bfc[i];
			}
		}
		else {
			basecandidates = new String[1];
			basecandidates[0] = s;
		}
		
		for (String b : basecandidates) {
			Synset[] synsets = database.getSynsets(b);
			if (synsets.length != 0) {
				String[] candidates = synsets[0].getWordForms();
				for (String c : candidates) {
					if (!c.equals(b) && !c.contains(" ") && synonyms.size() < 4) {
						synonyms.add(c);
					}
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
	
	private boolean equalsOneOf(String string,String[] strings) {
		for (String s : strings) {
			if (string.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
}
