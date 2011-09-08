package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.StringParser;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

public class PosTaggerModelCreation {

	static int N_GRAM = 8;
    static int NUM_CHARS = 256;
    static double LAMBDA_FACTOR = 8.0;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// set up parser with estimator as handler
        HmmCharLmEstimator estimator
            = new HmmCharLmEstimator(N_GRAM,NUM_CHARS,LAMBDA_FACTOR);
        Parser<ObjectHandler<Tagging<String>>> parser = new BrownPosParser();
        parser.setHandler(estimator);

        // train on files in data directory 
        File dataDir = new File(args[0]);
        File[] files = dataDir.listFiles();
        for (File file : files) {
            System.out.println("Training file=" + file);
            parser.parse(file);
        }

        // write output to file
        File modelFile = new File(args[1]);
        AbstractExternalizable.compileTo(estimator,modelFile);
        
        InputStream fileIn = new FileInputStream(modelFile);
		ObjectInputStream objIn = new ObjectInputStream(fileIn);
		HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
		Streams.closeQuietly(objIn);
		HmmDecoder tagger = new HmmDecoder(hmm);
		
		String sentence = "Give me all soccer clubs in Premier League.";
		System.out.println(tagger.tag(Arrays.asList(IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(sentence.toCharArray(), 0, sentence.length()).tokenize())));
		sentence = "In which films did Julia Roberts as well as Richard Gere play?";
		System.out.println(tagger.tag(Arrays.asList(IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(sentence.toCharArray(), 0, sentence.length()).tokenize())));
		
	}
	
	static class BrownPosParser 
    extends StringParser<ObjectHandler<Tagging<String>>> {

	int questionCount = 0;
    @Override
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i){
            if (!Strings.allWhitespace(sentences[i])){
            	if(sentences[i].endsWith("?/.")){
            		questionCount++;
            		processSentence(sentences[i]);
            	}
            }
        }
        System.out.println(questionCount);
    }

    public String normalizeTag(String rawTag) {
        String tag = rawTag;
        String startTag = tag;
        // remove plus, default to first
        int splitIndex = tag.indexOf('+');
        if (splitIndex >= 0)
            tag = tag.substring(0,splitIndex);

        int lastHyphen = tag.lastIndexOf('-');
        if (lastHyphen >= 0) {
            String first = tag.substring(0,lastHyphen);
            String suffix = tag.substring(lastHyphen+1);
            if (suffix.equalsIgnoreCase("HL") 
                || suffix.equalsIgnoreCase("TL")
                || suffix.equalsIgnoreCase("NC")) {
                tag = first;
            }
        }

        int firstHyphen = tag.indexOf('-');
        if (firstHyphen > 0) {
            String prefix = tag.substring(0,firstHyphen);
            String rest = tag.substring(firstHyphen+1);
            if (prefix.equalsIgnoreCase("FW")
                || prefix.equalsIgnoreCase("NC")
                || prefix.equalsIgnoreCase("NP"))
                tag = rest;
        }

        // neg last, and only if not whole thing
        int negIndex = tag.indexOf('*');
        if (negIndex > 0) {
            if (negIndex == tag.length()-1)
                tag = tag.substring(0,negIndex);
            else
                tag = tag.substring(0,negIndex)
                    + tag.substring(negIndex+1);
        }
        // multiple runs to normalize
        return tag.equals(startTag) ? tag : normalizeTag(tag);
    }

    private void processSentence(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        List<String> tokenList = new ArrayList<String>(tagTokenPairs.length);
        List<String> tagList = new ArrayList<String>(tagTokenPairs.length);
    
        for (String pair : tagTokenPairs) {
            int j = pair.lastIndexOf('/');
            String token = pair.substring(0,j);
            String tag = normalizeTag(pair.substring(j+1)); 
            tokenList.add(token);
            tagList.add(tag);
        }
        Tagging<String> tagging
            = new Tagging<String>(tokenList,tagList);
        getHandler().handle(tagging);
    }
    
}
	
	

}
