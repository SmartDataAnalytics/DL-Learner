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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;

import org.dllearner.core.EvaluatedDescription;
import org.protege.editor.owl.OWLEditorKit;
/**
 * This class is responsible for the view of the dllearner. It renders the
 * output for the user and is the graphical component of the plugin.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class DLLearnerView extends JPanel{

	
	private static final  long serialVersionUID = 624829578325729385L; 
	//TODO: gucken wie geht
	private HyperLinkHandler hyperHandler;
	// this is the Component which shows the view of the dllearner
	private JComponent learner;

	// Accept button to add the learned concept to the owl

	private JButton addButton;

	// Runbutton to start the learning algorithm

	private JButton runButton;

	// This is the label for the advanced button.

	private JLabel advancedLabel;

	// Advanced Button to activate/deactivate the example select panel

	private JToggleButton advancedButton;

	// Action Handler that manages the Button actions

	private ActionHandler actionHandler;


	// Panel for the suggested concepts

	private SuggestClassPanel sugPanel;

	// Selection panel for the positive and negative examples

	private OptionPanel optionsPanel;

	// Picture for the advanced button when it is not toggled

	private ImageIcon icon;

	// Picture of the advanced button when it is toggled
	private JTextPane wikiPane;
	private ImageIcon toggledIcon;
	private ImageIcon helpIcon;
	private JTextPane hint;
	private JButton helpButton;
	private JPanel advancedPanel;
	private JPanel hintPanel;
	private boolean isInconsistent;
	// This is the Panel for more details of the suggested concept
	private MoreDetailForSuggestedConceptsPanel detail;
	private OWLEditorKit editorKit;
	private JScrollPane learnerScroll;
	private static final int SCROLL_SPEED = 10;
	private static final int SCROLL_WIDTH = 600;
	private static final int SCROLL_HEIGHT = 400;
	private boolean toogled = false;
	private SuggestClassPanelHandler sugPanelHandler;
	private StatusBar statusBar;
	private static final String WIKI_STRING = "<html><font size=\"3\">See <a href=\"http://dl-learner.org/wiki/ProtegePlugin\">DL-Learner plugin page</a> for an introduction.</font></html>";

	/**
	 * The constructor for the DL-Learner tab in the class description
	 * editor.
	 * 
	 * @param editor OWLEditorKit
	 */
	public DLLearnerView(OWLEditorKit editor) {
		editorKit = editor;
		createUI();
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
	public OptionPanel getOptionsPanel() {
		return optionsPanel;
	}
	
	
	private void createUI(){
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		hyperHandler = new HyperLinkHandler();
		actionHandler = new ActionHandler(this);
		
		learnerScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		learnerScroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		
		JPanel addButtonPanel = new JPanel(new BorderLayout());
		addButton = new JButton("<html>ADD</html>");
		addButton.setPreferredSize(new Dimension(70, 40));
		addButton.setEnabled(false);
		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		addButtonPanel.add("North", addButton);
		add(addButtonPanel, c);
		
		JPanel runButtonPanel = new JPanel(new FlowLayout());
		runButton = new JButton();
		runButton.setPreferredSize(new Dimension(260, 30));
		runButton.setEnabled(false);
		runButtonPanel.add(BorderLayout.WEST, runButton);
		wikiPane = new JTextPane();
		wikiPane.setContentType("text/html");
		wikiPane.setBackground(learnerScroll.getBackground());
		wikiPane.setEditable(false);
		wikiPane.setText(WIKI_STRING);
		wikiPane.addHyperlinkListener(hyperHandler);
		runButtonPanel.add(BorderLayout.EAST, wikiPane);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 0;
		c.gridwidth = 3;
		add(runButtonPanel, c);
		
		sugPanel = new SuggestClassPanel(editorKit);
		sugPanelHandler = new SuggestClassPanelHandler(this);
		sugPanel.getSuggestionsTable().getSelectionModel().addListSelectionListener(sugPanelHandler);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		add(sugPanel, c);
		
		statusBar = new StatusBar();
		statusBar.setBackground(learnerScroll.getBackground());
		showStatusBar(false);
		c.fill = GridBagConstraints.WEST;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 2;
		add(statusBar, c);
		
		hintPanel = new JPanel(new FlowLayout());
		hint = new JTextPane();
		hint.setBackground(learnerScroll.getBackground());
		hint.setContentType("text/html");
		hint.setEditable(false);
		hintPanel.add(BorderLayout.CENTER, hint);
		URL helpIconUrl = this.getClass().getResource("Help-16x16.png");
		helpIcon = new ImageIcon(helpIconUrl);
		helpButton = new JButton(helpIcon);
		helpButton.setName("help");
		helpButton.addActionListener(actionHandler);
		helpButton.setPreferredSize(new Dimension(30, 30));
		helpButton.setVisible(false);
		hintPanel.add(BorderLayout.EAST, helpButton);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 3;
		add(hintPanel, c);
		
		detail = new MoreDetailForSuggestedConceptsPanel(editorKit);
//		detail.unsetPanel();
		detail.setVisible(false);
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.0;
		c.weighty = 0.0;
		add(detail, c);
		
		advancedPanel = new JPanel();
		URL iconUrl = this.getClass().getResource("arrow.gif");
		icon = new ImageIcon(iconUrl);
		URL toggledIconUrl = this.getClass().getResource("arrow2.gif");
		toggledIcon = new ImageIcon(toggledIconUrl);
		advancedLabel = new JLabel("<html>Advanced Settings</html>");
		advancedPanel.add(advancedLabel);
		advancedButton = new JToggleButton(icon);
		advancedButton.setName("Advanced");
		advancedButton.setIcon(icon);
		advancedButton.setSelected(false);
		advancedButton.setVisible(true);
		advancedButton.setSize(20, 20);
		advancedPanel.add(advancedButton);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 5;
		add(advancedPanel, c);
		
		optionsPanel = new OptionPanel();
		optionsPanel.setVisible(false);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 3;
		add(optionsPanel, c);
		
		this.addAcceptButtonListener(this.actionHandler);
		this.addRunButtonListener(this.actionHandler);
		this.addAdvancedButtonListener(this.actionHandler);	
		
		learnerScroll.setViewportView(this);
	}
	
	public void reset(){
		String learningType = "";
		if(Manager.getInstance().getLearningType() == LearningType.EQUIVALENT){
			learningType = "equivalent class";
		} else {
			learningType = "super class";
		}
		runButton.setText("<html>suggest " + learningType + " expressions</html>");
		sugPanel.getSuggestionsTable().clear();
		showGraphicalPanel(false);
		setHintMessage("");
		setHelpButtonVisible(false);
		
	}
	
	public JComponent getView(){
		return learnerScroll;
	}
	
	/**
	 * This method sets the right icon for the advanced Panel.
	 * @param toggled boolean
	 */
	public void setIconToggled(boolean toggled) {
		this.toogled = toggled;
		if (this.toogled) {
			advancedButton.setIcon(toggledIcon);
//			learnerPanel.setPreferredSize(new Dimension(WIDTH, OPTION_HEIGHT));
//			learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		}
		if (!this.toogled) {
			advancedButton.setIcon(icon);
//			learnerPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//			learnerScroll.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT));
		}
	}
	
	/**
	 * This method sets the status bar visible when learning
	 * is started.
	 */
	public void showStatusBar(boolean show) {
		statusBar.setVisible(show);
	}
	
	public void showGraphicalPanel(boolean show){
		detail.setVisible(show);
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
	public void showOptionsPanel(boolean show) {
		optionsPanel.setVisible(show);
	}
	
	public void showHintMessagePanel(boolean show){
		hintPanel.setVisible(show);
	}

	/**
	 * Returns the AddButton.
	 * @return JButton
	 */
	public JButton getAddButton() {
		return addButton;
	}

	
	/**
	 * This method unsets all results after closing the plugin.
	 */
	public void dispose() throws Exception{
		this.unsetEverything();
		sugPanel.getSuggestionsTable().clear();
		learner.removeAll();
		sugPanel = null;
	}


	/**
    * Destroys everything in the view after the plugin is closed.
    */
	public void unsetEverything() {
		runButton.setEnabled(true);
		learner.removeAll();
	}

	/**
	 * This Method returns the panel for more details for the chosen concept.
	 * @return MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanel getGraphicalPanel() {
		return detail;
	}

	
	public void setLearningEnabled(){
		runButton.setEnabled(true);
		setHintMessage("<html><font size=\"3\">To get suggestions for class descriptions," +
				" please click the button above.</font></html>");
	}
	
	public void showNoInstancesMessage() {
		String message = "<html><font size=\"3\" color=\"red\">There are no instances for "
				+ Manager.getInstance().getCurrentlySelectedClassRendered()
				+ " available. Please insert some instances.</font></html>";
		setHintMessage(message);
	}
	
	/**
	 * This method sets if ontology is inconsistent or not.
	 * @param isIncon boolean if ontology is consistent
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
		runButton.addActionListener(a);
	}

	/**
	 * Adds Actionlistener to the add button.
	 * @param a ActionListener
	 */
	public void addAcceptButtonListener(ActionListener a) {
		addButton.addActionListener(a);
	}

	/**
	 * Adds Actionlistener to the advanced button.
	 * @param a ActionListener
	 */
	public void addAdvancedButtonListener(ActionListener a) {
		advancedButton.addActionListener(a);
	}
	
	/**
	 * This method sets the run button enable after learning.
	 */
	public void showAlgorithmTerminatedMessage() {
		this.showStatusBar(false);
		String message = "<html><font size=\"3\" color=\"black\">Learning successful. All expressions up to length "
			+ (Manager.getInstance().getMinimumHorizontalExpansion()-1) + 
			" and some expressions up to <br>length "
			+ Manager.getInstance().getMaximumHorizontalExpansion() 
			+ " searched.";
		hint.setForeground(Color.RED);
		if(isInconsistent) {
			message +="<font size=\"3\" color=\"red\"><br>Class expressions marked red will lead to an inconsistent ontology. <br>Please click on them to view detail information.</font></html>";
		} else {
			message +="<br>To view details about why a class expression was suggested, please click on it.</font><html>";
		}
		runButton.setEnabled(true);
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
	 * This method returns the statusbar.
	 * @return statusbar
	 */
	public StatusBar getStatusBar() {
		return statusBar;
	}
	
	public HyperLinkHandler getHyperLinkHandler() {
		return hyperHandler;
	}
	
	public void setBusy(boolean busy){
		if(busy){
			getLearnerView().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			getLearnerView().setCursor(Cursor.getDefaultCursor());
		}
		
	}
	
	public void setBusyTaskStarted(String taskName){
		setBusy(true);
		setHelpButtonVisible(true);
		showStatusBar(true);
		statusBar.showProgress(true);
		statusBar.setMessage(taskName);
	}
	
	public void setBusyTaskEnded(){
		setBusy(false);
		setHelpButtonVisible(false);
		showStatusBar(false);
		statusBar.showProgress(false);
		statusBar.setMessage("");
	}
	
	public void setLearningStarted(){
		setBusy(true);
		setHelpButtonVisible(true);
		showStatusBar(true);
		runButton.setEnabled(false);
		statusBar.setMaximumValue(Manager.getInstance().getMaxExecutionTimeInSeconds() * 2);
		statusBar.setMessage("Learning ...");
	}
	
	public void setLearningFinished(){
		statusBar.setProgress(0);
		setBusy(false);
		showAlgorithmTerminatedMessage();
	}
	
	public void setRunButtonEnabled(boolean enabled){
		runButton.setEnabled(enabled);
	}
	
	public void showHorizontalExpansionMessage(int min, int max){
		StringBuffer sb = new StringBuffer();
		sb.append("<html><font size=\"3\">Currently searching class expressions with length between ");
		sb.append(min);
		sb.append(" and ");
		sb.append(max);
		sb.append(".</font></html>");
		setHintMessage(sb.toString());
	}
	
	public void setSuggestions(List<? extends EvaluatedDescription> suggestions){
		sugPanel.setSuggestions(suggestions);
	}
	
}
