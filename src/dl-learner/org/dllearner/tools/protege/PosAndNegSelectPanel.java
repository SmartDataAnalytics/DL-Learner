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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

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

	private JPanel posAndNegPanel;

	// this is the Panel where the check boxes are.

	private JPanel posAndNegSelectPanel;

	// This is the DLLearner Model

	private DLLearnerModel model;

	// This is the Scroll pane if there are more Check boxes than the view can
	// show

	private JScrollPane scrollPanel;

	// This is the Label that shows "Positive Examples"

	private JLabel pos;

	// This is the Label that shows "Negative Examples"

	private JLabel neg;

	// This is the Panel where the Label for Positive Examples and
	// a help Button is in

	private JPanel posLabelPanel;

	// This is the Panel where the Label for Negative Examples and
	// a help Button is in

	private JPanel negLabelPanel;

	// This is the Help button for positive examples

	private JButton helpForPosExamples;

	// This is the Help button for negative examples

	private JButton helpForNegExamples;

	// This is the Text area where the help message is displayed.

	private JTextArea help;

	// This is the frame that pops up when the help button is pressed.

	private JDialog assistPanel;
	private final Color colorBlack = Color.black;

	/**
	 * This is the constructor for the Panel that shows the check boxes.
	 * 
	 * @param model
	 *            DLLearnerModel
	 * @param action
	 *            ActionHandler
	 */
	public PosAndNegSelectPanel(DLLearnerModel model, ActionHandler action) {
		super();
		pos = new JLabel("Positive Examples");
		neg = new JLabel("Negative Examples");
		// help button for positive examples
		helpForPosExamples = new JButton("?");
		helpForPosExamples.setSize(10, 10);
		// help button for negative examples
		helpForNegExamples = new JButton("?");
		helpForNegExamples.setSize(10, 10);
		posLabelPanel = new JPanel();
		negLabelPanel = new JPanel();
		// panel for the positive check boxes with flow layout
		posLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		// panel for the negative check boxes with flow layout
		negLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		// sets the name for the positive examples help button to
		// differ which button is pressed
		helpForPosExamples.setName("PosHelpButton");
		posLabelPanel.add(pos);
		posLabelPanel.add(helpForPosExamples);
		// sets the name for the negative examples help button to
		// differ which button is pressed
		helpForNegExamples.setName("NegHelpButton");
		negLabelPanel.add(neg);
		negLabelPanel.add(helpForNegExamples);
		this.model = model;
		// panel for the check boxes
		posAndNegSelectPanel = new JPanel(new GridLayout(0, 2));

		model.clearVector();
		model.unsetListModel();
		model.initReasoner();
		model.setPosVector();
		posAndNegPanel = new JPanel(new GridLayout(0, 1));
		posAndNegPanel.add(posAndNegSelectPanel);
		// renders scroll bars if necessary
		scrollPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.setViewportView(posAndNegPanel);
		scrollPanel.setPreferredSize(new Dimension(490, 248));
		add(scrollPanel);
		addListeners(action);
	}

	/**
	 * This method adds the check boxes, the labels and the help buttons for
	 * positive and negative examples.
	 */
	public void setJCheckBoxen() {
		posAndNegSelectPanel.add(posLabelPanel);
		posAndNegSelectPanel.add(negLabelPanel);
		// adds check boxes for all examples of the ontology
		for (int j = 0; j < model.getPosVector().size(); j++) {
			// this is for the check boxes of the positive examples
			posAndNegSelectPanel.add(model.getPositivJCheckBox(j));
			// this is for the check boxes of the negative examples
			posAndNegSelectPanel.add(model.getNegativJCheckBox(j));
		}

	}

	/**
	 * This method removes the Check boxes, the labels and the help buttons
	 * after the DL-Learner tab is closed.
	 */
	public void unsetPosAndNegPanel() {
		posAndNegSelectPanel.removeAll();
	}

	/**
	 * This method adds the item listener for every check box.
	 * 
	 * @param action
	 *            ActionHandler
	 */
	public void addListeners(ActionHandler action) {
		// adds the listener for the checkboxes
		for (int i = 0; i < model.getPosVector().size(); i++) {
			// listener for the check boxes of the positive examples
			model.getPositivJCheckBox(i).addItemListener(action);
			// listener for the check boxes of the negative examples
			model.getNegativJCheckBox(i).addItemListener(action);
		}

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
		JScrollPane scrollHelp = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		help = new JTextArea();
		assistPanel = new JDialog();
		// no writing in the help panel is allowed
		help.setEditable(false);
		assistPanel.setName("Hilfe");
		assistPanel.setSize(300, 100);
		// window will be disposed if the x or the ok button is pressed
		assistPanel.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		assistPanel.setVisible(true);
		assistPanel.setResizable(false);
		// sets textcolor to black
		help.setForeground(colorBlack);
		help.setText("Help: " + assistance);
		scrollHelp.setViewportView(help);
		scrollHelp.setBounds(0, 0, 300, 100);
		assistPanel.add(scrollHelp);
	}

	/**
	 * This method adds the Action listener to the help buttons.
	 * 
	 * @param a
	 *            ActionHandler
	 */
	public void addHelpButtonListener(ActionHandler a) {
		// adds listener to the help button for the positive examples
		helpForPosExamples.addActionListener(a);
		// adds listener to the help button for the negative examples
		helpForNegExamples.addActionListener(a);
	}

}
