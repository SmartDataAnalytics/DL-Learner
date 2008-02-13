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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

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
import org.dllearner.core.config.StringTupleListConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.utilities.StringTuple;

/**
 * Panel for option StringTupleList, defined in
 * org.dllearner.core.config.StringTupleListConfigOption.
 * 
 * @author Tilo Hielscher
 */
public class WidgetPanelStringTupleList extends WidgetPanelAbstract implements
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
    private JTextField stringFieldA = new JTextField(10);
    private JTextField stringFieldB = new JTextField(10);
    private List<StringTuple> exampleList = new LinkedList<StringTuple>();

    private Component component;
    private Class<? extends Component> componentOption;

    private List<StringTuple> value = new LinkedList<StringTuple>();
    private JList stringList = new JList();
    private DefaultListModel listModel = new DefaultListModel();

    private JButton setButton = new JButton("set");

    public WidgetPanelStringTupleList(Config config, Component component,
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
	// ActionListeners
	addButton.addActionListener(this);
	removeButton.addActionListener(this);
	clearButton.addActionListener(this);
	setButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	// add to list
	if (e.getSource() == addButton
		&& !listModel.contains(stringFieldA.getText() + " --> "
			+ stringFieldB.getText())
		&& !stringFieldA.getText().equalsIgnoreCase("")
		&& !stringFieldB.getText().equalsIgnoreCase("")) {
	    listModel.addElement(stringFieldA.getText() + " --> "
		    + stringFieldB.getText());
	    exampleList.add(new StringTuple(stringFieldA.getText(),
		    stringFieldB.getText()));
	}
	// remove selection
	if (e.getSource() == removeButton) {
	    int[] selectedIndices = stringList.getSelectedIndices();
	    int count = 0;
	    // remove i.e. 2 and 4: after delete 2: 4 is now 3
	    for (int i : selectedIndices) {
		listModel.remove(i - count);
		exampleList.remove(i - count++);
	    }
	}
	// clear list
	if (e.getSource() == clearButton) {
	    listModel.clear();
	    exampleList.clear();
	}
	// set entry
	value = exampleList;
	setEntry();
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
	    // StringTupleListConfigOption
	    if (configOption.getClass().toString().contains(
		    "StringTupleListConfigOption")) {
		// previous set value
		if (configOption != null) {
		    // take list
		    value = (List<StringTuple>) config.getComponentManager()
			    .getConfigOptionValue(component,
				    configOption.getName());

		    // fill list
		    if (value != null) {
			for (Iterator<StringTuple> iterator = value.iterator(); iterator
				.hasNext();) {
			    StringTuple item = iterator.next();
			    listModel.addElement(item);
			}
		    }
		}
		// stringFieldA
		buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
		gridbag.setConstraints(stringFieldA, constraints);
		widgetPanel.add(stringFieldA, constraints);
		// arrow
		JLabel arrowLabel = new JLabel(" --> ");
		buildConstraints(constraints, 1, 1, 1, 1, 100, 100);
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(arrowLabel, constraints);
		widgetPanel.add(arrowLabel, constraints);
		// stringFieldB
		buildConstraints(constraints, 2, 1, 1, 1, 100, 100);
		gridbag.setConstraints(stringFieldB, constraints);
		widgetPanel.add(stringFieldB, constraints);
		// addButton
		buildConstraints(constraints, 3, 1, 1, 1, 100, 100);
		gridbag.setConstraints(addButton, constraints);
		widgetPanel.add(addButton, constraints);
		// list
		stringList.setModel(listModel);
		stringList.setLayoutOrientation(JList.VERTICAL);
		stringList.setVisibleRowCount(-1);
		JScrollPane stringListScroller = new JScrollPane(stringList);
		stringListScroller.setPreferredSize(new Dimension(280, 100));
		buildConstraints(constraints, 0, 2, 3, 2, 100, 100);
		gridbag.setConstraints(stringListScroller, constraints);
		widgetPanel.add(stringListScroller, constraints);
		// removeButton
		buildConstraints(constraints, 3, 2, 1, 1, 100, 100);
		gridbag.setConstraints(removeButton, constraints);
		widgetPanel.add(removeButton, constraints);
		// clearButton
		buildConstraints(constraints, 3, 3, 1, 1, 100, 100);
		gridbag.setConstraints(clearButton, constraints);
		widgetPanel.add(clearButton, constraints);
	    }
	    // UNKNOWN
	    else {
		JLabel notImplementedLabel = new JLabel(
			" not a StringTupleList");
		notImplementedLabel.setForeground(Color.RED);
		buildConstraints(constraints, 1, 0, 1, 1, 100, 100);
		gridbag.setConstraints(notImplementedLabel, constraints);
		widgetPanel.add(notImplementedLabel);
	    }
	} else { // configOption == NULL
	    JLabel noConfigOptionLabel = new JLabel(
		    " no init (StringTupleList)");
	    noConfigOptionLabel.setForeground(Color.MAGENTA);
	    buildConstraints(constraints, 1, 0, 1, 1, 100, 100);
	    gridbag.setConstraints(noConfigOptionLabel, constraints);
	    widgetPanel.add(noConfigOptionLabel, constraints);
	}
    }

    @Override
    public void setEntry() {
	StringTupleListConfigOption specialOption;
	specialOption = (StringTupleListConfigOption) config
		.getComponentManager().getConfigOption(componentOption,
			configOption.getName());

	try {
	    ConfigEntry<List<StringTuple>> specialEntry = new ConfigEntry<List<StringTuple>>(
		    specialOption, value);
	    config.getComponentManager().applyConfigEntry(component,
		    specialEntry);
	    System.out.println("set StringTupleList: " + configOption.getName()
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
