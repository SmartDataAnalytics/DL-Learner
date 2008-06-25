package org.dllearner.tools.ore;


import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.semanticweb.owl.model.OWLOntologyChange;

public class ChangePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -934113184795465461L;
	
		
	public ChangePanel(String label, List<OWLOntologyChange> changes, MouseListener mL){
		super();
		add(new JLabel(label));
		add(new UndoLabel(changes, mL));
		setBackground(Color.WHITE);
		
	}

}
