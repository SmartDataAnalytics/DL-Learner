package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Cursor;
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

import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.semanticweb.owl.model.OWLOntologyChange;

public class RepairDialog extends JDialog implements ActionListener, MouseListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	  
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int OK_RETURN_CODE = 2;
	public static final int VALID_RETURN_CODE = 3;
    
    private int returncode;
	
	private StatsPanel statsPanel;
	private DescriptionPanel descPanel;
	private ChangesPanel changesPanel;
	private JPanel ok_cancelPanel;
	private JPanel action_stats_Panel;
	
	private JScrollPane changesScroll;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private String mode;
		
	private ORE ore;
	private Individual ind;
	private Description actualDesc;
	private Description newDesc;
	private Set<OWLOntologyChange> allChanges;
	
	
	public RepairDialog(Individual ind, JDialog dialog, ORE ore, String mode){
		super(dialog, true);
		this.ind = ind;
		this.ore = ore;
		this.mode = mode;
		allChanges = new HashSet<OWLOntologyChange>();
		
	}
	
	public int showDialog(){
		if(mode.equals("neg"))
			this.setTitle("Repair negative example");
		else if(mode.equals("pos"))
			this.setTitle("Repair positive example");
		this.setSize(700, 700);
		this.setLayout(new BorderLayout());
		
		descPanel = new DescriptionPanel(ore, ind, this, mode);		
		JScrollPane descScroll = new JScrollPane();
		descScroll.setViewportView(descPanel);
		
		statsPanel = new StatsPanel(ore, ind);
		statsPanel.init();
		JScrollPane statsScroll = new JScrollPane();
		statsScroll.setViewportView(statsPanel);
		        
				
		changesPanel = new ChangesPanel();
		changesScroll = new JScrollPane();
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
		
		
		this.setModal(true);
		this.setVisible(true);
		
		return returncode;

	}

	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() instanceof DescriptionMenuItem){
			actualDesc = ((DescriptionMenuItem)e.getSource()).getDescription();
			int action = ((DescriptionMenuItem)e.getSource()).getActionID();
			if(action == 4){
				Individual obj = new Individual(e.getActionCommand());
				List<OWLOntologyChange> changes  = ore.modi.addObjectProperty(ind, (ObjectQuantorRestriction)actualDesc, obj);
				allChanges.addAll(changes);
				try {
					System.out.println(ore.reasoner.instanceCheck(actualDesc, ind));
				} catch (ReasoningMethodUnsupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				descPanel.updatePanel();
				try {
					System.out.println(ore.reasoner.instanceCheck(actualDesc, ind));
				} catch (ReasoningMethodUnsupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("added property assertion " + ((ObjectQuantorRestriction)actualDesc).getRole() + " to " + ind, changes, this));
				changesScroll.updateUI();
			}
			else if(action == 5){
				ObjectQuantorRestriction property = (ObjectQuantorRestriction)actualDesc;
				List<OWLOntologyChange> changes = null;
				for(Individual i : ore.getIndividualsOfPropertyRange(property, ind)){
					changes = ore.modi.removeObjectPropertyAssertion(ind, property, i);
					allChanges.addAll(changes);
				}
				
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("removed property assertions " + ((ObjectSomeRestriction)actualDesc).getRole() + " to range " + ((ObjectSomeRestriction)actualDesc).getChild(0), changes, this));
				changesScroll.updateUI();
			}
			else if(action == 6){
				List<OWLOntologyChange> changes = ore.modi.deleteObjectProperty(ind, (ObjectQuantorRestriction)actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("deleted property " + ((ObjectQuantorRestriction)actualDesc).getRole(), changes, this));
				changesScroll.updateUI();
			}
			else if(action == 0){
				newDesc = new NamedClass(e.getActionCommand());
				List<OWLOntologyChange> changes  = ore.modi.moveIndividual(ind, actualDesc, newDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("moved class assertion from " + actualDesc + " to " + newDesc, changes, this));
				changesScroll.updateUI();
			}
			else if(action == 3){
				List<OWLOntologyChange> changes  = ore.modi.removeClassAssertion(ind, actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("removed class assertion to " + actualDesc, changes, this));
				changesScroll.updateUI();
			}
			else if(action == 2){
				List<OWLOntologyChange> changes  = ore.modi.addClassAssertion(ind, actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("added class assertion to " + actualDesc, changes, this));
				changesScroll.updateUI();
			}
			else if(action == 7){
				ObjectQuantorRestriction property = (ObjectQuantorRestriction)actualDesc;
				List<OWLOntologyChange> changes = null;
				for(Individual i : ore.getIndividualsNotInPropertyRange(property, ind)){
					changes = ore.modi.removeObjectPropertyAssertion(ind, property, i);
					allChanges.addAll(changes);
				}
				
				
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("removed property assertion " + property.getRole() + " to " + ind, changes, this));
				changesScroll.updateUI();
			}
			else if(action == 1){
				Description oldDesc = new NamedClass(e.getActionCommand());
				List<OWLOntologyChange> changes  = ore.modi.moveIndividual(ind, oldDesc, actualDesc);
				allChanges.addAll(changes);
				descPanel.updatePanel();
				statsPanel.updatePanel();
				changesPanel.add(new ChangePanel("moved class assertion from " + oldDesc + " to " + actualDesc, changes, this));
				changesScroll.updateUI();
			}
		}
			
			

		
		
		else if(e.getActionCommand().equals("Ok")){
			if(descPanel.isCorrect())
				returncode = VALID_RETURN_CODE;
			else
				returncode = OK_RETURN_CODE;
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
				returncode = CANCEL_RETURN_CODE;
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
			statsPanel.updatePanel();
			changesPanel.updatePanel(((UndoLabel)e.getSource()).getParent());
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
		}
	}

	public void mouseEntered(MouseEvent e) {
		if(e.getSource() instanceof UndoLabel){
			((UndoLabel)e.getSource()).setText("<html><u>Undo</u></html>");
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		
	}

	public void mouseExited(MouseEvent e) {
		if(e.getSource() instanceof UndoLabel){
			((UndoLabel)e.getSource()).setText("Undo");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
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
