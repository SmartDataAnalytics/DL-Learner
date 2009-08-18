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
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.LinkLabel;
import org.dllearner.tools.ore.ui.MetricsPanel;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.KnowledgeSourcePanel;
import org.protege.editor.core.ui.OpenFromURIPanel;
import org.protege.editor.core.ui.error.ErrorLogPanel;

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
        knowledgePanel.addListeners(this);
        
        
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(knowledgePanel);
        
    }
    
    public void addMetricsPanel(){
    	MetricsPanel metrics = new MetricsPanel(OREManager.getInstance().getPelletReasoner().getOWLOntologyManager());
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
//    	setNextButtonAccordingToExistingOWLFile();
    }    

    /**
     * Actions for buttons.
     * @param e ActionListener
     */
    public void actionPerformed(ActionEvent e) {
    	String linkname = ((LinkLabel)e.getSource()).getName();
    	if(linkname.equals("openFromURILink")){
    		handleOpenFromURI();
    	} else if(linkname.equals("openFromFileLink")){
    		handleOpenFromFile();
    	} else if(linkname.equals("loadFromSparqlEndpointLink")){
    		handleLoadFromSparqlEndpoint();
    	}
    	
    	    	
//        setNextButtonAccordingToExistingOWLFile();
    }
    
    private void handleOpenFromURI() {
        try {
            URI uri = OpenFromURIPanel.showDialog();
            if(uri != null){
            	OREManager.getInstance().setCurrentKnowledgeSource(uri);
            	OREManager.getInstance().initPelletReasoner();
            }
        }
        catch (Exception e1) {
            ErrorLogPanel.showErrorDialog(e1);
        }
    }
    
	private void handleOpenFromFile() {

		JFileChooser filechooser = new JFileChooser();

		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		filechooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toLowerCase().endsWith(".owl")
						|| f.getName().toLowerCase().endsWith(".rdf");
			}

			@Override
			public String getDescription() {
				return "OWLs, RDFs";
			}
		});
		int status = filechooser.showOpenDialog(null);

		if (status == JFileChooser.APPROVE_OPTION) {
			String filePathString = filechooser.getSelectedFile()
					.getAbsolutePath();
			if (filePathString != null && !filePathString.isEmpty()) {
				OREManager.getInstance().setCurrentKnowledgeSource(
						new File(filePathString).toURI());
				OREManager.getInstance().initPelletReasoner();
//				getWizardModel().getOre().getPelletReasoner().classify();
				
			}

		} else {
			System.out.println("Auswahl abgebrochen");
		}

	}
	
	private void handleLoadFromSparqlEndpoint(){
		
	}
    
    private void showMetrics(){
    	
    }
  
            
    
    private void setNextButtonAccordingToExistingOWLFile() {
         
    	if (knowledgePanel.isExistingOWLFile()){
    		OREManager.getInstance().setCurrentKnowledgeSource(knowledgePanel.getOWLFile().toURI());
        	getWizard().setNextFinishButtonEnabled(true);
    	}else{
            getWizard().setNextFinishButtonEnabled(false); 
    	}
    
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
