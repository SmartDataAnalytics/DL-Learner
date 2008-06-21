package org.dllearner.tools.ore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;

public class StatsPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8418286820511803278L;
	ORE ore;
	Individual ind;
	
	public StatsPanel(ORE ore, Individual ind){
		super();
		this.ore = ore;
		this.ind = ind;
	}
	
	public void init(){
		
		setBorder(new TitledBorder("stats"));
		
	
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		        
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel descLb = new JLabel("Description:");
        JLabel indLb = new JLabel("Individual:");
        JLabel classLb = new JLabel("Classes:");
       
        
        JLabel descLb1 = new JLabel(ore.conceptToAdd.toString());
        JLabel indLb1 = new JLabel(ind.getName());
        Set<NamedClass> t = null;
		
		JPanel classesPanel = new JPanel(new GridLayout(0, 1));
		
		t = ore.reasoner2.getConcepts(ind);
		t.add(ore.getConcept());
		for(NamedClass nc : t)
			classesPanel.add(new JLabel(nc.getName()));
        
		

        
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
        add(descLb); //add the JLabel to the JPanel object

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
        add(indLb);
        
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
        add(classLb);
        
        gbc.gridx = 1;      // second column
        gbc.gridy = 0;      // first row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 100;    // horizontal size - second column
        gbc.weighty = 0;    // !!! vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell      
        gbc.anchor = GridBagConstraints.CENTER; // center aligning

        gbl.setConstraints(descLb1, gbc);
        add(descLb1);
        
        gbc.gridx = 1;      // second column
        gbc.gridy = 1;      // second row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 0;    // horizontal size for the column is defined already!
        gbc.weighty = 0;    // vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell          
        gbc.anchor = GridBagConstraints.CENTER; // center aligning

        gbl.setConstraints(indLb1, gbc);
        add(indLb1);    
        
        gbc.gridx = 1;      // second column
        gbc.gridy = 2;      // third row
        gbc.gridwidth = 1;  // occupies only one column
        gbc.gridheight = 1;  // occupies only one row 
        gbc.weightx = 0;    // horizontal size for the column is defined already!
        gbc.weighty = 0;    // vertical size for the row is defined already!
        gbc.fill = GridBagConstraints.HORIZONTAL;    // fill horizontally entire cell          
        gbc.anchor = GridBagConstraints.CENTER; // center aligning

//        gbl.setConstraints(scrollPane, gbc);
//        statsPanel.add(scrollPane);    
        gbl.setConstraints(classesPanel, gbc);
        add(classesPanel);    
	}
}
