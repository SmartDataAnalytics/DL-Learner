package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class ChangesTableModel extends AbstractTableModel {

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 2){
			return true;
		}
		return super.isCellEditable(rowIndex, columnIndex);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7573340273483900311L;
	
	private List<OWLOntologyChange> changes;
	
	public ChangesTableModel(){
		changes = new ArrayList<OWLOntologyChange>();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return changes.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			if(changes.get(rowIndex) instanceof RemoveAxiom){
				return "–";
			} else {
				return "+";
			}
		} else if(columnIndex == 1){
			return changes.get(rowIndex).getAxiom();
		}
		else {
			return new LinkLabel("Undo");
		}
		
	}
	
	public void addChanges(List<OWLOntologyChange> changes){
		this.changes.addAll(changes);
		fireTableDataChanged();
	}
	
	public void removeChanges(List<OWLOntologyChange> changes){
		this.changes.removeAll(changes);
		fireTableDataChanged();
	}
	
	public void removeChange(int rowIndex){
		List<OWLOntologyChange> undoChanges = new ArrayList<OWLOntologyChange>(1);
		undoChanges.add(changes.get(rowIndex));
		this.changes.remove(rowIndex);
		OREManager.getInstance().getModifier().undoChanges(undoChanges);
		fireTableDataChanged();
	}
	
	public void clear(){
		this.changes.clear();
		fireTableDataChanged();
	}
	
	public List<OWLOntologyChange> getChanges(){
		return changes;
	}

}
