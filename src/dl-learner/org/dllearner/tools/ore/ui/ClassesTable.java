package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.Set;

import javax.swing.ListSelectionModel;

import org.dllearner.core.owl.NamedClass;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class ClassesTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8466845175104839818L;

	public ClassesTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
		setModel(new ClassesTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getColumn(0).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		getColumn(0).setResizable(false);
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
	}
	
	public void clear(){
		((ClassesTableModel)getModel()).clear();
	}
	
	public void addClasses(Set<NamedClass> classes){
		((ClassesTableModel)getModel()).addClasses(classes);
	}
	
	public NamedClass getSelectedValue(){
		return ((ClassesTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
}
