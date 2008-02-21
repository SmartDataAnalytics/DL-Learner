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

import java.util.List;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;

// import org.dllearner.kb.*;

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
	private String[] kbBoxItems = {};
	private JComboBox cb = new JComboBox(kbBoxItems);
	private JPanel choosePanel = new JPanel();
	private JPanel initPanel = new JPanel();
	private int choosenClassIndex;
	private List<Class<? extends KnowledgeSource>> sources;
	private OptionPanel optionPanel;

	KnowledgeSourcePanel(final Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
		this.startGUI = startGUI;
		sources = config.getComponentManager().getKnowledgeSources();

		setButton = new JButton("Set");
		setButton.addActionListener(this);
		initButton = new JButton("Init KnowledgeSource");
		initButton.addActionListener(this);
		initButton.setEnabled(true);

		// add to comboBox
		for (int i = 0; i < sources.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(sources.get(i)));
		}
		cb.addActionListener(this);

		choosePanel.add(cb);
		choosePanel.add(setButton);
		choosenClassIndex = cb.getSelectedIndex();

		optionPanel = new OptionPanel(config, config.getKnowledgeSource(), config
				.getOldKnowledgeSource(), sources.get(choosenClassIndex));
		initPanel.add(initButton);

		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);

		setSource();
		updateAll();
	}

	public void actionPerformed(ActionEvent e) {
		// read selected KnowledgeSourceClass
		// choosenClassIndex = cb.getSelectedIndex();
		if (choosenClassIndex != cb.getSelectedIndex()) {
			choosenClassIndex = cb.getSelectedIndex();
			config.setInitKnowledgeSource(false);
			setSource();
		}

		if (e.getSource() == setButton) {
			setSource();
		}

		if (e.getSource() == initButton)
			init();
	}

	/**
	 * after this, you can change widgets
	 */
	public void setSource() {
		config.setKnowledgeSource(config.getComponentManager().knowledgeSource(
				sources.get(choosenClassIndex)));
		config.setInitKnowledgeSource(false);
		updateAll();
	}

	/**
	 * after this, next tab can be used
	 */
	public void init() {
		if (config.getKnowledgeSource() != null && config.isSetURL()) {
			try {
				config.getKnowledgeSource().init();
				config.setInitKnowledgeSource(true);
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
			for (int i = 0; i < sources.size(); i++)
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
		optionPanel.update(config.getKnowledgeSource(), config.getOldKnowledgeSource(), sources
				.get(choosenClassIndex));
	}

	/**
	 * make init-button red if you have to click
	 */
	public void updateInitButtonColor() {
		if (!config.isInitKnowledgeSource()) {
			initButton.setForeground(Color.RED);
		} else
			initButton.setForeground(Color.BLACK);
	}

}
