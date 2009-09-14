package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import org.jdesktop.swingx.JXTable;
import org.semanticweb.owl.model.OWLAxiom;

public class RemainingAxiomsTable extends JXTable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8948590659747547909L;

	public RemainingAxiomsTable(List<OWLAxiom> remainingAxioms) {
		
		setBackground(Color.WHITE);
		setModel(new RemainingAxiomsTableModel(remainingAxioms));
		setRowHeight(getRowHeight() + 5);
		getColumn(0).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		getColumn(1).setMaxWidth(30);
	}
	
	public List<OWLAxiom> getSelectedAxioms(){
		return ((RemainingAxiomsTableModel)getModel()).getSelectedAxioms();
	}

}
