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
package org.dllearner.tools.protege;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class is the Panel for the Check boxes where the positive and negative
 * examples are chosen.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class PosAndNegSelectPanel extends JPanel {

	private static final long serialVersionUID = 23632947283479L;

	// This is the Panel here the check boxes, the labels, and the help buttons
	// are in.

	//private JPanel posAndNegPanel;

	// this is the Panel where the check boxes are.

	private JPanel posAndNegSelectPanel;

	// This is the DLLearner Model

	private DLLearnerModel model;

	// This is the Scroll pane if there are more Check boxes than the view can
	// show

	private JScrollPane posScrollList;
	
	private JScrollPane negScrollList;
	
	private JList posList;
	private JList negList;
	private JPanel posPanel;
	private JPanel negPanel;
	private JPanel buttonPanel;
	private JButton addToNegExamples;
	private JButton addToPosExamples;
	private JPanel posLabelPanel;
	private JPanel negLabelPanel;

	// This is the Label that shows "Positive Examples"

	private JLabel pos;

	// This is the Label that shows "Negative Examples"

	private JLabel neg;

	// This is the Panel where the Label for Positive Examples and
	// a help Button is in


	// This is the Help button for positive examples

	private JButton helpForPosExamples;

	// This is the Help button for negative examples

	private JButton helpForNegExamples;

	// This is the Text area where the help message is displayed.
	private OptionPanel optionPanel;
	//private JComboBox optionBox;
	//private JPanel optionBoxPanel;
	//private ActionHandler action;
	private DefaultListModel posListModel;
	private DefaultListModel negListModel;
	private JPanel examplePanel;
	private PosAndNegSelectPanelHandler handler;
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;

	/**
	 * This is the constructor for the Panel that shows the check boxes.
	 * 
	 * @param model
	 *            DLLearnerModel
	 * @param act
	 *            ActionHandler
	 */
	public PosAndNegSelectPanel(DLLearnerModel model, ActionHandler act, OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView v) {
		//set layout for parent Panel
		super();
		setLayout(new GridLayout(0, 1));
		setPreferredSize(new Dimension(490, 250));
		view = v;
		this.model = model;
		handler = new PosAndNegSelectPanelHandler(model, view, this);
		//Instantiate all objects needed
		optionPanel = new OptionPanel();
		examplePanel = new JPanel(null);
		posLabelPanel = new JPanel(null);
		negLabelPanel = new JPanel(null);
		posListModel = new DefaultListModel();
		negListModel = new DefaultListModel();
		pos = new JLabel("Positive Examples");
		pos.setBounds(0, 0, 100, 30);
		neg = new JLabel("Negative Examples");
		neg.setBounds(0, 0, 100, 30);
		posList = new JList(posListModel);
		negList = new JList(negListModel);	
		addToPosExamples = new JButton("pos");
		addToNegExamples = new JButton("neg");
		helpForPosExamples = new JButton("?");
		helpForPosExamples.setBounds(100, 5, 20, 20);
		helpForNegExamples = new JButton("?");
		helpForNegExamples.setBounds(100, 5, 20, 20);
		helpForPosExamples.setName("PosHelpButton");
		helpForNegExamples.setName("NegHelpButton");
		//set size for components that have no layout.
		posPanel = new JPanel(null);
		posPanel.setPreferredSize(new Dimension(200, 100));
		negPanel = new JPanel(null);
		negPanel.setPreferredSize(new Dimension(200, 100));
		buttonPanel = new JPanel(null);
		buttonPanel.setPreferredSize(new Dimension(90, 85));
		addToPosExamples.setBounds(0, 50, 70, 30);
		addToNegExamples.setBounds(0, 80, 70, 30);
		addToPosExamples.setEnabled(false);
		addToNegExamples.setEnabled(false);
		buttonPanel.add(addToPosExamples);
		buttonPanel.add(addToNegExamples);
		posLabelPanel.add(pos);
		posLabelPanel.add(helpForPosExamples);
		negLabelPanel.add(neg);
		negLabelPanel.add(helpForNegExamples);
		posScrollList = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		posScrollList.setViewportView(posList);
		
		negScrollList = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		negScrollList.setViewportView(negList);
		
		posLabelPanel.setBounds(0, 0, 200, 30);
		posScrollList.setBounds(0, 40, 190, 85);
		
		posPanel.add(posScrollList);
		posPanel.add(posLabelPanel);
		
		negLabelPanel.setBounds(0, 0, 200, 30);
		negScrollList.setBounds(0, 40, 185, 85);
		negPanel.add(negLabelPanel);
		negPanel.add(negScrollList);
		
		posPanel.setBounds(0, 0, 200, 250);
		buttonPanel.setBounds(210, 0, 90, 250);
		negPanel.setBounds(300, 0, 200, 250);
		examplePanel.add(posPanel);
		examplePanel.add(buttonPanel);
		examplePanel.add(negPanel);
		addHelpButtonListener(handler);
		add(optionPanel);
		add(examplePanel);
	}

	/**
	 * This method adds the check boxes, the labels and the help buttons for
	 * positive and negative examples.
	 */
	public void setExampleList(DefaultListModel posData, DefaultListModel negData) {
		posListModel = posData;
		negListModel = negData;
		posList.setModel(posListModel);
		negList.setModel(negListModel);
	}
	
	public JButton getAddToPosPanelButton() {
		return addToPosExamples;
	}
	
	public JButton getAddToNegPanelButton() {
		return addToNegExamples;
	}
	
	/**
	 * This method removes the Check boxes, the labels and the help buttons
	 * after the DL-Learner tab is closed.
	 */
	public void unsetPosAndNegPanel() {
		
	}
	
	/**
	 * This method adds the item listener for every check box.
	 * 
	 * @param act
	 *            ActionHandler
	 */
	public void addListeners(ActionHandler act) {
		// adds the listener for the checkboxes

	}
	
	public void removeListeners(ActionHandler act) {

	}
	
	public void setCheckBoxesEnable(boolean enable) {
	}
	
	/**
	 * This method returns the Panel where the check boxes, labels and help
	 * buttons are in.
	 * 
	 * @return JPanel where check boxes, labels and help buttons are in.
	 */
	public JPanel getPosAndNegSelectPanel() {
		return posAndNegSelectPanel;
	}

	/**
	 * This method unselect the selected check boxes after learning.
	 */
	public void unsetCheckBoxes() {
		// after the learning the check boxes will be unset.
		model.unsetJCheckBoxen();
	}

	/**
	 * This message displays the help message after the help button is pressed.
	 * 
	 * @param assistance String
	 */
	public void renderHelpMessage(String assistance) {
		// renders scroll bar if necessary
		JOptionPane.showMessageDialog(null, assistance);

	}
	
	/**
	 * This method adds the Action listener to the help buttons.
	 * 
	 * @param a
	 *            ActionHandler
	 */
	public void addHelpButtonListener(PosAndNegSelectPanelHandler handle) {
		// adds listener to the help button for the positive examples
		helpForPosExamples.addActionListener(handle);
		// adds listener to the help button for the negative examples
		helpForNegExamples.addActionListener(handle);
	}
	
	public void removeHelpButtonListener(ActionHandler a) {
		helpForPosExamples.removeActionListener(a);
		helpForNegExamples.removeActionListener(a);
	}
	public OptionPanel getOptionPanel() {
		return optionPanel;
	}

}
