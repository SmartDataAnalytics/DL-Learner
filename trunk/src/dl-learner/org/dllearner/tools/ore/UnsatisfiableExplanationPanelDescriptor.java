package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class UnsatisfiableExplanationPanelDescriptor extends
		WizardPanelDescriptor implements ActionListener, ImpactManagerListener, ListSelectionListener{
	
	public static final String IDENTIFIER = "UNSATISFIABLE_PANEL";
    public static final String INFORMATION = "";

    private UnsatisfiableExplanationPanel panel;
    private ExplanationManager expMan;
    private ImpactManager impMan;
    private Reasoner reasoner;
    private boolean laconicMode = false;
    private OWLClass unsatClass;
	
	public UnsatisfiableExplanationPanelDescriptor(){
		setPanelDescriptorIdentifier(IDENTIFIER);
	}
	
	public void init() {
		reasoner = getWizardModel().getOre().getPelletReasoner()
				.getReasoner();
		expMan = ExplanationManager.getExplanationManager(reasoner);
		impMan = ImpactManager.getImpactManager(reasoner);
		impMan.addListener(this);
		panel = new UnsatisfiableExplanationPanel(expMan, impMan);
		panel.addActionListeners(this);
		panel.addListSelectionListener(this);
		setPanelComponent(panel);
		
		

	}
	
	private void showLaconicExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(true);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedLaconicUnsatisfiableExplanations(unsatClass)) {
			panel.addExplanation(explanation, unsatClass, counter);
			counter++;
		}
		
	}
    
    private void showRegularExplanations() {
    	panel.clearExplanationsPanel();
    	expMan.setLaconicMode(false);
		int counter = 1;
		for (List<OWLAxiom> explanation : expMan
				.getOrderedUnsatisfiableExplanations(unsatClass)) {
			panel.addExplanation(explanation, unsatClass, counter);
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
    	fillUnsatClassesList();
        getWizard().getInformationField().setText(INFORMATION);
        
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
		panel.clearExplanationsPanel();
		
		fillUnsatClassesList();
		panel.repaint();
		
	}
	
	private void fillUnsatClassesList(){
		List<OWLClass> unsatClasses = new ArrayList<OWLClass>();
		Set<OWLClass> rootClasses = new TreeSet<OWLClass>(expMan
				.getRootUnsatisfiableClasses());
		unsatClasses.addAll(rootClasses);
		Set<OWLClass> derivedClasses = new TreeSet<OWLClass>(expMan
				.getUnsatisfiableClasses());
		derivedClasses.removeAll(rootClasses);
		
		unsatClasses.addAll(derivedClasses);
		panel.fillUnsatClassesList(unsatClasses);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		JXList unsatList = (JXList) e.getSource();
		unsatClass = (OWLClass)unsatList.getSelectedValue();
		if (!unsatList.isSelectionEmpty()) {
			showExplanations();
		}
		
	}

}
