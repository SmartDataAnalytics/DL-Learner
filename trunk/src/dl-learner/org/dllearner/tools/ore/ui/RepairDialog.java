/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.dllearner.core.owl.Individual;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OntologyModifier;
import org.dllearner.tools.ore.ui.item.AddPropertyAssertionMenuItem;
import org.dllearner.tools.ore.ui.item.AddToClassMenuItem;
import org.dllearner.tools.ore.ui.item.MoveFromClassToMenuItem;
import org.dllearner.tools.ore.ui.item.MoveToClassFromMenuItem;
import org.dllearner.tools.ore.ui.item.RemoveAllPropertyAssertionsMenuItem;
import org.dllearner.tools.ore.ui.item.RemoveAllPropertyAssertionsNotToMenuItem;
import org.dllearner.tools.ore.ui.item.RemoveAllPropertyAssertionsToMenuItem;
import org.dllearner.tools.ore.ui.item.RemoveFromClassMenuItem;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;

/**
 * The repair dialog where the learned class description (including error parts), 
 * the statistics and the undo options are shown.
 * @author Lorenz Buehmann
 *
 */
public class RepairDialog extends JDialog implements ActionListener, OWLOntologyChangeListener{
	
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
	private JPanel okCancelPanel;
	private JPanel actionStatsPanel;
	
	private ChangesTable changesTable;
	private JScrollPane changesScroll;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private String mode;
		
	private OntologyModifier modifier;

