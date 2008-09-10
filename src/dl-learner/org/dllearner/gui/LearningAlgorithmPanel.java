package org.dllearner.gui;

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
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;

/**
 * LearningAlgorithmPanel, tab 3. Choose LearningAlgorithm, change Options and
 * final initiate LearningAlgorithm.
 * 
 * @author Tilo Hielscher
 */
public class LearningAlgorithmPanel extends ComponentPanel<LearningAlgorithm> {

	private static final long serialVersionUID = 8721490771860452959L;

	private Config config;
//	private StartGUI startGUI;
	private List<Class<? extends LearningAlgorithm>> selectableAlgorithms;
	private JPanel choosePanel = new JPanel();
	private OptionPanel optionPanel;
//	private JPanel initPanel = new JPanel();
//	private JButton initButton, autoInitButton;
	private String[] cbItems = {};
	private JComboBox cb = new JComboBox(cbItems);
	private int choosenClassIndex;

	public LearningAlgorithmPanel(Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
//		this.startGUI = startGUI;
		selectableAlgorithms = config.getComponentManager().getLearningAlgorithms();
		// to set a default learning algorithm, we move it to the beginning of the list
		selectableAlgorithms.remove(ExampleBasedROLComponent.class);
		selectableAlgorithms.add(0, ExampleBasedROLComponent.class);
		// we also remove the DBpedia Navigation Suggestor (maybe shouldn't be declared as a learning algorithm at all;
		// at least it is not doing anything useful at the moment)
		selectableAlgorithms.remove(DBpediaNavigationSuggestor.class);

//		initButton = new JButton("Init LearingAlgorithm");
//		initButton.addActionListener(this);
		// initPanel.add(initButton);
//		initButton.setEnabled(true);
//		autoInitButton = new JButton("Set");
//		autoInitButton.addActionListener(this);

		// add into comboBox
		for (int i = 0; i < selectableAlgorithms.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(selectableAlgorithms.get(i)));
		}

		choosePanel.add(cb);
//		choosePanel.add(autoInitButton);
		cb.addActionListener(this);

		LearningAlgorithm la = null;
		try {
			la = config.newLearningAlgorithm(selectableAlgorithms.get(cb.getSelectedIndex()));
		} catch (LearningProblemUnsupportedException e) {
			// TODO display message (or avoid selection at all)
			e.printStackTrace();
		}
		optionPanel = new OptionPanel(config, la);

		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);
//		add(initPanel, BorderLayout.PAGE_END);

		choosenClassIndex = cb.getSelectedIndex();
//		updateInitButtonColor();
	}

	public void actionPerformed(ActionEvent e) {
		// read selected Class
		if (choosenClassIndex != cb.getSelectedIndex()) {
			choosenClassIndex = cb.getSelectedIndex();
			config.changeLearningAlgorithm(selectableAlgorithms.get(choosenClassIndex));
			updateOptionPanel();
//			config.setInitLearningAlgorithm(false);
//			init();
		}

//		if (e.getSource() == autoInitButton)
//			setLearningAlgorithm();

//		if (e.getSource() == initButton)
//			init();
	}

	/**
	 * after this, you can change widgets
	 */
//	public void setLearningAlgorithm() {
//		if (config.getLearningProblem() != null && config.getReasoningService() != null) {
//			try {
//				config.setLearningAlgorithm(config.getComponentManager().learningAlgorithm(
//						selectableAlgorithms.get(choosenClassIndex), config.getLearningProblem(),
//						config.getReasoningService()));
//				updateOptionPanel();
//			} catch (LearningProblemUnsupportedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	/**
	 * after this, next tab can be used
	 */
	/*
	public void init() {
		setLearningAlgorithm();
		if (config.getLearningProblem() != null) {
			try {
				config.getLearningAlgorithm().init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
			config.setInitLearningAlgorithm(true);
			System.out.println("init LearningAlgorithm");
			startGUI.updateTabColors();
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
	}*/

	/**
	 * set ComboBox to selected class
	 */
	/*
	public void updateComboBox() {
		if (config.getLearningAlgorithm() != null)
			for (int i = 0; i < selectableAlgorithms.size(); i++)
				if (config.getLearningAlgorithm().getClass().equals(
						config.getComponentManager().getLearningAlgorithms().get(i))) {
					cb.setSelectedIndex(i);
				}
		this.choosenClassIndex = cb.getSelectedIndex();
	}*/

	/**
	 * update OptionPanel with new selection
	 */
	public void updateOptionPanel() {
		optionPanel.update(config.getLearningAlgorithm());
	}

	/**
	 * make init-button red if you have to click
	 */
	/*
	public void updateInitButtonColor() {
		if (!config.needsInitLearningAlgorithm()) {
			initButton.setForeground(Color.RED);
		} else
			initButton.setForeground(Color.BLACK);
	}*/

	/* (non-Javadoc)
	 * @see org.dllearner.gui.ComponentPanel#panelActivated()
	 */
	@Override
	public void panelActivated() {
		// TODO Auto-generated method stub
		
	}
}
