package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.InconsistencyExplanationPanel;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;

public class InconsistencyExplanationPanelDescriptor extends WizardPanelDescriptor implements ActionListener,  RepairManagerListener{
	public static final String IDENTIFIER = "INCONSISTENCY_PANEL";
    public static final String INFORMATION = "";

    private InconsistencyExplanationPanel panel;
    private ExplanationManager expMan;
    private ImpactManager impMan;
    private RepairManager repMan;
    private Reasoner reasoner;
    private boolean laconicMode = false;
       
    public InconsistencyExplanationPanelDescriptor() {

		setPanelDescriptorIdentifier(IDENTIFIER);

	}

	public void init() {
		reasoner = OREManager.getInstance().getPelletReasoner()
				.getReasoner();
		expMan = ExplanationManager.getInstance(reasoner);
		expMan.setComputeAllExplanationsMode(true);
		impMan = ImpactManager.getInstance(reasoner);
//		impMan.addListener(this);
		repMan = RepairManager.getRepairManager(reasoner);
		repMan.addListener(this);
		panel = new InconsistencyExplanationPanel(expMan, impMan, repMan);
		panel.addActionListeners(this);
		setPanelComponent(panel);
		
		

	}
    
    private void showLaconicExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(true);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getInconsistencyExplanations()) {
			panel.addExplanation(explanation, counter);
			counter++;
		}
		
	}
    
    private void showRegularExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(false);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getInconsistencyExplanations()) {
			panel.addExplanation(explanation, counter);
			counter++;
		}
    }
    
    private void showExplanations(){
    	if(laconicMode) {
    		showLaconicExplanations();
    	} else {
    		showRegularExplanations();
    	}
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
    	showRegularExplanations();
        getWizard().getInformationField().setText(INFORMATION);
        getWizard().setNextFinishButtonEnabled(false);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("regular")) {
			laconicMode = false;
		} else if (e.getActionCommand().equals("laconic")) {
			laconicMode = true;
		}
		showExplanations();
		
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
	
	
    
    
}
 