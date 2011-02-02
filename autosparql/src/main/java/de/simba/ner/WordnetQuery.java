/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.simba.ner;

import java.util.TreeSet;
import java.util.ArrayList;
import edu.smu.tspell.wordnet.*;

/**
 *
 * @author ngonga
 */
public class WordnetQuery {

    public WordnetQuery(String dictionary)
    {
        System.setProperty("wordnet.database.dir", dictionary);
    }

    public TreeSet<String> getSynset(String word) {
        TreeSet<String> synset = new TreeSet<String>();
        NounSynset nounSynset;
        NounSynset[] hyponyms;

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
        for (int i = 0; i < synsets.length; i++) {
            nounSynset = (NounSynset) (synsets[i]);
            for(int j=0; j < nounSynset.getWordForms().length; j++)
            {
                synset.add(nounSynset.getWordForms()[j].toLowerCase());
            }
        }
        System.out.println(synset);
        return synset;
    }

    public ArrayList<String> getSynsetSorted(String word) {
        ArrayList<String> synset = new ArrayList<String>();
        NounSynset nounSynset;
        NounSynset[] hyponyms;

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
        for (int i = 0; i < synsets.length; i++) {
            nounSynset = (NounSynset) (synsets[i]);
            for(int j=0; j < nounSynset.getWordForms().length; j++)
            {
                if(!synset.contains(nounSynset.getWordForms()[j].toLowerCase()))
                synset.add(nounSynset.getWordForms()[j].toLowerCase());
            }
        }
        System.out.println(synset);
        return synset;
    }

    public static void main(String args[])
    {
        WordnetQuery wnq = new WordnetQuery("D:\\Work\\Tools\\WordNetDict");
        wnq.getSynset("book");
        wnq.getSynsetSorted("book");
    }
}
