package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.ImpactManagerListener;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;

public class ImpactTableModel extends AbstractTableModel implements ImpactManagerListener, RepairManagerListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109818990550020196L;
	private ImpactManager impMan;
	private List<OWLAxiom> impact;
	
    public ImpactTableModel(ImpactManager impMan)
    {
      
        impact = new ArrayList<OWLAxiom>();
        this.impMan = impMan;
        impMan.addListener(this);
        rebuildData();
    }

    private void rebuildData()
    {
    	impact.clear();
    	impact.addAll(impMan.getImpactForAxioms2Remove());
        Collections.sort(impact);
        fireTableDataChanged();
    }



    public int getRowCount()
    {
        return impact.size();
    }

    public int getColumnCount()
    {
        return 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return impact.get(rowIndex);
    }

    
	@Override
	public void impactListChanged() {
		rebuildData();
		
	}


	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		rebuildData();
		
	}
	

}
