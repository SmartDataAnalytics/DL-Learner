package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.LearningManagerListener;

public class MarkableClassExpressionsTableModel extends AbstractTableModel implements LearningManagerListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5773843275446469889L;

	private List<EvaluatedDescriptionClass> descriptions;
	private int selectedRowIndex = 0;
	
	public MarkableClassExpressionsTableModel(){
		descriptions = new ArrayList<EvaluatedDescriptionClass>();
		LearningManager.getInstance().addListener(this);
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return descriptions.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0){
			if(rowIndex == selectedRowIndex){
				return ">";
			} else {
				return "";
			}
		} else {
			return descriptions.get(rowIndex).getDescription();
		}		
	}
	
	public void clear(){
		descriptions.clear();
		fireTableDataChanged();
	}
	
	private void addDescriptions(List<EvaluatedDescriptionClass> descriptions){
		this.descriptions.clear();
		this.descriptions.addAll(descriptions);
		fireTableDataChanged();
	}
	
	public EvaluatedDescriptionClass getSelectedDescription(int rowIndex){
		return descriptions.get(rowIndex);
	}
	
	public void setSelectedDescription(int rowIndex){
		int oldRowIndex = selectedRowIndex;
		selectedRowIndex = rowIndex;
		fireTableRowsUpdated(oldRowIndex, selectedRowIndex);
	}

	@Override
	public void newDescriptionSelected(int index) {
		setSelectedDescription(index);
		
	}

	@Override
	public void noDescriptionsLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newDescriptionsAdded(List<EvaluatedDescriptionClass> descriptions) {
		addDescriptions(descriptions);	
	}
}
