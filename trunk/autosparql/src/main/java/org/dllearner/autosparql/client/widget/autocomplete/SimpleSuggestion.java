package org.dllearner.autosparql.client.widget.autocomplete;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SimpleSuggestion implements Suggestion{
	
	private String label;
	
	public SimpleSuggestion(String label) {
		this.label = label;
	}

	@Override
	public String getDisplayString() {
		return label;
	}

	@Override
	public String getReplacementString() {
		return label;
	}

}
