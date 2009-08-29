package org.dllearner.tools.ore.ui;

import java.text.DecimalFormat;
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
	private DecimalFormat df;

	public ResultTableModel(){
		super();
		resultList = new ArrayList<EvaluatedDescriptionClass>();
		df = new DecimalFormat("00.00%");
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
		return resultList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
				return df.format(resultList.get(rowIndex).getAccuracy());
		} else {
				return resultList.get(rowIndex).getDescription();
		}
		
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 0) {
			return String.class;
		} else {
			return Description.class;
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
