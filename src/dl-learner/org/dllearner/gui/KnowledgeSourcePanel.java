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

import java.io.File;
import java.util.List;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//import org.dllearner.kb.*;
import org.dllearner.core.KnowledgeSource;


/**
 * KnowledgeSourcePanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class KnowledgeSourcePanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -7678275020058043937L;
	
	private JFileChooser fc;
	private JButton openButton, initButton;
	private JTextField fileDisplay;
    private String[] kbBoxItems = {"Pleae select a type"};
	private JComboBox cb = new JComboBox(kbBoxItems);	
	private JPanel openPanel;
	private Config config;
	private int choosenClassIndex;
	private List<Class<? extends KnowledgeSource>> sources;
	
	KnowledgeSourcePanel(Config config) {
		super(new BorderLayout());
	
		this.config = config;
		sources = config.getComponentManager().getKnowledgeSources();
		
		fc = new JFileChooser(new File("examples/"));
		openButton = new JButton("Open local file otherwise type URL");
		openButton.addActionListener(this);
		
		initButton = new JButton("Init KnowledgeSource");
		initButton.addActionListener(this);
		
		fileDisplay = new JTextField(35);
		fileDisplay.setEditable(true);
		
		for (int i=0; i<sources.size(); i++) {
			String ksClass = sources.get(i).toString().substring(23).concat(".class");
			cb.addItem(ksClass); 
		}
		
		cb.addActionListener(this);
		
		openPanel = new JPanel();
		
		JPanel choosePanel = new JPanel();
		choosePanel.add(cb);

		JPanel initPanel = new JPanel();
		initPanel.add(initButton);
		
		add(choosePanel, BorderLayout.PAGE_START);
		add(openPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);
	}
	
	public void actionPerformed(ActionEvent e) {
		// open File
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(KnowledgeSourcePanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String URI = "file://";
				URI = URI.concat(fc.getSelectedFile().toString()); // make "file://" before local URI's
				config.setURI(URI); //save variable
				fileDisplay.setText(URI);
			}
			return;
		}

		
		// something changes in cb
		if (cb.isEnabled()) {
	        System.out.println("Item: " + cb.getSelectedItem());
	        System.out.println("Item: " + cb.getSelectedIndex());
	        
	        choosenClassIndex = cb.getSelectedIndex() -1;
	        
	        makeCenterClean();
			openPanel.add(fileDisplay);
			openPanel.add(openButton);
	        
		}

		// init
		if (e.getSource() == initButton) {
			String testURI = config.getURI(); 
			config.setKnowledgeSource(config.getComponentManager().knowledgeSource(sources.get(choosenClassIndex)));
			config.getComponentManager().applyConfigEntry(config.getKnowledgeSource(), "url", testURI);				
			config.getKnowledgeSource().init();
			System.out.println("init KnowledgeSource");
		}
	}
	
	private void makeCenterClean() {
        openPanel.remove(fileDisplay);
        openPanel.remove(openButton);
        openPanel.repaint();
        StartGUI.myrun.renew();
    }
}
