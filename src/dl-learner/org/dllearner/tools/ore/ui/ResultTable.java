package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class ResultTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -497294373160119210L;

	public ResultTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new ResultTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getColumn(1).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		setColumnSizes();
		getColumn(0).setResizable(false);
		getColumn(1).setResizable(false);
		setEditable(false);
//		setAutoCreateRowSorter(true);
//		getRowSorter().toggleSortOrder(0);
		setGridColor(Color.LIGHT_GRAY);
	}
	
	private void setColumnSizes(){
		getColumn(0).setMaxWidth(70);
		getColumn(1).setPreferredWidth(430);
				
	}
	
	public void clear(){
		((ResultTableModel)getModel()).clear();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		((ResultTableModel)getModel()).addResults(resultList);
	}
	
	public EvaluatedDescriptionClass getSelectedValue(){	
		return ((ResultTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
}
