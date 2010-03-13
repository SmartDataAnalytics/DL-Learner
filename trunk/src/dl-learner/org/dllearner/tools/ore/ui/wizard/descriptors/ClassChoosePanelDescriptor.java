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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.LearningManager.LearningMode;
import org.dllearner.tools.ore.ui.wizard.WizardPanelDescriptor;
import org.dllearner.tools.ore.ui.wizard.panels.ClassChoosePanel;



/**
 * Wizard panel descriptor for selecting one of the atomic classes in OWL-ontology that 
 * has to be (re)learned.
 * @author Lorenz Buehmann
 *
 */
public class ClassChoosePanelDescriptor extends WizardPanelDescriptor implements OREManagerListener, ListSelectionListener, ChangeListener, ActionListener{
    
	/**
	 * Identification string for class choose panel.
	 */
    public static final String IDENTIFIER = "CLASS_CHOOSE_OWL_PANEL";
    /**
     * Information string for class choose panel.
     */
    public static final String AUTO_LEARN_INFORMATION = "Adjust the parameters for automatic learning mode, " 
    										 +"then press <Next>";
    										 	
    public static final String MANUAL_LEARN_INFORMATION = "Above all atomic classes which have at least one individual are listed. " 
		 + "Select one of them for which you want to learn equivalent class or superclass expressions," +
		 	" then press <Next>";
    
    public static final String SKIP_LEARN_INFORMATION = "Choose one of the learning modes above, or press "+
		 	" <Next> to finish the wizard.";
    
    public static final String NO_LEARNING_SUPPORTED_INFORMATION = "LEarning is not supported because the currently loaded ontology contains no individuals. "+
 	"Please choose another ontology or press <Next> to finish the wizard.";
    
    private ClassChoosePanel classChoosePanel;
    private Map<Integer, Set<NamedClass>> instanceCountToClasses;
    
    /**
     * Constructor creates new panel and adds listener to list.
     */
    public ClassChoosePanelDescriptor() {
        classChoosePanel = new ClassChoosePanel();
        classChoosePanel.addSelectionListener(this);
        classChoosePanel.addChangeListener(this);
        classChoosePanel.addActionsListeners(this);
             
        OREManager.getInstance().addListener(this);
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(classChoosePanel);
        
        instanceCountToClasses = new HashMap<Integer, Set<NamedClass>>();
      
    }
    
    @Override
	public Object getNextPanelDescriptor() {
    	LearningMode mode = LearningManager.getInstance().getLearningMode();
    	if(mode == LearningMode.AUTO){
    		return AutoLearnPanelDescriptor.IDENTIFIER;
    	} else if(mode == LearningMode.MANUAL){
    		return ManualLearnPanelDescriptor.IDENTIFIER;
    	} else {
    		return SavePanelDescriptor.IDENTIFIER;
    	}
        
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return KnowledgeSourcePanelDescriptor.IDENTIFIER;
    }
    
	@Override
	public void aboutToDisplayPanel() {
		if (OREManager.getInstance().getReasoner().getIndividuals().size() == 0) {
			LearningManager.getInstance().setLearningMode(LearningMode.OFF);
			classChoosePanel.setLearningSupported(false);
			classChoosePanel.refreshLearningPanel();
			getWizard().getInformationField().setText(NO_LEARNING_SUPPORTED_INFORMATION);
		} else {
			classChoosePanel.setLearningSupported(true);

			LearningMode mode = LearningManager.getInstance().getLearningMode();
			if (mode == LearningMode.AUTO) {
				getWizard().getInformationField().setText(AUTO_LEARN_INFORMATION);
			} else if (mode == LearningMode.MANUAL) {
				getWizard().getInformationField().setText(MANUAL_LEARN_INFORMATION);
			} else {
				getWizard().getInformationField().setText(SKIP_LEARN_INFORMATION);
			}
		}
		setNextButtonAccordingToConceptSelected();
	}
    
