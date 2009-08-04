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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.KnowledgeSourcePanel;

/**
 * Wizard panel descriptor where knowledge source is selected.
 * @author Lorenz Buehmann
 *
 */
public class KnowledgeSourcePanelDescriptor extends WizardPanelDescriptor implements ActionListener, DocumentListener{
    
    public static final String IDENTIFIER = "KNOWLEDGESOURCE_CHOOSE_PANEL";
    public static final String INFORMATION = "Select the type of knowledgesource you want to work with and then enter the URI."
    									     + " After all press \"Next\"-button";
    
    private KnowledgeSourcePanel knowledgePanel;
    
    public KnowledgeSourcePanelDescriptor() {
        
        knowledgePanel = new KnowledgeSourcePanel();
    
        knowledgePanel.addListeners(this, this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(knowledgePanel);
        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    	if(getWizard().getKnowledgeSourceType() == 0){
    		return ClassPanelOWLDescriptor.IDENTIFIER;
    	} else{
    		return ClassPanelSparqlDescriptor.IDENTIFIER;
    	}
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return IntroductionPanelDescriptor.IDENTIFIER;
    }
    
    
    @Override
	public void aboutToDisplayPanel() {
        getWizard().getInformationField().setText(INFORMATION);
    	setNextButtonAccordingToExistingOWLFile();
    }    

    /**
     * Actions for buttons.
     * @param e ActionListener
     */
    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	if(cmd.equals("browse")){
			knowledgePanel.openFileChooser();
		}else if(cmd.equals("OWL")){
			knowledgePanel.setOWLMode();
			getWizard().setKnowledgeSourceType(0);
		}else if(cmd.equals("SPARQL")){
			knowledgePanel.setSPARQLMode();
			getWizard().setKnowledgeSourceType(1);
		}
    	    	
        setNextButtonAccordingToExistingOWLFile();
    }
    
  
            
    
    private void setNextButtonAccordingToExistingOWLFile() {
         
    	if (knowledgePanel.isExistingOWLFile()){
    		getWizardModel().getOre().setKnowledgeSource(knowledgePanel.getOWLFile());
        	getWizard().setNextFinishButtonEnabled(true);
    	}else{
            getWizard().setNextFinishButtonEnabled(false); 
    	}
    
    }
    
    private void connect2Sparql(){
    	
    }
   

	public void changedUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}

	public void insertUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}

	public void removeUpdate(DocumentEvent e) {
		setNextButtonAccordingToExistingOWLFile();
		
	}
    public KnowledgeSourcePanel getPanel() {
		return knowledgePanel;
	}
  

}
