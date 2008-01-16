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
import javax.swing.event.*;

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
    private String[] kbBoxItems = {};
    private JComboBox cb = new JComboBox(kbBoxItems);	
	private JPanel openPanel;
	private Config config;
	private int choosenClassIndex;
	private List<Class<? extends KnowledgeSource>> sources;
	
	KnowledgeSourcePanel(final Config config) {
		super(new BorderLayout());
	
		this.config = config;
		sources = config.getComponentManager().getKnowledgeSources();
		
		fc = new JFileChooser(new File("examples/"));
		openButton = new JButton("Open local file or type URL");
		openButton.addActionListener(this);
		
		initButton = new JButton("Init KnowledgeSource");
		initButton.addActionListener(this);
		
		fileDisplay = new JTextField(35);
		fileDisplay.setEditable(true);
		
		// update config if textfield changed
		fileDisplay.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				config.setURI(fileDisplay.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				config.setURI(fileDisplay.getText());
			}
			public void changedUpdate(DocumentEvent e) {
				config.setURI(fileDisplay.getText());
			}
		});
		
		// parse 
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

		openPanel.add(fileDisplay);
		openPanel.add(openButton);
		
		add(choosePanel, BorderLayout.PAGE_START);
		add(openPanel, BorderLayout.CENTER);
		add(initPanel, BorderLayout.PAGE_END);
		
		choosenClassIndex = cb.getSelectedIndex();
	}
	
	public void actionPerformed(ActionEvent e) {
		// read selected KnowledgeSourceClass
        choosenClassIndex = cb.getSelectedIndex();
		
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
	
		// init
		if (e.getSource() == initButton) {
			config.setKnowledgeSource(config.getComponentManager().knowledgeSource(sources.get(choosenClassIndex)));
			config.getComponentManager().applyConfigEntry(config.getKnowledgeSource(), "url", config.getURI());				
			config.getKnowledgeSource().init();
			System.out.println("init KnowledgeSource with \n" + sources.get(choosenClassIndex) + " and \n" + config.getURI() + "\n");
		}
	}
  
}