	private Individual ind;
	
	
	public RepairDialog(Individual ind, JFrame dialog, String mode){
		super(dialog, true);
		final Component dialogd = this.getParent();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent we) {
		    	if(changesTable.getRowCount() > 0){
					if (JOptionPane.showConfirmDialog(dialogd,
					        "All changes will be lost!", "Warning!", 
					        JOptionPane.YES_NO_OPTION)
					     == JOptionPane.YES_OPTION){
		
						modifier.undoChanges(getAllChanges());
						changesTable.clear();
						returncode = CANCEL_RETURN_CODE;
						setVisible(false);
						dispose();
					}
				} else{
					returncode = CANCEL_RETURN_CODE;
					setVisible(false);
					dispose();
				}
					
			}
		    
		});

		
		this.ind = ind;
	
		this.modifier = OREManager.getInstance().getModifier();
		this.mode = mode;
		OREManager.getInstance().getReasoner().getOWLOntologyManager().addOntologyChangeListener(this);
		
	}
	
	/**
	 * Initializing and making dialog visible.
	 * @return integer value
	 */
	public int showDialog(){
	
		if(mode.equals("neg")){
			this.setTitle("Repair negative example");
		} else if(mode.equals("pos")){
			this.setTitle("Repair positive example");
		}
		this.setSize(700, 700);
		this.setLayout(new BorderLayout());
		
		descPanel = new DescriptionPanel(ind, this, mode);		
		JScrollPane descScroll = new JScrollPane();
		descScroll.setViewportView(descPanel);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(0.7);
		splitPane.setResizeWeight(0.7);
		
		statsPanel = new StatsPanel(ind);
		statsPanel.init();
		JScrollPane statsScroll = new JScrollPane();
		statsScroll.setViewportView(statsPanel);
		splitPane.add(statsScroll);        
		
		changesTable = new ChangesTable();
		changesScroll = new JScrollPane(changesTable);
		splitPane.add(changesScroll);
		
	    actionStatsPanel = new JPanel();
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.rowWeights = new double[] {0.0, 0.1, 0.1};
		gbl.rowHeights = new int[] {64, 7, 7};
		gbl.columnWeights = new double[] {0.1};
		gbl.columnWidths = new int[] {7};
		actionStatsPanel.setLayout(gbl);
		
		actionStatsPanel.add(descScroll, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		actionStatsPanel.add(splitPane, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		
		okCancelPanel = new JPanel();
		okCancelPanel.setLayout(new BorderLayout());
		okCancelPanel.add(new JSeparator(), BorderLayout.NORTH);
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		
		Box buttonBox = new Box(BoxLayout.X_AXIS);
        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(cancelButton);
		okCancelPanel.add(buttonBox, BorderLayout.EAST);
        
		add(descScroll, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(okCancelPanel, BorderLayout.SOUTH);
		
		this.setModal(true);
		this.setVisible(true);
		
		return returncode;

	}

	/**
	 * Method controls action events triggered by clicking on red labels in class description at the top of the dialog.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof RemoveFromClassMenuItem){
			RemoveFromClassMenuItem item = (RemoveFromClassMenuItem)e.getSource();
			List<OWLOntologyChange> changes  = modifier.removeClassAssertion(ind, item.getDescription());
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof MoveFromClassToMenuItem){
			MoveFromClassToMenuItem item = (MoveFromClassToMenuItem)e.getSource();
			List<OWLOntologyChange> changes  = modifier.moveIndividual(ind, item.getSource(), item.getDestination());
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof AddToClassMenuItem){
			AddToClassMenuItem item = (AddToClassMenuItem)e.getSource();
			List<OWLOntologyChange> changes  = modifier.addClassAssertion(ind, item.getDescription());
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof RemoveAllPropertyAssertionsMenuItem){
			RemoveAllPropertyAssertionsMenuItem item = (RemoveAllPropertyAssertionsMenuItem)e.getSource();
			List<OWLOntologyChange> changes = modifier.deleteObjectProperty(ind, item.getProperty());
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof RemoveAllPropertyAssertionsToMenuItem){
			RemoveAllPropertyAssertionsToMenuItem item = (RemoveAllPropertyAssertionsToMenuItem)e.getSource();
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			changes.addAll(modifier.removeAllObjectPropertyAssertions(ind, item.getProperty(), OREManager.getInstance().getIndividualsInPropertyRange(item.getDestination(), ind)));
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof RemoveAllPropertyAssertionsNotToMenuItem){
			RemoveAllPropertyAssertionsNotToMenuItem item = (RemoveAllPropertyAssertionsNotToMenuItem)e.getSource();
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			changes.addAll(modifier.removeAllObjectPropertyAssertions(ind, item.getProperty(), OREManager.getInstance().getIndividualsNotInPropertyRange(item.getDestination(), ind)));
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof MoveToClassFromMenuItem){
			MoveToClassFromMenuItem item = (MoveToClassFromMenuItem)e.getSource();
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			changes.addAll(modifier.moveIndividual(ind, item.getSource(), item.getDestination()));
			changesTable.addChanges(changes);
		} else if(e.getSource() instanceof AddPropertyAssertionMenuItem){
			AddPropertyAssertionMenuItem item = (AddPropertyAssertionMenuItem)e.getSource();
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			changes.addAll(modifier.addObjectProperty(ind, item.getProperty(), item.getObject()));
			changesTable.addChanges(changes);
		}
		
		else if(e.getActionCommand().equals("Ok")){
			if(descPanel.isCorrect()){
				returncode = VALID_RETURN_CODE;
			} else{
				returncode = OK_RETURN_CODE;
			}
			setVisible(false);
			dispose();
		} else if(e.getActionCommand().equals("Cancel")){
			if(changesTable.getRowCount() > 0){
				if (JOptionPane.showConfirmDialog(this,
				        "All changes will be lost!", "Warning!", 
				        JOptionPane.YES_NO_OPTION)
				     == JOptionPane.YES_OPTION){
	
					modifier.undoChanges(getAllChanges());
					changesTable.clear();
					returncode = CANCEL_RETURN_CODE;
					setVisible(false);
					dispose();
				}
			} else{
				returncode = CANCEL_RETURN_CODE;
				setVisible(false);
				dispose();
			}
				
		}
			

	}

	public List<OWLOntologyChange> getAllChanges() {
		return changesTable.getChanges();
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> arg0)
			throws OWLException {
		descPanel.updatePanel();
		statsPanel.updatePanel();
		
	}

}
