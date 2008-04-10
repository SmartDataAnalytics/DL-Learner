package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

public class KnowledgeSourcePanel extends JPanel{

	
	private javax.swing.JTextField fileURL;
	private javax.swing.JButton browseButton;
	
	private JPanel contentPanel;
	private JLabel message;
	
	public KnowledgeSourcePanel() {

		new LeftPanel(1);
		contentPanel = getContentPanel();
		
		setLayout(new java.awt.BorderLayout());
		
		//add(leftPanel,BorderLayout.WEST);
		add(contentPanel,BorderLayout.CENTER);

	}

	private JPanel getContentPanel() {

		JPanel contentPanel1 = new JPanel();
		
		message = new JLabel();
		message.setText("enter or browse OWL file");
		fileURL = new javax.swing.JTextField(40);
		
		browseButton = new javax.swing.JButton("browse");
	
		contentPanel1.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		panel.add(fileURL);
		panel.add(browseButton);
		
		contentPanel1.add(panel,BorderLayout.CENTER);
		contentPanel1.add(message,BorderLayout.SOUTH);
	
		

		return contentPanel1;
	}
	
	public void addListeners(ActionListener l, DocumentListener d) {
		browseButton.addActionListener(l);
		fileURL.addActionListener(l);
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
	

}
