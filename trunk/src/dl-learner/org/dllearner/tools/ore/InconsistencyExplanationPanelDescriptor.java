package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;

public class InconsistencyExplanationPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ImpactManagerListener{
	public static final String IDENTIFIER = "INCONSISTENCY_PANEL";
    public static final String INFORMATION = "";

    private InconsistencyExplanationPanel panel;
    private ExplanationManager expMan;
    private ImpactManager impMan;
    private Reasoner reasoner;
    private boolean laconicMode = false;
       
    public InconsistencyExplanationPanelDescriptor() {

		setPanelDescriptorIdentifier(IDENTIFIER);

	}

	public void init() {
		reasoner = getWizardModel().getOre().getPelletReasoner()
				.getReasoner();
		expMan = ExplanationManager.getExplanationManager(reasoner);
		impMan = ImpactManager.getImpactManager(reasoner);
		impMan.addListener(this);
		panel = new InconsistencyExplanationPanel(expMan, impMan);
		panel.addActionListeners(this);
		setPanelComponent(panel);
		
		

	}
    
    private void showLaconicExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(true);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedLaconicInconsistencyExplanations()) {
			panel.addExplanation(explanation, counter);
			counter++;
		}
		
	}
    
    private void showRegularExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(false);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedInconsistencyExplanations()) {
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
        return ClassPanelOWLDescriptor.IDENTIFIER;
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
	public void axiomForImpactChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted() {
		
		System.out.println("repair plan executed");
		showExplanations();
		panel.repaint();
		setNextButtonEnabled2ConsistentOntology();
		
	}
	
	
    
    
}
 