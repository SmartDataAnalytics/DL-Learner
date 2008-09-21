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

package org.dllearner.tools.ore;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;



/**
 * Wizard panel descriptor for selecting one of the atomic classes in OWL-ontology that 
 * has to be (re)learned.
 * @author Lorenz Buehmann
 *
 */
public class ClassPanelOWLDescriptor extends WizardPanelDescriptor implements ListSelectionListener{
    
    public static final String IDENTIFIER = "CLASS_CHOOSE_OWL_PANEL";
    public static final String INFORMATION = "In this panel all atomic classes in the ontology are shown in the list above. " +
    										 "Select one of them which should be (re)learned from, then press \"Next-Button\"";
    
    private ClassPanelOWL owlClassPanel;
    
    public ClassPanelOWLDescriptor() {
        owlClassPanel = new ClassPanelOWL();
        owlClassPanel.addSelectionListener(this);
             
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(owlClassPanel);
      
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return LearningPanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public void aboutToDisplayPanel() {
    	getWizard().getInformationField().setText(INFORMATION);
        setNextButtonAccordingToConceptSelected();
    }
    
  
	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected(); 
		if (!e.getValueIsAdjusting()) 
			 getWizardModel().getOre().setClassToLearn((NamedClass)owlClassPanel.getList().getSelectedValue());
			
	}
	
	private void setNextButtonAccordingToConceptSelected() {
        
    	if (owlClassPanel.getList().getSelectedValue()!= null){
    		getWizard().setNextFinishButtonEnabled(true);
    	}else{
    		getWizard().setNextFinishButtonEnabled(false);
    	}
   
    }

	public ClassPanelOWL getOwlClassPanel() {
		return owlClassPanel;
	}
	
	

	
    
   

    
    
}
