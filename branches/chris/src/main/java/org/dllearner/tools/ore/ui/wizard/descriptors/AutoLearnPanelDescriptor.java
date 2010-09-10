package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.LearningManager.LearningType;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.AutoLearnPanel;
import org.mindswap.pellet.utils.SetUtils;


public class AutoLearnPanelDescriptor extends WizardPanelDescriptor implements ActionListener{
	/**
	 * Identification string for class choose panel.
	 */
    public static final String IDENTIFIER = "AUTO_LEARN_PANEL";
    /**
     * Information string for class choose panel.
     */
    public static final String INFORMATION = "";
    
    private AutoLearnPanel autoLearnPanel;
    
    private List<NamedClass> classes;
    
    private LearningTask learningTask;
    
    private int currentClassIndex = 0;
    
   
    /**
     * Constructor creates new panel and adds listener to list.
     */
    public AutoLearnPanelDescriptor() {
    	autoLearnPanel = new AutoLearnPanel();
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(autoLearnPanel);        
    
        autoLearnPanel.addActionListener(this);
        classes = new ArrayList<NamedClass>();
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    	List<List<EvaluatedDescriptionClass>> selectedDescriptions = getSelectedDescriptions();
    	if(SetUtils.union(selectedDescriptions.get(0), 
    			selectedDescriptions.get(1)).isEmpty()){
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
    }
	
	public void fillClassesTable(){	
		TaskManager.getInstance().setTaskStarted("Retrieving atomic classes...");
		new ClassRetrievingTask().execute();
		autoLearnPanel.setNextButtonEnabled(true);
	}
	
	public void learnNextClassExpressions(){
		if(LearningManager.getInstance().getLearningType() == LearningType.EQUIVALENT){
			TaskManager.getInstance().getStatusBar().setMessage("Learning equivalent class expressions...");
		} else {
			TaskManager.getInstance().getStatusBar().setMessage("Learning super class expressions...");
		}
		getWizard().getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		learningTask = new LearningTask();
		learningTask.addPropertyChangeListener(TaskManager.getInstance().getStatusBar());
		learningTask.execute();
	}
	
	public void learnNextClass(){
		autoLearnPanel.resetPanel();
		autoLearnPanel.setSelectedClass(currentClassIndex);
		LearningManager.getInstance().setCurrentClass2Describe(classes.get(currentClassIndex));
		learnNextClassExpressions();
		currentClassIndex++;
		if(currentClassIndex >= classes.size()){
			autoLearnPanel.setNextButtonEnabled(false);
		}
	}
	
	public List<List<EvaluatedDescriptionClass>> getSelectedDescriptions(){
		return autoLearnPanel.getSelectedDescriptions();
	}
	
	public void resetPanel(){
		currentClassIndex = 0;
		autoLearnPanel.resetPanel();
		autoLearnPanel.clearClassesTable();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		learnNextClass();		
	}
  
    /**
     * Inner class to get all atomic classes in a background thread.
     * @author Lorenz Buehmann
     *
     */
    class ClassRetrievingTask extends SwingWorker<Set<NamedClass>, Void> {
    	

		@Override
		public Set<NamedClass> doInBackground() {
			
			OREManager.getInstance().makeOWAToCWA();
			Set<NamedClass> classes = new TreeSet<NamedClass>(OREManager.getInstance().getReasoner().getNamedClasses());
			classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
			Iterator<NamedClass> iter = classes.iterator();
			while(iter.hasNext()){
				NamedClass nc = iter.next();
				int instanceCount = OREManager.getInstance().getReasoner().getIndividuals(nc).size();
				if(instanceCount < LearningManager.getInstance().getMinInstanceCount()){
					iter.remove();
				}
			}			
			return classes;
		}

		@Override
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					Set<NamedClass> result = Collections.emptySet();
					try {
						result = get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					classes.clear();
					classes.addAll(result);
					autoLearnPanel.fillClassesTable(result);
					LearningManager.getInstance().setCurrentClass2Describe(classes.get(0));
					TaskManager.getInstance().setTaskFinished();
					learnNextClass();
				}
			});					
		}
	}
    
    class LearningTask extends SwingWorker<Void, List<? extends EvaluatedDescription>> {
    	
    	private Timer timer;

		@Override
		public Void doInBackground() {
		    timer = new Timer();
		    setProgress(0);
		    TaskManager.getInstance().getStatusBar().setMaximumValue(LearningManager.getInstance().getMaxExecutionTimeInSeconds());
			timer.schedule(new TimerTask(){

				int progress = 0;
				List<? extends EvaluatedDescription> result;
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if(LearningManager.getInstance().isLearning()){
						progress += 1;
						setProgress(progress);
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
			updateResultTable(result);
			TaskManager.getInstance().setTaskFinished();
			if(LearningManager.getInstance().getLearningType() == LearningType.EQUIVALENT){
				LearningManager.getInstance().setLearningType(LearningType.SUPER);
				learnNextClassExpressions();
			} else {
				LearningManager.getInstance().setLearningType(LearningType.EQUIVALENT);
			}
			
		}
		
		@Override
		protected void process(List<List<? extends EvaluatedDescription>> resultLists) {					
			for (List<? extends EvaluatedDescription> list : resultLists) {
				updateResultTable(list);
			}
		}
		
		@SuppressWarnings("unchecked")
		private void updateResultTable(final List<? extends EvaluatedDescription> result) {
			if(LearningManager.getInstance().getLearningType() == LearningType.EQUIVALENT){
				autoLearnPanel.fillEquivalentClassExpressionsTable((List<EvaluatedDescriptionClass>) result);
			} else {
				autoLearnPanel.fillSuperClassExpressionsTable((List<EvaluatedDescriptionClass>) result);
			}
			
		}
	}
    
}
