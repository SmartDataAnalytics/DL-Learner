package org.dllearner.algorithm.tbsl.nlp;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ApacheTokenizer implements Tokenizer{
	
	private opennlp.tools.tokenize.Tokenizer tokenizer;
	private static final String MODEL_PATH = "tbsl/models/en-token.bin";
	
	public ApacheTokenizer() {
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream(MODEL_PATH);
		TokenizerModel model = null;
		try {
		  model = new TokenizerModel(modelIn);
		}
		catch (IOException e) {
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
		tokenizer = new TokenizerME(model);
	}

	@Override
	public String[] tokenize(String sentence) {
		return tokenizer.tokenize(sentence);
	}

}
