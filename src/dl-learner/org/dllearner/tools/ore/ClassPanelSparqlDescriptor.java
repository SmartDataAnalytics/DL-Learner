package org.dllearner.tools.ore;





public class ClassPanelSparqlDescriptor extends WizardPanelDescriptor{
    
    public static final String IDENTIFIER = "CLASS_CHOOSE_SPARQL_PANEL";
    public static final String INFORMATION = "In this panel all atomic classes in the ontology are shown in the list above. " +
    										 "Select one of them which should be (re)learned from then press \"Next-Button\"";
    
    ClassPanelSparql panel3;
    
    public ClassPanelSparqlDescriptor() {
        panel3 = new ClassPanelSparql();
       
             
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
        
    }
    
  
	
	
	

	
    
   

    
    
}
