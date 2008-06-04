package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.reasoning.OWLAPIReasoner;

public class NegExampleRepairDialog extends JDialog implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private JPanel statsPanel;
	private JPanel actionsPanel;
	private JPanel ok_cancelPanel;

	private JButton okButton;
	private JButton cancelButton;
	
	
	private JTextArea classesField;
	private JScrollPane scrollPane;
	
	private ORE ore;
	private Individual ind;
	private OWLAPIReasoner reasoner;
	
	public NegExampleRepairDialog(Individual ind, JDialog dialog, ORE ore){
		super(dialog, "Auswahl", true);
		this.ind = ind;
		this.ore = ore;
		this.reasoner = ore.reasoner;
		init();
	}
	
	public void init(){
		setSize(700, 700);
		setLayout(new GridLayout(3, 0));
		
		statsPanel = new JPanel();
		statsPanel.setBorder(new TitledBorder("stats"));
		
		getContentPane().add(statsPanel);
		
		GridBagLayout gbl = new GridBagLayout();
		statsPanel.setLayout(gbl);
		
		        
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel descLb = new JLabel("Description:");
        JLabel indLb = new JLabel("Individual:");
        JLabel classLb = new JLabel("Classes:");
       
        
        JLabel descLb1 = new JLabel(ore.conceptToAdd.toString());
        JLabel indLb1 = new JLabel(ind.getName());
        Set<NamedClass> t = reasoner.getConcepts(ind);
        classesField = new JTextArea();
        String classes = new String();
        for(NamedClass nc : t)
        	classes += nc.getName() +"\n";
        classesField.setText(classes);
        	
        scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setPreferredSize(new Dimension(150,100));
        classesField.setBackground(UIManager.getDefaults().getColor("control"));
        classesField.setColumns(20);
        classesField.setEditable(false);
        classesField.setLineWrap(true);
        classesField.setRows(5);
        classesField.setWrapStyleWord(true);
        scrollPane.setViewportView(classesField);
        
        gbc.gridx = 0; // first column
        gbc.gridy = 0; // first row
        gbc.gridwidth = 1; // occupies only one column
        gbc.gridheight = 1; // occupies only one row
        gbc.weightx = 20; // relative horizontal size - first column
        gbc.weighty = 10; // relative vertical size - first row
        gbc.fill = GridBagConstraints.NONE; // stay as small as possible
        // suite for labels
        gbc.anchor = GridBagConstraints.CENTER; // center aligning
        //inform the layout about the control to be added and its constraints:
        gbl.setConstraints(descLb, gbc);
        statsPanel.add(descLb); //add the JLabel to the JPanel object

        gbc.gridx = 0; // first column
        gbc.gridy = 1; // second row
        gbc.gridwidth = 1; // occupies only one column
        gbc.gridheight = 1; // occupies only one row
        gbc.weightx = 0; // !!! horizontal size for the column is defined already!
        gbc.weighty = 10; // relative vertical size - second row
        gbc.fill = GridBagConstraints.NONE; // stay as small as possible, suites for labels
        gbc.anchor = GridBagConstraints.CENTER; // center aligning
        //inform the layout about the control to be added and its constraints:
        gbl.setConstraints(indLb, gbc);
        statsPanel.add(indLb);
        
        gbc.gridx = 0; // first column
        gbc.gridy = 2; // third row
        gbc.gridwidth = 1; // occupies only one column
        gbc.gridheight = 1; // occupies only one row
        gbc.weightx = 0; // !!! horizontal size for the column is defined already!
        gbc.weighty = 10; // relative vertical size - second row
        gbc.fill = GridBagConstraints.NONE; // stay as small as possible, suites for labels
        gbc.anchor = GridBagConstraints.CENTER; // center aligning
        //inform the layout about the control to be added and its constraints:
        gbl.setConstraints(classLb, gbc);
        statsPanel.add(classLb);
        
        gbc.gridx = 1;      // second column
        gbc.gridy = 0;      // first row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 100;    // horizontal size - second column
        gbc.weighty = 0;    // !!! vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell      
        gbc.anchor = GridBagConstraints.CENTER; // center aligning

        gbl.setConstraints(descLb1, gbc);
        statsPanel.add(descLb1);
        
        gbc.gridx = 1;      // second column
        gbc.gridy = 1;      // second row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 0;    // horizontal size for the column is defined already!
        gbc.weighty = 0;    // vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell          
        gbc.anchor = GridBagConstraints.CENTER; // center aligning

        gbl.setConstraints(indLb1, gbc);
        statsPanel.add(indLb1);    
        
        gbc.gridx = 2;      // third column
        gbc.gridy = 2;      // third row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 0;    // horizontal size for the column is defined already!
        gbc.weighty = 0;    // vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell          
        gbc.anchor = GridBagConstraints.WEST; // west aligning

        gbl.setConstraints(scrollPane, gbc);
        statsPanel.add(scrollPane);    
        
		actionsPanel = new JPanel();
		actionsPanel.setBorder(new TitledBorder("actions"));
		JButton delete = new JButton("delete instance");
		delete.addActionListener(this);
		actionsPanel.add(delete);
		JButton save = new JButton("save");
		save.addActionListener(this);
		actionsPanel.add(save);
		getContentPane().add(actionsPanel, java.awt.BorderLayout.EAST);
		
		JSeparator separator = new JSeparator();
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		ok_cancelPanel = new JPanel();
		ok_cancelPanel.setLayout(new BorderLayout());
		ok_cancelPanel.add(separator, BorderLayout.NORTH);
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		
        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(cancelButton);
		ok_cancelPanel.add(buttonBox, BorderLayout.EAST);
        
		getContentPane().add(ok_cancelPanel);
		
		
		setModal(true);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if(e.getActionCommand().equals("delete instance")){
			ore.modi.deleteIndividual(ind);
			
		}
		if(e.getActionCommand().equals("save")){
			ore.modi.saveOntology();
		}
		
		
	}
	
	
	
}
