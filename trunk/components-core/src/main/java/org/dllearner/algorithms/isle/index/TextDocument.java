package org.dllearner.algorithms.isle.index;

/**
 * A simple text document without further formatting or markup.
 *
 * @author Daniel Fleischhacker
 */
public class TextDocument implements Document {
    private String content;

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
}
