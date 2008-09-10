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

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.core.LearningProblem;

/**
 * Learning problem panel.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 */
public class LearningProblemPanel extends ComponentPanel<LearningProblem> {

	private static final long serialVersionUID = -3819627680918930203L;

	private Config config;
//	private StartGUI startGUI;
	private List<Class<? extends LearningProblem>> lpClasses;
	private String[] lpBoxItems = {};
	private JComboBox cb = new JComboBox(lpBoxItems);
	private JPanel choosePanel = new JPanel();
	private OptionPanel optionPanel;
	private JPanel initPanel = new JPanel();
//	private JButton setButton;
	private int choosenClassIndex;

	LearningProblemPanel(final Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
//		this.startGUI = startGUI;
		lpClasses = config.getComponentManager().getLearningProblems();

//		setButton = new JButton("Set");
//		setButton.addActionListener(this);
		choosePanel.add(cb);
//		choosePanel.add(setButton);
		cb.addActionListener(this);

		// add into comboBox
		for (int i = 0; i < lpClasses.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(lpClasses.get(i)));
		}

		// read choosen LearningProblem
		choosenClassIndex = cb.getSelectedIndex();

		LearningProblem lp = config.newLearningProblem(lpClasses.get(choosenClassIndex));
		optionPanel = new OptionPanel(config, lp);

		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);

		choosenClassIndex = cb.getSelectedIndex();
		// setLearningProblem();
//		updateInitButtonColor();
	}

	public void actionPerformed(ActionEvent e) {
		// read selected LearningProblemClass
		if (choosenClassIndex != cb.getSelectedIndex()) {
			this.choosenClassIndex = cb.getSelectedIndex();
			config.changeLearningProblem(lpClasses.get(choosenClassIndex));
			updateOptionPanel();	
		}

//		if (e.getSource() == setButton)
//			setLearningProblem();

//		if (e.getSource() == initButton)
//			init();
	}

	/**
	 * after this, you can change widgets
	 */
//	private void setLearningProblem() {
//		if (config.needsInitReasoner()) {
//			config.setLearningProblem(config.getComponentManager().learningProblem(
//					lpClasses.get(choosenClassIndex), config.getReasoningService()));
//			startGUI.updateTabs();
//			updateOptionPanel();
//		}
//	}

	/**
	 * after this, next tab can be used
	 */
	/*
	public void init() {
		setLearningProblem();
		if (config.getReasoner() != null && config.getLearningProblem() != null
				&& config.isSetExample()) {
			try {
				config.getLearningProblem().init();
				config.setInitLearningProblem(true);
				System.out.println("init LearningProblem");
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
//		updateInitButtonColor();
	}*/

	/**
	 * set ComboBox to selected class
	 */
	/*
	public void updateComboBox() {
		if (config.getLearningProblem() != null)
			for (int i = 0; i < lpClasses.size(); i++)
				if (config.getLearningProblem().getClass().equals(
						config.getComponentManager().getLearningProblems().get(i))) {
					cb.setSelectedIndex(i);
				}
		this.choosenClassIndex = cb.getSelectedIndex();
	}*/

	/**
	 * update OptionPanel with new selection
	 */
	public void updateOptionPanel() {
		optionPanel.update(config.getLearningProblem());
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
		if (!config.needsInitLearningProblem()) {
			initButton.setForeground(Color.RED);
		} else
			initButton.setForeground(Color.BLACK);
	}*/
}
