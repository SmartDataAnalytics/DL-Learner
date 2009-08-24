package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.semanticweb.owl.model.OWLClass;

public class UnsatisfiableClassesTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 59201134390657458L;
	
	public UnsatisfiableClassesTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
		setModel(new UnsatisfiableClassesTableModel());
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
		getColumn(0).setMaxWidth(20);
		
	}
	
	public void addUnsatClasses(List<OWLClass> unsatClasses){
		((UnsatisfiableClassesTableModel)getModel()).addUnsatClasses(unsatClasses);
	}
	
	public OWLClass getSelectedClass(){
		return (OWLClass)((UnsatisfiableClassesTableModel)getModel()).getValueAt(getSelectedRow(), 0);
	}
	
	public void clear(){
		((UnsatisfiableClassesTableModel)getModel()).clear();
	}

}
