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
	private EvaluatedDescriptionClass old;
	
	public SuggestionsTable(OWLEditorKit editorKit){
		super(new SuggestionsTableModel());
		
		progressRenderer = new ProgressBarTableCellRenderer();
		progressRenderer.setBackground(getBackground());
		getColumn(0).setCellRenderer(progressRenderer);
		
		owlRenderer = new OWLCellRenderer(editorKit, false, false);
		owlRenderer.setHighlightKeywords(true);
		owlRenderer.setHighlightUnsatisfiableClasses(false);
		owlRenderer.setHighlightUnsatisfiableProperties(false);
		owlRenderer.setWrap(false);
		getColumn(2).setCellRenderer(owlRenderer);
		
		setColumnSizes();
	}

	private void setColumnSizes(){
		getColumn(0).setMaxWidth(100);
		getColumn(1).setMaxWidth(20);
		getColumn(2).setPreferredWidth(430);
	}
	
	public void clear(){
		((SuggestionsTableModel)getModel()).clear();
	}
	
	public void setSuggestions(List<EvaluatedDescriptionClass> suggestionList){
		if(getSelectedRow() >= 0){
			old = getSelectedSuggestion();
		}
		((SuggestionsTableModel)getModel()).setSuggestions(suggestionList);
		if(old != null){
			int newRowIndex = ((SuggestionsTableModel)getModel()).getSelectionIndex(old);
			if(newRowIndex >= 0){
				getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
			}
		}
	}
	
	public EvaluatedDescriptionClass getSelectedSuggestion(){	
		return ((SuggestionsTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
}
