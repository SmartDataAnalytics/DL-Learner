/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.tools.ore.ui.wizard.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.LearningManagerListener;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OntologyModifier;
import org.dllearner.tools.ore.ui.RepairDialog;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.RepairPanel;
import org.semanticweb.owl.model.OWLOntologyChange;



/**
 * Wizard panel descriptor where it is possible to repair wrong examples.
 * @author Lorenz Buehmann
 *
 */
public class RepairPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener, MouseListener, LearningManagerListener{
    
    public static final String IDENTIFIER = "REPAIR_PANEL";
    public static final String INFORMATION = "In this panel all positive and negative examples, that cause failures are shown in the list above. " 
	 										+ "Select one of them and choose action to solve problem by press one of buttons aside the list.";
    
    private RepairPanel repairPanel;
    private Set<OWLOntologyChange> ontologyChanges;
 
    private OntologyModifier modi;
   
    
    public RepairPanelDescriptor() {
        
        repairPanel = new RepairPanel();
       
        repairPanel.addActionListeners(this);
        repairPanel.addSelectionListeners(this);
        repairPanel.addMouseListeners(this);
        
        LearningManager.getInstance().addListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(repairPanel);
        ontologyChanges = new HashSet<OWLOntologyChange>();
        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return SavePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
    	if(LearningManager.getInstance().isManualLearningMode()){
    		return ManualLearnPanelDescriptor.IDENTIFIER;
    	} else {
    		return AutoLearnPanelDescriptor.IDENTIFIER;
    	}
        
    }
    
    @Override
	public void aboutToDisplayPanel() {
    	getWizard().getInformationField().setText(INFORMATION);
    	OREManager oreMan = OREManager.getInstance();
    	repairPanel.setClassToDescribe(oreMan.getManchesterSyntaxRendering(oreMan.getCurrentClass2Learn()));
    }
    
    /**
     * Adds the wrong negative and positive examples to the lists.
     */
    public void fillExamplesLists(){ 	
    	repairPanel.getPosFailureTable().addIndividuals(OREManager.getInstance().getPositiveFailureExamples());
    	repairPanel.getNegFailureTable().addIndividuals(OREManager.getInstance().getNegativeFailureExamples());
    }
    
    public void setManualPanel(boolean value){
    	repairPanel.setManualStyle(value);
    }
   
    /**
     * Method to control actions by button pressed.
     */
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("next")){
			LearningManager.getInstance().setNextDescription();		
		} else {
	        modi = OREManager.getInstance().getModifier();       
			String actionType = ((JButton) event.getSource()).getParent().getName();
			if(actionType.equals("negative") && repairPanel.getNegFailureTable().getSelectedRow() >=0){
				Individual ind = repairPanel.getNegFailureTable().getSelectedIndividual();
					if(event.getActionCommand().equals("negRepair")){
						RepairDialog negDialog = new RepairDialog(ind, getWizard().getDialog(),  "neg");
						int returncode = negDialog.showDialog();
						if(returncode == 2){
							ontologyChanges.addAll(negDialog.getAllChanges());
						} else if(returncode == 3){
							ontologyChanges.addAll(negDialog.getAllChanges());
							repairPanel.getNegFailureTable().removeIndividual(ind);
						}
					} else if(event.getActionCommand().equals("negAdd")){
						ontologyChanges.addAll(modi.addClassAssertion(ind, OREManager.getInstance().getCurrentClass2Learn()));
						repairPanel.getNegFailureTable().removeIndividual(ind);
						
					} else if(event.getActionCommand().equals("negDelete")){
						ontologyChanges.addAll(modi.deleteIndividual(ind));
						repairPanel.getNegFailureTable().removeIndividual(ind);
					
					}
			} else if(actionType.equals("positive") && repairPanel.getPosFailureTable().getSelectedRow() >=0){
				Individual ind = repairPanel.getPosFailureTable().getSelectedIndividual();
				if(event.getActionCommand().equals("posRepair")){
					RepairDialog posDialog = new RepairDialog(ind, getWizard().getDialog(),  "pos");
					int returncode = posDialog.showDialog();
					if(returncode == 2){
						ontologyChanges.addAll(posDialog.getAllChanges());
					} else if(returncode == 3){
						ontologyChanges.addAll(posDialog.getAllChanges());
						repairPanel.getPosFailureTable().removeIndividual(ind);
					}
				} else if(event.getActionCommand().equals("posRemove")){
					ontologyChanges.addAll(modi.addClassAssertion(ind, OREManager.getInstance().getCurrentClass2Learn()));
					repairPanel.getPosFailureTable().removeIndividual(ind);
					
				} else if(event.getActionCommand().equals("posDelete")){
					ontologyChanges.addAll(modi.deleteIndividual(ind));
					repairPanel.getPosFailureTable().removeIndividual(ind);
					
				}
			} 
		}
		
	}
	
	/**
	 * Method provides repair action by double click on list element.
	 */
	public void mouseClicked(MouseEvent e) {
		
		if(e.getClickCount() == 2){
			if(e.getSource() == repairPanel.getNegFailureTable()){
				Individual ind = repairPanel.getNegFailureTable().getSelectedIndividual();
				RepairDialog negDialog = new RepairDialog(ind, getWizard().getDialog(),  "neg");
				int returncode = negDialog.showDialog();
				if(returncode == 2){
					ontologyChanges.addAll(negDialog.getAllChanges());
					
				} else if(returncode == 3){
					ontologyChanges.addAll(negDialog.getAllChanges());
					repairPanel.getNegFailureTable().removeIndividual(ind);
				}
			} else if(e.getSource() == repairPanel.getPosFailureTable()){
				Individual ind = repairPanel.getPosFailureTable().getSelectedIndividual();
				RepairDialog posDialog = new RepairDialog(ind, getWizard().getDialog(),  "pos");
				int returncode = posDialog.showDialog();
				if(returncode == 2){
					ontologyChanges.addAll(posDialog.getAllChanges());
					
				} else if(returncode == 3){
					ontologyChanges.addAll(posDialog.getAllChanges());
					repairPanel.getPosFailureTable().removeIndividual(ind);
				}
			}
		}
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
				
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Returns all ontology changes been done by repairing ontology.
	 */
	public Set<OWLOntologyChange> getOntologyChanges() {
		return ontologyChanges;
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newDescriptionSelected(int index) {
		fillExamplesLists();
		repairPanel.repaint();
	}

	@Override
	public void noDescriptionsLeft() {
		repairPanel.setNextButtonEnabled(false);
		
	}

	@Override
	public void newDescriptionsAdded(List<EvaluatedDescriptionClass> descriptions) {
		repairPanel.setNextButtonEnabled(true);
		
	}
	
}


