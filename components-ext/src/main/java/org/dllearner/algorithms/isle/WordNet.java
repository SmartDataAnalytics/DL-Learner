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

    private static final double SYNONYM_FACTOR = 0.8;
    private static final double HYPONYM_FACTOR = 0.4;
    public Dictionary dict;

    public WordNet() {
        try {
            JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
            dict = Dictionary.getInstance();
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public WordNet(String configPath) {
        try {
            JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream(configPath));
            dict = Dictionary.getInstance();
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public WordNet(InputStream propertiesStream) {
        try {
            JWNL.initialize(propertiesStream);
            dict = Dictionary.getInstance();
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new WordNet().getBestSynonyms(POS.VERB, "learn"));
        System.out.println(new WordNet().getSisterTerms(POS.NOUN, "actress"));
        System.out.println("Hypernyms **************************");
        System.out.println(new WordNet().getHypernyms(POS.NOUN, "man"));
        System.out.println("Hyponyms ****************************");
        System.out.println(new WordNet().getHyponyms(POS.NOUN, "god"));
        System.out.println("Words for first synset **************************");
        System.out.println(new WordNet().getWordsForFirstSynset(POS.NOUN, "man"));

    }

    public List<String> getBestSynonyms(POS pos, String s) {

        List<String> synonyms = new ArrayList<>();

        try {
            IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
            if (iw != null) {
                Synset[] synsets = iw.getSenses();
                Word[] words = synsets[0].getWords();
                for (Word w : words) {
                    String c = w.getLemma();
                    if (!c.equals(s) && !c.contains(" ") && synonyms.size() < 4) {
                        synonyms.add(c);
                    }
                }
            }

        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
        return synonyms;
    }

    /**
     * Returns the lemmas for the top {@code n} synsets of the given POS for the string {@code s}.
     *
     * @param pos the part of speech to retrieve synonyms for
     * @param s   the string to retrieve synonyms for
     * @param n   the number of synonyms to retrieve
     * @return list of the lemmas of the top n synonyms of s
     */
    public List<String> getTopSynonyms(POS pos, String s, int n) {

        List<String> synonyms = new ArrayList<>();

        try {
            IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
            if (iw != null) {
                Synset[] synsets = iw.getSenses();
                for (int i = 0; i < Math.min(n, synsets.length); i++) {
                    for (Word word : synsets[i].getWords()) {
                        String c = word.getLemma();
                        if (!c.equals(s) && !c.contains(" ")) {
                            synonyms.add(c);
                        }
                    }
                }
            }

        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
        return synonyms;
    }

    public List<String> getAllSynonyms(POS pos, String s) {
        List<String> synonyms = new ArrayList<>();
        try {
            IndexWord iw = dict.getIndexWord(pos, s);
            if (iw != null) {
                Synset[] synsets = iw.getSenses();
                for (Synset synset : synsets) {
                    for (Word w : synset.getWords()) {
                        String lemma = w.getLemma();
                        if (!lemma.equals(s) && !lemma.contains(" ")) {
                            synonyms.add(lemma);
                        }
                    }
                }
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }

        return synonyms;
    }

    public List<String> getSisterTerms(POS pos, String s) {
        List<String> sisterTerms = new ArrayList<>();

        try {
            IndexWord iw = dict.getIndexWord(pos, s);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
            if (iw != null) {
                Synset[] synsets = iw.getSenses();
                //System.out.println(synsets[0]);
                PointerTarget[] pointerArr = synsets[0].getTargets();
            }

        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
        return sisterTerms;
    }

    public List<String> getAttributes(String s) {

        List<String> result = new ArrayList<>();

        try {
            IndexWord iw = dict.getIndexWord(POS.ADJECTIVE, s);
            if (iw != null) {
                Synset[] synsets = iw.getSenses();
                Word[] words = synsets[0].getWords();
                for (Word w : words) {
                    String c = w.getLemma();
                    if (!c.equals(s) && !c.contains(" ") && result.size() < 4) {
                        result.add(c);
                    }
                }
            }

        }
        catch (JWNLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Returns a list of lemmas for the most frequent synset of the given word.
     * @param word word to get synonyms for
     * @param pos POS of the word to look up
     * @return list of lemmas of the most frequent synset
     */
    public List<String> getWordsForFirstSynset(POS pos, String word) {
        List<String> result = new ArrayList<>();
        IndexWord indexWord = null;
        Synset sense = null;

        try {
            indexWord = dict.getIndexWord(pos, word);
            sense = indexWord.getSense(1);
            for (Word w : sense.getWords()) {
                result.add(w.getLemma());
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    /**
     * Returns a list of words being lemmas of a most frequent synset for the given word or one of its hypernyms.
     */
    public List<String> getHypernyms(POS pos, String word) {
        List<String> result = new ArrayList<>();

        IndexWord indexWord;
        Synset sense;

        try {
            indexWord = dict.getIndexWord(pos, word);
            if (indexWord == null) {
                return result;
            }
            sense = indexWord.getSense(1);
            for (Word w : sense.getWords()) {
                result.add(w.getLemma());
            }
            PointerTargetNodeList target = PointerUtils.getInstance().getDirectHypernyms(sense);
            while (target != null && !target.isEmpty()) {
                for (Object aTarget : target) {
                    Synset s = ((PointerTargetNode) aTarget).getSynset();
                    for (Word w : sense.getWords()) {
                        result.add(w.getLemma());
                    }
                }
                target = PointerUtils.getInstance().getDirectHyponyms(((PointerTargetNode) target.get(0)).getSynset());
                System.out.println(target);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    public List<String> getHyponyms(POS pos, String s) {
        ArrayList<String> result = new ArrayList<>();
        try {
            IndexWord word = dict.getIndexWord(pos, s);
            if (word == null) {
                System.err.println("Unable to find index word for " + s);
                return result;
            }
            Synset sense = word.getSense(1);
            getHyponymsRecursive(result, sense, 3);
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    public void getHyponymsRecursive(List<String> lemmas, Synset sense, int depthToGo) {
        for (Word w : sense.getWords()) {
            lemmas.add(w.getLemma());
        }
        if (depthToGo == 0) {
            return;
        }
        try {
            PointerTargetNodeList directHyponyms = PointerUtils.getInstance().getDirectHyponyms(sense);
            for (Object directHyponym : directHyponyms) {
                getHyponymsRecursive(lemmas, ((PointerTargetNode) directHyponym).getSynset(), depthToGo - 1);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public List<LemmaScorePair> getHyponymsScored(POS pos, String s) {
        ArrayList<LemmaScorePair> result = new ArrayList<>();
        try {
            IndexWord word = dict.getIndexWord(pos, s);
            if (word == null) {
                System.err.println("Unable to find index word for " + s);
                return result;
            }
            Synset sense = word.getSense(1);
            getHyponymsScoredRecursive(result, sense, 3, SYNONYM_FACTOR);
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    public void getHyponymsScoredRecursive(List<LemmaScorePair> lemmas, Synset sense, int depthToGo, double score) {
        for (Word w : sense.getWords()) {
            lemmas.add(new LemmaScorePair(w.getLemma(), score));
        }
        if (depthToGo == 0) {
            return;
        }
        try {
            PointerTargetNodeList directHyponyms = PointerUtils.getInstance().getDirectHyponyms(sense);
            for (Object directHyponym : directHyponyms) {
                getHyponymsScoredRecursive(lemmas, ((PointerTargetNode) directHyponym).getSynset(), depthToGo - 1,
                        score * HYPONYM_FACTOR);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Funktion returns a List of Hypo and Hypernyms of a given string
     *
     * @param s Word for which you want to get Hypo and Hypersyms
     * @return List of Hypo and Hypernyms
     * @throws JWNLException
     */
    public List<String> getRelatedNouns(String s) {
        List<String> result = new ArrayList<>();
        IndexWord word = null;
        Synset sense = null;
        try {
            word = dict.getIndexWord(POS.NOUN, s);
            if (word != null) {
                sense = word.getSense(1);
                //Synset sense = word.getSense(1);

                PointerTargetNodeList relatedListHypernyms = null;
                PointerTargetNodeList relatedListHyponyms = null;
                try {
                    relatedListHypernyms = PointerUtils.getInstance().getDirectHypernyms(sense);
                }
                catch (JWNLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    relatedListHyponyms = PointerUtils.getInstance().getDirectHyponyms(sense);
                }
                catch (JWNLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Iterator i = relatedListHypernyms.iterator();
                while (i.hasNext()) {
                    PointerTargetNode related = (PointerTargetNode) i.next();
                    Synset s1 = related.getSynset();
                    String tmp = (s1.toString()).replace(s1.getGloss(), "");
                    tmp = tmp.replace(" -- ()]", "");
                    tmp = tmp.replaceAll("[0-9]", "");
                    tmp = tmp.replace("[Synset: [Offset: ", "");
                    tmp = tmp.replace("] [POS: noun] Words: ", "");
                    //its possible, that there is more than one word in a line from wordnet
                    String[] array_tmp = tmp.split(",");
                    for (String z : array_tmp) {
                        result.add(z.replace(" ", ""));
                    }
                }

                Iterator j = relatedListHyponyms.iterator();
                while (j.hasNext()) {
                    PointerTargetNode related = (PointerTargetNode) j.next();
                    Synset s1 = related.getSynset();
                    String tmp = (s1.toString()).replace(s1.getGloss(), "");
                    tmp = tmp.replace(" -- ()]", "");
                    tmp = tmp.replaceAll("[0-9]", "");
                    tmp = tmp.replace("[Synset: [Offset: ", "");
                    tmp = tmp.replace("] [POS: noun] Words: ", "");
                    //its possible, that there is more than one word in a line from wordnet
                    String[] array_tmp = tmp.split(",");
                    for (String z : array_tmp) {
                        result.add(z.replace(" ", ""));
                    }
                }
            }
        }
        catch (JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public static class LemmaScorePair implements Comparable<LemmaScorePair> {
        private String lemma;
        private Double score;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LemmaScorePair that = (LemmaScorePair) o;

            if (lemma != null ? !lemma.equals(that.lemma) : that.lemma != null) {
                return false;
            }
            if (score != null ? !score.equals(that.score) : that.score != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = lemma != null ? lemma.hashCode() : 0;
            result = 31 * result + (score != null ? score.hashCode() : 0);
            return result;
        }

        public String getLemma() {

            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public LemmaScorePair(String lemma, Double score) {

            this.lemma = lemma;
            this.score = score;
        }

        @Override
        public int compareTo(LemmaScorePair o) {
            int val = score.compareTo(o.score);

            if (val == 0) {
                val = lemma.compareTo(o.getLemma());
            }

            return val;
        }
    }

}
