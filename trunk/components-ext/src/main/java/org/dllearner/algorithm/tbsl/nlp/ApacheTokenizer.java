package org.dllearner.algorithm.tbsl.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ApacheTokenizer implements Tokenizer{
	
	private opennlp.tools.tokenize.Tokenizer tokenizer;
	private static final String MODEL_FILE = "src/main/resources/tbsl/models/en-token.bin";
	
	public ApacheTokenizer() {
		InputStream modelIn = null;
		TokenizerModel model = null;
		try {
			modelIn = new FileInputStream(MODEL_FILE);
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
