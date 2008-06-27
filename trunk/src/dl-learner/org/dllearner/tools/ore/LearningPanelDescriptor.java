package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;




public class LearningPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener{
    
    public static final String IDENTIFIER = "LEARNING_PANEL";
    
    LearningPanel panel4;
    ResultSwingWorker worker;
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
        return ConceptPanelDescriptor.IDENTIFIER;
    }
    
   
    
    class ResultSwingWorker extends
			SwingWorker<List<Description>, List<Description>> {
		LearningAlgorithm la;
	

		@SuppressWarnings("unchecked")
		@Override
		public List<Description> doInBackground() {
			
			panel4.getResultList().setCellRenderer(new ColumnListCellRenderer(getWizardModel().getOre()));
			panel4.getLoadingLabel().setBusy(true);
			panel4.getStatusLabel().setText("Learning");
			getWizardModel().getOre().setNoise(panel4.getNoise());
			
			
			
			la = getWizardModel().getOre().start();//started endlosen Algorithmus
			publish(la.getCurrentlyBestDescriptions(10));
			
					
			List<Description> result = getWizardModel().getOre().getLearningResults(100);

			return result;
		}

		@Override
		public void done() {
			
		
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
				updateList(list);
			}
		}
		
		void updateList(final List<Description> result) {
			Runnable doUpdateList = new Runnable() {

				public void run() {
					panel4.getModel().clear();
					for (Description d : result) {
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
			canceled = true;
			panel4.getStopButton().setEnabled(false);
			getWizardModel().getOre().getLa().stop();
            panel4.getStartButton().setEnabled(true);
            panel4.getStatusLabel().setText("Algorithm aborted");
            panel4.getLoadingLabel().setBusy(false);
		}
		
		
		
	}

	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected();
		if (!e.getValueIsAdjusting()){
			getWizardModel().getOre().setConceptToAdd((Description)(panel4.getResultList().getSelectedValue())); 
			
			for(Description d: getWizardModel().getOre().getAllChildren((Description)(panel4.getResultList().getSelectedValue()))){
				System.out.println(d + " : " + d.getClass());
				
				if(d instanceof ObjectSomeRestriction){

					
					getWizardModel().getOre().getIndividualsOfPropertyRange((ObjectSomeRestriction)d);

					
					
				}
				
			}
		
		}
		
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
