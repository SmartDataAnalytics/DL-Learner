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

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.config.*;

/**
 * OptionPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class OptionPanel extends JPanel {

    private static final long serialVersionUID = -3053205578443575240L;
    private Config config;
    private Class<? extends Component> componentOption;
    private List<ConfigOption<?>> optionList;
    private JPanel centerPanel = new JPanel();
    private Component component;
    private GridBagLayout gridBagLayout = new GridBagLayout();
    private GridBagConstraints constraints = new GridBagConstraints();

    public OptionPanel(Config config, Component component,
	    Class<? extends Component> componentOption) {
	super(new BorderLayout());

	this.config = config;
	this.component = component;
	this.componentOption = componentOption;

	optionList = ComponentManager.getConfigOptions(componentOption);

	// define GridBagLayout
	centerPanel.setLayout(gridBagLayout);
	constraints.anchor = GridBagConstraints.NORTHWEST;

	// add scrollPane
	JScrollPane centerScroller = new JScrollPane(centerPanel);
	centerScroller.setPreferredSize(new Dimension(400, 200));

	// add Panels
	add(centerScroller, BorderLayout.CENTER);

	showWidgets();

    }

    public void update(Component component,
	    Class<? extends Component> componentOption) {
	this.componentOption = componentOption;
	this.component = component;
	showWidgets();
    }

    /*
     * Define here what core.config.class is what type of widget.
     * WidgetPanelDefault is for none defined classes.
     */
    private void showWidgets() {
	JPanel widgetPanel;
	optionList = ComponentManager.getConfigOptions(componentOption);
	centerPanel.removeAll(); // clear panel
	for (int i = 0; i < optionList.size(); i++) {
	    buildConstraints(constraints, 0, i, 1, 1, 0, 0);
	    if (optionList.get(i).getClass().toString().contains(
		    "IntegerConfigOption")) {
		widgetPanel = new WidgetPanelInteger(config, component,
			componentOption, optionList.get(i));
	    } else if (optionList.get(i).getClass().toString().contains(
		    "BooleanConfigOption")) {
		widgetPanel = new WidgetPanelBoolean(config, component,
			componentOption, optionList.get(i));
	    } else if (optionList.get(i).getClass().toString().contains(
		    "DoubleConfigOption")) {
		widgetPanel = new WidgetPanelDouble(config, component,
			componentOption, optionList.get(i));
	    } else if (optionList.get(i).getClass().toString().contains(
		    "StringConfigOption")) {
		widgetPanel = new WidgetPanelString(config, component,
			componentOption, optionList.get(i));
	    } else {
		widgetPanel = new WidgetPanelDefault(config, component,
			componentOption, optionList.get(i));
	    }
	    gridBagLayout.setConstraints(widgetPanel, constraints);
	    centerPanel.add(widgetPanel);
	}
	centerPanel.updateUI(); // update graphic
    }

    /*
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
