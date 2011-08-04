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

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.gui.Config;

/**
 * Panel for option Boolean, defined in
 * {@link org.dllearner.core.options.BooleanConfigOption}.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 */
public class WidgetPanelBoolean extends AbstractWidgetPanel<Boolean> implements ActionListener {

	private static final long serialVersionUID = -4800583253223939928L;

	private Boolean value;
	private JComboBox cb;

	/**
	 * Provides a widget for boolean options.
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */	
	public WidgetPanelBoolean(Config config, AbstractComponent component, BooleanConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {			
		value = (cb.getSelectedIndex()==0);
		fireValueChanged(value);
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());
		
		value = config.getConfigOptionValue(component, configOption);
		
		if (value == null) {
			value = false;
		}
		
		// set cb-index
		String[] kbBoxItems = { "true", "false" };
		cb = new JComboBox(kbBoxItems);
		if (value) {
			cb.setSelectedIndex(0);
		} else {
			cb.setSelectedIndex(1);
		}
		
		cb.addActionListener(this);
		add(cb);
		
	}

}
