package org.dllearner.tools.evaluationplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private boolean allColumnsEnabled = true;
	
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
			return OWLAPIDescriptionConvertVisitor.getOWLClassExpression(descriptions.get(rowIndex).getDescription());
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
		if(allColumnsEnabled){
			return columnIndex >= 1;
		} else {
			return columnIndex <= 1 || columnIndex >= 5;
		}
		
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}
	
	public void setDescriptions(List<EvaluatedDescriptionClass> descriptions){
		this.descriptions = descriptions;
		this.selected = new ArrayList<Integer>(descriptions.size());
		for(int i = 0; i < descriptions.size(); i++){
			selected.add(i, Integer.valueOf(1));
		}
		fireTableDataChanged();
	}
	
	public EvaluatedDescriptionClass getSelectedEvaluatedDescription(int rowIndex){
		return descriptions.get(rowIndex);
	}
	
	public void setAllColumnsEnabled(boolean value){
		allColumnsEnabled = value;
	}
	
	public Map<EvaluatedDescriptionClass, Integer> getUserInputMap() {
		Map<EvaluatedDescriptionClass, Integer> inputMap = new HashMap<EvaluatedDescriptionClass, Integer>();
		for(EvaluatedDescriptionClass ec : descriptions){
			inputMap.put(ec, selected.get(descriptions.indexOf(ec)));
		}
		return inputMap;
	}
	
	public void setUserInput(Map<EvaluatedDescriptionClass, Integer> inputMap) {
		for(EvaluatedDescriptionClass ec : descriptions){
			selected.set(descriptions.indexOf(ec), inputMap.get(ec));
		}
	}

}
