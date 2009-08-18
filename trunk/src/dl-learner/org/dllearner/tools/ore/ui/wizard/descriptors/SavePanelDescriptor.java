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

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OntologyModifier;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.SavePanel;

/**
 * Wizard panel descriptor that provides saving ontology and going back to class choose panel.
 * @author Lorenz Buehmann
 *
 */
public class SavePanelDescriptor extends WizardPanelDescriptor implements ActionListener{
	public static final String IDENTIFIER = "SAVE_PANEL";
    public static final String INFORMATION = "Press 'Save and exit' button to save the changes you made and exit the program, " +
    		                                 "or 'Save and choose another class' button to save the changes and go back to class choose panel.";

    private SavePanel savePanel;
    
    public SavePanelDescriptor() {
    	savePanel = new SavePanel();
        
    	savePanel.addActionListeners(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(savePanel);
        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    @Override
	public void aboutToDisplayPanel() {
        getWizard().getInformationField().setText(INFORMATION);
    }    
    
    @Override
	public Object getBackPanelDescriptor() {
        return "REPAIR_PANEL";
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		OntologyModifier modifier = OREManager.getInstance().getModifier();
		if(e.getActionCommand().equals("Save and go to class choose panel")){
			modifier.saveOntology();
			getWizard().setCurrentPanel(ClassPanelOWLDescriptor.IDENTIFIER);
		}else if(e.getActionCommand().equals("Save and Exit")){
			modifier.saveOntology();
			getWizard().close(0);
		}

		
	}
    
     
}
