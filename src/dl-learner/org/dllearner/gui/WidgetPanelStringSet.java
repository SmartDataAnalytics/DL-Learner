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

/**
 * WidgetPanelStringSet
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelStringSet extends AbstractWidgetPanel implements
	ActionListener {

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
    private Class<? extends Component> componentOption;

    private Set<String> value = new HashSet<String>();
    private JList stringList = new JList();
    private DefaultListModel listModel = new DefaultListModel();

    public WidgetPanelStringSet(Config config, Component component,
	    Class<? extends Component> componentOption,
	    ConfigOption<?> configOption) {

	this.config = config;
	this.configOption = configOption;
	this.component = component;
	this.componentOption = componentOption;

	widgetPanel.setLayout(gridbag);
	add(widgetPanel, BorderLayout.CENTER);
	showLabel(); // name of option and tooltip
	showThingToChange(); // textfield, setbutton

	stringList.setModel(listModel);

	addButton.addActionListener(this);
	removeButton.addActionListener(this);
	clearButton.addActionListener(this);
    }

    public JPanel getPanel() {
	return this;
    }

    public void actionPerformed(ActionEvent e) {
	Set<String> exampleSet = new HashSet<String>();
	// add to list
	if (e.getSource() == addButton
		&& !listModel.contains(stringField.getText())) {
	    listModel.addElement(stringField.getText());
	}
	// remove selection
	if (e.getSource() == removeButton) {
	    int[] selectedIndices = stringList.getSelectedIndices();
	    int count = 0; // remove i.e. index 2 and 4: after delete 2, 4 is
	    // now index 3
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

    @Override
    protected void showLabel() {
	nameLabel = new JLabel(configOption.getName());
	nameLabel.setToolTipText(configOption.getDescription());
	buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
	constraints.anchor = GridBagConstraints.WEST;
	gridbag.setConstraints(nameLabel, constraints);
	widgetPanel.add(nameLabel, constraints);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void showThingToChange() {
	if (component != null) {
	    // StringSetConfigOption
	    if (configOption.getClass().toString().contains(
		    "StringSetConfigOption")) {
		// previous set value
		if (configOption != null) {
		    // take set
		    value = (Set<String>) config.getComponentManager()
			    .getConfigOptionValue(component,
				    configOption.getName());
		    // fill list
		    if (value != null) {
			for (Iterator<String> iterator = value.iterator(); iterator
				.hasNext();) {
			    String item = iterator.next();
			    listModel.addElement(item);
			}
		    }
		}
		buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
		gridbag.setConstraints(stringField, constraints);
		widgetPanel.add(stringField, constraints);

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

		buildConstraints(constraints, 1, 2, 1, 1, 100, 100);
		gridbag.setConstraints(removeButton, constraints);
		widgetPanel.add(removeButton, constraints);

		buildConstraints(constraints, 1, 3, 1, 1, 100, 100);
		gridbag.setConstraints(clearButton, constraints);
		widgetPanel.add(clearButton, constraints);

		// widgetPanel.add(setButton);
		// setButton.addActionListener(this);
	    }
	    // UNKNOWN
	    else {
		JLabel notImplementedLabel = new JLabel("not a set of strings");
		notImplementedLabel.setForeground(Color.RED);
		buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
		gridbag.setConstraints(notImplementedLabel, constraints);
		widgetPanel.add(notImplementedLabel);
	    }
	} else { // configOption == NULL
	    JLabel noConfigOptionLabel = new JLabel("no init (StringSet)");
	    noConfigOptionLabel.setForeground(Color.MAGENTA);
	    widgetPanel.add(noConfigOptionLabel);
	}
    }

    @Override
    protected void setEntry() {
	StringSetConfigOption specialOption;
	// value = stringField.getText(); // get from input
	specialOption = (StringSetConfigOption) config.getComponentManager()
		.getConfigOption(componentOption, configOption.getName());

	try {
	    ConfigEntry<Set<String>> specialEntry = new ConfigEntry<Set<String>>(
		    specialOption, value);
	    config.getComponentManager().applyConfigEntry(component,
		    specialEntry);
	    System.out.println("set StringSet: " + configOption.getName()
		    + " = " + value);
	} catch (InvalidConfigOptionValueException s) {
	    s.printStackTrace();
	}

    }

    /**
     * Define GridBagConstraints
     */
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy,
	    int gw, int gh, int wx, int wy) {
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}
