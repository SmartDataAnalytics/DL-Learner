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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;

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
public class WidgetPanelStringSet extends WidgetPanelAbstract implements ActionListener {

	private static final long serialVersionUID = 7832726987046601916L;
	private Config config;
	private ConfigOption<?> configOption;
	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();

	private JLabel nameLabel;
	private JPanel widgetPanel = new JPanel();
	private JButton addButton = new JButton("add");
	private JButton removeButton = new JButton("remove");
	private JButton clearButton = new JButton("clear");
	private JTextField stringField = new JTextField(25);

	private Component component;
	private Component oldComponent;
	private Class<? extends Component> componentOption;

	private Set<String> value = new HashSet<String>();
	private JList stringList = new JList();
	private DefaultListModel listModel = new DefaultListModel();

	private JButton setButton = new JButton("set");
	private CheckBoxList cBL = new CheckBoxList();

	public WidgetPanelStringSet(Config config, Component component, Component oldComponent,
			Class<? extends Component> componentOption, ConfigOption<?> configOption) {

		this.config = config;
		this.configOption = configOption;
		this.component = component;
		this.oldComponent = oldComponent;
		this.componentOption = componentOption;

		widgetPanel.setLayout(gridbag);
		add(widgetPanel, BorderLayout.CENTER);
		showLabel(); // name of option and tooltip
		showThingToChange(); // textfield, setbutton
		stringList.setModel(listModel);
		// ActionListeners
		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		clearButton.addActionListener(this);
		setButton.addActionListener(this);
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
		} else {
			// SPECIAL LAYOUT
			// setButton
			if (e.getSource() == setButton) {
				value = cBL.getSelections();
				setEntry();
			}
		}

	}

	@Override
	public void showLabel() {
		nameLabel = new JLabel(configOption.getName());
		nameLabel.setToolTipText(configOption.getDescription());
		buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(nameLabel, constraints);
		widgetPanel.add(nameLabel, constraints);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void showThingToChange() {
		if (component != null) {
			// StringSetConfigOption
			if (configOption.getClass().toString().contains("StringSetConfigOption")) {
				// previous set value
				if (configOption != null) {
					// take set
					value = (Set<String>) config.getComponentManager().getConfigOptionValue(
							component, configOption.getName());
					// previous set value from old
					if (component != null && oldComponent != null) {
						if (oldComponent.getClass().equals(component.getClass())) {
							value = (Set<String>) config.getComponentManager()
									.getConfigOptionValue(oldComponent, configOption.getName());
							if (value != null) {
								setEntry();
							}
						}
					}
					// fill list
					if (value != null) {
						for (Iterator<String> iterator = value.iterator(); iterator.hasNext();) {
							String item = iterator.next();
							listModel.addElement(item);
						}
					}
				}

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
					stringListScroller.setPreferredSize(new Dimension(280, 100));
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
					// SPECIAL LAYOUT
					// ComboBoxList
					buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
					gridbag.setConstraints(cBL, constraints);
					widgetPanel.add(cBL, constraints);
					// setButton
					buildConstraints(constraints, 1, 1, 1, 1, 100, 100);
					gridbag.setConstraints(setButton, constraints);
					widgetPanel.add(setButton, constraints);
					// DEFINE LIST
					// positiveExamples or negativeExamples
					if (configOption.getName().equalsIgnoreCase("positiveExamples")
							|| configOption.getName().equalsIgnoreCase("negativeExamples")) {
						// fill lists
						Set<Individual> individualsSet = config.getReasoningService()
								.getIndividuals();
						LinkedList<Individual> individuals = new LinkedList<Individual>(
								individualsSet);
						for (Individual ind : individuals)
							cBL.add(ind.getName());
					}
					// allowedConcepts or ignoredConcepts
					if (configOption.getName().equalsIgnoreCase("allowedConcepts")
							|| configOption.getName().equalsIgnoreCase("ignoredConcepts")) {
						// fill lists
						Set<NamedClass> atomicsSet = config.getReasoningService()
								.getAtomicConcepts();
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
								.getAtomicRoles();
						LinkedList<ObjectProperty> atomicRoles = new LinkedList<ObjectProperty>(
								atomicsSet);
						for (ObjectProperty ind : atomicRoles)
							cBL.add(ind.getName());
					}
					// set selections
					if (value != null)
						cBL.setSelections(value);
				}
			}
			// UNKNOWN
			else {
				JLabel notImplementedLabel = new JLabel(" not a set of strings");
				notImplementedLabel.setForeground(Color.RED);
				buildConstraints(constraints, 1, 0, 1, 1, 100, 100);
				gridbag.setConstraints(notImplementedLabel, constraints);
				widgetPanel.add(notImplementedLabel);
			}
		} else { // configOption == NULL
			JLabel noConfigOptionLabel = new JLabel(" no init (StringSet)");
			noConfigOptionLabel.setForeground(Color.MAGENTA);
			buildConstraints(constraints, 1, 0, 1, 1, 100, 100);
			gridbag.setConstraints(noConfigOptionLabel, constraints);
			widgetPanel.add(noConfigOptionLabel, constraints);
		}
	}

	@Override
	public void setEntry() {
		StringSetConfigOption specialOption;
		specialOption = (StringSetConfigOption) config.getComponentManager().getConfigOption(
				componentOption, configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Set<String>> specialEntry = new ConfigEntry<Set<String>>(specialOption,
						value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				System.out.println("set StringSet: " + configOption.getName() + " = " + value);
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

}
