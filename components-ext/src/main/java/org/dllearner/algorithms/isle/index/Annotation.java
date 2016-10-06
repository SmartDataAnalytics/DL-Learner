/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A (non-semantic) annotation which represents an entity in a document by its offset and length.
 * @author Lorenz Buehmann
 *
 */
public class Annotation implements Serializable{
	
	private Document referencedDocument;
    private ArrayList<Token> tokens;
    private String matchedString;

    public String getMatchedString() {
        return matchedString;
    }

    public void setMatchedString(String matchedString) {
        this.matchedString = matchedString;
    }

    public Annotation(Document referencedDocument, List<Token> tokens) {
		this.referencedDocument = referencedDocument;
        this.tokens = new ArrayList<>(tokens);
    }

	public Document getReferencedDocument() {
		return referencedDocument;
	}
	
	/**
	 * @return the tokens
	 */
	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public String getString(){
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(t.getStemmedForm());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Annotation that = (Annotation) o;

        if (matchedString != null ? !matchedString.equals(that.matchedString) : that.matchedString != null) {
            return false;
        }
        if (referencedDocument != null ? !referencedDocument.equals(that.referencedDocument) :
                that.referencedDocument != null) {
            return false;
        }
        if (tokens != null ? !tokens.equals(that.tokens) : that.tokens != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = referencedDocument != null ? referencedDocument.hashCode() : 0;
        result = 31 * result + (tokens != null ? tokens.hashCode() : 0);
        result = 31 * result + (matchedString != null ? matchedString.hashCode() : 0);
        return result;
    }

    /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
	@Override
	public String toString() {
        return getString();
    }
}
