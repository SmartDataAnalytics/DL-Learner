package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class StatsTable extends JXTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -653996873095101940L;

	public StatsTable(){
		
		setModel(new StatsTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setEditable(false);
		setGridColor(Color.LIGHT_GRAY);
		setRowHeight(getRowHeight() + 4);
		getColumn(0).setMaxWidth(100);
		setRowSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
	}
	
	public void setChanges(List<OWLOntologyChange> changes){
		((StatsTableModel)getModel()).setChanges(changes);
	}
	
}
