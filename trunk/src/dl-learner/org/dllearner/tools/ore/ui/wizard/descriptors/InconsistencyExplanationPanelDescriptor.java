package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ExplanationManagerListener;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.InconsistencyExplanationPanel;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLOntologyChange;

public class InconsistencyExplanationPanelDescriptor extends WizardPanelDescriptor implements ActionListener,ChangeListener, ExplanationManagerListener,  RepairManagerListener{
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
		reasoner = OREManager.getInstance().getReasoner().getReasoner();
		
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		expMan.addListener(this);
		
		repMan = RepairManager.getInstance(OREManager.getInstance());
		repMan.addListener(this);
		
		panel = new InconsistencyExplanationPanel();
		panel.addActionListeners(this);
		panel.addChangeListener(this);
		
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
			} else if (e.getActionCommand().equals("all")){
				conditionalWarning("Computing all explanations might take a long time!", getWizard().getDialog());
				expMan.setComputeAllExplanationsMode(true);
				panel.setMaxExplanationsMode(false);
			} else if (e.getActionCommand().equals("max")){
				expMan.setComputeAllExplanationsMode(false);
				panel.setMaxExplanationsMode(true);
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
		showExplanations();	
	}
	
	@Override
	public void explanationTypeChanged() {
		showExplanations();	
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		showExplanations();
		panel.repaint();
		setNextButtonEnabled2ConsistentOntology();
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
			panel.clearExplanationsPanel();
			int counter = 1;
			for (Explanation explanation : expMan.getInconsistencyExplanations()) {
				panel.addExplanation(explanation, counter);
				counter++;
				if(counter > expMan.getMaxExplantionCount() && !expMan.isComputeAllExplanationsMode()){
					break;
				}
			}
			panel.validate();
			TaskManager.getInstance().setTaskFinished();
		}

	}

}
 