package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.dllearner.kb.sparql.SparqlEndpoint;

public class KnowledgeSourcePanel extends JPanel{

	private static final long serialVersionUID = -3997200565180270088L;
	private javax.swing.JTextField fileURL;
	private JTextField sparqlURL;
	private javax.swing.JButton browseButton;
	
	private JComboBox sparqlBox;
	
	private JPanel contentPanel;
	private JLabel message;
	
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
		message = new JLabel();
		message.setText("enter or browse OWL file");
		fileURL = new javax.swing.JTextField(60);
		browseButton = new javax.swing.JButton("browse");
		layout.setHorizontalGroup(layout.createSequentialGroup()
			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			         .addComponent(fileURL)
			         .addComponent(message))
			    .addComponent(browseButton));
		layout.linkSize(SwingConstants.HORIZONTAL, fileURL, message);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				        .addComponent(fileURL)
				        .addComponent(browseButton))
      
			    .addComponent(message));
		
		
		JPanel sparqlPanel = new JPanel();
		sparqlPanel.setBorder(new TitledBorder("SPARQL"));
		sparqlURL = new JTextField(60);
		sparqlURL.setEnabled(false);
		
		Vector<URL> model = new Vector<URL>();
		for(SparqlEndpoint e : SparqlEndpoint.listEndpoints())
			model.add(e.getURL());
		sparqlBox = new JComboBox(model);
		sparqlBox.setEditable(false);
		sparqlBox.setSelectedIndex(-1);
		sparqlBox.setEnabled(false);
		sparqlPanel.add(sparqlBox);
		
		
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
			
			message.setText(fileURL.getText()+" does not exist");
			return false;
		}
		if(!fileURL.getText().equals("") && (getOWLFile().isDirectory() || (getOWLFile().isFile() && !getOWLFile().getPath().endsWith(".owl")))){
			System.err.println(getOWLFile().getPath());
			message.setText(fileURL.getText()+" is not a OWL file");
			return false;
		}
		if(fileURL.getText().equals("")){
			message.setText("enter or browse OWL file");
			return false;
		}
		if(getOWLFile().exists() && getOWLFile().getPath().endsWith(".owl")){
			message.setText("");
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
		message.setVisible(true);
		
		sparqlBox.setEditable(false);
		sparqlBox.setEnabled(false);
	
	}
	
	public void setSPARQLMode(){
		fileURL.setEnabled(false);
		browseButton.setEnabled(false);
		message.setVisible(false);
		
		sparqlBox.setEnabled(true);
		sparqlBox.setEditable(true);
		
	}
	
	
}
