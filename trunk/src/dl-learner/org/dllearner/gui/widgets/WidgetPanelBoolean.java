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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.dllearner.core.Component;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.gui.Config;

/**
 * Panel for option Boolean, defined in
 * org.dllearner.core.config.BooleanConfigOption.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 */
public class WidgetPanelBoolean extends AbstractWidgetPanel<Boolean> implements ActionListener {

	private static final long serialVersionUID = -4800583253223939928L;

	private Boolean value;
//	private String[] kbBoxItems; // = { "false", "true" };
	private JComboBox cb; // = new JComboBox(kbBoxItems);

	public WidgetPanelBoolean(Config config, Component component, BooleanConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (cb.getSelectedIndex() == 0)
			value = false;
		else
			value = true;
		
		BooleanConfigOption specialOption;
		specialOption = (BooleanConfigOption) config.getComponentManager().getConfigOption(
				component.getClass(), configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Boolean> specialEntry = new ConfigEntry<Boolean>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set Boolean: " + configOption.getName() +
				// " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("Boolean: not valid value");		
	}

	public void setEntry() {
		BooleanConfigOption specialOption;
		specialOption = (BooleanConfigOption) config.getComponentManager().getConfigOption(
				component.getClass(), configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Boolean> specialEntry = new ConfigEntry<Boolean>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set Boolean: " + configOption.getName() +
				// " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("Boolean: not valid value");
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());
		
		value = config.getConfigOptionValue(component, configOption);
		
		if (value == null)
			value = false;
		else
			setEntry();
		
		// set cb-index
		String[] kbBoxItems = { "false", "true" };
		cb = new JComboBox(kbBoxItems);
		if (!value)
			cb.setSelectedIndex(0);
		else
			cb.setSelectedIndex(1);		
		
		cb.addActionListener(this);
		add(cb);
		
	}

}
