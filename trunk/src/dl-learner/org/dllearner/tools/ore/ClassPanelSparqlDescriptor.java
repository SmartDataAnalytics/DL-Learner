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

/**
 * Class for SPARQL-Mode
 * @author Lorenz Buehmann
 *
 */
public class ClassPanelSparqlDescriptor extends WizardPanelDescriptor{
    
    public static final String IDENTIFIER = "CLASS_CHOOSE_SPARQL_PANEL";
    public static final String INFORMATION = "In this panel all atomic classes in the ontology are shown in the list above. " +
    										 "Select one of them which should be (re)learned from then press \"Next-Button\"";
    
    ClassPanelSparql panel3;
    
    public ClassPanelSparqlDescriptor() {
        panel3 = new ClassPanelSparql();
       
             
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel3);
      
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
        
    }
    
  
	
	
	

	
    
   

    
    
}
