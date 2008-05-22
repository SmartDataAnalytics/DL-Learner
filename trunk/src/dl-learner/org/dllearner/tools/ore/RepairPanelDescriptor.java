package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.Individual;




public class RepairPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener{
    
    public static final String IDENTIFIER = "REPAIR_PANEL";
    
    RepairPanel panel4;
   
    
    public RepairPanelDescriptor() {
        
        panel4 = new RepairPanel();
        panel4.addDeleteButtonListener(this);
        panel4.addMoveButtonListener(this);
        panel4.addAddButtonListener(this);
        panel4.addSaveButtonListener(this);
        panel4.addSelectionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel4);
        
     
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
		if (!e.getValueIsAdjusting()) 
			 System.out.println(panel4.getResultList().getSelectedValue());
		
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("save")){
			getWizardModel().getOre().getModi().saveOntology();
			            
		}
		if(event.getActionCommand().equals("delete")){
			
			int idx = panel4.getResultList().getSelectedIndex();
			if (-1 == idx) {
				//No item selected
				return;
			}
			System.out.println("Index: " +idx);
			
		
			
			getWizardModel().getOre().getModi().deleteIndividual((Individual)panel4.getResultList().getSelectedValue());
			panel4.getModel().removeElementAt(idx);
			
		
		}
		if(event.getActionCommand().equals("move")){
			
			int idx = panel4.getResultList().getSelectedIndex();
			if (-1 == idx) {
				//No item selected
				return;
			}
			System.out.println(getWizardModel().getOre().allAtomicConcepts);
			
			MoveDialog dialog = new MoveDialog(getWizardModel().getOre().allAtomicConcepts, getWizard().getDialog());
			dialog.init();
			System.err.println("Verschiebe " +(Individual)panel4.getResultList().getSelectedValue()+
					" von " + "......." + " nach " +dialog.getSelectedValue());
		
			
		}
		
	}}

