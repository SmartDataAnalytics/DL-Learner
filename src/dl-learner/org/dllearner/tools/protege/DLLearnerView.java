/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.tools.protege;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owl.model.OWLDescription;
/**
 * This class is responsible for the view of the dllearner. It renders the
 * output for the user and is the graphical component of the plugin.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class DLLearnerView {

	
	private static final  long serialVersionUID = 624829578325729385L; 
	// this is the Component which shows the view of the dllearner
	private final JComponent learner;

	// Accept button to add the learned concept to the owl

	private final JButton accept;

	// Runbutton to start the learning algorithm

	private final JButton run;

	// This is the label for the advanced button.

	private final JLabel adv;

	// This is the color for the error message. It is red.

	private final Color colorRed = Color.red;

	// This is the text area for the error message when an error occurred

	private final JTextArea errorMessage;

	// Advanced Button to activate/deactivate the example select panel

	private final JToggleButton advanced;

	// Action Handler that manages the Button actions

	private final ActionHandler action;

	// This is the model of the dllearner plugin which includes all data

	private final DLLearnerModel model;

	// Panel for the suggested concepts

	private SuggestClassPanel sugPanel;

	// Selection panel for the positive and negative examples

	private final PosAndNegSelectPanel posPanel;

	// Picture for the advanced button when it is not toggled

	private final ImageIcon icon;

	// Picture of the advanced button when it is toggled
	private final JPanel addButtonPanel;
	private final JLabel wikiPane;
	private final ImageIcon toggledIcon;
	private final JTextArea hint;
	private boolean isInconsistent;
	// This is the Panel for more details of the suggested concept
	private MoreDetailForSuggestedConceptsPanel detail;
	private ReadingOntologyThread readThread;
	private final OWLEditorKit editorKit;
	private final String label;

	/**
	 * The constructor for the DL-Learner tab in the class description
	 * editor.
	 * 
	 * @param editor OWLEditorKit
	 * @param label String
	 */
	public DLLearnerView(String label, OWLEditorKit editor) {
		this.label = label;
		editorKit = editor;
		model = new DLLearnerModel(editorKit, this);
		model.setID(label);
		sugPanel = new SuggestClassPanel();
		action = new ActionHandler(model, this, label);
		wikiPane = new JLabel("<html>See <a href=\"http://dl-learner.org/wiki/ProtegePlugin\">http://dl-learner.org/wiki/ProtegePlugin</a> for an introduction.</html>");
		URL iconUrl = this.getClass().getResource("arrow.gif");
		icon = new ImageIcon(iconUrl);
		URL toggledIconUrl = this.getClass().getResource("arrow2.gif");
		toggledIcon = new ImageIcon(toggledIconUrl);
		adv = new JLabel("Advanced Settings");
		advanced = new JToggleButton(icon);
		advanced.setVisible(true);
		run = new JButton(label);
		accept = new JButton("ADD");
		addButtonPanel = new JPanel(new BorderLayout());
		sugPanel.addSuggestPanelMouseListener(action);
		errorMessage = new JTextArea();
		errorMessage.setEditable(false);
		hint = new JTextArea();
		hint.setEditable(false);
		hint.setText("To get suggestions for class descriptions, please click the button above.");
		learner = new JPanel();
		advanced.setSize(20, 20);
		learner.setLayout(null);
		learner.setPreferredSize(new Dimension(600, 520));
		accept.setPreferredSize(new Dimension(290, 50));
		advanced.setName("Advanced");
		posPanel = new PosAndNegSelectPanel(model, action);
		addAcceptButtonListener(this.action);
		addRunButtonListener(this.action);
		addAdvancedButtonListener(this.action);
		
	}
	
	/**
	 * This method returns the SuggestClassPanel.
	 * @return SuggestClassPanel
	 */
	public SuggestClassPanel getSuggestClassPanel() {
		return sugPanel;
	}
	/**
	 * This method returns the PosAndNegSelectPanel.
	 * @return PosAndNegSelectPanel
	 */
	public PosAndNegSelectPanel getPosAndNegSelectPanel() {
		return posPanel;
	}
	
	/**
	 * This Method renders the view of the plugin.
	 */
	public void makeView() {
		run.setEnabled(false);
		hint.setText("To get suggestions for class descriptions, please click the button above.");
		isInconsistent = false;
		readThread = new ReadingOntologyThread(editorKit, this, model);
		readThread.start();
		hint.setVisible(true);
		advanced.setIcon(icon);
		accept.setEnabled(false);
		action.resetToggled();
		addButtonPanel.add("North", accept);
		sugPanel.setSuggestList(new DefaultListModel());
		sugPanel = sugPanel.updateSuggestClassList();
		advanced.setSelected(false);
		sugPanel.setBounds(10, 35, 490, 110);
		adv.setBounds(40, 200, 200, 20);
		wikiPane.setBounds(220, 0, 350, 30);
		addButtonPanel.setBounds(510, 40, 80, 110);
		run.setBounds(10, 0, 200, 30);
		advanced.setBounds(10, 200, 20, 20);
		sugPanel.setVisible(true);
		posPanel.setVisible(false);
		posPanel.setBounds(10, 230, 490, 250);
		posPanel.getOptionPanel().resetOptions();
		accept.setBounds(510, 40, 80, 110);
		hint.setBounds(10, 150, 490, 35);
		errorMessage.setBounds(10, 180, 490, 20);
		learner.add(run);
		learner.add(wikiPane);
		learner.add(adv);
		learner.add(advanced);
		learner.add(sugPanel);
		learner.add(addButtonPanel);
		learner.add(hint);
		learner.add(errorMessage);
		learner.add(posPanel);
		this.renderErrorMessage("");
		detail = new MoreDetailForSuggestedConceptsPanel(model);	
	}
	/**
	 * This method sets the right icon for the advanced Panel.
	 * @param toggled boolean
	 */
	public void setIconToggled(boolean toggled) {
		if (toggled) {
			advanced.setIcon(toggledIcon);
		}
		if (!toggled) {
			advanced.setIcon(icon);
		}
	}
	
	/**
	 * This Method changes the hint message. 
	 * @param message String hintmessage
	 */
	public void setHintMessage(String message) {
		hint.setText(message);
	}
	
	/**
	 * This method returns the hint panel.
	 * @return hint panel
	 */
	public JTextArea getHintPanel() {
		return hint;
	}
	
	/**
	 * Sets the panel to select/deselect the examples visible/invisible.
	 * @param visible boolean
	 */
	public void setExamplePanelVisible(boolean visible) {
		posPanel.setVisible(visible);
	}

	/**
	 * Returns the AddButton.
	 * @return JButton
	 */
	public JButton getAddButton() {
		return accept;
	}

	/**
	 * Returns all added descriptions.
	 * @return Set(OWLDescription) 
	 */
	public Set<OWLDescription> getSolutions() {

		return model.getNewOWLDescription();
	}
	
	public void dispose() {
		this.unsetEverything();
		sugPanel.getSuggestList().removeAll();
		learner.removeAll();
		sugPanel = null;
		model.getSuggestModel().clear();
		model.getIndividual().clear();
	}

	/**
	 * Returns the last added description.
	 * @return OWLDescription
	 */
	public OWLDescription getSolution() {
		return model.getSolution();
	}

	/**
    * Destroys everything in the view after the plugin is closed.
    */
	public void unsetEverything() {
		run.setEnabled(true);
		model.getNewOWLDescription().clear();
		action.destroyDLLearnerThread();
		errorMessage.setText("");
		learner.removeAll();
	}

	/**
	 * Renders the error message when an error occured.
	 * @param s String 
	 */
	public void renderErrorMessage(String s) {
		errorMessage.setForeground(colorRed);
		errorMessage.setText(s);
	}
	/**
	 * This Method returns the panel for more details for the chosen concept.
	 * @return MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanel getMoreDetailForSuggestedConceptsPanel() {
		return detail;
	}

	/**
	 * This Method returns the run button.
	 * @return JButton
	 */
	public JButton getRunButton() {
		return run;
	}
	
	/**
	 * This method sets if ontology is inconsistent or not.
	 * @param isIncon boolean if ontology is consisten
	 */
	public void setIsInconsistent(boolean isIncon) {
		this.isInconsistent = isIncon;
	}
	
	/**
	 * Adds Actionlistener to the run button.
	 * @param a ActionListener
	 */
	public void addRunButtonListener(ActionListener a) {
		run.addActionListener(a);
	}

	/**
	 * Adds Actionlistener to the add button.
	 * @param a ActionListener
	 */
	public void addAcceptButtonListener(ActionListener a) {
		accept.addActionListener(a);
	}

	/**
	 * Adds Actionlistener to the advanced button.
	 * @param a ActionListener
	 */
	public void addAdvancedButtonListener(ActionListener a) {
		advanced.addActionListener(a);
	}
	
	/**
	 * This method sets the run button enable after learning.
	 */
	public void algorithmTerminated() {
		String error = "learning succesful";
		String message = "";
		if(isInconsistent) {
			message = "Class descriptions marked red will lead to an inconsistent ontology. \nPlease double click on them to view detail information.";
		} else {
			message = "To view details about why a class description was suggested, please doubleclick on it.";
		}
		run.setEnabled(true);
		// start the algorithm and print the best concept found
		renderErrorMessage(error);
		setHintMessage(message);
	}
	
	public JComponent getLearnerView() {
		return learner;
	}
	
	public DLLearnerModel getDLLearnerModel() {
		return model;
	}
	
	public ReadingOntologyThread getReadingOntologyThread() {
		return readThread;
	}
}
