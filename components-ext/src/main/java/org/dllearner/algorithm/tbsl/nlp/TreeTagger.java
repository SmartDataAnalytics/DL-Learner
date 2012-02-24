package org.dllearner.algorithm.tbsl.nlp;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import com.aliasi.tag.Tagging;

public class TreeTagger implements PartOfSpeechTagger {

	TreeTaggerWrapper<String> tt;
	
	private String tagging;
	
	public TreeTagger() throws IOException {
		System.setProperty("treetagger.home","/home/lorenz/Downloads/TreeTagger");
		tt = new TreeTaggerWrapper<String>();
		tt.setModel(this.getClass().getClassLoader().getResource("tbsl/models/treetagger/english.par").getPath());
	}
	
	public String tag(String s) {
		tagging = "";
		List<String> input = Arrays.asList(s.split(" "));		
		try {
		     tt.setHandler(new TokenHandler<String>() {
		         public void token(String token, String pos, String lemma) {
		             tagging += token+"/"+pos + " ";
		         }
		     });
		     tt.process(input);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			e.printStackTrace();
		}
		
		return tagging.trim();
	}
	
	public void close(){
		tt.destroy();
	}
	
	@Override
	public String getName() {
		return "Tree Tagger";
	}
	
	@Override
	public Tagging<String> getTagging(String sentence) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> tagTopK(String sentence) {
		return Collections.singletonList(tag(sentence));
	}
}
