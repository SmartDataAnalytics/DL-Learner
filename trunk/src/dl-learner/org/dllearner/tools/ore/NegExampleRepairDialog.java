package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.reasoning.OWLAPIReasoner;

public class NegExampleRepairDialog extends JDialog implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private JPanel statsPanel;
	private JPanel actionsPanel;
	private JPanel ok_cancelPanel;
	private JPanel action_stats_Panel;
	
	
	private JButton okButton;
	private JButton cancelButton;
	
	
	
	private ORE ore;
	private Individual ind;
	private OWLAPIReasoner reasoner;
	private Description actualDesc;
	private Description newDesc;
	private Individual object;
	
	
	public NegExampleRepairDialog(Individual ind, JDialog dialog, ORE ore){
		super(dialog, "Repair negative example", true);
		this.ind = ind;
		this.ore = ore;
		this.reasoner = ore.reasoner2;
		init();
	}
	
	public void init(){
		setSize(700, 700);
		setLayout(new BorderLayout());
		
		action_stats_Panel = new JPanel();
		action_stats_Panel.setLayout(new GridLayout(2,0));
		
		statsPanel = new JPanel();
		statsPanel.setBorder(new TitledBorder("stats"));
		
	
		
		GridBagLayout gbl = new GridBagLayout();
		statsPanel.setLayout(gbl);
		
		        
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel descLb = new JLabel("Description:");
        JLabel indLb = new JLabel("Individual:");
        JLabel classLb = new JLabel("Classes:");
       
        
        JLabel descLb1 = new JLabel(ore.conceptToAdd.toString());
        JLabel indLb1 = new JLabel(ind.getName());
        Set<NamedClass> t = null;
		
		JPanel classesPanel = new JPanel(new GridLayout(0, 1));
		
		t = reasoner.getConcepts(ind);
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
        statsPanel.add(classesPanel);    
       
        
		actionsPanel = new JPanel(new GridLayout(0, 1));
		
		
		
		actionsPanel.setBorder(new TitledBorder("actions"));
		JButton delete = new JButton("delete instance");
		delete.addActionListener(this);
		actionsPanel.add(delete);
		for(Description d :ore.getCriticalDescriptions(ind, ore.getConceptToAdd())){
			
			if(!(d instanceof Negation)){
				if(d instanceof NamedClass){
					actionsPanel.add(new DescriptionButton("remove class assertion to " + d.toString(), d ,this));
					actionsPanel.add(new DescriptionButton("move class assertion " + d.toString() + " to ...", d, this));
				}
				else if(d instanceof ObjectSomeRestriction)
					actionsPanel.add(new DescriptionButton("remove property assertion " + d.toString(), d, this));
			}
			else if(d instanceof Negation){
				if(d.getChild(0) instanceof NamedClass)
					actionsPanel.add(new DescriptionButton("add class assertion to " + d.getChild(0).toString(), d, this));
				else if(d.getChild(0) instanceof ObjectSomeRestriction)
					actionsPanel.add(new DescriptionButton("add property " + d.toString(), d, this));
			}
		}
		JScrollPane scroll = new JScrollPane();
	    scroll.setViewportView(statsPanel);
		action_stats_Panel.add(scroll);
		action_stats_Panel.add(actionsPanel);
		
		
		JSeparator separator = new JSeparator();
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		
		ok_cancelPanel = new JPanel();
		ok_cancelPanel.setLayout(new BorderLayout());
		ok_cancelPanel.add(separator, BorderLayout.NORTH);
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		
		
        getContentPane().add(ok_cancelPanel, java.awt.BorderLayout.SOUTH);
        getContentPane().add(action_stats_Panel, java.awt.BorderLayout.CENTER);
		
		
        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(cancelButton);
		ok_cancelPanel.add(buttonBox, BorderLayout.EAST);
        
		getContentPane().add(ok_cancelPanel, BorderLayout.SOUTH);
		
		
		setModal(true);
		setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		
		
		if(e.getActionCommand().equals("delete instance")){
			ore.modi.deleteIndividual(ind);
		}
		else{
			 actualDesc = ((DescriptionButton)e.getSource()).getDescription();
			if(e.getActionCommand().startsWith("remove class")){
				ore.modi.removeClassAssertion(ind, actualDesc);
			}
			else if(e.getActionCommand().startsWith("move class")){
			
				SwingUtilities.invokeLater(new Runnable(){
			
					@Override
					public void run() {
						newDesc = (Description)new ChooseDialog(ore, ind).getSelectedElement();
					}
				
				});
				if(newDesc != null)
					ore.modi.moveIndividual(ind, actualDesc, newDesc);
				
			}
			else if(e.getActionCommand().equals("add property")){
				SwingUtilities.invokeLater(new Runnable(){
					
					@Override
					public void run() {
						object = (Individual)new ChooseDialog(ore, actualDesc).getSelectedElement();
					}
				
				});
				if(newDesc != null)
					
				ore.modi.addObjectProperty(ind, (ObjectSomeRestriction)actualDesc, object);
			}
		}
	}
	
	
	
	


	
	
	
}
