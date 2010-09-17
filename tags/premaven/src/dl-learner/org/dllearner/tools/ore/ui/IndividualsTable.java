package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.ListSelectionModel;

import org.dllearner.core.owl.Individual;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxTableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class IndividualsTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6874941283913237464L;

	public IndividualsTable(){
		super(new IndividualsTableModel());
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
		getColumn(0).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
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
	
	public void addIndividuals(Set<Individual> individuals){
		((IndividualsTableModel)getModel()).addIndividuals(individuals);
	}
	
	public void removeIndividual(Individual ind){
		((IndividualsTableModel)getModel()).removeIndividual(ind);
	}
	
	public Individual getSelectedIndividual(){
		return ((IndividualsTableModel)getModel()).getSelectedIndividual(getSelectedRow());
	}
}
