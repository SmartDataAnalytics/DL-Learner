/**
 * 
 */
package org.dllearner.algorithms.isle.index;

/**
 * @author Lorenz Buehmann
 *
 */
public class Token {
	
	private String rawForm;
	private String stemmedForm;
	private String posTag;
	private boolean isPunctuation;
	private boolean isStopWord;
	
	public Token(String rawForm) {
		this.rawForm = rawForm;
	}
	
	public Token(String rawForm, String stemmedForm, String posTag, boolean isPunctuation, boolean isStopWord) {
		this.rawForm = rawForm;
		this.stemmedForm = stemmedForm;
		this.posTag = posTag;
		this.isPunctuation = isPunctuation;
		this.isStopWord = isStopWord;
	}

	/**
	 * @return the rawForm
	 */
	public String getRawForm() {
		return rawForm;
	}
	
	/**
	 * @return the stemmedForm
	 */
	public String getStemmedForm() {
		return stemmedForm;
	}
	
	/**
	 * @return the posTag
	 */
	public String getPOSTag() {
		return posTag;
	}
	
	/**
	 * @return the isPunctuation
	 */
	public boolean isPunctuation() {
		return isPunctuation;
	}
	
	/**
	 * @return the isStopWord
	 */
	public boolean isStopWord() {
		return isStopWord;
	}
	
	/**
	 * @param stemmedForm the stemmedForm to set
	 */
	public void setStemmedForm(String stemmedForm) {
		this.stemmedForm = stemmedForm;
	}
	
	/**
	 * @param posTag the posTag to set
	 */
	public void setPOSTag(String posTag) {
		this.posTag = posTag;
	}
	
	/**
	 * @param isPunctuation the isPunctuation to set
	 */
	public void setIsPunctuation(boolean isPunctuation) {
		this.isPunctuation = isPunctuation;
	}
	
	/**
	 * @param isStopWord the isStopWord to set
	 */
	public void setIsStopWord(boolean isStopWord) {
		this.isStopWord = isStopWord;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n[Word: " + rawForm + "\n" 
				+ "Stemmed word: " + stemmedForm + "\n"
				+ "POS tag: " + posTag + "]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;

        if (!posTag.equals(token.posTag)) {
            return false;
        }
        if (!stemmedForm.equals(token.stemmedForm)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = stemmedForm.hashCode();
        result = 31 * result + posTag.hashCode();
        return result;
    }
}
