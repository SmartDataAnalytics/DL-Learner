package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.NamedClass;

public class MarkableClassesTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5773843275446469889L;

	private List<NamedClass> classes;
	private int selectedRowIndex = 0;
	
	public MarkableClassesTableModel(){
		classes = new ArrayList<NamedClass>();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return classes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			if(rowIndex == selectedRowIndex){
				return ">";
			} else {
				return "";
			}
		} else {
			return classes.get(rowIndex);
		}		
	}
	
	public void clear(){
		classes.clear();
		fireTableDataChanged();
	}
	
	public void addClasses(Set<NamedClass> classes){
		this.classes.clear();
		this.classes.addAll(classes);
		fireTableDataChanged();
	}
	
	public NamedClass getSelectedValue(int rowIndex){
		return classes.get(rowIndex);
	}
	
	public void setSelectedClass(int rowIndex){
		int oldRowIndex = selectedRowIndex;
		selectedRowIndex = rowIndex;
		fireTableRowsUpdated(oldRowIndex, selectedRowIndex);
	}

}
