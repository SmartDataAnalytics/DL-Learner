package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.ImpactManagerListener;
import org.dllearner.tools.ore.OREManager;
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
	private RepairManager repMan;
	private List<OWLAxiom> impact;
	
    public ImpactTableModel()
    {
    	 this.impMan = ImpactManager.getInstance(OREManager.getInstance());
    	 this.repMan = RepairManager.getRepairManager(OREManager.getInstance());
      
        impact = new ArrayList<OWLAxiom>();
     
        repMan.addListener(this);
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


    @Override
    public int getRowCount()
    {
        return impact.size();
    }
    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
    	if(rowIndex >= 0){
    		if(columnIndex == 1){
    			return impact.get(rowIndex);
    		} else {
    			return repMan.getAxioms2Keep().contains(getOWLAxiomAtRow(rowIndex));
    		}
    	}
    	return null;
    }
    
    @Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 1) {
			return OWLAxiom.class;
		} else {
			return Boolean.class;
		} 
	}
    
    @Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return true;
		return false;
	}
    
    @Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			OWLAxiom ax = getOWLAxiomAtRow(rowIndex);
			if(!repMan.getAxioms2Keep().contains(ax)){
				repMan.addAxiom2Keep(ax);
			} else {
				repMan.removeAxiom2Keep(ax);
			}
		}
		super.setValueAt(value, rowIndex, columnIndex);
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
	
	public OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return impact.get(rowIndex);
	}
	

}
