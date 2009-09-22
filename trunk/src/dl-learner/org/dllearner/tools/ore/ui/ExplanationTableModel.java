package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class ExplanationTableModel extends AbstractTableModel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4537633628250304813L;
	private Explanation exp;
	private ExplanationManager expMan;
	private ImpactManager impMan;
	private RepairManager repMan;
	private OWLOntology ont;
	private boolean strikeOutIrrelevantParts;
	private Explanation laconicExplanation;
	
	public ExplanationTableModel(Explanation exp, OWLClass cl){
		this.exp = exp;
		this.expMan = ExplanationManager.getInstance(OREManager.getInstance());
		this.impMan = ImpactManager.getInstance(OREManager.getInstance());
		this.repMan = RepairManager.getInstance(OREManager.getInstance());
		this.ont = OREManager.getInstance().getReasoner().getOWLAPIOntologies();
	}
	
	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {	
		return exp.getAxioms().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
		if(columnIndex == 0){
			int depth2Root = expMan.getOrdering(exp).get(rowIndex).values().iterator().next();
			if(strikeOutIrrelevantParts){			
				return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax), depth2Root, laconicExplanation);				
			} else {
				return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax), depth2Root);
			}        
		} else if(columnIndex == 1){
			return expMan.getGlobalArity(ax);
		} else if(columnIndex == 2) {
			return expMan.getUsage(ax).size();
		} else if(columnIndex == 3){
			return expMan.getInconsistencyValue(ax);
		} else if(columnIndex == 4){
			return Boolean.valueOf(impMan.isSelected(ax));
		} else {
			return "rewrite";
		}	
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 4){
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			if(impMan.isSelected(ax)){
				impMan.removeSelection(ax);
				if(expMan.isLaconicMode() && !ont.containsAxiom(ax)){
					List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
					for(OWLAxiom source : expMan.getSourceAxioms(ax)){
						impMan.removeSelection(source);
						changes.add(new RemoveAxiom(ont, source));
						for(OWLAxiom remain : expMan.getRemainingAxioms(source, ax)){
							changes.add(new AddAxiom(ont, remain));
						}
					}
					repMan.removeFromRepairPlan(changes);
				} else {
					repMan.removeFromRepairPlan(new RemoveAxiom(ont, ax));
				}
			} else {
//				impMan.addSelection(ax);
				if(expMan.isLaconicMode() && !ont.containsAxiom(ax)){
//					List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
//					for(OWLAxiom source : expMan.getSourceAxioms(ax)){
//						impMan.addSelection(source);
//						changes.add(new RemoveAxiom(ont, source));
//						for(OWLAxiom remain : expMan.getRemainingAxioms(source, ax)){
//							changes.add(new AddAxiom(ont, remain));
//						}
//						
//					}
					RemainingAxiomsDialog dialog = new RemainingAxiomsDialog(ax, ont);
					int ret = dialog.showDialog();
					if(ret == RemainingAxiomsDialog.OK_RETURN_CODE){
						impMan.addSelection(ax);
						List<OWLOntologyChange> changes = dialog.getChanges();
						repMan.addToRepairPlan(changes);
					}
					
				} else {
					impMan.addSelection(ax);
					repMan.addToRepairPlan(new RemoveAxiom(ont, ax));
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
		} else if(columnIndex == 2){
			return int.class;
		} else if(columnIndex == 3){
			return double.class;
		} else if(columnIndex == 4){
			return Boolean.class;
		} else {
			return String.class;
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 4 || columnIndex == 5){
			return true;
		}
		return false;
	}
	
	public OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return expMan.getOrdering(exp).get(rowIndex).keySet().iterator().next();
	}
	
	public void setStriked(boolean strikeOut){
		this.strikeOutIrrelevantParts = strikeOut;
		if(strikeOut){
			laconicExplanation = expMan.getLaconicExplanation(exp);
		}
		fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int column) {
		if(column == 0){
			return "Axiom";
		} else if(column == 1){
			return "Arity";
		} else if(column == 2){
			return "Usage";
		} else if(column == 3){
			return "";
		} else {
			return "";
		}
	}
	
}
