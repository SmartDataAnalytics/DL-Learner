package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class ExplanationTableModel extends AbstractTableModel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4537633628250304813L;
	private List<OWLAxiom> axioms;
	private List<Boolean> remove;
	private ExplanationManager expMan;
	private ImpactManager impMan;
	private OWLClass unsat;
	
	public ExplanationTableModel(List<OWLAxiom> axioms, ExplanationManager expMan, ImpactManager impMan, OWLClass cl){
		this.axioms = axioms;
		this.expMan = expMan;
		this.impMan = impMan;
		this.unsat = cl;
		remove = new ArrayList<Boolean>();
		for(int i = 0; i < axioms.size(); i++){
			remove.add(false);
		}
	}
	
	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {	
		return axioms.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0 && rowIndex >=0){
			return axioms.get(rowIndex);
		} else if(columnIndex == 1){
			return expMan.getArity(unsat, axioms.get(rowIndex));
		} else if(columnIndex == 2) {
			return Boolean.valueOf(impMan.isSelected(getOWLAxiomAtRow(rowIndex)));//remove.get(rowIndex);
		} else {
			return "rewrite";
		}
		
	}
	
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 2){
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			if(impMan.isSelected(ax)){
				impMan.removeAxiomFromImpactList(ax);
			} else {
				impMan.addAxiom2ImpactList(ax);
			}
		}
		super.setValueAt(value, rowIndex, columnIndex);
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 0) {
			return OWLAxiom.class;
		} else if(columnIndex == 1){
			return int.class;
		} else if(columnIndex == 2) {
			return Boolean.class;
		} else {
			return String.class;
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 2 || columnIndex == 3)
			return true;
		return false;
	}
	
	public OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return axioms.get(rowIndex);
	}
	
	

}
