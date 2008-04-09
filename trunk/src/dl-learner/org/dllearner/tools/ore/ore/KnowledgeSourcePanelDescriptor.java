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
    
    KnowledgeSourcePanel panel2;
    
    public KnowledgeSourcePanelDescriptor() {
        
        panel2 = new KnowledgeSourcePanel();
    
        panel2.addListeners(this, this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel2);
        
    }
    
    public Object getNextPanelDescriptor() {
        return ConceptPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return IntroductionPanelDescriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        setNextButtonAccordingToExistingOWLFile();
    }    

    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	if(cmd.equals("browse")){
			panel2.openFileChooser();
		}
    	    	
        setNextButtonAccordingToExistingOWLFile();
    }
    
  
            
    
    private void setNextButtonAccordingToExistingOWLFile() {
         
    	if (panel2.isExistingOWLFile()){
    		getWizardModel().getOre().setKnowledgeSource(panel2.getOWLFile());
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
  class ConceptRetriever extends SwingWorker<Set<NamedClass>, NamedClass>
  {
    @Override 
    public Set<NamedClass> doInBackground()
    {		
  	  getWizardModel().getOre().detectReasoner();
  	  Set<NamedClass> ind = getWizardModel().getOre().getReasoningService().getAtomicConcepts();
  	  ConceptPanelDescriptor nextPanel = (ConceptPanelDescriptor)getWizardModel().getPanelHashMap().get(getNextPanelDescriptor());
  	  nextPanel.panel3.getModel().clear();
   
    	for (NamedClass cl : ind){
    		publish(cl);
    		 nextPanel.panel3.getModel().addElement(cl);
    		System.out.println(cl.toString());
    	}
    	return ind;
    }

    
    
    
  }
	
	
    
    
    
}
