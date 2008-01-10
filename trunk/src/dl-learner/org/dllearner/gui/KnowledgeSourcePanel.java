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
//import java.util.List;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dllearner.kb.OWLFile;
// import org.dllearner.kb.KBFile;

//import org.dllearner.core.KnowledgeSource;


/**
 * KnowledgeSourcePanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class KnowledgeSourcePanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -7678275020058043937L;
	
	private JFileChooser fc;
	private JButton openButton;
	private JTextField fileDisplay;
    private String[] kbBoxItems = {"Pleae select a type", "KBFile", "OWLFile", "SparqleEndpoint"};
	private JComboBox cb = new JComboBox(kbBoxItems);	
	private JPanel openPanel = new JPanel();
	private Config config;
	
	KnowledgeSourcePanel(Config config) {
		super(new BorderLayout());
	
		this.config = config;
		
		fc = new JFileChooser(new File("examples/"));
		openButton = new JButton("Open File");
		openButton.addActionListener(this);
		fileDisplay = new JTextField(35);
		fileDisplay.setEditable(false);
		
		// test output - if you activat next 2 lines, it will show alle available kb.classes
		   // activate import too!
		//List<Class<? extends KnowledgeSource>> sources = StartGUI.myconfig.getComponentManager().getKnowledgeSources();
		//for (int i=0; i<sources.size(); i++) cb.addItem(sources.get(i)); 
		cb.addActionListener(this);
		
		JPanel choosePanel = new JPanel();
		choosePanel.add(cb);

		add(choosePanel, BorderLayout.PAGE_START);
		add(openPanel, BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e) {
		// open File
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(KnowledgeSourcePanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				config.setFile(fc.getSelectedFile()); //save variable
				fileDisplay.setText(config.getFile().toString());
				//System.out.println("Init KnowledgeSource after loading file ... show over output");
				//System.out.println("test: " + StartGUI.myconfig.getFile().toURI().toString());
				config.getComponentManager().applyConfigEntry(config.getKnowledgeSource(), "url", config.getFile().toURI().toString());				
				config.getKnowledgeSource().init();
			}
			return;
		}
		
		// choose none
		if (cb.getSelectedItem().toString() == kbBoxItems[0]) { 
	        //System.out.println("Item: " + cb.getSelectedItem());
	        makeCenterClean();
		}
		// choose KB class
		if (cb.getSelectedItem().toString() == kbBoxItems[1]) { 
	        //System.out.println("Item: " + cb.getSelectedItem());
			makeCenterClean();
		}
		// choose OWL class
		if (cb.getSelectedItem().toString() == kbBoxItems[2]) { 
	        //System.out.println("Item: " + cb.getSelectedItem());
			makeCenterClean();
			openPanel.add(fileDisplay);
			openPanel.add(openButton);
			openPanel.repaint();
			config.setKnowledgeSource(config.getComponentManager().knowledgeSource(OWLFile.class));
		}
		// choose SPARCLE class
		if (cb.getSelectedItem().toString() == kbBoxItems[3]) { 
	        //System.out.println("Item: " + cb.getSelectedItem());
	        makeCenterClean();
		}
	}
	
	private void makeCenterClean() {
        openPanel.remove(fileDisplay);
        openPanel.remove(openButton);
        openPanel.repaint();
        StartGUI.myrun.renew();
    }
}
