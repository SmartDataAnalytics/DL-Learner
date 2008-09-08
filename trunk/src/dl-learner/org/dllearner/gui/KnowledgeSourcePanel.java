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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;


/**
 * KnowledgeSourcePanel, tab 0. Choose Source, change Options and final initiate
 * KnowledgeSource.
 * 
 * @author Tilo Hielscher
 */
public class KnowledgeSourcePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -7678275020058043937L;

	private Config config;
	private StartGUI startGUI;
	private JButton initButton;
	private JButton setButton;
	private JButton clearButton;
	private String[] kbBoxItems = {};
	private JComboBox cb = new JComboBox(kbBoxItems);
	private JPanel choosePanel = new JPanel();
	private JPanel initPanel = new JPanel();
	private int choosenClassIndex;
	private List<Class<? extends KnowledgeSource>> selectableSources;
	private OptionPanel optionPanel;

	KnowledgeSourcePanel(final Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
		this.startGUI = startGUI;
		selectableSources = config.getComponentManager().getKnowledgeSources();

		setButton = new JButton("Set");
		setButton.addActionListener(this);
		setButton = new JButton("Clear All");
		setButton.addActionListener(this);
		initButton = new JButton("Init KnowledgeSource");
		initButton.addActionListener(this);
		initButton.setEnabled(true);

		// add to comboBox
		for (int i = 0; i < selectableSources.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(selectableSources.get(i)));
		}
		cb.addActionListener(this);

		choosePanel.add(cb);
		choosePanel.add(setButton);
		choosenClassIndex = cb.getSelectedIndex();

		// whenever a component is selected, we immediately create an instance (non-initialised)
		KnowledgeSource ks = config.newKnowledgeSource(selectableSources.get(cb.getSelectedIndex()));
		optionPanel = new OptionPanel(config, ks);
		
//		optionPanel = new OptionPanel(config, config.getKnowledgeSource(), sources.get(choosenClassIndex));
		// initPanel.add(initButton);

		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);

		setSource();
		updateAll();
	}

	public void actionPerformed(ActionEvent e) {
		if (choosenClassIndex != cb.getSelectedIndex()) {
			choosenClassIndex = cb.getSelectedIndex();
			// create a new knowledge source component
			config.newKnowledgeSource(selectableSources.get(choosenClassIndex));
			updateAll();
//			config.setInitKnowledgeSource(false);
//			init();
		}

		if (e.getSource() == setButton) {
			setSource();
		}

		if (e.getSource() == initButton) {
			init();
		}

		if (e.getSource() == clearButton) {
			config.reInit();
		}
	}

	/**
	 * after this, you can change widgets
	 */
	public void setSource() {
		config.setKnowledgeSource(config.getComponentManager().knowledgeSource(
				selectableSources.get(choosenClassIndex)));
//		config.setInitKnowledgeSource(false);
		updateAll();
	}

	/**
	 * after this, next tab can be used
	 */
	public void init() {
		setSource();
		if (config.getKnowledgeSource() != null && config.isSetURL()) {
			try {
				config.getKnowledgeSource().init();
//				config.setInitKnowledgeSource(true);
				System.out.println("init KnowledgeSource");
				startGUI.updateTabColors();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * updateAll
	 */
	public void updateAll() {
		updateComboBox();
		updateOptionPanel();
		updateInitButtonColor();
	}

	/**
	 * set ComboBox to selected class
	 */
	public void updateComboBox() {
		if (config.getKnowledgeSource() != null)
			for (int i = 0; i < selectableSources.size(); i++)
				if (config.getKnowledgeSource().getClass().equals(
						config.getComponentManager().getKnowledgeSources().get(i))) {
					cb.setSelectedIndex(i);
				}
		this.choosenClassIndex = cb.getSelectedIndex();
	}

	/**
	 * update OptionPanel with new selection
	 */
	public void updateOptionPanel() {
		optionPanel.update(config.getKnowledgeSource());
	}

	/**
	 * make init-button red if you have to click
	 */
	public void updateInitButtonColor() {
		if (!config.needsInitKnowledgeSource()) {
			initButton.setForeground(Color.RED);
		} else
			initButton.setForeground(Color.BLACK);
	}

}
