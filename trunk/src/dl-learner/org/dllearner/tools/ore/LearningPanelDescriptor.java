package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.dllearner.core.owl.Description;




public class LearningPanelDescriptor extends WizardPanelDescriptor implements ActionListener{
    
    public static final String IDENTIFIER = "LEARNING_PANEL";
    
    LearningPanel panel4;
    
    public LearningPanelDescriptor() {
        
        panel4 = new LearningPanel();
        panel4.addButtonListener(this);
             
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel4);
     
    }
    
    public Object getNextPanelDescriptor() {
        return LearningPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return ConceptPanelDescriptor.IDENTIFIER;
    }
    
 
    public void displayingPanel(){
    	
    }
    
    class Result extends SwingWorker<Description, Void>
    {
      @Override 
      public Description doInBackground()
      {		
    	  getWizardModel().getOre().start();
    	  Description result = getWizardModel().getOre().getLearningResult();
    	
      	      	      
      	return result;
      }
      
      public void done(){
    	  Description result=null;
		try {
			result = get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  panel4.setResult(result.toString());
      }
      
      
      
    }

	
    
    


	@Override
	public void actionPerformed(ActionEvent arg0) {
		new Result().execute();
		
	}}
