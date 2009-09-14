package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.semanticweb.owl.model.OWLAxiom;

public class RemainingAxiomsTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7214659543838574629L;
	private List<OWLAxiom> remainingAxioms;
	private List<Boolean> selectionList;
	
	public RemainingAxiomsTableModel(List<OWLAxiom> remainingAxioms){
		this.remainingAxioms = remainingAxioms;
		selectionList = new ArrayList<Boolean>(remainingAxioms.size());
		for(int i = 0; i < remainingAxioms.size(); i++ ){
			selectionList.add(i, Boolean.FALSE);
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return remainingAxioms.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			return remainingAxioms.get(rowIndex);
		} else {
			return selectionList.get(rowIndex);
		}
	}
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			return false;
		}
		return true;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 1){
			selectionList.set(rowIndex, (Boolean)value);
		}
		super.setValueAt(value, rowIndex, columnIndex);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0) {
			return OWLAxiom.class;
		} else {
			return Boolean.class;
		}
	}
	
	public List<OWLAxiom> getSelectedAxioms(){
		List<OWLAxiom> selectedAxioms = new ArrayList<OWLAxiom>();
		for(int i = 0; i < remainingAxioms.size(); i++){
			if(selectionList.get(i) == Boolean.TRUE){
				selectedAxioms.add(remainingAxioms.get(i));
			}
		}
		return selectedAxioms;
	}

}
