package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.ListSelectionModel;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxTableCellRenderer;
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
//		getColumn(0).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		getColumn(0).setResizable(false);
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
	}
	
	@Override
	public String getToolTipText(MouseEvent e){
		String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if(rowIndex != -1){
        	tip = getValueAt(rowIndex, 0).toString();
        	
        } else {
        	tip = super.getToolTipText(e);
        }
        return tip;
	}
	
	public void addClasses(Set<NamedClass> classes){
		((ClassesTableModel)getModel()).addClasses(classes);
	}
	
	public NamedClass getSelectedValue(){
		return ((ClassesTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
	
	public void clear(){
		((ClassesTableModel)getModel()).clear();
	}
}
