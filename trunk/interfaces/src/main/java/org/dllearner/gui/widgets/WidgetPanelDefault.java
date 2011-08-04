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

import java.awt.Color;

import javax.swing.JLabel;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.gui.Config;

/**
 * Panel for not defined options.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelDefault extends AbstractWidgetPanel<Object> {

	private static final long serialVersionUID = 4059515858894036769L;

	/**
	 * Provides a default widget panel (should never be shown).
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */
	public WidgetPanelDefault(Config config, AbstractComponent component, ConfigOption<Object> configOption) {
		super(config, component, configOption);
	}

	@Override
	public void buildWidgetPanel() {
		JLabel notImplementedLabel = new JLabel(configOption.getClass().getSimpleName()
				+ " not implemented");
		notImplementedLabel.setForeground(Color.RED);
		add(notImplementedLabel);		
	}

}
