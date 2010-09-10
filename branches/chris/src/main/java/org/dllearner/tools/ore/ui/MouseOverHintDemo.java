package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import org.dllearner.tools.ore.MouseOverHintManager;

 

 

class MouseOverHintDemo {

  public static void main( String[] args ) {

    JLabel hintBar = new JLabel(" ");

   MouseOverHintManager hintManager = new MouseOverHintManager(hintBar);

    JFrame frame = new JFrame("MouseOverHintDemo");

    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

   

    JMenuBar menuBar = new JMenuBar();

    JMenu menu = new JMenu("File");

    JMenuItem item1 = new JMenuItem("Load");

    JMenuItem item2 = new JMenuItem("Save");

    JMenuItem item3 = new JMenuItem("Exit");

   

    Box mainPanel = Box.createVerticalBox();

    JButton button = new JButton("Apply");

    JCheckBox checkBox = new JCheckBox("Disable hints");

    JLabel label = new JLabel("Backup strategy");

    JComboBox comboBox = new JComboBox(new String[] {"Always","Just the last","Never"});

    JFormattedTextField formattedText = new JFormattedTextField(new Date());

    Box radioPanel = Box.createVerticalBox();

    ButtonGroup radioGroup = new ButtonGroup();

    JRadioButton radio1 = new JRadioButton("left");

    JRadioButton radio2 = new JRadioButton("right");

    JTable table = new JTable(new String[][] {{"Copy","Ctrl+C"},{"Paste","Ctrl+V"},{"Cut","Ctrl+X"}}, new String[] {"Action","Shortcut"});

   

    hintManager.addHintFor( item1, "Loads a new file" );

    hintManager.addHintFor( item2, "Saves the current file" );

    hintManager.addHintFor( item3, "Exits the application" );

    hintManager.addHintFor( button, "Apply any changes made" );

    hintManager.addHintFor( checkBox, "Turns off the display of hints" );

    hintManager.addHintFor( comboBox, "Selects how many backups to make" );

    hintManager.addHintFor( formattedText, "Enters the date for next run" );

    hintManager.addHintFor( radioPanel, "Selects the position for application's toolbar" );

    hintManager.addHintFor( table, "Shortcuts for each application's action" );

   

    frame.setJMenuBar( menuBar );

    menuBar.add( menu );

    menu.add( item1 );

    menu.add( item2 );

    menu.add( item3 );

    frame.getContentPane().add( mainPanel, BorderLayout.CENTER );

    mainPanel.add( Box.createVerticalStrut(5) );

    mainPanel.add( button );

    mainPanel.add( Box.createVerticalStrut(5) );

    mainPanel.add( checkBox );

    mainPanel.add( Box.createVerticalStrut(5) );

    mainPanel.add( label );

    label.setLabelFor( comboBox );

    mainPanel.add( comboBox );

    mainPanel.add( Box.createVerticalStrut(5) );

   mainPanel.add( formattedText );

    mainPanel.add( Box.createVerticalStrut(5) );

    radioGroup.add( radio1 );

    radioGroup.add( radio2 );

    radioPanel.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Toolbar") );

    radioPanel.add( radio1 );

    radioPanel.add( radio2 );

    mainPanel.add( radioPanel );

    mainPanel.add( Box.createVerticalStrut(5) );

    mainPanel.add( table.getTableHeader() );

    mainPanel.add( table );

    mainPanel.add( Box.createVerticalStrut(5) );

    hintBar.setBorder( BorderFactory.createLoweredBevelBorder() );

    frame.getContentPane().add( hintBar, BorderLayout.SOUTH );

    frame.pack();

   

    hintManager.enableHints( frame );

    frame.setVisible( true );

  }

}
