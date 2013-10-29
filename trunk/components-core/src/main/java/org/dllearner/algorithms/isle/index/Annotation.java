/**
 * 
 */
package org.dllearner.algorithms.isle.index;


/**
 * A (non-semantic) annotation which represents an entity in a document by its offset and length.
 * @author Lorenz Buehmann
 *
 */
public class Annotation {
	
	private Document referencedDocument;
	private int offset;
	private int length;
    private String matchedString;

    public String getMatchedString() {
        return matchedString;
    }

    public void setMatchedString(String matchedString) {
        this.matchedString = matchedString;
    }

    public Annotation(Document referencedDocument, int offset, int length) {
		this.referencedDocument = referencedDocument;
		this.offset = offset;
		this.length = length;
	}

	public Document getReferencedDocument() {
		return referencedDocument;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public String getToken(){
		return referencedDocument.getContent().substring(offset, offset + length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((referencedDocument == null) ? 0 : referencedDocument.hashCode());
		result = prime * result + length;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (referencedDocument == null) {
			if (other.referencedDocument != null)
				return false;
		} else if (!referencedDocument.equals(other.referencedDocument))
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\"" + referencedDocument.getContent().substring(offset, offset+length) + "\" at position " + offset;
	}
}
