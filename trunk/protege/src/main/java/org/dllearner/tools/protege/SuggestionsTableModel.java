package org.dllearner.tools.protege;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class SuggestionsTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6920806148989403795L;
	
	private List<EvaluatedDescriptionClass> suggestionList;
	private final Icon inconsistentIcon = new ImageIcon(this.getClass().getResource("warning-icon.png"));
	private final Icon followsIcon = new ModelsIcon();
	
	public SuggestionsTableModel(){
		super();
		suggestionList = new ArrayList<EvaluatedDescriptionClass>();
	}
	
	public SuggestionsTableModel(List<EvaluatedDescriptionClass> suggestionList){
		this.suggestionList = suggestionList;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return suggestionList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return (int) (suggestionList.get(rowIndex).getAccuracy() * 100);
		case 1:
			if(DLLearnerPreferences.getInstance().isCheckConsistencyWhileLearning()){
				if (!suggestionList.get(rowIndex).isConsistent()) {
					return inconsistentIcon;
				} 
			}
			if(suggestionList.get(rowIndex).followsFromKB()){
				return followsIcon;
			}break;
		case 2:
			return OWLAPIDescriptionConvertVisitor
					.getOWLClassExpression(suggestionList.get(rowIndex)
							.getDescription());
		}
		return null;

	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Icon.class;
		case 2:
			return OWLClassExpression.class;
		}
		return null;
	}
	@Override
	public String getColumnName(int column){
		if(column == 0){
			return "Accuracy";
		} else if (column == 2){
			return "Class expression";
		} else {
			return "";
		}
	}
	
	public EvaluatedDescriptionClass getEntryAtRow(int row){
		if(suggestionList.size() >= row){
			return suggestionList.get(row);
		} else {
			return null;
		}
	}
	
	public void clear(){
		suggestionList.clear();
		fireTableDataChanged();
	}
	
	public void setSuggestions(List<EvaluatedDescriptionClass> suggestionList){
		this.suggestionList.clear();
		this.suggestionList.addAll(suggestionList);
		fireTableDataChanged();
	}
	
	public EvaluatedDescriptionClass getSelectedValue(int rowIndex){
		return suggestionList.get(rowIndex);
	}
	
	public int getSelectionIndex(EvaluatedDescriptionClass e){
		return suggestionList.indexOf(e);
	}

}
