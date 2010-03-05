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
import javax.swing.JTextPane;
import javax.swing.JToggleButton;

import org.dllearner.algorithms.celoe.CELOE;
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
	//TODO: gucken wie geht
	private HyperLinkHandler hyperHandler;
	// this is the Component which shows the view of the dllearner
	private final JComponent learner;

	// Accept button to add the learned concept to the owl

	private final JButton accept;

	// Runbutton to start the learning algorithm

	private final JButton run;

	// This is the label for the advanced button.

	private final JLabel adv;

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
	private final JTextPane wikiPane;
	private final ImageIcon toggledIcon;
	private ImageIcon helpIcon;
	private final JTextPane hint;
	private JButton helpButton;
	private final JPanel runPanel;
	private final JPanel advancedPanel;
	private JPanel hintPanel;
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
	private String labels;
	private int individualSize;
	private SuggestClassPanelHandler sugPanelHandler;
	private StatusBar2 stat;
	private static final String WIKI_STRING = "<html><font size=\"3\">See <a href=\"http://dl-learner.org/wiki/ProtegePlugin\">DL-Learner plugin page</a> for an introduction.</font></html>";

	/**
	 * The constructor for the DL-Learner tab in the class description
	 * editor.
	 * 
	 * @param editor OWLEditorKit
	 */
	public DLLearnerView(OWLEditorKit editor) {
		editorKit = editor;
		labels = "";
		individualSize = 0;
		hyperHandler = new HyperLinkHandler();
		model = new DLLearnerModel(editorKit, this);
		sugPanel = new SuggestClassPanel(model, this);
		learnerPanel = new JPanel();
		hintPanel = new JPanel(new FlowLayout());
		learnerPanel.setLayout(new BorderLayout());
		learnerScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		action = new ActionHandler(model, this);
		wikiPane = new JTextPane();
		wikiPane.setContentType("text/html");
		wikiPane.setBackground(learnerScroll.getBackground());
		wikiPane.setEditable(false);
		wikiPane.setText(WIKI_STRING);
		wikiPane.addHyperlinkListener(hyperHandler);
		URL iconUrl = this.getClass().getResource("arrow.gif");
		icon = new ImageIcon(iconUrl);
		URL toggledIconUrl = this.getClass().getResource("arrow2.gif");
		toggledIcon = new ImageIcon(toggledIconUrl);
		adv = new JLabel("<html>Advanced Settings</html>");
		advanced = new JToggleButton(icon);
		advanced.setVisible(true);
		advancedPanel = new JPanel();
		run = new JButton();
		URL helpIconUrl = this.getClass().getResource("Help-16x16.png");
		helpIcon = new ImageIcon(helpIconUrl);
		helpButton = new JButton(helpIcon);
		helpButton.setPreferredSize(new Dimension(20, 20));
		helpButton.setName("help");
		helpButton.addActionListener(action);
		runPanel = new JPanel(new FlowLayout());
		accept = new JButton("<html>ADD</html>");
		addButtonPanel = new JPanel(new BorderLayout());
		stat = new StatusBar2();
		stat.setBackground(learnerScroll.getBackground());
		this.setStatusBarVisible(false);
		hint = new JTextPane();
		hint.setBackground(learnerScroll.getBackground());
		hint.setContentType("text/html");
		hint.setEditable(false);
		hint.setText("<html><font size=\"3\">To get suggestions for class expression, please click the button above.</font></html>");
		learner = new JPanel();
		advanced.setSize(20, 20);
		learner.setLayout(new GridBagLayout());
		accept.setPreferredSize(new Dimension(70, 40));
		run.setPreferredSize(new Dimension(260, 30));
		advanced.setName("Advanced");
		learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		learnerScroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		posPanel = new PosAndNegSelectPanel(model, action);
		detail = new MoreDetailForSuggestedConceptsPanel(model);
		sugPanelHandler = new SuggestClassPanelHandler(this, model, action);
		sugPanel.getSuggestionsTable().getSelectionModel().addListSelectionListener(sugPanelHandler);
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
	 * @param label label if it is an equivalent or superclass
	 */
	public void makeView(String label) {
		run.setEnabled(false);

		helpButton.setVisible(false);
		hint.setForeground(Color.BLACK);
		hint.setText("<html><font size=\"3\">To get suggestions for class expression, please click the button above.</font></html>");
		String currentConcept = editorKit.getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass().toString();
		if(!labels.equals(currentConcept) || individualSize != editorKit.getModelManager().getActiveOntology().getIndividualAxioms().size()) {
			if(individualSize != editorKit.getModelManager().getActiveOntology().getIndividualAxioms().size()) {
				model.setKnowledgeSourceIsUpdated(true);
			} else {
				model.setKnowledgeSourceIsUpdated(false);
			}
			readThread = new ReadingOntologyThread(editorKit, this, model);
		}
		if(!readThread.isAlive() && !labels.equals(currentConcept)|| individualSize != editorKit.getModelManager().getActiveOntology().getIndividualAxioms().size()) {
			readThread.start();
		}
		if(readThread.hasIndividuals()) {
			run.setEnabled(true);
		}
		individualSize = editorKit.getModelManager().getActiveOntology().getIndividualAxioms().size();
		labels = currentConcept;
		run.setText("<html>suggest " + label + " expression</html>");
		GridBagConstraints c = new GridBagConstraints();
		learner.remove(detail);
		model.setID(label);
		runPanel.add(BorderLayout.WEST, run);
		runPanel.add(BorderLayout.EAST, wikiPane);
		
		
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
		learner.add(addButtonPanel, c);
		
		c.fill = GridBagConstraints.WEST;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 2;
		learner.add(stat, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 3;
		helpButton.setPreferredSize(new Dimension(30, 30));
		hintPanel.add(BorderLayout.CENTER, hint);
		hintPanel.add(BorderLayout.EAST, helpButton);
		learner.add(hintPanel, c);
		
		advancedPanel.add(advanced);
		advancedPanel.add(adv);
		advanced.setIcon(icon);
		advanced.setSelected(false);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 4;
		learner.add(advancedPanel, c);
		
		posPanel.setVisible(false);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 3;
		learner.add(posPanel, c);
		
		
		detail.unsetPanel();
		learnerPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		detail.setVisible(false);
		isInconsistent = false;

		hint.setVisible(true);
		action.resetToggled();
		detail.setVisible(true);
		sugPanel.setVisible(true);
		learnerScroll.setViewportView(learner);
		this.getSuggestClassPanel().getSuggestModel().clear();
			
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
	/**
	 * This methode returns the help button.
	 * @return help button
	 */
	public JButton getHelpButton() {
		return helpButton;
	}
	/**
	 * This method sets the status bar visible when learning
	 * is started.
	 */
	public void setStatusBarVisible(boolean b) {
		stat.setVisible(b);
	}
	/**
	 * This method enables the GraphicalCoveragePanel after a class expression is
	 * selected from the list.
	 */
	public void setGraphicalPanel() {
		GridBagConstraints c = new GridBagConstraints();
		learner.remove(posPanel);
		learner.remove(advancedPanel);
		detail.setVisible(true);
		
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.0;
		c.weighty = 0.0;
		learner.add(detail, c);
		

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 5;
		learner.add(advancedPanel, c);
		
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = GridBagConstraints.REMAINDER;
		learner.add(posPanel, c);
		
		learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		learnerScroll.setViewportView(learner);
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
	public JTextPane getHintPanel() {
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
	
	/**
	 * This method unsets all results after closing the plugin.
	 */
	public void dispose() {
		this.unsetEverything();
		sugPanel.getSuggestionsTable().clear();
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
		learner.removeAll();
	}

	/**
	 * This Method returns the panel for more details for the chosen concept.
	 * @return MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanel getMoreDetailForSuggestedConceptsPanel() {
		return detail;
	}

	/**
	 * This method returns the run button.
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
	 * This method returns if the ontology is inconsistent.
	 * @return boolean if ontology is inconsistent
	 */
	public boolean getIsInconsistent() {
		return isInconsistent;
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
		CELOE celoe = (CELOE) model.getLearningAlgorithm();
		this.setStatusBarVisible(false);
		String message = "<html><font size=\"3\" color=\"black\">Learning successful. All expressions up to length " + (celoe.getMinimumHorizontalExpansion()-1) +  " and some expressions up to <br>length " + celoe.getMaximumHorizontalExpansion() + " searched.";
		hint.setForeground(Color.RED);
		if(isInconsistent) {
			message +="<font size=\"3\" color=\"red\"><br>Class expressions marked red will lead to an inconsistent ontology. <br>Please click on them to view detail information.</font></html>";
		} else {
			message +="<br>To view details about why a class expression was suggested, please click on it.</font><html>";
		}
		run.setEnabled(true);
		// start the algorithm and print the best concept found;
		this.setHintMessage(message);
	}
	
	/**
	 * This method returns the view of the plugin.
	 * @return Plugin view
	 */
	public JComponent getLearnerView() {
		return learnerScroll;
	}
	
	/**
	 * This method sets the help button visible.
	 * @param isVisible boolean if help button is visible
	 */
	public void setHelpButtonVisible(boolean isVisible) {
		helpButton.setVisible(isVisible);
	}
	
	/**
	 * This method returns the model of the DL-Learner plugin.
	 * @return model of the plugin
	 */
	public DLLearnerModel getDLLearnerModel() {
		return model;
	}
	
	/**
	 * This method returns the thread for initializing the reasoner and reading the ontology.
	 * @return thread that initializes the reasoner
	 */
	public ReadingOntologyThread getReadingOntologyThread() {
		return readThread;
	}
	
	/**
	 * This method starts the status bar.
	 */
	public void startStatusBar() {
		stat.showProgress(true);
	}
	
	/**
	 * This method stops the status bar.
	 */
	public void stopStatusBar() {
		stat.showProgress(false);
	}
	
	/**
	 * This method returns the statusbar.
	 * @return statusbar
	 */
	public StatusBar2 getStatusBar() {
		return stat;
	}
	
	public HyperLinkHandler getHyperLinkHandler() {
		return hyperHandler;
	}
	
}
