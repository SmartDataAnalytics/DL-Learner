package org.dllearner.gui;

/**
 * Copyright (C) 2007, Jens Lehmann
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
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigOption;


/**
 * WidgetPanelDefault
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelDefault extends JPanel {

	private static final long serialVersionUID = 4059515858894036769L;
	private JLabel nameLabel;
	private JPanel centerPanel = new JPanel();
		
	public WidgetPanelDefault(Config config, Component component, Class<? extends Component> componentOption, ConfigOption<?> configOption) {
		
		// default
		nameLabel = new JLabel(configOption.getName());

		// text
		nameLabel = new JLabel(configOption.getName());
		JLabel notImplementedLabel = new JLabel(configOption.getClass().getSimpleName() + " not implemented");
		notImplementedLabel.setForeground(Color.RED);
		centerPanel.add(nameLabel);
		centerPanel.add(notImplementedLabel);
		
		// default
		add(centerPanel, BorderLayout.CENTER);
	}
	
}
