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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RecentManager;
import org.dllearner.tools.ore.ui.ExtractFromSparqlPanel;
import org.dllearner.tools.ore.ui.LinkLabel;
import org.dllearner.tools.ore.ui.StatusBar;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.KnowledgeSourcePanel;
import org.protege.editor.core.ui.OpenFromURIPanel;
import org.protege.editor.core.ui.error.ErrorLogPanel;

/**
 * Wizard panel descriptor where knowledge source is selected.
 * @author Lorenz Buehmann
 *
 */
public class KnowledgeSourcePanelDescriptor extends WizardPanelDescriptor implements ActionListener{
    
    public static final String IDENTIFIER = "KNOWLEDGESOURCE_CHOOSE_PANEL";
    public static final String INFORMATION = "Choose an OWL-ontology from filesystem or URI. Your are also able to extract a fragment " +
    										"from a SPARQL-endpoint. After all press <Next>";
    
    private KnowledgeSourcePanel knowledgePanel;
    
    public KnowledgeSourcePanelDescriptor() {
        
        knowledgePanel = new KnowledgeSourcePanel();
        knowledgePanel.addListeners(this);
 
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(knowledgePanel);
        
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    		return ClassChoosePanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return IntroductionPanelDescriptor.IDENTIFIER;
    }
    
    
    @Override
	public void aboutToDisplayPanel() {
        getWizard().getInformationField().setText(INFORMATION);
        getWizard().setNextFinishButtonEnabled(OREManager.getInstance().getKnowledgeSource() != null);
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
    	} else {
    		handleOpenFromRecent(URI.create(((LinkLabel)e.getSource()).getText()));
    	}
    }
    
    public void loadOntology(URI uri){
    	OREManager.getInstance().setCurrentKnowledgeSource(uri);
    	
    	RecentManager.getInstance().addURI(uri);
    	RecentManager.getInstance().serialize();
    	new OntologyLoadingTask(getWizard().getStatusBar()).execute();
    	
    }
    
    private void handleOpenFromURI() {
        try {
            URI uri = OpenFromURIPanel.showDialog();
            if(uri != null){
            	loadOntology(uri);
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
				URI uri = new File(filePathString).toURI();
				loadOntology(uri);
				
			}

		} else {
			System.out.println("Auswahl abgebrochen");
		}

	}
	
	private void handleLoadFromSparqlEndpoint(){
		
		OREManager.getInstance().setCurrentKnowledgeSource(
				ExtractFromSparqlPanel.showDialog());
		new OntologyLoadingTask(getWizard().getStatusBar()).execute();
	}
	
	private void handleOpenFromRecent(URI uri){
		OREManager.getInstance().setCurrentKnowledgeSource(
				uri);
		new OntologyLoadingTask(getWizard().getStatusBar()).execute();
	}
    
    private void updateMetrics(){
    	knowledgePanel.updateMetrics();
    }
  
    public KnowledgeSourcePanel getPanel() {
		return knowledgePanel;
	}
    
    class OntologyLoadingTask extends SwingWorker<Void, Void>{
		
		private StatusBar statusBar;
		private OREManager oreMan;
		
		public OntologyLoadingTask(StatusBar statusBar) {		
			this.statusBar = statusBar;
			this.oreMan = OREManager.getInstance();
		}

		@Override
		public Void doInBackground() {
			getWizard().setNextFinishButtonEnabled(false);
			getWizard().getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			statusBar.showProgress(true);
			statusBar.setProgressTitle("Loading ontology");
			try{
	        	oreMan.initPelletReasoner();
	        	
			} catch(Throwable e){
				statusBar.showProgress(false);
				statusBar.setProgressTitle("Error occured");
				getWizard().getDialog().setCursor(null);
				 ErrorLogPanel.showErrorDialog(e);
				 return null;

			}
			if(oreMan.consistentOntology()){
				statusBar.setProgressTitle("Classifying ontology");
				oreMan.getReasoner().classify();
	        	statusBar.setProgressTitle("Realising ontology");
	        	oreMan.getReasoner().realise();
			}
			return null;
		}

		@Override
		public void done() {
			statusBar.showProgress(false);
			statusBar.setProgressTitle("Done");
			getWizard().getDialog().setCursor(null);
			getWizard().setNextFinishButtonEnabled(true);
			updateMetrics();
		}
	}
}
