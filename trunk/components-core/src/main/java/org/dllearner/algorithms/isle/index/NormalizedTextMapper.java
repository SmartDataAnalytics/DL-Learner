package org.dllearner.algorithms.isle.index;

import java.util.ArrayList;

/**
 * Provides text normalization and mapping of normalized ranges to the original ones.
 */
public class NormalizedTextMapper {
    private Document originalDocument;
    private String originalText;
    private String normalizedText;

    private ArrayList<OccurenceMappingPair> normalizedIndexToOriginalIndex;

    public NormalizedTextMapper(Document original) {
        this.originalDocument = original;
        this.originalText = original.getContent();
        this.normalizedIndexToOriginalIndex = new ArrayList<OccurenceMappingPair>();

        StringBuilder sb = new StringBuilder();
        int currentOriginalIndex = 0;
        for (String originalWord : originalText.split(" ")) {
            String normalizedWord = getNormalizedWord(originalWord);
            normalizedIndexToOriginalIndex
                    .add(new OccurenceMappingPair(currentOriginalIndex, originalWord.length(), sb.length(),
                            normalizedWord.length()));
            currentOriginalIndex += originalWord.length() + 1;
            sb.append(normalizedWord);
            sb.append(" ");
        }
        normalizedText = sb.toString();
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    /**
     * Returns the annotation for the original text matching the given position and length in the normalized
     * text.
     *
     * @param position   position in the normalized text to get annotation for
     * @param length length of the text to get annotation for
     * @return
     */
    public Annotation getOriginalAnnotationForPosition(int position, int length) {
        int curNormalizedLength = 0;
        int originalStart = -1;
        int curOriginalLength = 0;

        for (OccurenceMappingPair p : normalizedIndexToOriginalIndex) {
            if (p.getNormalizedIndex() == position) {
                originalStart = p.getOriginalIndex();
            }
            if (originalStart != -1) {
                curNormalizedLength += p.getNormalizedLength();
                curOriginalLength += p.getOriginalLength();
                if (curNormalizedLength >= length) {
                    return new Annotation(originalDocument, originalStart, curOriginalLength);
                }

                // include space
                curNormalizedLength += 1;
                curOriginalLength += 1;
            }
        }

        return null;
    }

    /**
     * Returns the normalized form of the given word. Word must not contain any spaces or the like.
     * @param word
     * @return
     */
    private String getNormalizedWord(String word) {
        return LinguisticUtil.getInstance().getNormalizedForm(word);
    }

    public static void main(String[] args) {
        NormalizedTextMapper n = new NormalizedTextMapper(new TextDocument("This is a testing text using letters"));
        System.out.println(n.getOriginalText());
        System.out.println(n.getNormalizedText());
        for (OccurenceMappingPair p : n.normalizedIndexToOriginalIndex) {
            System.out.println(p);
        }
        System.out.println(n.getOriginalAnnotationForPosition(7,6));
        System.out.println(n.getOriginalAnnotationForPosition(23,6));
        System.out.println(n.getOriginalAnnotationForPosition(7,1));
        System.out.println(n.getOriginalAnnotationForPosition(14,15));
    }

    /**
     * Maps words identified by index and length in the normalized texts to the original word.
     */
    private class OccurenceMappingPair {
        private int originalIndex;
        private int originalLength;
        private int normalizedIndex;
        private int normalizedLength;

        private OccurenceMappingPair(int originalIndex, int originalLength, int normalizedIndex, int normalizedLength) {

            this.originalIndex = originalIndex;
            this.originalLength = originalLength;
            this.normalizedIndex = normalizedIndex;
            this.normalizedLength = normalizedLength;
        }

        private int getNormalizedIndex() {
            return normalizedIndex;
        }

        private int getNormalizedLength() {
            return normalizedLength;
        }

        private int getOriginalLength() {
            return originalLength;
        }

        private int getOriginalIndex() {
            return originalIndex;
        }

        @Override
        public String toString() {
            return "OccurenceMappingPair{" +
                    "originalIndex=" + originalIndex +
                    ", originalLength=" + originalLength +
                    ", normalizedIndex=" + normalizedIndex +
                    ", normalizedLength=" + normalizedLength +
                    '}';
        }
    }
}
