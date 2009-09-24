package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.InconsistencyExplanationPanel;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLOntologyChange;

public class InconsistencyExplanationPanelDescriptor extends WizardPanelDescriptor implements ActionListener,  RepairManagerListener{
	public static final String IDENTIFIER = "INCONSISTENCY_PANEL";
    public static final String INFORMATION = "";

    private InconsistencyExplanationPanel panel;
    private ExplanationManager expMan;
    private RepairManager repMan;
    private Reasoner reasoner;
       
    public InconsistencyExplanationPanelDescriptor() {
		setPanelDescriptorIdentifier(IDENTIFIER);
	}

	public void init() {
		reasoner = OREManager.getInstance().getReasoner()
				.getReasoner();
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		expMan.setComputeAllExplanationsMode(true);
		repMan = RepairManager.getInstance(OREManager.getInstance());
		repMan.addListener(this);
		panel = new InconsistencyExplanationPanel();
		panel.addActionListeners(this);
		setPanelComponent(panel);
	}
    
    private void showExplanations(){
    	ExplanationTask task = new ExplanationTask();
    	TaskManager.getInstance().setTaskStarted("Computing explanations...");
    	task.execute(); 	
    }
    
    private void setNextButtonEnabled2ConsistentOntology(){
    	if(reasoner.isConsistent()){
    		getWizard().setNextFinishButtonEnabled(true);
    	} else {
    		getWizard().setNextFinishButtonEnabled(false);
    	}
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return ClassChoosePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public void aboutToDisplayPanel() {
//    	showExplanations();
        getWizard().getInformationField().setText(INFORMATION);
        getWizard().setNextFinishButtonEnabled(false);
    }
    
    @Override
    public void displayingPanel() {
    	showExplanations();
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("regular")) {
			expMan.setLaconicMode(false);
		} else if (e.getActionCommand().equals("laconic")) {
			expMan.setLaconicMode(true);
		}	
	}

	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		showExplanations();
		panel.repaint();
		setNextButtonEnabled2ConsistentOntology();
	}
	
	class ExplanationTask extends SwingWorker<Void, Void>{
		
		@Override
		public Void doInBackground() {
			expMan.getInconsistencyExplanations();
			return null;
		}

		@Override
		public void done() {
			if(!isCancelled()){
				showExplanations();	
			}
			TaskManager.getInstance().setTaskFinished();
		}
		
		private void showExplanations(){
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					panel.clearExplanationsPanel();
					int counter = 1;
					for (Explanation explanation : expMan
							.getInconsistencyExplanations()) {
						panel.addExplanation(explanation, counter);
						counter++;
					}
					panel.validate();
					
				}
			});
			TaskManager.getInstance().setTaskFinished();
		}

	}

}
 