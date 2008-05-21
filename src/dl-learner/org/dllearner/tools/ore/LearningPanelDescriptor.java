package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

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
    Timer timer;
    
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
        return ConceptPanelDescriptor.IDENTIFIER;
    }
    
   
    
    class ResultSwingWorker extends
			SwingWorker<List<Description>, List<Description>> {
		LearningAlgorithm la;

		@Override
		public List<Description> doInBackground() {
			panel4.getResultList().setCellRenderer(new ColumnListCellRenderer(getWizardModel().getOre()));
			panel4.getLoadingLabel().setBusy(true);
			panel4.getStatusLabel().setText("Learning");
			la = getWizardModel().getOre().start();
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					publish(getWizardModel().getOre().getLearningResults(10));
				}

			}, 0, 1000);

			List<Description> result = getWizardModel().getOre()
					.getLearningResults(5);

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
			panel4.getModel().clear();
			for (List<Description> list : resultLists) {
				for( Description d : list)
					System.out.println(d);
				updateList(list);
			}
		}
		
		void updateList(final List<Description> result) {
			Runnable doUpdateList = new Runnable() {

				public void run() {
					panel4.getModel().clear();
					for (Description d : result) {
						System.err.println(d+"=="+getWizardModel().getOre().getCorrectness(d));
						
						panel4.getModel().addElement(d);
					}

				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}
		
//		void updateList(final List<Description> result) {
//			Runnable doUpdateList = new Runnable() {
//
//				public void run() {
//					
//				
//					int i = panel4.getModel().getRowCount();
//					if(!(i == 0))
//						for(int j = panel4.getModel().getRowCount(); j >= 0 ; j--){
//							System.out.println(panel4.getModel().getRowCount());
//							panel4.getModel().removeRow(j);
//						}
//					
//											
//						
//					
//					for (Description d : result) {
//						Object[] rowData = new Object[2];
//						rowData[0] = d;
//						rowData[1] = getWizardModel().getOre().getCorrectness(d);
//						System.err.println(d+"=="+rowData[1]);
//						
//						panel4.getModel().addRow(rowData );
//					}
//
//				}
//			};
//			SwingUtilities.invokeLater(doUpdateList);
//
//		}

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
			getWizardModel().getOre().getLa().stop();
            panel4.getStartButton().setEnabled(true);
            panel4.getStatusLabel().setText("Algorithm aborted");
		}
		
		
		
	}

	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected();
		if (!e.getValueIsAdjusting()) 
			getWizardModel().getOre().setConceptToAdd((Description)(panel4.getResultList().getSelectedValue())); 
			System.out.println(panel4.getResultList().getSelectedValue());
		
	}

	@Override
	public void aboutToDisplayPanel() {
        setNextButtonAccordingToConceptSelected();
    }    
	
	private void setNextButtonAccordingToConceptSelected() {
        
    	if (panel4.getResultList().getSelectedValue()!= null){
    		getWizard().setNextFinishButtonEnabled(true);
    	}else{
    		getWizard().setNextFinishButtonEnabled(false);
    	}
   
    }
}
