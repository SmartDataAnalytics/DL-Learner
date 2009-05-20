package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.semanticweb.owl.model.OWLAxiom;

public class RepairTableModel extends AbstractTableModel implements ImpactManagerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5898900692701380258L;
	private ImpactManager impMan;
	private List<OWLAxiom> axioms2Remove;
	
    public RepairTableModel(ImpactManager impMan)
    {
      
    	axioms2Remove = new ArrayList<OWLAxiom>();
        this.impMan = impMan;
        impMan.addListener(this);
        rebuildData();
    }

    private void rebuildData()
    {
    	axioms2Remove.clear();
    	axioms2Remove.addAll(impMan.getAxioms2Remove());
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
	public void axiomForImpactChanged() {
		rebuildData();
		
	}

	@Override
	public void repairPlanExecuted() {
		rebuildData();
		
	}

}
