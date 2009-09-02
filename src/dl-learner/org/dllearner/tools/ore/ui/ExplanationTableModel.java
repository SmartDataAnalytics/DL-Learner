package org.dllearner.tools.ore.ui;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class ExplanationTableModel extends AbstractTableModel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4537633628250304813L;
	private Explanation exp;
	private ExplanationManager expMan;
	private ImpactManager impMan;
	private RepairManager repMan;
	private OWLClass unsat;
	
	public ExplanationTableModel(Explanation exp, OWLClass cl){
		this.exp = exp;
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
		return exp.getAxioms().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			int depth2Root = expMan.getOrdering(exp).get(rowIndex).values().iterator().next();
           return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax), depth2Root);
		} else if(columnIndex == 1){
			return expMan.getArity(unsat, getOWLAxiomAtRow(rowIndex));
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
				if(expMan.isLaconicMode()){
					for(OWLAxiom source : expMan.getSourceAxioms(ax)){
						repMan.addAxiom2Remove(source);
						repMan.addAxioms2Keep(expMan.getRemainingAxioms(source, ax));
					}		
				} else {
					repMan.addAxiom2Remove(ax);
				}
				
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
		return expMan.getOrdering(exp).get(rowIndex).keySet().iterator().next();
	}
	
	

}
