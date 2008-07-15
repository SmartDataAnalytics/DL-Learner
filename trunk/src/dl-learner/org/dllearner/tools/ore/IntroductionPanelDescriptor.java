package org.dllearner.tools.ore;



public class IntroductionPanelDescriptor extends WizardPanelDescriptor {
    
    public static final String IDENTIFIER = "INTRODUCTION_PANEL";
       
    public IntroductionPanelDescriptor() {
        super(IDENTIFIER, new IntroductionPanel());
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return null;
    }
    
   
    
}
