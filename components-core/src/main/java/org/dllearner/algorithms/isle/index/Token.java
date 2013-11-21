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
	
	public Token(String rawForm) {
		posTag = rawForm;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Word: " + rawForm + "\n" 
				+ "Stemmed word: " + stemmedForm + "\n"
				+ "POS tag: " + posTag;
	}
}