    /**
     * Method is called when other element in list is selected, and sets next button enabled.
     * @param e ListSelectionEvent
     */
	public void valueChanged(ListSelectionEvent e) {
		setNextButtonAccordingToConceptSelected(); 
		if (!e.getValueIsAdjusting() && classChoosePanel.getClassesTable().getSelectedRow() >= 0) {
			 OREManager.getInstance().setCurrentClass2Learn((NamedClass) classChoosePanel.getClassesTable().getSelectedValue());
			 LearningManager.getInstance().setCurrentClass2Describe(classChoosePanel.getClassesTable().getSelectedValue());
			 
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner)e.getSource();
		fillClassesList(((Integer)spinner.getValue()).intValue());
	}
	
	private void setNextButtonAccordingToConceptSelected() {
        LearningManager man = LearningManager.getInstance();
    	if (classChoosePanel.getClassesTable().getSelectedRow() >= 0 
    			|| classChoosePanel.isAutoLearnMode() 
    			|| man.getLearningMode() == LearningMode.OFF){
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
		return classChoosePanel;
	}
	
	public void resetPanel(){
		classChoosePanel.reset();
	}
	
	public void retrieveClasses(){
		if(instanceCountToClasses.isEmpty()){
			TaskManager.getInstance().setTaskStarted("Retrieving atomic classes...");
			new ClassRetrievingTask().execute();
		} 
	}
	
	public void fillClassesList(int minInstanceCount){
		SortedSet<NamedClass> classes = new TreeSet<NamedClass>();
		for(Integer instanceCount  : instanceCountToClasses.keySet()){
			if(instanceCount.intValue() >= minInstanceCount){
				classes.addAll(instanceCountToClasses.get(instanceCount));
			}
		}
		classChoosePanel.getClassesTable().addClasses(classes);
	}
  
    /**
     * Inner class to get all atomic classes in a background thread.
     * @author Lorenz Buehmann
     *
     */
    class ClassRetrievingTask extends SwingWorker<Void, Void> {
    	

		@Override
		public Void doInBackground() {
			instanceCountToClasses.clear();
			OREManager.getInstance().makeOWAToCWA();
			Set<NamedClass> classes = new TreeSet<NamedClass>(OREManager.getInstance().getReasoner().getNamedClasses());
			classes.remove(new NamedClass("http://www.w3.org/2002/07/owl#Thing"));
			Iterator<NamedClass> iter = classes.iterator();
			while(iter.hasNext()){
				NamedClass nc = iter.next();
				int instanceCount = OREManager.getInstance().getReasoner().getIndividuals(nc).size();
				Set<NamedClass> temp = instanceCountToClasses.get(Integer.valueOf(instanceCount));
				if(temp == null) {
					temp = new HashSet<NamedClass>();
					temp.add(nc);
					instanceCountToClasses.put(Integer.valueOf(instanceCount), temp);
				}
				temp.add(nc);				
			}			
			return null;
		}

		@Override
		public void done() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					fillClassesList(3);		
					TaskManager.getInstance().setTaskFinished();
				}
			});					
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("auto")){
			LearningManager.getInstance().setLearningMode(LearningMode.AUTO);
		} else if(e.getActionCommand().equals("manual")){
			LearningManager.getInstance().setLearningMode(LearningMode.MANUAL);
			retrieveClasses();
		} else {
			LearningManager.getInstance().setLearningMode(LearningMode.OFF);
		}
		updateWizardInformation();
		classChoosePanel.refreshLearningPanel();
		setNextButtonAccordingToConceptSelected(); 
		
	}
	
	private void updateWizardInformation(){
		LearningMode mode = LearningManager.getInstance().getLearningMode();
    	if(mode == LearningMode.AUTO){
    		getWizard().getInformationField().setText(AUTO_LEARN_INFORMATION);
    	} else if(mode == LearningMode.MANUAL){
    		getWizard().getInformationField().setText(MANUAL_LEARN_INFORMATION);
    	} else {
    		getWizard().getInformationField().setText(SKIP_LEARN_INFORMATION);
    	}
	}
	
	
	public void setAutoLearningOptions(){
		classChoosePanel.setLearningOptions();
	}

	@Override
	public void activeOntologyChanged() {
		instanceCountToClasses.clear();
		
	}
}
