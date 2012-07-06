package org.dllearner.algorithm.tbsl.diadem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PatternAnalyzer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordFrequencyCounter {

    private List<String> stopwords = new ArrayList<String>();
    public WordFrequencyCounter(){
        
//        stopwords.addAll(Arrays.asList());
    }

    /**
     * 
     * @param inputWords
     * @return
     */
    public ArrayList<Word> getKeywordsSortedByFrequency(String inputWords){

        PatternAnalyzer keywordAnalyzer     = PatternAnalyzer.EXTENDED_ANALYZER;
        TokenStream pageTokens              = keywordAnalyzer.tokenStream("", inputWords);
        CharTermAttribute charTermAttribute = pageTokens.getAttribute(CharTermAttribute.class);
        ArrayList<String> tokens            = new ArrayList<String>(1000);

        ShingleFilter filter = new ShingleFilter(pageTokens, 2, 3);
        
        try{
            
            while (filter.incrementToken()) {
                
                // we need to filter these stop words, mostly references in wikipedia
                String token = charTermAttribute.toString();
                if ( token.length() > 2 && !stopwords.contains(token) ) tokens.add(token.trim());
            }
        }
        catch (IOException exp){
            
            exp.printStackTrace();
        }

        HashMap<String,Word> map = new HashMap<String,Word>();
        for(String token : tokens){
            
            Word word = map.get(token);
            if ( word == null ) {
                
                word = new Word(token,1);
                map.put(token, word);
            }
            else word.incrementFrequency();
        }
        // sort the values by there frequency and return them
        ArrayList<Word> sortedKeywordList = new ArrayList<Word>(map.values());
        Collections.sort(sortedKeywordList);
        
        Iterator<Word> wordsIterator = sortedKeywordList.iterator();
        while ( wordsIterator.hasNext() ) {
            
            Word word = wordsIterator.next();
            if ( word.getFrequency() <= 10 ) wordsIterator.remove(); 
        }
        
        return sortedKeywordList;
    }
}
