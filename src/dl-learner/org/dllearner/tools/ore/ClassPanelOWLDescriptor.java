package org.dllearner.tools.ore;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;




public class ClassPanelOWLDescriptor extends WizardPanelDescriptor implements ListSelectionListener{
    
    public static final String IDENTIFIER = "CLASS_CHOOSE_OWL_PANEL";
    public static final String INFORMATION = "In this panel all atomic classes in the ontology are shown in the list above. " +
    										 "Select one of them which should be (re)learned from then press \"Next-Button\"";
    
    ClassPanelOWL panel3;
    
    public ClassPanelOWLDescriptor() {
        panel3 = new ClassPanelOWL();
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
    	getWizard().getInformationField().setText(INFORMATION);
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
