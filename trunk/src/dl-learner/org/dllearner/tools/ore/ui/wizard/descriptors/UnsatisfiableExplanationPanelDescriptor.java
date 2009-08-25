package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ExplanationManagerListener;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.ImpactManagerListener;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.ui.StatusBar;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.UnsatisfiableExplanationPanel;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntologyChange;

public class UnsatisfiableExplanationPanelDescriptor extends
		WizardPanelDescriptor implements ActionListener, ImpactManagerListener, ListSelectionListener, ChangeListener, ExplanationManagerListener, RepairManagerListener{
	
	public static final String IDENTIFIER = "UNSATISFIABLE_PANEL";
    public static final String INFORMATION = "";

    private UnsatisfiableExplanationPanel panel;
    private ExplanationManager expMan;
    private ImpactManager impMan;
    private RepairManager repMan;
    private Reasoner reasoner;
    private OWLClass unsatClass;
	
	public UnsatisfiableExplanationPanelDescriptor(){
		setPanelDescriptorIdentifier(IDENTIFIER);
	}
	
	public void init() {
		reasoner = OREManager.getInstance().getPelletReasoner()
				.getReasoner();
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		expMan.addListener(this);
		impMan = ImpactManager.getInstance(OREManager.getInstance());
		impMan.addListener(this);
		repMan = RepairManager.getRepairManager(OREManager.getInstance());
		repMan.addListener(this);
		panel = new UnsatisfiableExplanationPanel();
		panel.addActionListeners(this);
		panel.addListSelectionListener(this);
		panel.addChangeListener(this);
		
		setPanelComponent(panel);
		
		

	}
	  
    private void showExplanations(){
//    	panel.clearExplanationsPanel();
    	new ExplanationTask(getWizard().getStatusBar()).execute();
    	
//		int counter = 1;
//		Set<List<OWLAxiom>> explanations = expMan.getUnsatisfiableExplanations(unsatClass);
//		
//		for (List<OWLAxiom> explanation : explanations) {
//			panel.addExplanation(explanation, unsatClass, counter);
//			counter++;
//			if(counter > expMan.getMaxExplantionCount() && !expMan.isComputeAllExplanationsMode()){
//				break;
//			}
//		}   	
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
    	fillUnsatClassesTable();
        getWizard().getInformationField().setText(INFORMATION);
        
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("regular")) {
			expMan.setLaconicMode(false);
		} else if (e.getActionCommand().equals("laconic")) {
			expMan.setLaconicMode(true);
		} else if (e.getActionCommand().equals("all")){
			conditionalWarning("Computing all explanations might be very expensive", getWizard().getDialog());
			expMan.setComputeAllExplanationsMode(true);
			panel.setMaxExplanationsMode(false);
		} else if (e.getActionCommand().equals("max")){
			expMan.setComputeAllExplanationsMode(false);
			panel.setMaxExplanationsMode(true);
		}
		
		
	}

	@Override
	public void impactListChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		panel.clearExplanationsPanel();	
		fillUnsatClassesTable();
		panel.repaint();
		
	}
	
	private void fillUnsatClassesTable(){
		List<OWLClass> unsatClasses = new ArrayList<OWLClass>();
		Set<OWLClass> rootClasses = new TreeSet<OWLClass>(expMan
				.getRootUnsatisfiableClasses());
		unsatClasses.addAll(rootClasses);
		Set<OWLClass> derivedClasses = new TreeSet<OWLClass>(expMan
				.getUnsatisfiableClasses());
		derivedClasses.removeAll(rootClasses);
		
		unsatClasses.addAll(derivedClasses);
		panel.fillUnsatClassesTable(unsatClasses);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		unsatClass = (OWLClass)panel.getUnsatTable().getSelectedClass();
		if (!e.getValueIsAdjusting() && unsatClass != null) {
			showExplanations();
		}
		
	}
	
	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner)e.getSource();
		expMan.setMaxExplantionCount(((Integer)spinner.getValue()).intValue());
		
	}

	@Override
	public void explanationLimitChanged() {
		if(unsatClass != null){
			showExplanations();
		}	
	}
	
	@Override
	public void explanationTypeChanged() {
		if(unsatClass != null){
			showExplanations();
		}
	}
	
	private void conditionalWarning(final String notice, Component parent) {
        class NotifyPanel extends JPanel {
            /**
			 * 
			 */
			private static final long serialVersionUID = -5602333953438722592L;

			public NotifyPanel() {
                final JCheckBox enough = new JCheckBox("Don't show this message again", expMan.isAllExplanationWarningChecked());
                enough.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        expMan.setAllExplanationWarningChecked();
                    }
                });
                setLayout(new BorderLayout());
                add(new JLabel("<html><font size=+1>" + notice + "</font></html>"), BorderLayout.CENTER);
                add(enough, BorderLayout.SOUTH);
            }
        }
        if( ! expMan.isAllExplanationWarningChecked())
            JOptionPane.showMessageDialog(parent, new NotifyPanel(), "Warning", JOptionPane.WARNING_MESSAGE);
    }

	 class ExplanationTask extends SwingWorker<Void, Void>{
			
			private StatusBar statusBar;
			
			public ExplanationTask(StatusBar statusBar) {
				this.statusBar = statusBar;
				
			}

			@Override
			public Void doInBackground() {
				statusBar.showProgress(true);
				statusBar.setProgressTitle("computing explanations");
				getWizard().getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				expMan.getUnsatisfiableExplanations(unsatClass);
				return null;
			}

			@Override
			public void done() {
				statusBar.showProgress(false);
				statusBar.setProgressTitle("");
				getWizard().getDialog().setCursor(null);
				showExplanations();
				
			}
			
			private void showExplanations(){
				
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						panel.clearExplanationsPanel();
						int counter = 1;
						
						for (List<OWLAxiom> explanation : expMan.getUnsatisfiableExplanations(unsatClass)) {
							panel.addExplanation(explanation, unsatClass, counter);
							counter++;
							if(counter > expMan.getMaxExplantionCount() && !expMan.isComputeAllExplanationsMode()){
								break;
							}
						}
						panel.validate();
					}
				});
				
			}

		}


}
