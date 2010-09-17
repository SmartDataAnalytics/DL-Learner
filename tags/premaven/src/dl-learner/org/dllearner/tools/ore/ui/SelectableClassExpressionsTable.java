package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxTableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class SelectableClassExpressionsTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 486680925931781915L;
	
	private EvaluatedDescriptionClass old = null;
	
	public SelectableClassExpressionsTable(){
		setBackground(Color.WHITE);
		setHighlighters(HighlighterFactory.createAlternateStriping());
//		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new SelectableClassExpressionsTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getColumn(1).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		setColumnSizes();
		getColumn(0).setResizable(false);
		getColumn(1).setResizable(false);
		setGridColor(Color.LIGHT_GRAY);
	}
	
	private void setColumnSizes(){
		getColumn(0).setMaxWidth(95);
//		getColumn(1).setPreferredWidth(430);
		getColumn(2).setMaxWidth(30);
				
	}
	
	
	
	public void clear(){
		((SelectableClassExpressionsTableModel)getModel()).clear();
	}
	
	public void removeSelection(){
		((SelectableClassExpressionsTableModel)getModel()).removeSelection();
	}
	
	public void addResults(List<EvaluatedDescriptionClass> resultList){
		
		if(getSelectedRow() >= 0){
			old = getSelectedValue();
		}
		((SelectableClassExpressionsTableModel)getModel()).addResults(resultList);
		if(old != null){
			int newRowIndex = ((SelectableClassExpressionsTableModel)getModel()).getSelectionIndex(old);
			if(newRowIndex >= 0){
				getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
			}
			
		}
	}
	
	public EvaluatedDescriptionClass getSelectedValue(){	
		return ((SelectableClassExpressionsTableModel)getModel()).getSelectedValue(getSelectedRow());
	}
	
	public EvaluatedDescriptionClass getSelectedClassExpression(){
		return ((SelectableClassExpressionsTableModel)getModel()).getSelectedClassExpression();
	}
	
	public EvaluatedDescriptionClass getBestClassExpression(){
		return ((SelectableClassExpressionsTableModel)getModel()).getBestClassExpression();
	}
	
	public int getSelectedPosition(){
		return ((SelectableClassExpressionsTableModel)getModel()).getSelectedPosition();
	}
	
	public List<EvaluatedDescriptionClass> getSelectedDescriptions(){
		return ((SelectableClassExpressionsTableModel)getModel()).getSelectedDescriptions();
	}

}
