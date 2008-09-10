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
package org.dllearner.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
 * Panel for configuring reasoner.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 */
public class ReasonerPanel extends ComponentPanel<ReasonerComponent> {

	private static final long serialVersionUID = -7678275020058043937L;

	private Config config;
//	private StartGUI startGUI;
	private List<Class<? extends ReasonerComponent>> selectableReasoners;
	private JPanel choosePanel = new JPanel();
	private JPanel initPanel = new JPanel();
	private OptionPanel optionPanel;
	private JButton initButton;
	private String[] cbItems = {};
	private JComboBox cb = new JComboBox(cbItems);
	private int choosenClassIndex;

	ReasonerPanel(final Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
//		this.startGUI = startGUI;
		selectableReasoners = config.getComponentManager().getReasonerComponents();
		// to set a default reasoner, we move it to the beginning of the list
		selectableReasoners.remove(OWLAPIReasoner.class);
		selectableReasoners.add(0, OWLAPIReasoner.class);

		initButton = new JButton("Init Reasoner");
		initButton.addActionListener(this);
		// initPanel.add(initButton);
		initButton.setEnabled(true);
//		setButton = new JButton("Set");
//		setButton.addActionListener(this);

		choosePanel.add(cb);

		// add into comboBox
		for (int i = 0; i < selectableReasoners.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(selectableReasoners.get(i)));
		}

		ReasonerComponent rc = config.newReasoner(selectableReasoners.get(cb.getSelectedIndex()));
		optionPanel = new OptionPanel(config, rc);

//		choosePanel.add(setButton);
		cb.addActionListener(this);

		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);

		choosenClassIndex = cb.getSelectedIndex();
//		setReasoner();
//		updateInitButtonColor();
	}

	public void actionPerformed(ActionEvent e) {
		// read selected Class
		// choosenClassIndex = cb.getSelectedIndex();
		if (choosenClassIndex != cb.getSelectedIndex()) {
			choosenClassIndex = cb.getSelectedIndex();
			// create a new knowledge source component
			config.changeReasoner(selectableReasoners.get(choosenClassIndex));
			updateOptionPanel();			
		}

//		if (e.getSource() == setButton) {
////			config.setInitReasoner(false);
//			setReasoner();
//		}

//		if (e.getSource() == initButton)
//			init();
	}

	/**
	 * after this, you can change widgets
	 */
//	public void setReasoner() {
//		if (config.needsInitKnowledgeSource()) {
//			config.setReasoner(config.getComponentManager().reasoner(
//					reasoner.get(choosenClassIndex), config.getKnowledgeSource()));
//			updateOptionPanel();
////			startGUI.updateTabColors();
////			config.setInitReasoner(false);
////			updateInitButtonColor();
//		}
//	}

	/**
	 * after this, next tab can be used
	 */
	/*
	public void init() {
		setReasoner();
		if (config.getKnowledgeSource() != null && config.getReasoner() != null) {
			try {
				config.getReasoner().init();
				System.out.println("init Reasoner");
				// set ReasoningService
				config.setReasoningService(config.getComponentManager().reasoningService(
						config.getReasoner()));
				System.out.println("init ReasoningService");
				config.setInitReasoner(true);
				startGUI.updateTabColors();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}

		}
	}*/

	/**
	 * updateAll
	 */
	/*
	public void updateAll() {
		updateComboBox();
		updateOptionPanel();
		updateInitButtonColor();
	}
	*/

	/**
	 * set ComboBox to selected class
	 */
	/*
	public void updateComboBox() {
		if (config.getReasoner() != null)
			for (int i = 0; i < selectableReasoners.size(); i++)
				if (config.getReasoner().getClass().equals(
						config.getComponentManager().getReasonerComponents().get(i))) {
					cb.setSelectedIndex(i);
				}
		this.choosenClassIndex = cb.getSelectedIndex();
	}*/

	/**
	 * update OptionPanel with new selection
	 */
	public void updateOptionPanel() {
		optionPanel.update(config.getReasoner());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.gui.ComponentPanel#panelActivated()
	 */
	@Override
	public void panelActivated() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * make init-button red if you have to click
	 */
	/*
	public void updateInitButtonColor() {
		if (!config.needsInitReasoner()) {
			initButton.setForeground(Color.RED);
		} else
			initButton.setForeground(Color.BLACK);
	}*/
}
