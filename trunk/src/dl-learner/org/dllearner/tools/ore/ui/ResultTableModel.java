package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public class ResultTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6920806148989403795L;
	
	private List<EvaluatedDescriptionClass> resultList;

	public ResultTableModel(){
		super();
		resultList = new ArrayList<EvaluatedDescriptionClass>();
	}
	
	public ResultTableModel(List<EvaluatedDescriptionClass> resultList){
		this.resultList = resultList;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return 10;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(!resultList.isEmpty()){
			if(columnIndex == 0 && rowIndex >=0 && resultList.size() > rowIndex){
				return resultList.get(rowIndex).getAccuracy();
			} else if(columnIndex == 1 && rowIndex >=0 && resultList.size() > rowIndex){
				return resultList.get(rowIndex).getDescription();
			}
			return "";
		} else {
			return "";
		}
		
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 0) {
			return double.class;
		} else {
			return Description.class;
		}
	}
	
	public void clear(){
		resultList.clear();
		fireTableDataChanged();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		this.resultList.clear();
		this.resultList.addAll(resultList);
		fireTableDataChanged();
	}
	
	public EvaluatedDescriptionClass getSelectedValue(int rowIndex){
		return resultList.get(rowIndex);
	}

}
