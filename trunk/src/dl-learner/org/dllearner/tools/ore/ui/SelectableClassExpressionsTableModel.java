package org.dllearner.tools.ore.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public class SelectableClassExpressionsTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8582947007601430481L;

	private List<EvaluatedDescriptionClass> resultList;
	private DecimalFormat df;
	private List<Boolean> selectionList;

	public SelectableClassExpressionsTableModel(){
		super();
		resultList = new ArrayList<EvaluatedDescriptionClass>();
		selectionList = new ArrayList<Boolean>();
		df = new DecimalFormat("00%");
	}
	
	public SelectableClassExpressionsTableModel(List<EvaluatedDescriptionClass> resultList){
		this.resultList = resultList;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return resultList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
				return df.format(resultList.get(rowIndex).getAccuracy());			
		} else if(columnIndex == 1){
				return resultList.get(rowIndex).getDescription();
		} else {
			return selectionList.get(rowIndex);
		}		
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		
		selectionList.set(rowIndex, (Boolean)value);
			
		super.fireTableCellUpdated(rowIndex, columnIndex);
		
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		switch(columnIndex){	
			case 0: return String.class;
			case 1: return Description.class;
			case 2: return Boolean.class;
		}
		return null;	
	}
	
	@Override
	public String getColumnName(int column){
		switch(column){
			case 0: return "Accuracy";
			case 1: return "Class expression";
		}
		return "";
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 2){
			return true;
		} 
		return false;
	}
	
	public void clear(){
		resultList.clear();
		selectionList.clear();
		fireTableDataChanged();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		this.resultList.clear();
		this.selectionList.clear();
		this.resultList.addAll(resultList);
		for(int i = 0; i < resultList.size(); i++){
			selectionList.add(i, Boolean.FALSE);
		}
	
		fireTableRowsUpdated(0, this.resultList.size());
	}
	
	public EvaluatedDescriptionClass getSelectedValue(int rowIndex){
		return resultList.get(rowIndex);
	}
	
	public int getSelectionIndex(EvaluatedDescriptionClass e){
		return resultList.indexOf(e);
	}
	
	public List<EvaluatedDescriptionClass> getSelectedDescriptions(){
		List<EvaluatedDescriptionClass> selected = new ArrayList<EvaluatedDescriptionClass>();
		for(int i = 0; i < selectionList.size(); i++){
			if(selectionList.get(i).equals(Boolean.TRUE)){
				selected.add(resultList.get(i));
			}
		}
		
		return selected;
	}

}
