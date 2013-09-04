package org.dllearner.algorithms.isle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNet {
	
	public Dictionary dict;	
	
	public WordNet() {
		try {
			JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public WordNet(String configPath) {
		try {
			JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream(configPath));
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public WordNet(InputStream propertiesStream) {
		try {
			JWNL.initialize(propertiesStream);
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getBestSynonyms(POS pos, String s) {
		
		List<String> synonyms = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				Word[] words = synsets[0].getWords();
				for(Word w : words){
					String c = w.getLemma();
					if (!c.equals(s) && !c.contains(" ") && synonyms.size() < 4) {
						synonyms.add(c);
					}
				}
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return synonyms;
	}
	
	public List<String> getSisterTerms(POS pos, String s){
		List<String> sisterTerms = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				//System.out.println(synsets[0]);
				PointerTarget[] pointerArr = synsets[0].getTargets();
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return sisterTerms;
	}
	
	public List<String> getAttributes(String s) {
		
		List<String> result = new ArrayList<String>();
		
		try {
			IndexWord iw = dict.getIndexWord(POS.ADJECTIVE, s);			
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				Word[] words = synsets[0].getWords();
				for(Word w : words){
					String c = w.getLemma();
					if (!c.equals(s) && !c.contains(" ") && result.size() < 4) {
						result.add(c);
					}
				}
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println(new WordNet().getBestSynonyms(POS.VERB, "learn"));
		System.out.println(new WordNet().getSisterTerms(POS.NOUN, "actress"));
	}
	
	/**
	 * Funktion returns a List of Hypo and Hypernyms of a given string 
	 * @param s Word for which you want to get Hypo and Hypersyms
	 * @return List of Hypo and Hypernyms
	 * @throws JWNLException
	 */
	public List<String> getRelatedNouns(String s) {
		List<String> result = new ArrayList<String>();
		IndexWord word = null;
		Synset sense=null;
		try{
			word=dict.getIndexWord(POS.NOUN,s);
			if(word!=null){
				sense = word.getSense(1);
				//Synset sense = word.getSense(1);
				
				PointerTargetNodeList relatedListHypernyms = null;
				PointerTargetNodeList relatedListHyponyms = null;
				try {
					relatedListHypernyms = PointerUtils.getInstance().getDirectHypernyms(sense);
				} catch (JWNLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					relatedListHyponyms = PointerUtils.getInstance().getDirectHyponyms(sense);
				} catch (JWNLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Iterator i = relatedListHypernyms.iterator();
				while (i.hasNext()) {
				  PointerTargetNode related = (PointerTargetNode) i.next();
				  Synset s1 = related.getSynset();
				  String tmp=(s1.toString()).replace(s1.getGloss(), "");
				  tmp=tmp.replace(" -- ()]","");
				  tmp=tmp.replaceAll("[0-9]","");
				  tmp=tmp.replace("[Synset: [Offset: ","");
				  tmp=tmp.replace("] [POS: noun] Words: ","");
				//its possible, that there is more than one word in a line from wordnet
				  String[] array_tmp=tmp.split(",");
				  for(String z : array_tmp) result.add(z.replace(" ", ""));
				}
				
				Iterator j = relatedListHyponyms.iterator();
				while (j.hasNext()) {
				  PointerTargetNode related = (PointerTargetNode) j.next();
				  Synset s1 = related.getSynset();
				  String tmp=(s1.toString()).replace(s1.getGloss(), "");
				  tmp=tmp.replace(" -- ()]","");
				  tmp=tmp.replaceAll("[0-9]","");
				  tmp=tmp.replace("[Synset: [Offset: ","");
				  tmp=tmp.replace("] [POS: noun] Words: ","");
				//its possible, that there is more than one word in a line from wordnet
				  String[] array_tmp=tmp.split(",");
				  for(String z : array_tmp) result.add(z.replace(" ", ""));
				}
			}
		}catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return result;
	}
	
}
