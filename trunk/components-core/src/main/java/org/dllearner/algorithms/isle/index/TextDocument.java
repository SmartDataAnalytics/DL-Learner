package org.dllearner.algorithms.isle.index;

/**
 * A simple text document without further formatting or markup.
 *
 * @author Daniel Fleischhacker
 */
public class TextDocument implements Document {
    private String content;


    /**
     * Initializes a text document with the given content.
     *
     * @param content content of this text document
     */
    public TextDocument(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    /**
     * The text content of this document. Returns the same data as {@link #getContent()}.
     *
     * @return text content of this document
     */
    @Override
    public String getRawContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextDocument that = (TextDocument) o;

        if (!content.equals(that.content)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return content;
    }
}
