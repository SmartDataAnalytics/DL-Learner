package org.dllearner.tools.ore.ui;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.semanticweb.owl.model.OWLAxiom;

public class RepairTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1879034591819021799L;

	private RepairManager repMan;
	
	public RepairTableCellRenderer(){
		this.repMan = RepairManager.getRepairManager(OREManager.getInstance());
	}
	
	@Override
	protected void setValue(Object value) {
		if(value instanceof OWLAxiom){
			if(repMan.getAxioms2Keep().contains((OWLAxiom)value)){
				setIcon(new ImageIcon("src/dl-learner/org/dllearner/tools/ore/plus.png"));
			}
			else {
				setIcon(new ImageIcon("src/dl-learner/org/dllearner/tools/ore/minus.png"));
			}
		} else {
			super.setValue(value);
		}
	}
}
