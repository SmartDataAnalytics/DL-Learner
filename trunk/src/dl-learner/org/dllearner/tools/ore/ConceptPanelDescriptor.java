package org.dllearner.tools.ore;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;




public class ConceptPanelDescriptor extends WizardPanelDescriptor implements ListSelectionListener{
    
    public static final String IDENTIFIER = "CONCEPT_CHOOSE_PANEL";
    
    ConceptPanel panel3;
    
    public ConceptPanelDescriptor() {
        
        panel3 = new ConceptPanel();
        panel3.addSelectionListener(this);
             
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel3);
     
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return LearningPanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public void aboutToDisplayPanel() {
        setNextButtonAccordingToConceptSelected();
    }    


	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected(); 
		if (!e.getValueIsAdjusting()) 
			 getWizardModel().getOre().setConcept((NamedClass)panel3.getList().getSelectedValue());
			
	}
	
	private void setNextButtonAccordingToConceptSelected() {
        
    	if (panel3.getList().getSelectedValue()!= null){
    		getWizard().setNextFinishButtonEnabled(true);
    	}else{
    		getWizard().setNextFinishButtonEnabled(false);
    	}
   
        }
    
   

    
    
}
