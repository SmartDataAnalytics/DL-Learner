/**
 * Copyright (C) 2007-2009, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.tools.protege;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.protege.editor.owl.OWLEditorKit;

/**
 * This class is the panel for the suggest list. It shows the descriptions made
 * by the DL-Learner.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class SuggestClassPanel extends JPanel {

	private static final long serialVersionUID = 724628423947230L;

	private final SuggestionsTable suggestionTable;

	private final JScrollPane suggestScroll;

	/**
	 * This is the constructor for the suggest panel. It creates a new Scroll
	 * panel and puts the Suggest List in it.
	 * @param m model of the DL-Learner
	 * @param v view of the DL-Learner
	 */
	public SuggestClassPanel(OWLEditorKit editorKit) {
		super();
		this.setLayout(new BorderLayout());
		// renders scroll bars if necessary
		suggestScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		suggestionTable = new SuggestionsTable(editorKit);
		suggestionTable.setVisibleRowCount(6);
		suggestScroll.setViewportView(suggestionTable);
		add(BorderLayout.CENTER, suggestScroll);
	}

	@SuppressWarnings("unchecked")
	public void setSuggestions(List<? extends EvaluatedDescription> result){
		suggestionTable.setSuggestions((List<EvaluatedDescriptionClass>)result);
	}

	public SuggestionsTable getSuggestionsTable() {
		return suggestionTable;
	}
	
	public EvaluatedDescriptionClass getSelectedSuggestion(){
		return suggestionTable.getSelectedSuggestion();
	}


}
