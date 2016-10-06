package org.dllearner.algorithms.isle.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dllearner.algorithms.isle.TextDocumentGenerator;

/**
 * A simple text document without further formatting or markup.
 *
 * @author Daniel Fleischhacker
 */
public class TextDocument extends LinkedList<Token> implements Document {
    public static void main(String[] args) {
        String s = "This is a very long, nice text for testing our new implementation of TextDocument.";
        TextDocument doc = TextDocumentGenerator.getInstance().generateDocument(s);

        System.out.println(doc.getRawContent());
    }

    @Override
    public String getContent() {
        return getContentStartingAtToken(this.getFirst(), SurfaceFormLevel.STEMMED);
    }

    @Override
    public String getRawContent() {
        return getContentStartingAtToken(this.getFirst(), SurfaceFormLevel.RAW);
    }

    @Override
    public String getPOSTaggedContent() {
        return getContentStartingAtToken(this.getFirst(), SurfaceFormLevel.POS_TAGGED);
    }

    /**
     * Returns a string containing all tokens starting at the token {@code start} until the end of the list. The
     * surface forms according to {@code level} are used to build the string.
     *
     * @param start token to start building the string at, i.e., the first token in the returned string
     * @param l     level of surface forms to use
     * @return built string
     */
    public String getContentStartingAtToken(Token start, SurfaceFormLevel l) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (Token t : this) {
            if (found) {
                sb.append(" ");
                String surfaceForm = getStringForLevel(t, l);
                if (surfaceForm != null) {
                    sb.append(surfaceForm);
                }
            }
            else if (t == start) {
                found = true;
                sb.append(getStringForLevel(t, l));
            }
        }

        return sb.toString();
    }

    /**
     * Returns a list containing {@code numberOfTokens} successive tokens from this document starting at the given start
     * token. If {@code ignorePunctuation} is set, tokens which represent punctuation are added to the result but not
     * counted for the number of tokens.
     *
     * @param start             token to start collecting tokens from the document
     * @param numberOfTokens    number of tokens to collect from the document
     * @param ignorePunctuation if true, punctuation are not counted towards the number of tokens to return
     * @return list containing the given number of relevant tokens, depending in the value of ignorePunctuation, the
     *          list might contain additional non-relevant (punctuation) tokens
     */
    public List<Token> getTokensStartingAtToken(Token start, int numberOfTokens, boolean ignorePunctuation) {
        ArrayList<Token> tokens = new ArrayList<>();

        int relevantTokens = 0;
        boolean found = false;

        for (Token t : this) {
            if (found) {
                tokens.add(t);
                if (!ignorePunctuation || !t.isPunctuation()) {
                    relevantTokens++;
                }
            }
            else if (t == start) {
                found = true;
                tokens.add(t);
            }
            if (relevantTokens == numberOfTokens) {
                break;
            }
        }

        return tokens;
    }

    /**
     * Returns a list containing all successive tokens from this document starting at the given start
     * token. If {@code ignorePunctuation} is set, tokens which represent punctuation are added to the result but not
     * counted for the number of tokens.
     *
     * @param start             token to start collecting tokens from the document
     * @param ignorePunctuation if true, punctuation are not counted towards the number of tokens to return
     * @return list containing all relevant tokens, depending in the value of ignorePunctuation, the
     *          list might contain additional non-relevant (punctuation) tokens
     */
    public List<Token> getTokensStartingAtToken(Token start, boolean ignorePunctuation) {
        ArrayList<Token> tokens = new ArrayList<>();

        int relevantTokens = 0;
        boolean found = false;

        for (Token t : this) {
            if (found) {
                tokens.add(t);
                if (!ignorePunctuation || !t.isPunctuation()) {
                    relevantTokens++;
                }
            }
            else if (t == start) {
                found = true;
                tokens.add(t);
            }
        }

        return tokens;
    }

    private String getStringForLevel(Token t, SurfaceFormLevel l) {
        switch (l) {
            case RAW:
                return t.getRawForm();
            case POS_TAGGED:
                return t.getPOSTag();
            case STEMMED:
                return t.isPunctuation() ? null : t.getStemmedForm();
        }

        return null;
    }
}
