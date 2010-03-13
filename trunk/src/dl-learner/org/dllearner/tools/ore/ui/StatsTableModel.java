package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class StatsTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3186572572333098359L;
	private List<OWLOntologyChange> changes;
	
	public StatsTableModel(){
		changes = new ArrayList<OWLOntologyChange>();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return changes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			if(changes.get(rowIndex) instanceof RemoveAxiom){
				return "Removed";
			} else {
				return "Added";
			}
		} else {
			return ManchesterSyntaxRenderer.render(changes.get(rowIndex).getAxiom(), false, 0);
		}
	}
	
	@Override
	public String getColumnName(int column) {
		if(column == 0){
			return "Action";
		} else if(column == 1){
			return "Axiom";
		} else {
			return "";
		}
	}
	
	public void setChanges(List<OWLOntologyChange> changes){
		this.changes.clear();
		this.changes.addAll(changes);
		fireTableDataChanged();
	}

}
