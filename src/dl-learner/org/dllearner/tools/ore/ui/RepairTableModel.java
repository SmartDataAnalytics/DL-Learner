package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

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
	private List<OWLAxiom> axioms2Remove;
	
    public RepairTableModel(RepairManager impMan)
    {
      
    	axioms2Remove = new ArrayList<OWLAxiom>();
        this.repMan = impMan;
        repMan.addListener(this);
        rebuildData();
    }

    private void rebuildData()
    {
    	axioms2Remove.clear();
    	axioms2Remove.addAll(repMan.getAxioms2Remove());
        Collections.sort(axioms2Remove);
        fireTableDataChanged();
    }



    public int getRowCount()
    {
        return axioms2Remove.size();
    }

    public int getColumnCount()
    {
        return 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return axioms2Remove.get(rowIndex);
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
