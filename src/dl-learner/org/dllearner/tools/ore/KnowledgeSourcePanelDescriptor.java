package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dllearner.core.owl.NamedClass;


public class KnowledgeSourcePanelDescriptor extends WizardPanelDescriptor implements ActionListener, DocumentListener{
    
    public static final String IDENTIFIER = "KNOWLEDGESOURCE_CHOOSE_PANEL";
    public static final String INFORMATION = "Select the KnowledgeSource(OWL-FILE) on which you want to work and " +
    										"then press \"Next\"-button";
    
    private KnowledgeSourcePanel knowledgePanel;
    
    public KnowledgeSourcePanelDescriptor() {
        
        knowledgePanel = new KnowledgeSourcePanel();
    
        knowledgePanel.addListeners(this, this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(knowledgePanel);
        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    	if(getWizard().getKnowledgeSourceType() == 0)
    		return ClassPanelOWLDescriptor.IDENTIFIER;
    	else
    		return ClassPanelSparqlDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return IntroductionPanelDescriptor.IDENTIFIER;
    }
    
    
    @Override
	public void aboutToDisplayPanel() {
        getWizard().getInformationField().setText(INFORMATION);
    	setNextButtonAccordingToExistingOWLFile();
    }    

    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	if(cmd.equals("browse")){
			knowledgePanel.openFileChooser();
		}else if(cmd.equals("OWL")){
			knowledgePanel.setOWLMode();
			getWizard().setKnowledgeSourceType(0);
		}else if(cmd.equals("SPARQL")){
			knowledgePanel.setSPARQLMode();
			getWizard().setKnowledgeSourceType(1);
		}
    	    	
        setNextButtonAccordingToExistingOWLFile();
    }
    
  
            
    
    private void setNextButtonAccordingToExistingOWLFile() {
         
    	if (knowledgePanel.isExistingOWLFile()){
    		getWizardModel().getOre().setKnowledgeSource(knowledgePanel.getOWLFile());
        	getWizard().setNextFinishButtonEnabled(true);
//        	new ConceptRetriever().execute();
//            System.err.println("test");    
        }
    

        	 
         
            
         else
            getWizard().setNextFinishButtonEnabled(false);           
    
    }
   

	public void changedUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}

	public void insertUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}

	public void removeUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}
  public KnowledgeSourcePanel getPanel() {
		return knowledgePanel;
	}
  
  class ConceptRetriever extends SwingWorker<Set<NamedClass>, NamedClass>
  {
    @Override 
    public Set<NamedClass> doInBackground()
    {		
  	  getWizardModel().getOre().detectReasoner();
  	  Set<NamedClass> ind = getWizardModel().getOre().getReasoningService().getAtomicConcepts();
  	  ClassPanelOWLDescriptor nextPanel = (ClassPanelOWLDescriptor)getWizardModel().getPanelHashMap().get(getNextPanelDescriptor());
  	  nextPanel.panel3.getModel().clear();
   
    	for (NamedClass cl : ind){
    		publish(cl);
    		 nextPanel.panel3.getModel().addElement(cl);
    		
    	}
    	return ind;
    }

    
    
    
  }
	
	
    
    
    
}
