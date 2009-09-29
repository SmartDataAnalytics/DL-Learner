package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class EquivalentClassExpressionsTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 486680925931781915L;
	
	private EvaluatedDescriptionClass old = null;
	
	public EquivalentClassExpressionsTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
//		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new EquivalentClassExpressionsTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getColumn(1).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		setColumnSizes();
		getColumn(0).setResizable(false);
		getColumn(1).setResizable(false);
		setGridColor(Color.LIGHT_GRAY);
	}
	
	private void setColumnSizes(){
		getColumn(0).setMaxWidth(100);
		getColumn(1).setPreferredWidth(430);
		getColumn(2).setMaxWidth(30);
				
	}
	
	
	
	public void clear(){
		((EquivalentClassExpressionsTableModel)getModel()).clear();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		
		if(getSelectedRow() >= 0){
			old = getSelectedValue();
		}
		((EquivalentClassExpressionsTableModel)getModel()).addResults(resultList);
		if(old != null){
			int newRowIndex = ((EquivalentClassExpressionsTableModel)getModel()).getSelectionIndex(old);
			if(newRowIndex >= 0){
				getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
			}
			
		}
	}
	
	public EvaluatedDescriptionClass getSelectedValue(){	
		return ((EquivalentClassExpressionsTableModel)getModel()).getSelectedValue(getSelectedRow());
	}

}
