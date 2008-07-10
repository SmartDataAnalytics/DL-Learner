package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.Individual;
import org.semanticweb.owl.model.OWLOntologyChange;




public class RepairPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener, MouseListener{
    
    public static final String IDENTIFIER = "REPAIR_PANEL";
    
    RepairPanel panel4;
    private Set<OWLOntologyChange> ontologyChanges;
   
    
    public RepairPanelDescriptor() {
        
        panel4 = new RepairPanel();
       
        panel4.addSaveButtonListener(this);
        panel4.addSelectionListeners(this);
        panel4.addMouseListeners(this);
       
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel4);
        ontologyChanges = new HashSet<OWLOntologyChange>();
     
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return RepairPanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return LearningPanelDescriptor.IDENTIFIER;
    }
    
   
    
   

	public void valueChanged(ListSelectionEvent e) {
		
//		if (!e.getValueIsAdjusting()) 
//			 System.err.println(panel4.getNegFailureList().getSelectedValue());
		
	}

	public void actionPerformed(ActionEvent event) {
		System.out.println(getOntologyChanges());
		if(event.getActionCommand().equals("save")){
			getWizardModel().getOre().getModi().saveOntology();
			            
		}
		
		
	}
	
	public void mouseClicked(MouseEvent e) {
		
		if(e.getClickCount() == 2 && e.getSource() == panel4.getNegFailureList() ){
			Individual ind = (Individual)panel4.getNegFailureList().getSelectedValue();
			ontologyChanges.addAll(new NegExampleRepairDialog(ind, getWizard().getDialog(), getWizardModel().getOre()).getAllChanges());
			getWizardModel().getOre().getModi().saveOntology();
		}
		
	}

	public void mouseEntered(MouseEvent e) {
			
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public Set<OWLOntologyChange> getOntologyChanges() {
		return ontologyChanges;
	}
	
}


