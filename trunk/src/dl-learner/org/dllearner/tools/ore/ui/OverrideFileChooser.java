package org.dllearner.tools.ore.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;

public class OverrideFileChooser extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void approveSelection() {
		File f = super.getSelectedFile();
		if (f.exists()) {
			int ans = JOptionPane.showConfirmDialog(null, "" + f.getName() + " already exists. Overwrite?", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ans == JOptionPane.OK_OPTION) {
				try {
					OREManager.getInstance().saveOntology(f);
					super.approveSelection();
				} catch (OWLOntologyStorageException e) {
					JOptionPane.showMessageDialog(this, "Could not save file: " + e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		} else {
			try {
				OREManager.getInstance().saveOntology(f);
				super.approveSelection();
			} catch (OWLOntologyStorageException e) {
				JOptionPane.showMessageDialog(this, "Could not save file: " + e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
	
	

}
