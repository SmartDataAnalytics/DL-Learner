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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.StringTupleListConfigOption;
import org.dllearner.gui.Config;
import org.dllearner.utilities.datastructures.StringTuple;

/**
 * Panel for option StringTupleList, defined in
 * org.dllearner.core.config.StringTupleListConfigOption.
 * 
 * @author Tilo Hielscher
 */
public class WidgetPanelStringTupleList extends AbstractWidgetPanel<List<StringTuple>> implements
		ActionListener {

	private static final long serialVersionUID = 7832726987046601916L;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();

	private JPanel widgetPanel; // = new JPanel();
	private JButton addButton; // = new JButton("add");
	private JButton removeButton; // = new JButton("remove");
	private JButton clearButton; // = new JButton("clear");
	private JTextField stringFieldA; // = new JTextField(10);
	private JTextField stringFieldB; // = new JTextField(10);
	private List<StringTuple> exampleList; // = new LinkedList<StringTuple>();

	private List<StringTuple> value; // = new LinkedList<StringTuple>();
	private JList stringList; // = new JList();
	private DefaultListModel listModel; // = new DefaultListModel();

	private JButton setButton; // = new JButton("set");

	/**
	 * Provides a widget for string tuple list options.
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */
	public WidgetPanelStringTupleList(Config config, AbstractComponent component,
			StringTupleListConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// add to list
		if (e.getSource() == addButton
				&& !listModel.contains(stringFieldA.getText() + " --> " + stringFieldB.getText())
				&& !stringFieldA.getText().equalsIgnoreCase("")
				&& !stringFieldB.getText().equalsIgnoreCase("")) {
			listModel.addElement(stringFieldA.getText() + " --> " + stringFieldB.getText());
			exampleList.add(new StringTuple(stringFieldA.getText(), stringFieldB.getText()));
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
		// setEntry();
		fireValueChanged(value);
	}

	private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx,
			int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	@Override
	public void buildWidgetPanel() {

		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();

		widgetPanel = new JPanel();
		addButton = new JButton("add");
		removeButton = new JButton("remove");
		clearButton = new JButton("clear");
		stringFieldA = new JTextField(10);
		stringFieldB = new JTextField(10);
		exampleList = new LinkedList<StringTuple>();

		stringList = new JList();
		listModel = new DefaultListModel();

		setButton = new JButton("set");

		widgetPanel.setLayout(gridbag);
		add(widgetPanel, BorderLayout.CENTER);
		add(getLabel());

		value = config.getConfigOptionValue(component, configOption);

		if (value != null) {
			// setEntry();
			exampleList = value;
		}

		// fill list
		if (value != null) {
			for (Iterator<StringTuple> iterator = value.iterator(); iterator.hasNext();) {
				StringTuple item = iterator.next();
				listModel.addElement(item);
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

		stringList.setModel(listModel);
		// ActionListeners
		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		clearButton.addActionListener(this);
		setButton.addActionListener(this);
	}

}
