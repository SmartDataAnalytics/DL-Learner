package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;

public class RepairTableModel extends AbstractTableModel implements RepairManagerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5898900692701380258L;
	private RepairManager repMan;
	private List<OWLAxiom> axioms;
	
    public RepairTableModel()
    {
    	axioms = new ArrayList<OWLAxiom>();
        this.repMan = RepairManager.getRepairManager(OREManager.getInstance());
        repMan.addListener(this);
        rebuildData();
    }

    private void rebuildData()
    {
    	axioms.clear();
    	axioms.addAll(repMan.getAxioms2Remove());
    	axioms.addAll(repMan.getAxioms2Keep());
        Collections.sort(axioms);
        fireTableDataChanged();
    }

    public int getRowCount()
    {
        return axioms.size();
    }

    public int getColumnCount()
    {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
    	if(columnIndex == 1){
    		return ManchesterSyntaxRenderer.render(axioms.get(rowIndex), false, 0);
    	} else {
    		   return axioms.get(rowIndex);
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

}
