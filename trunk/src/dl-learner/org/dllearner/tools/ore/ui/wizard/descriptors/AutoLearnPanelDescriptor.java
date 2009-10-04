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
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.AutoLearnPanel;

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
    
    private EquivalentLearningTask equivalentLearningTask;
    private SuperClassLearningTask superLearningTask;
    
    private Timer timer;
	private LearningAlgorithm la;
    
    private SwingWorker<Void, List<? extends EvaluatedDescription>> currentLearningTask;
    
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
    	if(getSelectedDescriptions().isEmpty()){
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
	}
	
	public void learnEquivalentClassExpressions(){
		TaskManager.getInstance().getStatusBar().setMessage("Learning equivalent class expressions...");
		getWizard().getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		equivalentLearningTask = new EquivalentLearningTask();
		currentLearningTask = equivalentLearningTask;
		equivalentLearningTask.addPropertyChangeListener(TaskManager.getInstance().getStatusBar());
		equivalentLearningTask.execute();
	}
	
	public void learnSuperClassExpressions(){
		TaskManager.getInstance().getStatusBar().setMessage("Learning superclass expressions...");
		TaskManager.getInstance().getStatusBar().setProgress(0);
		getWizard().getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		superLearningTask = new SuperClassLearningTask();
		currentLearningTask = superLearningTask;
		superLearningTask.addPropertyChangeListener(TaskManager.getInstance().getStatusBar());
		superLearningTask.execute();
	}
	
	public void learnNextClass(){
		autoLearnPanel.resetPanel();
		autoLearnPanel.setSelectedClass(currentClassIndex);
		OREManager.getInstance().setCurrentClass2Learn(classes.get(currentClassIndex));
		learnEquivalentClassExpressions();
		currentClassIndex++;
	}
	
	public List<EvaluatedDescriptionClass> getSelectedDescriptions(){
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
				if(instanceCount < OREManager.getInstance().getMinInstanceCount()){
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
					OREManager.getInstance().setCurrentClass2Learn(classes.get(0));
					TaskManager.getInstance().setTaskFinished();
					learnNextClass();
				}
			});					
		}
	}
    
    class EquivalentLearningTask extends SwingWorker<Void, List<? extends EvaluatedDescription>> {
    	
    	private Timer timer;
    	private LearningAlgorithm la;
    	private final int maxNrOfResults = OREManager.getInstance().getMaxNrOfResults();
	    private final double threshold = OREManager.getInstance().getThreshold();

		@Override
		public Void doInBackground() {
			OREManager.getInstance().setLearningType("equivalence");
			OREManager.getInstance().setLearningProblem();
		    OREManager.getInstance().setLearningAlgorithm();
		    la = OREManager.getInstance().getLa();
		  		    
		    timer = new Timer();
		    setProgress(0);
			TaskManager.getInstance().getStatusBar().setMaximumValue(OREManager.getInstance().getMaxExecutionTimeInSeconds());
			timer.schedule(new TimerTask(){

				int progress = 0;
				List<? extends EvaluatedDescription> result;
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if(la.isRunning()){
						progress += 1;
						setProgress(progress);
						result = la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true);
						publish(result);
					}
				}
				
			}, 1000, 1000);
			la.start();
			return null;
		}

		@Override
		public void done() {
			timer.cancel();
			List<? extends EvaluatedDescription> result = la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true);
			updateResultTable(result);
			if(result.isEmpty()){
				EvaluatedDescriptionClass best = (EvaluatedDescriptionClass)la.getCurrentlyBestEvaluatedDescription();
			} 
			
			learnSuperClassExpressions();
		
		}
		
		@Override
		protected void process(List<List<? extends EvaluatedDescription>> resultLists) {					
			for (List<? extends EvaluatedDescription> list : resultLists) {
				updateResultTable(list);
			}
		}
		
		@SuppressWarnings("unchecked")
		private void updateResultTable(final List<? extends EvaluatedDescription> result) {		
			autoLearnPanel.fillEquivalentClassExpressionsTable((List<EvaluatedDescriptionClass>) result);
		}
	}
    
    class SuperClassLearningTask extends SwingWorker<Void, List<? extends EvaluatedDescription>> {
    	
    	private Timer timer;
    	private LearningAlgorithm la;
    	private final int maxNrOfResults = OREManager.getInstance().getMaxNrOfResults();
	    private final double threshold = OREManager.getInstance().getThreshold();

		@Override
		public Void doInBackground() {System.out.println("Learning super class for " + OREManager.getInstance().getCurrentClass2Learn());
			OREManager.getInstance().setLearningType("superClass");
			OREManager.getInstance().setLearningProblem();
		    OREManager.getInstance().setLearningAlgorithm();
		    la = OREManager.getInstance().getLa();
  		    
		    timer = new Timer();
		    setProgress(0);
			TaskManager.getInstance().getStatusBar().setMaximumValue(OREManager.getInstance().getMaxExecutionTimeInSeconds());
			timer.schedule(new TimerTask(){

				int progress = 0;
				List<? extends EvaluatedDescription> result;
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if(la.isRunning()){
						progress += 1;
						setProgress(progress);
						result = la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true);
						publish(result);
					}
				}
				
			}, 1000, 1000);
			la.start();
			return null;
		}

		@Override
		public void done() {
			timer.cancel();
			List<? extends EvaluatedDescription> result = la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults, threshold, true);
			updateResultTable(result);
			TaskManager.getInstance().setTaskFinished();
			setProgress(0);
		}
		
		@Override
		protected void process(List<List<? extends EvaluatedDescription>> resultLists) {				
			for (List<? extends EvaluatedDescription> list : resultLists) {
				updateResultTable(list);
			}
		}
		
		@SuppressWarnings("unchecked")
		private void updateResultTable(final List<? extends EvaluatedDescription> result) {
			autoLearnPanel.fillSuperClassExpressionsTable((List<EvaluatedDescriptionClass>) result);
		}
	}
}
