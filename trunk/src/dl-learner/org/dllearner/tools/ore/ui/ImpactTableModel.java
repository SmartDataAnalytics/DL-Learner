package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.ImpactManagerListener;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.TaskManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class ImpactTableModel extends AbstractTableModel implements ImpactManagerListener, RepairManagerListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109818990550020196L;
	private ImpactManager impMan;
	private RepairManager repMan;
	private List<OWLAxiom> impact;
	private Set<OWLAxiom> lostEntailments;
	private Set<OWLAxiom> addedEntailemnts;
	private OWLOntology ont;
	
    public ImpactTableModel()
    {
    	 this.impMan = ImpactManager.getInstance(OREManager.getInstance());
    	 this.repMan = RepairManager.getInstance(OREManager.getInstance());
      
        impact = new ArrayList<OWLAxiom>();
        lostEntailments = new HashSet<OWLAxiom>();
        addedEntailemnts = new HashSet<OWLAxiom>();
     
        ont = OREManager.getInstance().getPelletReasoner().getOWLAPIOntologies();
        
        repMan.addListener(this);
//        impMan.addListener(this);
    }
   
    private void rebuildData()
    {	
    	impact.clear();
    	lostEntailments.clear();
        addedEntailemnts.clear();
        lostEntailments.addAll(impMan.getLostEntailments());
        addedEntailemnts.addAll(impMan.getAddedEntailments());
    	impact.addAll(lostEntailments);
    	impact.addAll(addedEntailemnts);
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
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
    
    	if(columnIndex == 0){
    		if(lostEntailments.contains(getOWLAxiomAtRow(rowIndex))){
				return "Lost";
			}
			else {
				return "Added";
			}
    		
    	} else if(columnIndex == 1){
    		return impact.get(rowIndex);
    	} else {
    		if(lostEntailments.contains(getOWLAxiomAtRow(rowIndex))){
				return "Keep";
			}
			else {
				return "";
			}
    	}
    	
    }
    
    @Override
	public Class<? extends Object> getColumnClass(int columnIndex){
		if(columnIndex == 0) {
			return String.class;
		} else if(columnIndex == 1){
			return OWLAxiom.class;
		} else {
			return String.class;
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
			AddAxiom add = new AddAxiom(ont, ax);
			if(!repMan.getRepairPlan().contains(add)){
				repMan.addToRepairPlan(add);
			} else {
				repMan.removeFromRepairPlan(add);
			}
		}
		super.setValueAt(value, rowIndex, columnIndex);
	}
    
    public void addToRepairPlan(int rowIndex){
    	OWLAxiom ax = impact.get(rowIndex);
    	if(lostEntailments.contains(ax)){
    		repMan.addToRepairPlan(new AddAxiom(ont, ax));
    	} else {
    		repMan.addToRepairPlan(new RemoveAxiom(ont, ax));
    	}
    }
    
    public boolean isLostEntailment(int rowIndex){
    	return lostEntailments.contains(getOWLAxiomAtRow(rowIndex));
    }

    
	@Override
	public void impactListChanged() {
//		rebuildData();
		
	}


	@Override
	public void repairPlanChanged() {
		new LostEntailmentsComputingTask().execute();
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		impact.clear();
    	lostEntailments.clear();
        addedEntailemnts.clear();
        fireTableDataChanged();
		
	}
	
	public OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return impact.get(rowIndex);
	}
	
	class LostEntailmentsComputingTask extends SwingWorker<Void, Void>{
		
	
		@Override
		public Void doInBackground() {
			TaskManager.getInstance().setTaskStarted("Computing impact");
			impMan.computeImpactForAxiomsInRepairPlan();
			return null;
		}

		@Override
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					rebuildData();
					
				}
			});
			
			TaskManager.getInstance().setTaskFinished();
		}
	}
	

}
