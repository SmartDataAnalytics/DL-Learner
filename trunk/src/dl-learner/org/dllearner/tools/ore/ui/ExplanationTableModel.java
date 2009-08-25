package org.dllearner.tools.ore.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class ExplanationTableModel extends AbstractTableModel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4537633628250304813L;
	private List<OWLAxiom> axioms;
	private ExplanationManager expMan;
	private ImpactManager impMan;
	private RepairManager repMan;
	private OWLClass unsat;
	
	public ExplanationTableModel(List<OWLAxiom> axioms, OWLClass cl){
		this.axioms = axioms;
		this.expMan = ExplanationManager.getInstance(OREManager.getInstance());
		this.impMan = ImpactManager.getInstance(OREManager.getInstance());
		this.repMan = RepairManager.getRepairManager(OREManager.getInstance());
		this.unsat = cl;
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
		if(columnIndex == 0){
//			return getOWLAxiomAtRow(rowIndex);
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax));
		} else if(columnIndex == 1){
			return expMan.getArity(unsat, axioms.get(rowIndex));
		} else if(columnIndex == 2) {
			return Boolean.valueOf(impMan.isSelected(getOWLAxiomAtRow(rowIndex)));
		} else {
			return "rewrite";
		}
		
	}
	
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 2){
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			if(impMan.isSelected(ax)){
				repMan.removeAxiom2Remove(ax);
				impMan.removeAxiomFromImpactList(ax);
			} else {
				impMan.addAxiom2ImpactList(ax);
				repMan.addAxiom2Remove(ax);
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
