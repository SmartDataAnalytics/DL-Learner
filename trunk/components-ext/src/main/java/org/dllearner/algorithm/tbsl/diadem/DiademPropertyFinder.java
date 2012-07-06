package org.dllearner.algorithm.tbsl.diadem;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.util.StringUtils;

/**
 * 
 */
public class DiademPropertyFinder {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        List<String> lines = FileUtils.readLines(new File("/Users/gerb/Development/workspaces/experimental/diadem/descriptions.txt"));
        String allDEscriptions = StringUtils.join(lines, " ");
        
        WordFrequencyCounter wfc = new WordFrequencyCounter();
        for ( Word word : wfc.getKeywordsSortedByFrequency(allDEscriptions)) {
            
            System.out.println(word.getWord() + ":\t" + word.getFrequency());
        }
    }
}
