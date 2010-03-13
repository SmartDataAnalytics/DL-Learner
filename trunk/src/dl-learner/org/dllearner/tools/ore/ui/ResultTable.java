package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxTableCellRenderer;
import org.dllearner.tools.ore.ui.rendering.ProgressBarTableCellRenderer;
import org.dllearner.tools.protege.SuggestionsTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class ResultTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -497294373160119210L;
	
	private EvaluatedDescriptionClass old = null;

	public ResultTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
//		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new ResultTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		ProgressBarTableCellRenderer renderer = new ProgressBarTableCellRenderer();
		renderer.setBackground(getBackground());
		getColumn(0).setCellRenderer(renderer);
		
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
		getColumn(0).setMaxWidth(100);
		getColumn(1).setPreferredWidth(430);
	}
	
	
	
	public void clear(){
		((ResultTableModel)getModel()).clear();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		
		if(getSelectedRow() >= 0){
			old = getSelectedValue();
		}
		((ResultTableModel)getModel()).addResults(resultList);
		if(old != null){
			int newRowIndex = ((ResultTableModel)getModel()).getSelectionIndex(old);
			if(newRowIndex >= 0){
				getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
			}
			
		}
	}
	
	public EvaluatedDescriptionClass getSelectedValue(){	
		return ((ResultTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
	
	public EvaluatedDescriptionClass getValueAtRow(int rowIndex){
		return ((ResultTableModel)getModel()).getValueAtRow(rowIndex);
		
	}
}
