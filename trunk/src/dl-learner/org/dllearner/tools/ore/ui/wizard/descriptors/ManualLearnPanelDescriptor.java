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

package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.LearningManager.LearningType;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.ManualLearnPanel;



/**
 * Wizard panel descriptor where learned class description are shown.
 * @author Lorenz Buehmann
 *
 */
public class ManualLearnPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener, OREManagerListener{
    
    public static final String IDENTIFIER = "MANUAL_LEARN_PANEL";
    public static final String INFORMATION = "Press <Start> to start learning. While it is running, " 
	 										+ "temporary results are shown in the list above. Select one of them and press <Next>";
    
    private ManualLearnPanel learnPanel;
    private LearningTask learningTask;
    private CELOE la;
    private Timer timer;
   
    
    public ManualLearnPanelDescriptor() {
        
        learnPanel = new ManualLearnPanel();
        learnPanel.addStartButtonListener(this);
        learnPanel.addStopButtonListener(this);
        learnPanel.addSelectionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(learnPanel);
        
     
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    	EvaluatedDescriptionClass newClassDesc = OREManager.getInstance().getNewClassDescription();
    	if(newClassDesc != null && newClassDesc.getAccuracy() == 1.0){
    		return SavePanelDescriptor.IDENTIFIER;
    	} else {
    		return RepairPanelDescriptor.IDENTIFIER;
    	}
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return ClassChoosePanelDescriptor.IDENTIFIER;
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
		
		if (!e.getValueIsAdjusting() && learnPanel.getResultTable().getSelectedRow() >= 0){
			EvaluatedDescriptionClass selectedClassExpression = learnPanel.getResultTable().getSelectedValue();
			learnPanel.showInconsistencyWarning(!selectedClassExpression.isConsistent());
			OREManager.getInstance().setNewClassDescription(selectedClassExpression);
			learnPanel.updateCurrentGraphicalCoveragePanel(OREManager.getInstance().getNewClassDescription());
		}
		
		
	}

	/**
	 * Actions for pressing start- or stop-button.
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("Start")){
			learnPanel.getStartButton().setEnabled(false);
	        learnPanel.getStopButton().setEnabled(true);
	        
			String learningType = "";
			
			LearningManager.getInstance().setNoisePercentage(learnPanel.getOptionsPanel().getMinAccuracy());
			LearningManager.getInstance().setMaxExecutionTimeInSeconds(learnPanel.getOptionsPanel().getMaxExecutionTime());
			LearningManager.getInstance().setMaxNrOfResults(learnPanel.getOptionsPanel().getNrOfConcepts());
			LearningManager.getInstance().setThreshold(learnPanel.getOptionsPanel().getThreshold());
	        
			if(learnPanel.isEquivalentClassesTypeSelected()){
				LearningManager.getInstance().setLearningType(LearningType.EQUIVALENT);
				learningType = "equivalent";
			} else {
				LearningManager.getInstance().setLearningType(LearningType.SUPER);
				learningType = "super";
			}
			
			TaskManager.getInstance().setTaskStarted("Learning " + learningType + " class expressions...", false);
			
	        learnPanel.reset();
	        
//	        TaskManager.getInstance().setTaskStarted("Learning " + learningType + " class expressions...");
//	        Timer timer = new Timer();
//	        timer.schedule(new TimerTask() {
//	        	int progress = 0;
//				
//				@Override
//				public void run() {
//					progress++;
//					fillTable(LearningManager.getInstance().getCurrentlyLearnedDescriptions());
//					TaskManager.getInstance().getStatusBar().setProgress(progress);
//					
//				}
//			}, 1000, 1000);
//	        LearningManager.getInstance().learnAsynchronously();
	       
	        learningTask = new LearningTask();
	        learningTask.addPropertyChangeListener(TaskManager.getInstance().getStatusBar());
	        learningTask.execute();
		} else{
			
			learnPanel.getStopButton().setEnabled(false);
			LearningManager.getInstance().stopLearning();
	        timer.cancel();
			learnPanel.getStartButton().setEnabled(true);
			getWizard().getStatusBar().showProgress(false);
			getWizard().getStatusBar().setProgressTitle("Learning stopped");
	        
		}
		
	}
	
	private void fillTable(final List<EvaluatedDescriptionClass> result){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				learnPanel.getResultTable().addResults(result);
			}
		};
		if(SwingUtilities.isEventDispatchThread()){
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
			
	}

	private void setNextButtonAccordingToConceptSelected() {
	    
		if (learnPanel.getResultTable().getSelectedRow() >= 0){
			getWizard().setNextFinishButtonEnabled(true);
		}else{
			getWizard().setNextFinishButtonEnabled(false);
		}
	
	}
	
	/**
	 * Returns the swing worker thread instance.
	 * @return swing worker
	 */
	public LearningTask getWorkerThread(){
		return learningTask;
	}
	
	/**
	 * Returns the timer instance.
	 * @return timer
	 */
	public Timer getTimer(){
		return timer;
	}
	
	/**
	 * Returns the learning algorithm instance.
	 * @return learning algorithm
	 */
	public LearningAlgorithm getLa() {
		return la;
	}
	
	/**
	 * Clear list and coverage panel.
	 */
	public void resetPanel(){
		learnPanel.reset();
	}


	/**
	 * Inner class, containing the background thread for learning class descriptions.
	 * @author Lorenz Buehmann
	 *
	 */
	class LearningTask extends SwingWorker<Void, List<? extends EvaluatedDescription>> {
		    	
    	
		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {
			TaskManager.getInstance().getStatusBar().setMaximumValue(LearningManager.getInstance().getMaxExecutionTimeInSeconds());
			timer = new Timer();
			timer.schedule(new TimerTask(){
				int progress = 0;
				List<? extends EvaluatedDescription> result;
				@Override
				public void run() {
					progress += 1;
					setProgress(progress);
					if(!isCancelled() && LearningManager.getInstance().isLearning()){
						result = LearningManager.getInstance().getCurrentlyLearnedDescriptions();
						publish(result);
					}
				}
				
			}, 1000, 1000);
			LearningManager.getInstance().startLearning();
			
			
			return null;
		}

		@Override
		public void done() {
			
			timer.cancel();
			
			List<? extends EvaluatedDescription> result = LearningManager.getInstance().getCurrentlyLearnedDescriptions();
			updateList(result);
			TaskManager.getInstance().setTaskFinished();
			setProgress(0);
			learnPanel.getStartButton().setEnabled(true);
			learnPanel.getStopButton().setEnabled(false);
			
		}

		@Override
		protected void process(List<List<? extends EvaluatedDescription>> resultLists) {
			for (List<? extends EvaluatedDescription> list : resultLists) {
				updateList(list);
			}
		}
		
		@SuppressWarnings("unchecked")
		private void updateList(final List<? extends EvaluatedDescription> result) {			
			learnPanel.getResultTable().addResults((List<EvaluatedDescriptionClass>) result);
		}
	}


	@Override
	public void activeOntologyChanged() {
		learnPanel.getResultTable().clear();
		
	}
}
