package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dllearner.core.LearningAlgorithm;
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
    
   
    
    class ResultSwingWorker extends
			SwingWorker<List<Description>, List<Description>> {
		LearningAlgorithm la;

		@Override
		public List<Description> doInBackground() {

			la = getWizardModel().getOre().start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				public void run() {
					publish(getWizardModel().getOre().getLearningResults(5));
				}

			}, 0, 1000);

			List<Description> result = getWizardModel().getOre()
					.getLearningResults(5);

			return result;
		}

		public void done() {
			timer.cancel();
			List<Description> result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			panel4.getStartButton().setEnabled(true);
			panel4.getStopButton().setEnabled(false);
			updateList(result);

		}

		@Override
		protected void process(List<List<Description>> resultLists) {
			panel4.getModel().clear();
			for (List<Description> list : resultLists) {
				for( Description d : list)
					System.out.println(d);
				updateList(list);
			}
		}
		
		void updateList(final List<Description> result) {
			Runnable doUpdateList = new Runnable() {

				@Override
				public void run() {
					panel4.getModel().clear();
					for (Description d : result) {
						panel4.getModel().addElement(d);
					}

				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}

		public LearningAlgorithm getLa() {
			return la;
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
            worker.getLa().stop();
        	panel4.getStartButton().setEnabled(true);
		}
		
		
		
	}}
