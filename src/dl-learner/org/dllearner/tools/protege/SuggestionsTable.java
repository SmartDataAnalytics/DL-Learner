package org.dllearner.tools.protege;

import java.util.List;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.jdesktop.swingx.JXTable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;

public class SuggestionsTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -497294373160119210L;
	
	private final OWLCellRenderer owlRenderer;
	private final ProgressBarTableCellRenderer progressRenderer;
	
	public SuggestionsTable(OWLEditorKit editorKit){
		super(new SuggestionsTableModel());
		progressRenderer = new ProgressBarTableCellRenderer();
		progressRenderer.setBackground(getBackground());
		getColumn(0).setCellRenderer(progressRenderer);
		owlRenderer = new OWLCellRenderer(editorKit, false, false);
		owlRenderer.setHighlightKeywords(true);
		owlRenderer.setWrap(false);
		getColumn(1).setCellRenderer(owlRenderer);
		setColumnSizes();
	}

	private void setColumnSizes(){
		getColumn(0).setMaxWidth(100);
		getColumn(1).setPreferredWidth(430);
	}
	
	public void clear(){
		((SuggestionsTableModel)getModel()).clear();
	}
	
	public void setSuggestions(List<EvaluatedDescriptionClass> suggestionList){
		((SuggestionsTableModel)getModel()).setSuggestions(suggestionList);
	}
	
	public EvaluatedDescriptionClass getSelectedSuggestion(){	
		return ((SuggestionsTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
}
