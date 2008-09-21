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

package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 * Wizard panel where radio buttons for choosing knowledge source type, button for browsing
 * file system and textfields for inserting file name or SPARQL-URL are added.
 * @author Lorenz Buehmann
 *
 */
public class KnowledgeSourcePanel extends JPanel{

	private static final long serialVersionUID = -3997200565180270088L;
	private javax.swing.JTextField fileURL;
	private JTextField sparqlURL;
	private JButton browseButton;
	private JButton connectButton;
	
//	private JComboBox sparqlBox;
	
	private JPanel contentPanel;
	
	private JLabel owlMessage;
	private JLabel sparqlMessage;
	
	private JRadioButton owl;
	private JRadioButton sparql;
	
	
	public KnowledgeSourcePanel() {

		new LeftPanel(1);
		contentPanel = getContentPanel();
		
		setLayout(new java.awt.BorderLayout());
		
		add(contentPanel,BorderLayout.CENTER);

	}

	private JPanel getContentPanel() {

		JPanel contentPanel1 = new JPanel();
		
		JPanel buttonPanel = new JPanel();
		ButtonGroup bg = new ButtonGroup();
		Box box = Box.createVerticalBox();
		owl = new JRadioButton("OWL", true);
		sparql = new JRadioButton("SPARQL");
		bg.add(owl);
		bg.add(sparql);
		box.add(owl);
		box.add(sparql);
		buttonPanel.add(box);
		
		JPanel owlPanel = new JPanel();
		GroupLayout layout = new GroupLayout(owlPanel);
		owlPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		owlPanel.setBorder(new TitledBorder("OWL"));
		owlMessage = new JLabel();
		owlMessage.setText("enter or browse OWL file");
		fileURL = new javax.swing.JTextField(60);
		browseButton = new javax.swing.JButton("browse");
		layout.setHorizontalGroup(layout.createSequentialGroup()
			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			         .addComponent(fileURL)
			         .addComponent(owlMessage))
			    .addComponent(browseButton));
		layout.linkSize(SwingConstants.HORIZONTAL, fileURL, owlMessage);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				        .addComponent(fileURL)
				        .addComponent(browseButton))
      
			    .addComponent(owlMessage));
		
		
		JPanel sparqlPanel = new JPanel();
		GroupLayout sparqlLayout = new GroupLayout(sparqlPanel);
		sparqlPanel.setLayout(sparqlLayout);
		sparqlLayout.setAutoCreateGaps(true);
		sparqlLayout.setAutoCreateContainerGaps(true);
		sparqlPanel.setBorder(new TitledBorder("SPARQL"));
		sparqlMessage = new JLabel();
		sparqlMessage.setText("enter SPARQL-URL and press connect");
		sparqlMessage.setVisible(false);
		sparqlURL = new JTextField(60);
		sparqlURL.setEnabled(false);
		
//		Vector<URL> model = new Vector<URL>();
//		for(SparqlEndpoint e : SparqlEndpoint.listEndpoints())
//			model.add(e.getURL());
//		sparqlBox = new JComboBox(model);
//		sparqlBox.setEditable(false);
//		sparqlBox.setSelectedIndex(-1);
//		sparqlBox.setEnabled(false);
//		sparqlPanel.add(sparqlURL);
		connectButton = new javax.swing.JButton("connect");
		sparqlLayout.setHorizontalGroup(sparqlLayout.createSequentialGroup()
			    .addGroup(sparqlLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			         .addComponent(sparqlURL)
			         .addComponent(sparqlMessage))
			    .addComponent(connectButton));
		sparqlLayout.linkSize(SwingConstants.HORIZONTAL, sparqlURL, sparqlMessage);

		sparqlLayout.setVerticalGroup(sparqlLayout.createSequentialGroup()
				.addGroup(sparqlLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				        .addComponent(sparqlURL)
				        .addComponent(connectButton))
      
			    .addComponent(sparqlMessage));
		
		
		contentPanel1.setLayout(new GridLayout(0,1));
		contentPanel1.add(buttonPanel);
		contentPanel1.add(owlPanel);
		contentPanel1.add(sparqlPanel);
	
		

		return contentPanel1;
	}
	
	public void addListeners(ActionListener l, DocumentListener d) {
		browseButton.addActionListener(l);
		fileURL.addActionListener(l);
		sparqlURL.addActionListener(l);
		owl.addActionListener(l);
		sparql.addActionListener(l);
		fileURL.getDocument().addDocumentListener(d);
    }
	
	
	
	public void openFileChooser(){
		JFileChooser filechooser = new JFileChooser();
		
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String choosenPath = fileURL.getText();
		if(!choosenPath.equals("") && (new File(choosenPath)).exists())
			filechooser.setCurrentDirectory(new File(fileURL.getText()));
	
		filechooser.addChoosableFileFilter(new FileFilter() {
		    @Override
			public boolean accept(File f) {
		      if (f.isDirectory()) return true;
		      return f.getName().toLowerCase().endsWith(".owl");
		    }
		    @Override
			public String getDescription () { return "OWLs"; }  
		  });
		int status = filechooser.showOpenDialog( null );
        
        if ( status == JFileChooser.APPROVE_OPTION ){
        	String strURL = filechooser.getSelectedFile().getAbsolutePath();
        	fileURL.setText(strURL);
        
            
           
        }else{
            System.out.println( "Auswahl abgebrochen" );
        }
	}
	
	public boolean isExistingOWLFile(){
		if(!fileURL.getText().equals("") && !getOWLFile().exists()){
			
			owlMessage.setText(fileURL.getText()+" does not exist");
			return false;
		}
		if(!fileURL.getText().equals("") && (getOWLFile().isDirectory() || (getOWLFile().isFile() && !getOWLFile().getPath().endsWith(".owl")))){
			System.err.println(getOWLFile().getPath());
			owlMessage.setText(fileURL.getText()+" is not a OWL file");
			return false;
		}
		if(fileURL.getText().equals("")){
			owlMessage.setText("enter or browse OWL file");
			return false;
		}
		if(getOWLFile().exists() && getOWLFile().getPath().endsWith(".owl")){
			owlMessage.setText("");
			return true;
		}
		return true;
		
		
		
	}

	public File getOWLFile() {
		return new File(fileURL.getText());
	}
	
	public void setFileURL(String fileURL){
		this.fileURL.setText(fileURL);
	}
	
	public void setOWLMode(){
		fileURL.setEnabled(true);
		browseButton.setEnabled(true);
		owlMessage.setVisible(true);
		
		sparqlURL.setEnabled(false);
		connectButton.setEnabled(false);
		sparqlMessage.setVisible(false);
//		sparqlBox.setEditable(false);
//		sparqlBox.setEnabled(false);
	
	}
	
	public void setSPARQLMode(){
		fileURL.setEnabled(false);
		browseButton.setEnabled(false);
		owlMessage.setVisible(false);
		
		sparqlURL.setEnabled(true);
		connectButton.setEnabled(true);
		sparqlMessage.setVisible(true);
//		sparqlBox.setEnabled(true);
//		sparqlBox.setEditable(true);
		
	}
	
	
}
