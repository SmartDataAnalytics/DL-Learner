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
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * This class is the Panel for the Check boxes where the positive and negative
 * examples are chosen.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class PosAndNegSelectPanel extends JPanel {

	private static final long serialVersionUID = 23632947283479L;

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
	private DefaultListModel posListModel;
	private ImageIcon addToPosListIcon;
	private ImageIcon addToNegListIcon;
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
	 * @param v DLLearnerView
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
		URL iconUrl = this.getClass().getResource("backspace.gif");
		addToPosListIcon = new ImageIcon(iconUrl);
		URL toggledIconUrl = this.getClass().getResource("space.gif");
		addToNegListIcon = new ImageIcon(toggledIconUrl);
		posListModel = new DefaultListModel();
		negListModel = new DefaultListModel();
		pos = new JLabel("Positive Examples");
		pos.setBounds(0, 0, 100, 30);
		neg = new JLabel("Negative Examples");
		neg.setBounds(0, 0, 100, 30);
		posList = new JList(posListModel);
		posList.setName("pos");
		posList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		negList = new JList(negListModel);
		negList.setName("neg");
		negList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addToPosExamples = new JButton(addToPosListIcon);
		addToPosExamples.setName("pos");
		addToNegExamples = new JButton(addToNegListIcon);
		addToNegExamples.setName("neg");
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
		posList.addMouseListener(handler);
		negList.addMouseListener(handler);
		addToPosExamples.addActionListener(handler);
		addToNegExamples.addActionListener(handler);
	}

	/**
	 * This method adds the check boxes, the labels and the help buttons for
	 * positive and negative examples.
	 * @param posData DefaultListModel
	 * @param negData DefaultListModel
	 */
	public void setExampleList(DefaultListModel posData, DefaultListModel negData) {
		posListModel = posData;
		negListModel = negData;
		posList.setModel(posListModel);
		negList.setModel(negListModel);
	}
	
	/**
	 * This method returns the pos button.
	 * @return JButton
	 */
	public JButton getAddToPosPanelButton() {
		return addToPosExamples;
	}
	
	/**
	 * This method returns the neg button.
	 * @return JButton
	 */
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
	
	/**
	 * Nothing.
	 * @param act ActionHandler
	 */
	public void removeListeners(ActionHandler act) {

	}
	
	/**
	 * Nothing.
	 * @param enable boolean
	 */
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
	 * This message displays the help message after the help button is pressed.
	 * 
	 * @param assistance String
	 */
	public void renderHelpMessage(String assistance) {
		// renders scroll bar if necessary
		JOptionPane.showMessageDialog(null, assistance);

	}
	
	/**
	 * this method sets the example to the other list when button is pressed.
	 * @param toPos boolean
	 * @param example String
	 */
	public void setExampleToOtherList(boolean toPos, String example) {
		if (toPos) {
			for(int i = 0; i < negListModel.size(); i++) {
				if(negListModel.get(i).equals(example)) {
					negListModel.remove(i);
					for (int j = 0; j < model.getIndividualVector().size(); j++) {
						if (model.getIndividualVector().get(j).getIndividualString().contains(example)) {
							model.getIndividualVector().get(j).setExamplePositive(true);
							break;
						}
					}
					
				}
			}
			posListModel.add(0, example);
		} else {
			for(int i = 0; i < posListModel.size(); i++) {
				if(posListModel.get(i).equals(example)) {
					posListModel.remove(i);
					for (int j = 0; j < model.getIndividualVector().size(); j++) {
						if (model.getIndividualVector().get(j).getIndividualString().contains(example)) {
							model.getIndividualVector().get(j).setExamplePositive(false);
						}
					}
					break;
				}
			}
			negListModel.add(0, example);
		}
		setExampleList(posListModel, negListModel);
		addToPosExamples.setEnabled(false);
		addToNegExamples.setEnabled(false);
		
	}
	
	/**
	 * This method adds the Action listener to the help buttons.
	 * 
	 * @param handle
	 *            PosAndNegSelectPanelHandler
	 */
	public void addHelpButtonListener(PosAndNegSelectPanelHandler handle) {
		// adds listener to the help button for the positive examples
		helpForPosExamples.addActionListener(handle);
		// adds listener to the help button for the negative examples
		helpForNegExamples.addActionListener(handle);
	}
	/**
	 * This Method removes the listeners for the help button.
	 * @param a ActionHandler
	 */
	public void removeHelpButtonListener(ActionHandler a) {
		helpForPosExamples.removeActionListener(a);
		helpForNegExamples.removeActionListener(a);
	}
	
	/**
	 * This method returns the option panel.
	 * @return OptionPanel 
	 */
	public OptionPanel getOptionPanel() {
		return optionPanel;
	}
	
	/**
	 * This method returns the list of positive examples.
	 * @return JList posExampleList
	 */
	public JList getPosExampleList() {
		return posList;
	}
	
	/**
	 * This method returns the list of negative examples.
	 * @return JList negExampleList
	 */
	public JList getNegExampleList() {
		return negList;
	}

}
