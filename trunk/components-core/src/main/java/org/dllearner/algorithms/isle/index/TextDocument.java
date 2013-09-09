package org.dllearner.algorithms.isle.index;

/**
 * A simple text document without further formatting or markup.
 *
 * @author Daniel Fleischhacker
 */
public class TextDocument implements Document {
    private String content;
    private String rawContent;


    /**
     * Initializes a text document with the given raw content. Internally, the content is cleaned up so that it only
     * contains letters adhering to the regular expression pattern [A-Za-z].
     *
     * @param content the raw content of this text document
     */
    public TextDocument(String content) {
        this.rawContent = content;
        this.content = content.toLowerCase();
        this.content = this.content.replaceAll("[^a-z ]", " ");
        this.content = this.content.replaceAll("\\s{2,}", " ");
        this.content = content.trim();
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
        return rawContent;
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
