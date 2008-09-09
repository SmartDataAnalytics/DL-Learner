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
package org.dllearner.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.gui.CheckBoxList;
import org.dllearner.gui.Config;

/**
 * Panel for option StringSet, defined in
 * org.dllearner.core.config.StringSetConfigOption.
 * 
 * There are 2 layouts defined. First for normal option and a second for special
 * options. Second layout shows a list of JCheckBox's.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelStringSet extends AbstractWidgetPanel<Set<String>> implements ActionListener {

	private static final long serialVersionUID = 7832726987046601916L;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();

	private JPanel widgetPanel = new JPanel();
	private JButton addButton = new JButton("add");
	private JButton removeButton = new JButton("remove");
	private JButton clearButton = new JButton("clear");
	private JTextField stringField = new JTextField(30);

	private Set<String> value = new HashSet<String>();
	private JList stringList = new JList();
	private DefaultListModel listModel = new DefaultListModel();

	private CheckBoxList cBL = new CheckBoxList(this);

	public WidgetPanelStringSet(Config config, Component component, StringSetConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (!isSpecial()) {
			// NORMAL LAYOUT
			Set<String> exampleSet = new HashSet<String>();
			// add to list
			if (e.getSource() == addButton && !listModel.contains(stringField.getText())) {
				listModel.addElement(stringField.getText());
			}
			// remove selection
			if (e.getSource() == removeButton) {
				int[] selectedIndices = stringList.getSelectedIndices();
				int count = 0;
				// remove i.e. 2 and 4: after delete 2: 4 is now 3
				for (int i : selectedIndices)
					listModel.remove(i - count++);
			}
			// clear list
			if (e.getSource() == clearButton) {
				listModel.clear();
			}
			// update
			// stringList.setModel(listModel);
			for (int i = 0; i < listModel.size(); i++) {
				if (!listModel.get(i).toString().equalsIgnoreCase(""))
					exampleSet.add(listModel.get(i).toString());
			}
			// set entry
			value = exampleSet;
			setEntry();
		}
	}

	/**
	 * Use this, to set entry for layout 2.
	 */
	public void specialSet() {
		if (isSpecial()) {
			this.value = cBL.getSelections();
			setEntry();
		}
	}

	public void setEntry() {
		StringSetConfigOption specialOption;
		specialOption = (StringSetConfigOption) config.getComponentManager().getConfigOption(
				component.getClass(), configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Set<String>> specialEntry = new ConfigEntry<Set<String>>(specialOption,
						value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set StringSet: " + configOption.getName()
				// + " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("StringSet: not valid value");

	}

	/**
	 * Define GridBagConstraints
	 */
	private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx,
			int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	/**
	 * special layout returns true if 2nd layout should used
	 */
	private boolean isSpecial() {
		if (configOption.getName().equalsIgnoreCase("positiveExamples")
				|| configOption.getName().equalsIgnoreCase("negativeExamples")
				|| configOption.getName().equalsIgnoreCase("allowedConcepts")
				|| configOption.getName().equalsIgnoreCase("ignoredConcepts")
				|| configOption.getName().equalsIgnoreCase("allowedRoles")
				|| configOption.getName().equalsIgnoreCase("ignoredRoles"))
			return true;
		else
			return false;
	}

	@Override
	public void buildWidgetPanel() {
		gridbag = new GridBagLayout();		
		widgetPanel = new JPanel();
		widgetPanel.setLayout(gridbag);
		add(widgetPanel, BorderLayout.CENTER);
		add(getLabel());
		
		value = config.getConfigOptionValue(component, configOption);

		listModel = new DefaultListModel();		
		// fill list
		if (value != null) {
			setEntry();
			for (Iterator<String> iterator = value.iterator(); iterator.hasNext();) {
				String item = iterator.next();
				listModel.addElement(item);
			}
		}

		constraints = new GridBagConstraints();
		cBL = new CheckBoxList(this);
		stringList = new JList();
		stringField = new JTextField(30);
		addButton = new JButton("add");
		removeButton = new JButton("remove");
		clearButton = new JButton("clear");
		
		if (!isSpecial()) {
			// NORMAL LAYOUT
			// stringField
			buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
			gridbag.setConstraints(stringField, constraints);
			widgetPanel.add(stringField, constraints);
			// addButton
			buildConstraints(constraints, 1, 1, 1, 1, 100, 100);
			gridbag.setConstraints(addButton, constraints);
			widgetPanel.add(addButton, constraints);
			// list
			stringList.setModel(listModel);
			stringList.setLayoutOrientation(JList.VERTICAL);
			stringList.setVisibleRowCount(-1);
			JScrollPane stringListScroller = new JScrollPane(stringList);
			stringListScroller.setPreferredSize(new Dimension(380, 100));
			buildConstraints(constraints, 0, 2, 1, 2, 100, 100);
			gridbag.setConstraints(stringListScroller, constraints);
			widgetPanel.add(stringListScroller, constraints);
			// removeButton
			buildConstraints(constraints, 1, 2, 1, 1, 100, 100);
			gridbag.setConstraints(removeButton, constraints);
			widgetPanel.add(removeButton, constraints);
			// clearButton
			buildConstraints(constraints, 1, 3, 1, 1, 100, 100);
			gridbag.setConstraints(clearButton, constraints);
			widgetPanel.add(clearButton, constraints);
		} else {
			System.out.println("SPECIAL OPTION " + configOption.getName());
			
			// SPECIAL LAYOUT
			// ComboBoxList
			buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
			gridbag.setConstraints(cBL, constraints);
			widgetPanel.add(cBL, constraints);
			// DEFINE LIST
			// positiveExamples or negativeExamples
			if (configOption.getName().equalsIgnoreCase("positiveExamples")
					|| configOption.getName().equalsIgnoreCase("negativeExamples")) {
				// fill lists
				Set<Individual> individualsSet = config.getReasoningService()
						.getIndividuals();
				LinkedList<Individual> individuals = new LinkedList<Individual>(
						individualsSet);
				for (Individual ind : individuals) {
					System.out.println(ind.getName());
					cBL.add(ind.getName());
				}
			}
			// allowedConcepts or ignoredConcepts
			if (configOption.getName().equalsIgnoreCase("allowedConcepts")
					|| configOption.getName().equalsIgnoreCase("ignoredConcepts")) {
				// fill lists
				Set<NamedClass> atomicsSet = config.getReasoningService()
						.getNamedClasses();
				LinkedList<NamedClass> atomicConcepts = new LinkedList<NamedClass>(
						atomicsSet);
				for (NamedClass ind : atomicConcepts)
					cBL.add(ind.getName());
			}
			// allowedRoles or ignoredRoles
			if (configOption.getName().equalsIgnoreCase("allowedRoles")
					|| configOption.getName().equalsIgnoreCase("ignoredRoles")) {
				// fill lists
				Set<ObjectProperty> atomicsSet = config.getReasoningService()
						.getObjectProperties();
				LinkedList<ObjectProperty> atomicRoles = new LinkedList<ObjectProperty>(
						atomicsSet);
				for (ObjectProperty ind : atomicRoles)
					cBL.add(ind.getName());
			}
			// set selections
			if (value != null)
				cBL.setSelections(value);
		}		
		
		stringList.setModel(listModel);
				
		// ActionListeners
		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		clearButton.addActionListener(this);		
		
	}

}
