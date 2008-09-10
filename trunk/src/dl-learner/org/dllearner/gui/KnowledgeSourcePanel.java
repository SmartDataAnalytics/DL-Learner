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

import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;


/**
 * Knowledge source panel, tab 0. Choose source, change mandatory options and finally initialise
 * knowledge source.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 */
public class KnowledgeSourcePanel extends ComponentPanel<KnowledgeSource> {

	private static final long serialVersionUID = -7678275020058043937L;

	private Config config;
//	private StartGUI startGUI;
	private JButton clearButton;
	private String[] kbBoxItems = {};
	private JComboBox cb = new JComboBox(kbBoxItems);
	private JPanel choosePanel = new JPanel();
	private int choosenClassIndex;
	private List<Class<? extends KnowledgeSource>> selectableSources;
	private OptionPanel optionPanel;

	KnowledgeSourcePanel(final Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
//		this.startGUI = startGUI;
		selectableSources = config.getComponentManager().getKnowledgeSources();
		// to set a default source, we move it to the beginning of the list
		selectableSources.remove(OWLFile.class);
		selectableSources.add(0, OWLFile.class);
		// OWL API ontology is only useful programmatically (not in the GUI itself)
		selectableSources.remove(OWLAPIOntology.class);

		clearButton = new JButton("Reset to Default Values");
		clearButton.addActionListener(this);

		// add to comboBox
		for (int i = 0; i < selectableSources.size(); i++) {
			cb.addItem(config.getComponentManager().getComponentName(selectableSources.get(i)));
		}
		cb.addActionListener(this);

		choosePanel.add(cb);
//		choosePanel.add(new JLabel("       "));
		choosePanel.add(clearButton);
		choosenClassIndex = cb.getSelectedIndex();

		// whenever a component is selected, we immediately create an instance (non-initialised)
		KnowledgeSource ks = config.newKnowledgeSource(selectableSources.get(cb.getSelectedIndex()));
		optionPanel = new OptionPanel(config, ks);

		add(choosePanel, BorderLayout.NORTH);
		add(optionPanel, BorderLayout.CENTER);

		choosenClassIndex = cb.getSelectedIndex();
		
//		setSource();
//		updateAll();
	}

	public void actionPerformed(ActionEvent e) {
		if (choosenClassIndex != cb.getSelectedIndex()) {
			choosenClassIndex = cb.getSelectedIndex();
			// create a new knowledge source component
			config.changeKnowledgeSource(selectableSources.get(choosenClassIndex));
			updateOptionPanel();
		}

		if (e.getSource() == clearButton) {
//			config.setKnowledgeSource(config.getComponentManager().knowledgeSource(
//					selectableSources.get(choosenClassIndex)));
			updateOptionPanel();
		}

//		if (e.getSource() == initButton) {
//			init();
//		}

//		if (e.getSource() == clearButton) {
//			config.reInit();
//		}
	}

	/**
	 * after this, you can change widgets
	 */
//	public void setSource() {
//		
//	}

	/**
	 * update OptionPanel with new selection
	 */
	public void updateOptionPanel() {
		optionPanel.update(config.getKnowledgeSource());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.gui.ComponentPanel#panelActivated()
	 */
	@Override
	public void panelActivated() {
		// TODO Auto-generated method stub
		
	}

}
