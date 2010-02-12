package org.dllearner.tools.evaluationplugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;

public class EvaluationTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<EvaluatedDescriptionClass> descriptions = new ArrayList<EvaluatedDescriptionClass>();
	private List<Integer> selected;
	
	private static final String[] COLUMN_NAMES = {
		"equivalent class expression",
		"Improvement",
		"Equal Quality (+)",
		"Equal Quality (-)",
		"Inferior",
		"Not acceptable",
		"Error"		};

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public int getRowCount() {
		return descriptions.size();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0){
			return Description.class;
		} else {
			return Object.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			return OWLAPIDescriptionConvertVisitor.getOWLDescription(descriptions.get(rowIndex).getDescription());
		} else {
			return Boolean.valueOf(selected.get(rowIndex) == columnIndex);
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex >= 1){
			selected.set(rowIndex, Integer.valueOf(columnIndex));
			fireTableRowsUpdated(rowIndex, rowIndex);
		} else {
			super.setValueAt(aValue, rowIndex, columnIndex);
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex >= 1;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}
	
	public void setDescriptions(List<EvaluatedDescriptionClass> descriptions){
		this.descriptions = descriptions;
		this.selected = new ArrayList<Integer>(descriptions.size());
		for(int i = 0; i < descriptions.size(); i++){
			selected.add(i, Integer.valueOf(2));
		}
		fireTableDataChanged();
	}
	
	public EvaluatedDescriptionClass getSelectedEvaluatedDescription(int rowIndex){
		return descriptions.get(rowIndex);
	}

}
