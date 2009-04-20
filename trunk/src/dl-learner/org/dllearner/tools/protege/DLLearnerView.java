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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	private final JPanel runPanel;
	private final JPanel advancedPanel;
	private boolean isInconsistent;
	// This is the Panel for more details of the suggested concept
	private final MoreDetailForSuggestedConceptsPanel detail;
	private ReadingOntologyThread readThread;
	private final OWLEditorKit editorKit;
	private final JPanel learnerPanel;
	private final JScrollPane learnerScroll;
	private static  final int SCROLL_SPEED = 10;
	private static final int WIDTH = 575;
	private static final int HEIGHT = 350;
	private static final int OPTION_HEIGHT = 400;
	private static final int SCROLL_WIDTH = 600;
	private static final int SCROLL_HEIGHT = 400;
	private boolean toogled = false;

	/**
	 * The constructor for the DL-Learner tab in the class description
	 * editor.
	 * 
	 * @param editor OWLEditorKit
	 * @param label String
	 */
	public DLLearnerView(OWLEditorKit editor) {
		editorKit = editor;
		model = new DLLearnerModel(editorKit, this);
		sugPanel = new SuggestClassPanel();
		learnerPanel = new JPanel();
		learnerPanel.setLayout(new BorderLayout());
		learnerScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		action = new ActionHandler(model, this);
		wikiPane = new JLabel("<html>See <a href=\"http://dl-learner.org/wiki/ProtegePlugin\">http://dl-learner.org/wiki/ProtegePlugin</a> for an introduction.</html>");
		URL iconUrl = this.getClass().getResource("arrow.gif");
		icon = new ImageIcon(iconUrl);
		URL toggledIconUrl = this.getClass().getResource("arrow2.gif");
		toggledIcon = new ImageIcon(toggledIconUrl);
		adv = new JLabel("Advanced Settings");
		advanced = new JToggleButton(icon);
		advanced.setVisible(true);
		advancedPanel = new JPanel();
		run = new JButton();
		runPanel = new JPanel(new FlowLayout());
		accept = new JButton("ADD");
		addButtonPanel = new JPanel(new BorderLayout());
		sugPanel.addSuggestPanelMouseListener(action);
		errorMessage = new JTextArea();
		errorMessage.setEditable(false);
		hint = new JTextArea();
		hint.setEditable(false);
		hint.setText("To get suggestions for class expression, please click the button above.");
		hint.setPreferredSize(new Dimension(485, 30));
		learner = new JPanel();
		advanced.setSize(20, 20);
		learner.setLayout(new GridBagLayout());
		accept.setPreferredSize(new Dimension(70, 40));
		run.setPreferredSize(new Dimension(260, 30));
		advanced.setName("Advanced");
		model.initReasoner();
		learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		learnerScroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		posPanel = new PosAndNegSelectPanel(model, action);
		detail = new MoreDetailForSuggestedConceptsPanel(model);
		this.addAcceptButtonListener(this.action);
		this.addRunButtonListener(this.action);
		this.addAdvancedButtonListener(this.action);	
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
	public void makeView(String label) {
		run.setText("suggest " + label + " expression");
		GridBagConstraints c = new GridBagConstraints();
		learner.remove(detail);
		model.setID(label);
		runPanel.add(BorderLayout.WEST, run);
		runPanel.add(BorderLayout.EAST, wikiPane);
		run.setEnabled(false);
		
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 0;
		c.gridwidth = 3;
		learner.add(runPanel, c);
		
		sugPanel.setSuggestList(new DefaultListModel());
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		sugPanel.setSuggestList(model.getSuggestModel());
		learner.add(sugPanel, c);
		
		accept.setEnabled(false);
		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		addButtonPanel.add("North", accept);
		//c.gridwidth = GridBagConstraints.REMAINDER;
		learner.add(addButtonPanel, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 2;
		learner.add(hint, c);
		
		advancedPanel.add(advanced);
		advancedPanel.add(adv);
		advanced.setIcon(icon);
		advanced.setSelected(false);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 3;
		learner.add(advancedPanel, c);
		
		posPanel.setVisible(false);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 3;
		learner.add(posPanel, c);
		
		
		detail.unsetPanel();
		learnerPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		detail.setVisible(false);
		hint.setText("");
		isInconsistent = false;
		readThread = new ReadingOntologyThread(editorKit, this, model);
		readThread.start();
		hint.setVisible(true);
		action.resetToggled();
		detail.setVisible(true);
		sugPanel.setVisible(true);
		learnerScroll.setViewportView(learner);
		this.renderErrorMessage("");
			
	}
	
	/**
	 * This method sets the right icon for the advanced Panel.
	 * @param toggled boolean
	 */
	public void setIconToggled(boolean toggled) {
		this.toogled = toggled;
		if (this.toogled) {
			advanced.setIcon(toggledIcon);
			learnerPanel.setPreferredSize(new Dimension(WIDTH, OPTION_HEIGHT));
			learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		}
		if (!this.toogled) {
			advanced.setIcon(icon);
			learnerPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		}
	}
	
	public void setGraphicalPanel() {
		GridBagConstraints c = new GridBagConstraints();
		learner.remove(posPanel);
		learner.remove(advancedPanel);
		//learner.removeAll();
		detail.setVisible(true);
		
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.0;
		c.weighty = 0.0;
		learner.add(detail, c);
		

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 4;
		learner.add(advancedPanel, c);
		
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		learner.add(posPanel, c);
		
		learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		learnerScroll.setViewportView(learner);
		learnerScroll.repaint();
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
		detail.repaint();
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
		String error = "learning\nsuccessful";
		String message = "";
		if(isInconsistent) {
			message = "Class expressions marked red will lead to an inconsistent ontology. \nPlease double click on them to view detail information.";
		} else {
			message = "To view details about why a class expression was suggested, please click on it.";
		}
		run.setEnabled(true);
		// start the algorithm and print the best concept found
		renderErrorMessage(error);
		setHintMessage(message);
	}
	
	public JComponent getLearnerView() {
		return learnerScroll;
	}
	
	public DLLearnerModel getDLLearnerModel() {
		return model;
	}
	
	public ReadingOntologyThread getReadingOntologyThread() {
		return readThread;
	}
}
