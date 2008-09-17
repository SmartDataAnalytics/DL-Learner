/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
    public static final String INFORMATION = "In this panel you can start the learning algorithm. While it ist running, " +
	 "temporary results are shown in the list above. Select one of them and press Next";
    
    private LearningPanel learnPanel;
    private LearnSwingWorker worker;
    private LearningAlgorithm la;
    private Timer timer;
   
    
    public LearningPanelDescriptor() {
        
        learnPanel = new LearningPanel();
        learnPanel.addStartButtonListener(this);
        learnPanel.addStopButtonListener(this);
        learnPanel.addSelectionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(learnPanel);
        
     
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
    	getWizard().getInformationField().setText(INFORMATION);
	    setNextButtonAccordingToConceptSelected();
	}



	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected();
		
//		Description range = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//				new NamedClass("http://example.com/father#female"));
//		ObjectAllRestriction role = new ObjectAllRestriction(new ObjectProperty("http://example.com/father#hasChild"),
//				range);
//		Description de = new NamedClass("http://example.com/father#male");
		
		if (!e.getValueIsAdjusting()){
			getWizardModel().getOre().setNewClassDescription((Description)(learnPanel.getResultList().getSelectedValue())); 
			
					
		}
		
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("Start")){
			learnPanel.getListModel().clear();
			learnPanel.getStartButton().setEnabled(false);
	        learnPanel.getStopButton().setEnabled(true);
	        worker = new LearnSwingWorker();
	        worker.execute();
		}
		else{
			
			learnPanel.getStopButton().setEnabled(false);
			la.stop();
	        timer.cancel();
			learnPanel.getStartButton().setEnabled(true);
	        learnPanel.getStatusLabel().setText("Algorithm aborted");
	        learnPanel.getLoadingLabel().setBusy(false);
	        
		}
		
		
		
	}



	private void setNextButtonAccordingToConceptSelected() {
	    
		if (learnPanel.getResultList().getSelectedValue()!= null){
			getWizard().setNextFinishButtonEnabled(true);
		}else{
			getWizard().setNextFinishButtonEnabled(false);
		}
	
	}
	
	public LearnSwingWorker getWorkerThread(){
		return worker;
	}
	
	public Timer getTimer(){
		return timer;
	}
	
	public LearningAlgorithm getLa() {
		return la;
	}
	
	public void setPanelDefaults(){
		learnPanel.getListModel().clear();
		learnPanel.getStatusLabel().setText("");
	}



	class LearnSwingWorker extends SwingWorker<List<Description>, List<Description>> {
		
    	
    	Thread t;
    	
    	
    	
		@SuppressWarnings("unchecked")
		@Override
		public List<Description> doInBackground() {
			
			learnPanel.getResultList().setCellRenderer(new ColumnListCellRenderer(getWizardModel().getOre()));
			learnPanel.getLoadingLabel().setBusy(true);
			learnPanel.getStatusLabel().setText("Learning");
			getWizardModel().getOre().setNoise(learnPanel.getNoise());
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
			
			learnPanel.getStartButton().setEnabled(true);
			learnPanel.getStopButton().setEnabled(false);
			updateList(result);
			learnPanel.getLoadingLabel().setBusy(false);
			learnPanel.getStatusLabel().setText("Algorithm terminated successfully.");
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
//					learnPanel.getListModel().clear();
					for (Description d : result) {
						dm.addElement(d);
//						panel4.getModel().addElement(d);
						
					}
					learnPanel.getResultList().setModel(dm);

				}
			};
			SwingUtilities.invokeLater(doUpdateList);

		}
		

		

	}
}
