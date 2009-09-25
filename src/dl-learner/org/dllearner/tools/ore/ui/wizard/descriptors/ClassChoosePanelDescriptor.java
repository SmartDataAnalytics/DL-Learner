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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JSpinner;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.ClassChoosePanel;



/**
 * Wizard panel descriptor for selecting one of the atomic classes in OWL-ontology that 
 * has to be (re)learned.
 * @author Lorenz Buehmann
 *
 */
public class ClassChoosePanelDescriptor extends WizardPanelDescriptor implements ListSelectionListener, ChangeListener{
    
	/**
	 * Identification string for class choose panel.
	 */
    public static final String IDENTIFIER = "CLASS_CHOOSE_OWL_PANEL";
    /**
     * Information string for class choose panel.
     */
    public static final String INFORMATION = "Above all atomic classes which have at least one individual are listed. " 
    										 + "Select one of them for which you want to learn equivalent class expressions," +
    										 	" then press <Next>";
    
    private ClassChoosePanel owlClassPanel;
    
    /**
     * Constructor creates new panel and adds listener to list.
     */
    public ClassChoosePanelDescriptor() {
        owlClassPanel = new ClassChoosePanel();
        owlClassPanel.addSelectionListener(this);
        owlClassPanel.addChangeListener(this);
             
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
    
    /**
     * Method is called when other element in list is selected, and sets next button enabled.
     * @param e ListSelectionEvent
     */
	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected(); 
		if (!e.getValueIsAdjusting() && owlClassPanel.getClassesTable().getSelectedRow() >= 0) {
			 OREManager.getInstance().setCurrentClass2Learn((NamedClass) owlClassPanel.getClassesTable().getSelectedValue());
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner)e.getSource();
		fillClassesList(((Integer)spinner.getValue()).intValue());
	}
	
	private void setNextButtonAccordingToConceptSelected() {
        
    	if (owlClassPanel.getClassesTable().getSelectedRow() >= 0){
    		getWizard().setNextFinishButtonEnabled(true);
    	}else{
    		getWizard().setNextFinishButtonEnabled(false);
    	}
   
    }
	
	/**
	 * Returns the JPanel with the GUI elements.
	 * @return extended JPanel
	 */
	public ClassChoosePanel getOwlClassPanel() {
		return owlClassPanel;
	}
	
	public void refill(){
		TaskManager.getInstance().setTaskStarted("Retrieving atomic classes...");
		new ClassRetrievingTask(1).execute();
	}
	
	public void fillClassesList(int minInstanceCount){
		TaskManager.getInstance().setTaskStarted("Retrieving atomic classes...");
		new ClassRetrievingTask(minInstanceCount).execute();
	}
	
	/**
     * Inner class to get all atomic classes in a background thread.
     * @author Lorenz Buehmann
     *
     */
    class ClassRetrievingTask extends SwingWorker<Set<NamedClass>, NamedClass> {
    	
    	private int minInstanceCount;
    	
    	public ClassRetrievingTask(int minInstanceCount){
    		this.minInstanceCount = minInstanceCount;
    	}

		@Override
		public Set<NamedClass> doInBackground() {
			OREManager.getInstance().makeOWAToCWA();
			Set<NamedClass> classes = new TreeSet<NamedClass>(OREManager.getInstance().getReasoner().getNamedClasses());
			classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
			Iterator<NamedClass> iter = classes.iterator();
			while(iter.hasNext()){
				NamedClass nc = iter.next();
				int instanceCount = OREManager.getInstance().getReasoner().getIndividuals(nc).size();
				if(instanceCount < minInstanceCount){
					iter.remove();
				}
			}
			return classes;
		}

		@Override
		public void done() {
			Set<NamedClass> classes = null;
			try {
				classes = get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			owlClassPanel.getClassesTable().addClasses(classes);
			TaskManager.getInstance().setTaskFinished();
		}

	}

	

}
