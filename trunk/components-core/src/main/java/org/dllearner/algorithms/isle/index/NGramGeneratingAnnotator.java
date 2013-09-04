package org.dllearner.algorithms.isle.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Generates word n-grams
 * @author Daniel Fleischhacker
 */
public class NGramGeneratingAnnotator implements LinguisticAnnotator {
    private int length;

    /**
     * Initializes the annotator to generate word n-grams of the given length ({@code length} words per n-gram)
     * @param length length of the single n-grams
     */
    public NGramGeneratingAnnotator(int length) {
        this.length = length;
    }

    @Override
    public Set<Annotation> annotate(Document document) {
        String text = document.getContent();

        Pattern legalChars = Pattern.compile("[A-Za-z]");

        // clean up all texts
        int curWordStartPosition = 0;
        StringBuilder curWord = new StringBuilder();
        ArrayList<String> wordsInText = new ArrayList<String>();
        ArrayList<Integer> wordStart = new ArrayList<Integer>();
        ArrayList<Integer> wordEnd = new ArrayList<Integer>();

        int i = 0;
        while (i < text.length()) {
            Character curChar = text.charAt(i);
            if (!legalChars.matcher(curChar.toString()).matches()) {
                if (curWord.length() == 0) {
                    curWordStartPosition = i + 1;
                    i++;
                    continue;
                }
                // current word finished
                wordsInText.add(curWord.toString());
                wordStart.add(curWordStartPosition);
                wordEnd.add(i);
                curWord = new StringBuilder();
                curWordStartPosition = i + 1;
            }
            else {
                curWord.append(curChar);
            }
            i++;
        }

        HashSet<Annotation> annotations = new HashSet<Annotation>();

        i = 0;
        while (i < wordsInText.size() - (length-1)) {
            StringBuilder sb = new StringBuilder();
            int curStart = wordStart.get(i);
            int lastEnd = wordEnd.get(i);
            for (int j = 1; j < length; j++) {
                sb.append(wordsInText.get(i + j));
                lastEnd = wordEnd.get(i + j);
            }
            String nGram = sb.toString().trim();
            annotations.add(new Annotation(document, curStart, lastEnd - curStart));
            i++;
        }

        return annotations;
    }
}
