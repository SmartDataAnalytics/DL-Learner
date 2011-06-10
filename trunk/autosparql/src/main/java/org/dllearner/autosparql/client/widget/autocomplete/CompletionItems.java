package org.dllearner.autosparql.client.widget.autocomplete;

public interface CompletionItems {
    /**
      * Returns an array of all completion items matching
      * @param match The user-entered text all compleition items have to match
      * @return      Array of strings
      */
    public String[] getCompletionItems(String match);
}
