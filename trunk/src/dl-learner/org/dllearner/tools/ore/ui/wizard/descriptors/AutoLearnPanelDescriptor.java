package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.AutoLearnPanel;

public class AutoLearnPanelDescriptor extends WizardPanelDescriptor {

	/**
	 * Identification string for class choose panel.
	 */
    public static final String IDENTIFIER = "AUTO_LEARN_PANEL";
    /**
     * Information string for class choose panel.
     */
    public static final String INFORMATION = "";
    
    private AutoLearnPanel autoLearnPanel;
   
    /**
     * Constructor creates new panel and adds listener to list.
     */
    public AutoLearnPanelDescriptor() {
    	autoLearnPanel = new AutoLearnPanel();
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(autoLearnPanel);        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return RepairPanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return ClassChoosePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public void aboutToDisplayPanel() {
    	getWizard().getInformationField().setText(INFORMATION);
    	fillClassesTable();
    }
	
	public void fillClassesTable(){
		new ClassRetrievingTask().execute();
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
					Set<NamedClass> classes = Collections.emptySet();
					try {
						classes = get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					autoLearnPanel.fillClassesTable(classes);		
					TaskManager.getInstance().setTaskFinished();
				}
			});					
		}
	}
    
    class EquivalentLearningTask extends SwingWorker<Void, Void> {
    	

		@Override
		public Void doInBackground() {
			OREManager.getInstance().setLearningType("equivalence");
			OREManager.getInstance().setLearningProblem();
		    OREManager.getInstance().setLearningAlgorithm();
			OREManager.getInstance().getLa().start();
			return null;
		}

		@Override
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					
					autoLearnPanel.fillEquivalentClassExpressionsTable((List<EvaluatedDescriptionClass>)OREManager.getInstance().getLa().
							getCurrentlyBestEvaluatedDescriptions(OREManager.getInstance().getMaxNrOfResults(), OREManager.getInstance().getThreshold(), true));		
					TaskManager.getInstance().setTaskFinished();
				}
			});					
		}
	}
    
    class SubclassLearningTask extends SwingWorker<Void, Void> {
    	

		@Override
		public Void doInBackground() {
			OREManager.getInstance().setLearningType("superClass");
			OREManager.getInstance().setLearningProblem();
		    OREManager.getInstance().setLearningAlgorithm();
			OREManager.getInstance().getLa().start();
			return null;
		}

		@Override
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					
					autoLearnPanel.fillSubClassExpressionsTable((List<EvaluatedDescriptionClass>)OREManager.getInstance().getLa().
							getCurrentlyBestEvaluatedDescriptions(OREManager.getInstance().getMaxNrOfResults(), OREManager.getInstance().getThreshold(), true));		
					TaskManager.getInstance().setTaskFinished();
				}
			});					
		}
	}
}
