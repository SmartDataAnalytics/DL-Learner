package org.dllearner.tools.ore.ui;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;
import org.semanticweb.owl.model.OWLAxiom;

public class SimpleExplanationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1488778624376881716L;
	
	private Explanation exp;
	private ExplanationManager expMan;
	private ImpactManager impMan;
	
	public SimpleExplanationTableModel(Explanation exp){
		this.exp = exp;
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		impMan = ImpactManager.getInstance(OREManager.getInstance());
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return exp.getAxioms().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		OWLAxiom ax;
		if(exp.getAxioms().size() > 1){
			ax = getOWLAxiomAtRow(rowIndex);
			int depth2Root = expMan.getOrdering(exp).get(rowIndex).values().iterator().next();
			return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax), depth2Root);
		} else {
			ax = exp.getAxioms().iterator().next();
			return ManchesterSyntaxRenderer.render(ax, impMan.isSelected(ax), 0);
		}
       
	}
	
	@Override
	public String getColumnName(int column) {
		return "Axiom";
	}
	
	public OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return expMan.getOrdering(exp).get(rowIndex).keySet().iterator().next();
	}

}
