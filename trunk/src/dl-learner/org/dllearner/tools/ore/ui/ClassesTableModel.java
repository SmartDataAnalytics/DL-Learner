package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.NamedClass;

public class ClassesTableModel extends AbstractTableModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6705129877879013372L;
	
	private List<NamedClass> classes;
	
	public ClassesTableModel(){
		classes = new ArrayList<NamedClass>();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return classes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
			return classes.get(rowIndex);
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

}
