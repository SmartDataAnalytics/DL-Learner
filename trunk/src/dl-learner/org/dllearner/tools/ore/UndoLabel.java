package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;

import org.semanticweb.owl.model.OWLOntologyChange;

public class UndoLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2058081574518973309L;

	private List<OWLOntologyChange> owlChanges;
	
	public UndoLabel(List<OWLOntologyChange> changes, MouseListener mL){
		super("Undo");
		setForeground(Color.RED);
		this.owlChanges = changes;
		addMouseListener(mL);
	}
	
	public List<OWLOntologyChange> getChanges(){
		return owlChanges;
	}
}
