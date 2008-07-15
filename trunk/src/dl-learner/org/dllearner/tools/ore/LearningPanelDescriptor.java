package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Description;




public class LearningPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener{
    
    public static final String IDENTIFIER = "LEARNING_PANEL";
    
    LearningPanel panel4;
    ResultSwingWorker worker;
    LearningAlgorithm la;
    Timer timer;
    Boolean canceled = false;
    
    public LearningPanelDescriptor() {
        
        panel4 = new LearningPanel();
        panel4.addStartButtonListener(this);
        panel4.addStopButtonListener(this);
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
        return ClassPanelOWLDescriptor.IDENTIFIER;
    }
    
   
    
    @Override
	public void aboutToDisplayPanel() {
	    setNextButtonAccordingToConceptSelected();
	}



	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected();
		
//		ObjectAllRestriction role = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//						new NamedClass("http://example.com/father#female"));
//		Description de = new Intersection(new NamedClass("http://example.com/father#male"), role);
		
		if (!e.getValueIsAdjusting()){
			getWizardModel().getOre().setConceptToAdd((Description)(panel4.getResultList().getSelectedValue())); 
			
					
		}
		
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("Start")){
			panel4.getModel().clear();
			panel4.getStartButton().setEnabled(false);
	        panel4.getStopButton().setEnabled(true);
	        worker = new ResultSwingWorker();
	        worker.execute();
		}
		else{
			canceled = true;
			panel4.getStopButton().setEnabled(false);
			la.stop();
	        timer.cancel();
			panel4.getStartButton().setEnabled(true);
	        panel4.getStatusLabel().setText("Algorithm aborted");
	        panel4.getLoadingLabel().setBusy(false);
	        
		}
		
		
		
	}



	private void setNextButtonAccordingToConceptSelected() {
	    
		if (panel4.getResultList().getSelectedValue()!= null){
			getWizard().setNextFinishButtonEnabled(true);
		}else{
			getWizard().setNextFinishButtonEnabled(false);
		}
	
	}



	class ResultSwingWorker extends SwingWorker<List<Description>, List<Description>> {
		
    	
    	Thread t;
    	
    	
    	
		@SuppressWarnings("unchecked")
		@Override
		public List<Description> doInBackground() {
			
			panel4.getResultList().setCellRenderer(new ColumnListCellRenderer(getWizardModel().getOre()));
			panel4.getLoadingLabel().setBusy(true);
			panel4.getStatusLabel().setText("Learning");
			getWizardModel().getOre().setNoise(panel4.getNoise());
			la = getWizardModel().getOre().getLa();
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					if(la != null){
						
						publish(la.getCurrentlyBestDescriptions(30, true));
					}
				}
				
			}, 1000, 2000);
			
			
			t = new Thread(new Runnable(){

				@Override
				public void run() {
					
					getWizardModel().getOre().start();
				}
				
			});
//			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			
			
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			List<Description> result = getWizardModel().getOre().getLearningResults(30);
			
			return result;
		}

		@Override
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
			panel4.getLoadingLabel().setBusy(false);
			panel4.getStatusLabel().setText("Algorithm terminated successfully.");
		}

		@Override
		protected void process(List<List<Description>> resultLists) {
			
//			panel4.getModel().clear();
			
			for (List<Description> list : resultLists) {
				updateList(list);
			}
		}
		
		void updateList(final List<Description> result) {
			
			Runnable doUpdateList = new Runnable() {
				
				
				DefaultListModel dm = new DefaultListModel();
				public void run() {
					panel4.getModel().clear();
					for (Description d : result) {
						dm.addElement(d);
//						panel4.getModel().addElement(d);
						
					}
					panel4.getResultList().setModel(dm);

				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}
		

		public LearningAlgorithm getLa() {
			return la;
		}

	}
}
