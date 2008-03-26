package org.dllearner.tools.ore;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;


public class IntroductionPanel extends JPanel {
 
	private JTextArea instructionsField;
    private JScrollPane jScrollPane1;
  
	
      
    private JLabel welcomeTitle;
    private JPanel contentPanel;
     
    
    
    public IntroductionPanel() {
        
    
    	
		
    
        contentPanel = getContentPanel();
    
        
    

        setLayout(new java.awt.BorderLayout());


        
        //add(leftPanel, BorderLayout.WEST);
        
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    
    private JPanel getContentPanel() {
        
        JPanel contentPanel1 = new JPanel();
        JPanel jPanel1 = new JPanel();
        
        jScrollPane1 = new JScrollPane();
        instructionsField = new JTextArea();
        
        
        //setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane1.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        instructionsField.setBackground(UIManager.getDefaults().getColor("control"));
        instructionsField.setColumns(20);
        instructionsField.setEditable(false);
        instructionsField.setLineWrap(true);
        instructionsField.setRows(5);
        instructionsField.setFont(new Font("Serif",Font.PLAIN,14));
        instructionsField.setText("This is an test of a wizard dialog, which allows a knowledge engineer to select " +
        							"a concept of an ontology which should be (re)learned.  \n\nOn " +
        							"the next page, choose a JAR file that contains some components.  It will find any " +
        							"JavaBeans listed in the manifest, and also let you add classes from the JAR file.  " +
        							"When the resulting NetBeans plug-in is installed in NetBeans, the components you " +
        							"selected will be on the Component Palette, and can be dragged and dropped onto Swing " +
        							"forms.  The library display name you enter is also the name that will be used for the " +
        							"category in the component palette where your components will appear.");
        instructionsField.setWrapStyleWord(true);
        jScrollPane1.setViewportView(instructionsField);

        
        
        
        
        welcomeTitle = new JLabel();
        welcomeTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
       
        

        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 14));
        welcomeTitle.setText("Welcome to the DL-Learner ORE-Tool!");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1,0,0));

        //jPanel1.add(blankSpace);
       
     
        jPanel1.add(jScrollPane1);

        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

        return contentPanel1;
        
    }
 
}
