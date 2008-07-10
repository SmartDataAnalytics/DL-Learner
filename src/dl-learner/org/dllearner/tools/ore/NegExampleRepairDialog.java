package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class NegExampleRepairDialog extends JDialog implements ActionListener, MouseListener{
	
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
	private Description actualDesc;
	private Description newDesc;
	private Set<OWLOntologyChange> allChanges;
	
	
	public NegExampleRepairDialog(Individual ind, JDialog dialog, ORE ore){
		super(dialog, "Repair negative example", true);
		this.ind = ind;
		this.ore = ore;
		allChanges = new HashSet<OWLOntologyChange>();
		init();
	}
	
	public void init(){
		
		setSize(700, 700);
		setLayout(new BorderLayout());
		
		descPanel = new DescriptionPanel(ore, ind, this);		
		JScrollPane descScroll = new JScrollPane();
		descScroll.setViewportView(descPanel);
		
		statsPanel = new StatsPanel(ore, ind);
		statsPanel.init();
		JScrollPane statsScroll = new JScrollPane();
		statsScroll.setViewportView(statsPanel);
		        
				
		changesPanel = new ChangesPanel();
		JScrollPane changesScroll = new JScrollPane();
		changesScroll.setViewportView(changesPanel);
		
	    action_stats_Panel = new JPanel();
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.rowWeights = new double[] {0.0, 0.1, 0.1};
		gbl.rowHeights = new int[] {64, 7, 7};
		gbl.columnWeights = new double[] {0.1};
		gbl.columnWidths = new int[] {7};
		action_stats_Panel.setLayout(gbl);
		
		
		action_stats_Panel.add(descScroll, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		action_stats_Panel.add(statsScroll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		action_stats_Panel.add(changesScroll, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		
		
		JSeparator separator = new JSeparator();
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		
		ok_cancelPanel = new JPanel();
		ok_cancelPanel.setLayout(new BorderLayout());
		ok_cancelPanel.add(separator, BorderLayout.NORTH);
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		
       
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
		
		if(e.getSource() instanceof DescriptionMenuItem){
			actualDesc = ((DescriptionMenuItem)e.getSource()).getDescription();
			System.out.println(e.getActionCommand());
			if(e.getActionCommand().startsWith("remove class")){                       //remove class
				
				List<OWLOntologyChange> changes  = ore.modi.removeClassAssertion(ind, actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel("remove", actualDesc);
				changesPanel.add(new ChangePanel("removed class assertion to " + actualDesc, changes, this));
				
				
			}
			else if(e.getActionCommand().startsWith("add class")){                     //add class
				List<OWLOntologyChange> changes  = ore.modi.addClassAssertion(ind, actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				changesPanel.add(new ChangePanel("added class assertion to " + actualDesc, changes, this));
				
				
			}
			else if(e.getActionCommand().startsWith("add property")){                      //add property
				Individual ind = new Individual(e.getActionCommand());
				List<OWLOntologyChange> changes  = ore.modi.addObjectProperty(ind, (ObjectSomeRestriction)actualDesc, ind);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				changesPanel.add(new ChangePanel("added property assertion " + ((ObjectSomeRestriction)actualDesc).getRole() + " to " + ind, changes, this));
			
			}
			else if(e.getActionCommand().startsWith("remove complete property")){                  //delete property
				List<OWLOntologyChange> changes = ore.modi.deleteObjectProperty(ind, (ObjectSomeRestriction)actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				changesPanel.add(new ChangePanel("removed property " + ((ObjectSomeRestriction)actualDesc).getRole(), changes, this));
			}
			else if(e.getActionCommand().startsWith("remove all property")){                  //remove property assertions
				List<OWLOntologyChange> changes = ore.modi.deleteObjectPropertyAssertions(ind, (ObjectSomeRestriction)actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				changesPanel.add(new ChangePanel("added property assertion " + ((ObjectSomeRestriction)actualDesc).getRole() + " to " + ind, changes, this));
			}
			
		}else if(e.getSource() instanceof MoveMenuItem){
			actualDesc = ((MoveMenuItem)e.getSource()).getSource();
			newDesc = new NamedClass(e.getActionCommand());
			List<OWLOntologyChange> changes  = ore.modi.moveIndividual(ind, actualDesc, newDesc);
			allChanges.addAll(changes);
			descPanel.updatePanel();
			statsPanel.updatePanel("move", actualDesc);
			changesPanel.add(new ChangePanel("moved class assertion from " + actualDesc + " to " + newDesc, changes, this));
		}
			
		
		else if(e.getActionCommand().equals("delete")){
			List<OWLOntologyChange> changes  = ore.modi.deleteIndividual(ind);
			allChanges.addAll(changes);
			for(OWLOntologyChange ol : changes)
				System.out.println(((RemoveAxiom)ol).getAxiom());
		}
		else if(e.getActionCommand().equals("Ok")){
			setVisible(false);
			dispose();
		}
		else if(e.getActionCommand().equals("Cancel")){
			if (JOptionPane.showConfirmDialog(this,
			        "All changes will be lost!", "Warning!", 
			        JOptionPane.YES_NO_OPTION)
			     == JOptionPane.YES_OPTION){

				ore.modi.undoChanges(allChanges);
				allChanges.clear();
				setVisible(false);
				dispose();
			}
		}
			

	}
			

	public void mouseClicked(MouseEvent e) {
		if(e.getSource() instanceof UndoLabel){
			List<OWLOntologyChange> changes = ((UndoLabel)e.getSource()).getChanges();
			ore.modi.undoChanges(changes);
			descPanel.updatePanel();
			changesPanel.updatePanel(((UndoLabel)e.getSource()).getParent());
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public Set<OWLOntologyChange> getAllChanges() {
		return allChanges;
	}
		

	
}
