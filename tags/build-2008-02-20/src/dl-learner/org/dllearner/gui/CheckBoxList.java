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

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * CheckBoxList constitute a list of CheckBox's
 * 
 * @author Tilo Hielscher
 */
public class CheckBoxList extends JPanel {
	private static final long serialVersionUID = -7119007550662195455L;
	private JPanel checkBoxPanel = new JPanel();
	private LinkedList<JCheckBox> list = new LinkedList<JCheckBox>();
	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();

	/**
	 * Make a JPanel with GridBagLayout.
	 */
	public CheckBoxList() {
		checkBoxPanel.setLayout(gridbag);
		add(checkBoxPanel, BorderLayout.CENTER);
		constraints.anchor = GridBagConstraints.WEST;
	}

	/**
	 * Add new entry and make a new JCheckBox.
	 * 
	 * @param label
	 *            This text will be shown. It is the name of new JCheckBox and
	 *            will add to list.
	 */
	public void add(String label) {
		list.add(new JCheckBox(label));
		update();
	}

	/**
	 * Return a set of selected items.
	 */
	public Set<String> getSelections() {
		Set<String> selectionSet = new HashSet<String>();
		selectionSet.clear(); // remove all
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isSelected())
				selectionSet.add(list.get(i).getText());
		}
		return selectionSet;
	}

	/**
	 * Select items.
	 * 
	 * @param selectionSet
	 *            Is a Set of Strings.
	 */
	public void setSelections(Set<String> selectionSet) {
		for (int i = 0; i < this.list.size(); i++) {
			if (selectionSet.contains(list.get(i).getText()))
				list.get(i).setSelected(true);
			else
				this.list.get(i).setSelected(false);
		}
	}

	/**
	 * update JCheckBox's
	 */
	private void update() {
		checkBoxPanel.removeAll();
		for (int i = 0; i < list.size(); i++) {
			buildConstraints(constraints, 0, i, 1, 1, 100, 100);
			gridbag.setConstraints(list.get(i), constraints);
			checkBoxPanel.add(list.get(i), constraints);
		}
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
}