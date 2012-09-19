/**
 * 
 */
package org.dllearner.algorithm.tbsl.diadem;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Word implements Comparable<Word> {

    public boolean isFromWikipedia() {

        return isFromWikipedia;
    }

    private String word;
    private int frequency;
    private boolean isFromWikipedia; // Is that term extracted from a Wikipedia
                                     // article

    public Word(String word, int frequency, boolean fromWikipedia) {

        isFromWikipedia = fromWikipedia;
        this.word = word;
        this.frequency = frequency;
    }

    public Word(String word, int count) {

        this(word, count, false);
    }

    /**
     * Increases the total frequency with 1
     * 
     * @return The new frequency
     */
    public int incrementFrequency() {

        return ++frequency;
    }

    public int compareTo(Word otherWord) {

        if (this.frequency == otherWord.frequency) {
            return this.word.compareTo(otherWord.word);
        }
        return otherWord.frequency - this.frequency;
    }

    public String getWord() {

        return word;
    }

    public int getFrequency() {

        return frequency;
    }

    @Override
    public String toString() {

        return word;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((word == null) ? 0 : word.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Word other = (Word) obj;
        if (word == null) {
            if (other.word != null)
                return false;
        }
        else
            if (!word.equals(other.word))
                return false;
        return true;
    }

    public Word setFrequency(int i) {

        this.frequency = i;
        return this;
    }
}
