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

package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.dllearner.tools.ore.ui.LinkLabel;
import org.dllearner.tools.ore.ui.MetricsPanel;

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
	
	private Box box;
	private LinkLabel openFromFileLink;
	private LinkLabel openFromURILink;
	private LinkLabel loadFromSparqlEndpointLink;
	private Box recentLinkBox;

	
	
	public KnowledgeSourcePanel() {

		new LeftPanel(1);
		contentPanel = getContentPanel();
		
		setLayout(new java.awt.BorderLayout());
		setLayout(new GridLayout(1,2));
		
		add(contentPanel);

	}

	private JPanel getContentPanel() {

	
		JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
       
        int strutHeight = 10;

       
        box = new Box(BoxLayout.Y_AXIS);
        box.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
        panel.add(box);
        openFromFileLink = new LinkLabel("Open OWL-Ontology from filesystem");
        openFromFileLink.setName("openFromFileLink");
        box.add(openFromFileLink);
        box.add(Box.createVerticalStrut(strutHeight));
        openFromURILink = new LinkLabel("Open OWL-Ontology from URI");
        openFromURILink.setName("openFromURILink");
        box.add(openFromURILink);
        box.add(Box.createVerticalStrut(strutHeight));
        loadFromSparqlEndpointLink = new LinkLabel("Open OWL-Ontology from Sparql-Endpoint");
        loadFromSparqlEndpointLink.setName("loadFromSparqlEndpointLink");
        box.add(loadFromSparqlEndpointLink);
        panel.add(box);
        
        box.add(Box.createVerticalStrut(2 * strutHeight));
      

		return panel;
	}
	
	private void addMetricsPanel(MetricsPanel metrics){
		add(metrics);
	}
	
	public void addListeners(ActionListener aL) {
		openFromFileLink.addLinkListener(aL);
		openFromURILink.addLinkListener(aL);
		loadFromSparqlEndpointLink.addLinkListener(aL);
		
    }

	
	
	
	
	public void openFileChooser(){
		JFileChooser filechooser = new JFileChooser();
		
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		String choosenPath = fileURL.getText();
//		if(!choosenPath.equals("") && (new File(choosenPath)).exists()){
//			filechooser.setCurrentDirectory(new File(fileURL.getText()));
//		}
	
		filechooser.addChoosableFileFilter(new FileFilter() {
		    @Override
			public boolean accept(File f) {
		      if(f.isDirectory()){ 
		    	  return true;
		      }
		      return f.getName().toLowerCase().endsWith(".owl") || f.getName().toLowerCase().endsWith(".rdf");
		    }
		    @Override
			public String getDescription(){
		    	return "OWLs, RDFs"; 
		    }  
		  });
		int status = filechooser.showOpenDialog(null);
        
        if (status == JFileChooser.APPROVE_OPTION){
        	String strURL = filechooser.getSelectedFile().getAbsolutePath();
        	fileURL.setText(strURL);
        
            
           
        } else{
            System.out.println("Auswahl abgebrochen");
        }
	}
	
	public boolean isExistingOWLFile(){
		if(!fileURL.getText().equals("") && !getOWLFile().exists()){
			
			owlMessage.setText(fileURL.getText()+" does not exist");
			return false;
		}
		if(!fileURL.getText().equals("") && (getOWLFile().isDirectory() || 
				(getOWLFile().isFile() && !getOWLFile().getPath().endsWith(".owl")))){ 
				//(getOWLFile().isFile() && !getOWLFile().getPath().endsWith(".rdf")))){
			System.err.println(getOWLFile().getPath());
			owlMessage.setText(fileURL.getText()+" is not a OWL or RDF file");
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
//		this.fileURL.setText(fileURL);
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
