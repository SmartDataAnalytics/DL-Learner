package org.dllearner.gui;

//File   : gui/containers/dialogs/filechooser/CountWords.java
//Purpose: Counts words in file.
//       Illustrates menus, JFileChooser, Scanner..
//Author : Fred Swartz - 2006-10-10 - Placed in public domain.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//import org.dllearner.algorithms.RandomGuesser;
//import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
//import org.dllearner.core.LearningAlgorithm;
//import org.dllearner.core.LearningProblem;
//import org.dllearner.core.ReasonerComponent;
//import org.dllearner.core.ReasoningService;
import org.dllearner.kb.OWLFile;
//import org.dllearner.learningproblems.PosNegDefinitionLP;
//import org.dllearner.reasoning.DIGReasoner;

import java.io.*;
import java.util.*;

////////////////////////////////////////////////////////CountWords
public class StartGUI_v4 extends JFrame {
	

 /**
	 * 
	 */
	private static final long serialVersionUID = 9151367563590748364L;
	
//====================================================== fields
 JTextField   _fileNameTF  = new JTextField(15);
 JTextField   _wordCountTF = new JTextField(4);
 JFileChooser _fileChooser = new JFileChooser();
 JTextArea    _textField   = new JTextArea();
 ComponentManager cm = ComponentManager.getInstance(); // create singleton instance
  
 //================================================= constructor
 StartGUI_v4() {
     //... Create / set component characteristics.
     _fileNameTF.setEditable(false);
     _wordCountTF.setEditable(false);

     //... Add listeners <-- extra classes

     //... Create content pane, layout components
     JPanel content = new JPanel();
     content.setLayout(new FlowLayout());
     content.add(new JLabel("File:"));  // name of opend file
     content.add(_fileNameTF);
     content.add(new JLabel("Word Count:")); // test
     content.add(_wordCountTF);  // test
     content.add(_textField);  // test_output

     //... Create menu elements (menubar, menu, menu item)
     JMenuBar menubar  = new JMenuBar();
     JMenu    fileMenu = new JMenu("File");
     JMenuItem openItem = new JMenuItem("Open");
     openItem.addActionListener(new OpenAction());
     JMenuItem saveItem = new JMenuItem("Save");
     saveItem.addActionListener(new SaveAction());
     JMenuItem exitItem = new JMenuItem("Exit");
     exitItem.addActionListener(new ExitAction());
     

     //... Assemble the menu
     menubar.add(fileMenu);
     fileMenu.add(openItem);
     //fileMenu.add(saveItem);
     fileMenu.add(exitItem);

     //... _textField (TextArea) into a JScrollPane
     JScrollPane scrollPane = new JScrollPane(_textField);
     scrollPane.setPreferredSize(new Dimension(320, 240));
     
     //... Set window characteristics
     this.setJMenuBar(menubar);
     this.setContentPane(content);
     this.setTitle("DL-Learner");
     this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     this.add(scrollPane);
     this.pack();                      // Layout components.
     this.setLocationRelativeTo(null); // Center window.

     // RUN

     
     //System.out.println("components");
     //System.out.println(cm.getComponents()); // show components
 	 //System.out.println("knowledgeSources");
 	 //System.out.println(cm.getKnowledgeSources()); // possible sources
 	 	


/*
 	// create DIG reasoning service with standard settings
	ReasonerComponent reasoner = cm.reasoner(DIGReasoner.class, source);
	// ReasoningService rs = cm.reasoningService(DIGReasonerNew.class, source);
	reasoner.init();
	ReasoningService rs = cm.reasoningService(reasoner);
	
	// create a learning problem and set positive and negative examples
	LearningProblem lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
	Set<String> positiveExamples = new TreeSet<String>();
	positiveExamples.add("http://example.com/father#stefan");
	positiveExamples.add("http://example.com/father#markus");
	positiveExamples.add("http://example.com/father#martin");
	Set<String> negativeExamples = new TreeSet<String>();
	negativeExamples.add("http://example.com/father#heinz");
	negativeExamples.add("http://example.com/father#anna");
	negativeExamples.add("http://example.com/father#michelle");
	cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
	cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
	lp.init();
	
	// create the learning algorithm
	LearningAlgorithm la = cm.learningAlgorithm(RandomGuesser.class, lp, rs);
	cm.applyConfigEntry(la, "numberOfTrees", 100);
	cm.applyConfigEntry(la, "maxDepth", 5);
	la.init();
	
	// start the algorithm and print the best concept found
	//la.start();
	//System.out.println(la.getBestSolution());
*/ 
     
 }

 // a test method
 //============================================= countWordsInFile
 private int countWordsInFile(File f) {

     int numberOfWords = 0;  // Count of words.

     try {
         Scanner in = new Scanner(f);

         while (in.hasNext()) {
             String word = in.next();  // Read a "token".
             numberOfWords++;
         }
         in.close();        // Close Scanner's file.

     } catch (FileNotFoundException fnfex) {
         // ... We just got the file from the JFileChooser,
         //     so it's hard to believe there's problem, but...
         JOptionPane.showMessageDialog(StartGUI_v4.this,
                     fnfex.getMessage());
     }
     return numberOfWords;
 }


 ///////////////////////////////////////////////////// OpenAction
 class OpenAction implements ActionListener {
     public void actionPerformed(ActionEvent ae) {
         //... Open a file dialog.
         int retval = _fileChooser.showOpenDialog(StartGUI_v4.this);
         if (retval == JFileChooser.APPROVE_OPTION) {
             //... The user selected a file, get it, use it.
             File file = _fileChooser.getSelectedFile();

             //... Update user interface.
             _fileNameTF.setText(file.getName());
             _wordCountTF.setText("" + countWordsInFile(file));
             //show file in _textField
             try {
                 _textField.read(new FileReader(file), "");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         	// create knowledge source
         	KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
         	//String example = "examples/father.owl";
         	cm.applyConfigEntry(source, "url", file.toURI().toString());
         	source.init();
         }
     }
 }

 ///////////////////////////////////////////////////// OpenAction
 class SaveAction implements ActionListener {
     public void actionPerformed(ActionEvent ae) {
         //... Open a file dialog.
         int retval = _fileChooser.showOpenDialog(StartGUI_v4.this);
         if (retval == JFileChooser.APPROVE_OPTION) {
             //... The user selected a file, get it, use it.
             File file = _fileChooser.getSelectedFile();

             //... Update user interface.
             _fileNameTF.setText(file.getName());
             _wordCountTF.setText("" + countWordsInFile(file));
         }
     }
 }
 
 ///////////////////////////////////////////////////// ExitAction
 class ExitAction implements ActionListener {
     public void actionPerformed(ActionEvent ae) {
    	 System.exit(0);
     }
 }
 
 //========================================================= main
 public static void main(String[] args) {
     JFrame window = new StartGUI_v4();
     window.setVisible(true);
 }
}