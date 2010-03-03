package org.dllearner.tools.protege;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owl.model.OWLDescription;

public class SuggestionsTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6920806148989403795L;
	
	private List<EvaluatedDescriptionClass> suggestionList;

	public SuggestionsTableModel(){
		super();
		suggestionList = new ArrayList<EvaluatedDescriptionClass>();
	}
	
	public SuggestionsTableModel(List<EvaluatedDescriptionClass> suggestionList){
		this.suggestionList = suggestionList;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return suggestionList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
				return (int)(suggestionList.get(rowIndex).getAccuracy() * 100);
		} else {
				return OWLAPIDescriptionConvertVisitor.getOWLDescription(suggestionList.get(rowIndex).getDescription());
		}
		
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 0) {
			return String.class;
		} else {
			return OWLDescription.class;
		}
	}
	@Override
	public String getColumnName(int column){
		if(column == 0){
			return "Accuracy";
		} else {
			return "Class expression";
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
	

}
