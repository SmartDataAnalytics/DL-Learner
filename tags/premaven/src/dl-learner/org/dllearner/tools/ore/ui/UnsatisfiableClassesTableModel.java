package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.model.OWLClass;

public class UnsatisfiableClassesTableModel extends AbstractTableModel {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3720592375790162193L;
	
	private List<OWLClass> unsatClasses;
	
	public UnsatisfiableClassesTableModel(){
		super();
		unsatClasses = new ArrayList<OWLClass>();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return unsatClasses.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			return unsatClasses.get(rowIndex);
		} else {
			return "<html><b><font color=\"red\">" + OREManager.getInstance().getManchesterSyntaxRendering(unsatClasses.get(rowIndex)) + " </font></b></html>";
		}
	}
		
	public void addUnsatClasses(List<OWLClass> unsatClasses){
		this.unsatClasses.clear();
		this.unsatClasses = unsatClasses;
		fireTableDataChanged();
	}
	
	public void clear(){
		this.unsatClasses.clear();
		fireTableDataChanged();
	}
	
	public OWLClass getClassAt(int rowIndex){
		return unsatClasses.get(rowIndex);
	}

}
