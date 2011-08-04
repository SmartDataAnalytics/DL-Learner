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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.options.*;
import org.dllearner.gui.widgets.WidgetPanelBoolean;
import org.dllearner.gui.widgets.WidgetPanelDefault;
import org.dllearner.gui.widgets.WidgetPanelDouble;
import org.dllearner.gui.widgets.WidgetPanelInteger;
import org.dllearner.gui.widgets.WidgetPanelString;
import org.dllearner.gui.widgets.WidgetPanelStringSet;
import org.dllearner.gui.widgets.WidgetPanelStringTupleList;
import org.dllearner.gui.widgets.WidgetPanelURL;

/**
 * OptionPanel reads all possible options and use all widgets. Definition map is
 * here.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 * 
 */
public class OptionPanel extends JPanel {

	private static final long serialVersionUID = -3053205578443575240L;
	
	private Config config;
//	private Class<? extends Component> componentClass;
	private JPanel centerPanel = new JPanel();
	private AbstractComponent component;
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();

	/**
	 * Constructs a panel which displays all options of a component
	 * and their current value.
	 * @param config The central configuration handler (used to 
	 * retrieve option values).
	 * @param component The active component (i.e. the one 
	 */
	public OptionPanel(Config config, AbstractComponent component) {
		super(new BorderLayout());
		
		this.config = config;
		this.component = component;

		// define GridBagLayout
		centerPanel.setLayout(gridBagLayout);
		constraints.anchor = GridBagConstraints.NORTHWEST;

		// add scrollPane
		JScrollPane centerScroller = new JScrollPane(centerPanel);
		centerScroller.setPreferredSize(new Dimension(400, 200));
		// add Panels
		add(centerScroller, BorderLayout.CENTER);
		// show widgets
		showWidgets();
	}

	/** 
	 * Update this option panel by completely rebuilding its
	 * content.
	 * @param newComponent The new active component. 
	 */
	public void rebuild(AbstractComponent newComponent) {
		this.component = newComponent;
		showWidgets();
	}

	/**
	 * Define here what core.config.class is what type of widget.
	 * WidgetPanelDefault is for none defined classes.
	 */
	@SuppressWarnings("unchecked")
	private void showWidgets() {
		//	clear panel
		centerPanel.removeAll(); 
		
		JPanel widgetPanel;
		List<ConfigOption<?>> optionList = ComponentManager.getConfigOptions(component.getClass());		
				
		for (int i = 0; i < optionList.size(); i++) {
			buildConstraints(constraints, 0, i, 1, 1, 0, 0);
			ConfigOption option = optionList.get(i);
			if (option instanceof IntegerConfigOption) {
				widgetPanel = new WidgetPanelInteger(config, component, (IntegerConfigOption) option);
			} else if (option instanceof BooleanConfigOption) {
				widgetPanel = new WidgetPanelBoolean(config, component, (BooleanConfigOption) option);
			} else if (option instanceof DoubleConfigOption) {
				widgetPanel = new WidgetPanelDouble(config, component, (DoubleConfigOption) option);
			} else if (option instanceof StringConfigOption) {
				widgetPanel = new WidgetPanelString(config, component, (StringConfigOption) option);
			} else if (option instanceof URLConfigOption) {
				widgetPanel = new WidgetPanelURL(config, component, (URLConfigOption) option);
			} else if (option instanceof StringSetConfigOption) {
				widgetPanel = new WidgetPanelStringSet(config, component, (StringSetConfigOption) option);
			} else if (option instanceof StringTupleListConfigOption) {
				widgetPanel = new WidgetPanelStringTupleList(config, component, (StringTupleListConfigOption) option);
			} else {
				widgetPanel = new WidgetPanelDefault(config, component, option);
			}
			gridBagLayout.setConstraints(widgetPanel, constraints);
			centerPanel.add(widgetPanel);
		}
		centerPanel.updateUI(); // update graphic
	}

	// define grid bag constraints
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
