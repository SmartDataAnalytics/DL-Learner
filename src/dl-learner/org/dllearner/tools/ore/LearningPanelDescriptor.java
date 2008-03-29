package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.dllearner.core.owl.Description;




public class LearningPanelDescriptor extends WizardPanelDescriptor implements ActionListener{
    
    public static final String IDENTIFIER = "LEARNING_PANEL";
    
    LearningPanel panel4;
    ResultSwingWorker worker;
    Timer timer;
    
    public LearningPanelDescriptor() {
        
        panel4 = new LearningPanel();
        panel4.addStartButtonListener(this);
        panel4.addStopButtonListener(this);
        
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
    
    class ResultSwingWorker extends SwingWorker<Description, Description>
    {
      @Override 
      public Description doInBackground() {
			
			getWizardModel().getOre().start();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				public void run() {
					System.err.println(getWizardModel().getOre()
							.getLearningResults(3));
				}
				
			}, 0, 1000);
      
			
			Description result = getWizardModel().getOre().getLearningResult();

			return result;
		}
      
      public void done() {
    	  timer.cancel();
    	  Description result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			} catch (ExecutionException e) {
			
				e.printStackTrace();
			}
			panel4.getStartButton().setEnabled(true);
			panel4.getStopButton().setEnabled(false);

			panel4.setResult(result.toString());
		}
      
      
      
    }

	
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("Start")){
			panel4.getStartButton().setEnabled(false);
            panel4.getStopButton().setEnabled(true);
            worker = new ResultSwingWorker();
            worker.execute();
		}
		else{
			panel4.getStopButton().setEnabled(false);
            worker.cancel(true);
        	panel4.getStartButton().setEnabled(true);
		}
		
		
		
	}}
