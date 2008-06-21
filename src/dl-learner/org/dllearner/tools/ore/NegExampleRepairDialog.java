package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

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
	
	
	private StatsPanel statsPanel;
	private DescriptionPanel descPanel;
	private ChangesPanel changesPanel;
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
		
		descPanel = new DescriptionPanel(ore, ind, this);		
		
		
		statsPanel = new StatsPanel(ore, ind);
		statsPanel.init();
		JScrollPane scroll = new JScrollPane();
	    scroll.setViewportView(statsPanel);
		        
				
		changesPanel = new ChangesPanel();
	    changesPanel.init();
	    
	    
	    action_stats_Panel = new JPanel();
		action_stats_Panel.setLayout(new GridLayout(4,0));
	    
		
		
		action_stats_Panel.add(descPanel);
		action_stats_Panel.add(scroll);
		action_stats_Panel.add(changesPanel);
		
		
		JSeparator separator = new JSeparator();
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		
		ok_cancelPanel = new JPanel();
		ok_cancelPanel.setLayout(new BorderLayout());
		ok_cancelPanel.add(separator, BorderLayout.NORTH);
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		
		
       
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
		System.out.println(e.getSource());
		if(e.getSource() instanceof DescriptionMenuItem){
			actualDesc = ((DescriptionMenuItem)e.getSource()).getDescription();
			
			if(e.getActionCommand().startsWith("remove class")){                       //remove class
				System.err.println("vorher: " + ore.modi.checkInstanceNewOntology(ore.getConceptToAdd() ,ind));
				System.err.println("vorher: " + ore.modi.getCriticalDescriptions(ind, ore.conceptToAdd));
				ore.modi.removeClassAssertion(ind, actualDesc);
				
				descPanel.updatePanel();
				changesPanel.add(new JLabel("removed class assertion to " + actualDesc));
				changesPanel.add(new JButton("Undo"));
				System.err.println("nachher: " + ore.modi.checkInstanceNewOntology(ore.conceptToAdd, ind));
				System.err.println("nachher: " + ore.modi.getCriticalDescriptions(ind, ore.conceptToAdd));
			}
//			else if(e.getActionCommand().startsWith("add class")){                     //add class
//					System.err.println(actualDesc);
//					ore.modi.addClassAssertion(ind, actualDesc);
//					actionsPanel.updatePanel();
//					changes.add(new JLabel("added class assertion to " + actualDesc));
//					
//			}
//			else if(e.getActionCommand().startsWith("move class")){                    //move class
//			
//				newDesc = (Description)new ChooseDialog(this, ore, ind).getSelectedElement();
//				System.err.println(newDesc);
//				if(newDesc != null){
//					System.out.print(ind + " from " + actualDesc + " to " + newDesc);
//					ore.modi.moveIndividual(ind, actualDesc, newDesc);
//					}
//				
//			}
//			else if(e.getActionCommand().equals("add property")){                      //add property
//				object = (Individual)new ChooseDialog(this, ore, actualDesc).getSelectedElement();
//				if(object != null)
//					ore.modi.addObjectProperty(ind, (ObjectSomeRestriction)actualDesc, object);
//			}
//			else if(e.getActionCommand().equals("remove property")){                  //delete property
//				ore.modi.deleteObjectProperty(ind, (ObjectSomeRestriction)actualDesc);
//			}
//		}
		
//		if(e.getActionCommand().equals("delete instance")){
//			ore.modi.deleteIndividual(ind);
//			
//		}
//		else{
//			actualDesc = ((DescriptionButton)e.getSource()).getDescription();
//			
//			if(e.getActionCommand().startsWith("remove class")){                       //remove class
//				System.out.println(ore.reasoner2.instanceCheck(ore.getConceptToAdd() ,ind));
//				ore.modi.removeClassAssertion(ind, actualDesc);
//				
//				actionsPanel.updatePanel();
//				changes.add(new JLabel("removed class assertion to " + actualDesc));
//				changes.add(new JButton("Undo"));
//				System.err.println(ore.modi.checkInstanceNewOntology(ore.conceptToAdd, ind));
//				
//			}
//			else if(e.getActionCommand().startsWith("add class")){                     //add class
//					System.err.println(actualDesc);
//					ore.modi.addClassAssertion(ind, actualDesc);
//					actionsPanel.updatePanel();
//					changes.add(new JLabel("added class assertion to " + actualDesc));
//					
//			}
//			else if(e.getActionCommand().startsWith("move class")){                    //move class
//			
//				newDesc = (Description)new ChooseDialog(this, ore, ind).getSelectedElement();
//				System.err.println(newDesc);
//				if(newDesc != null){
//					System.out.print(ind + " from " + actualDesc + " to " + newDesc);
//					ore.modi.moveIndividual(ind, actualDesc, newDesc);
//					}
//				
//			}
//			else if(e.getActionCommand().equals("add property")){                      //add property
//				object = (Individual)new ChooseDialog(this, ore, actualDesc).getSelectedElement();
//				if(object != null)
//					ore.modi.addObjectProperty(ind, (ObjectSomeRestriction)actualDesc, object);
//			}
//			else if(e.getActionCommand().equals("remove property")){                  //delete property
//				ore.modi.deleteObjectProperty(ind, (ObjectSomeRestriction)actualDesc);
//			}
//		}
	}
	}
	
	
	
	
	


	
	
	
}
