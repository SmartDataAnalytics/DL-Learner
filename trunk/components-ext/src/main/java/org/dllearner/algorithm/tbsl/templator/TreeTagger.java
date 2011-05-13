package org.dllearner.algorithm.tbsl.templator;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

public class TreeTagger {

	TreeTaggerWrapper<String> tt;
	
	public TreeTagger() throws IOException {
		System.setProperty("treetagger.home","/home/christina/Software/TreeTagger");
		tt = new TreeTaggerWrapper<String>();
		tt.setModel("/home/christina/Software/TreeTagger/lib/english.par:iso8859-1");
	}
	
	public void tagthis(String s) throws IOException, TreeTaggerException {
		
		List<String> input = Arrays.asList(s.split(" "));		
		try {
		     tt.setHandler(new TokenHandler<String>() {
		         public void token(String token, String pos, String lemma) {
		             System.out.println(token+"/"+pos+"/"+lemma);
		         }
		     });
		     System.out.println("Tagged with TreeTagger:\n");
		     tt.process(input);
		     System.out.println(tt.getStatus());
		}
		 finally {
		     tt.destroy();
		 }
	}
}
