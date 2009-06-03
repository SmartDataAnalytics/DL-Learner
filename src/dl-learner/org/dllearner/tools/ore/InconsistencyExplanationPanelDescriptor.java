package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;

public class InconsistencyExplanationPanelDescriptor extends WizardPanelDescriptor implements ActionListener{
	public static final String IDENTIFIER = "INCONSISTENCY_PANEL";
    public static final String INFORMATION = "";

    private InconsistencyExplanationPanel panel;
    private ExplanationManager expMan;
    private ImpactManager impMan;
       
    public InconsistencyExplanationPanelDescriptor() {

		setPanelDescriptorIdentifier(IDENTIFIER);

	}

	public void init() {
		Reasoner reasoner = getWizardModel().getOre().getPelletReasoner()
				.getReasoner();
		expMan = ExplanationManager.getExplanationManager(reasoner);
		impMan = ImpactManager.getImpactManager(reasoner);
		panel = new InconsistencyExplanationPanel(expMan, impMan);
		panel.addActionListeners(this);
		setPanelComponent(panel);
		

	}
    
    private void showLaconicExplanations() {
    	panel.clearExplanationsPanel();
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedLaconicInconsistencyExplanations()) {
			panel.addExplanation(explanation, counter);
			counter++;
		}
		
	}
    
    private void showRegularExplanations() {
    	panel.clearExplanationsPanel();
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedInconsistencyExplanations()) {
			panel.addExplanation(explanation, counter);
			counter++;
		}
    }
    
    
    
    
    
    @Override
	public Object getNextPanelDescriptor() {
        return ClassPanelOWLDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return null;
    }
    
    @Override
	public void aboutToDisplayPanel() {
    	showRegularExplanations();
        getWizard().getInformationField().setText(INFORMATION);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("regular")) {
			showRegularExplanations();
		} else if (e.getActionCommand().equals("laconic")) {
			showLaconicExplanations();

		}
		
	}
    
    
}
 