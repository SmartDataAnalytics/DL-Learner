package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class RepairTableModel extends AbstractTableModel implements RepairManagerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5898900692701380258L;
	private RepairManager repMan;
	private List<OWLOntologyChange> repairPlan;

	public RepairTableModel() {
		repairPlan = new ArrayList<OWLOntologyChange>();
		this.repMan = RepairManager.getInstance(OREManager.getInstance());
		repMan.addListener(this);
		rebuildData();
	}

	private void rebuildData() {
		repairPlan.clear();
		repairPlan.addAll(repMan.getRepairPlan());
		fireTableDataChanged();
	}

	public int getRowCount() {
		return repairPlan.size();
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		if(columnIndex == 0){
			if (repairPlan.get(rowIndex) instanceof RemoveAxiom) {
				return "–";
			} else {
				return "+";
			}
		} else if (columnIndex == 1) {
			return ManchesterSyntaxRenderer.render(repairPlan.get(rowIndex)
					.getAxiom(), false, 0);
		} else {
			return null;
		}
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		rebuildData();
	}

	@Override
	public void repairPlanChanged() {
		rebuildData();
	}

	public OWLOntologyChange getChangeAt(int rowIndex) {
		return repairPlan.get(rowIndex);
	}

}
