package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.dllearner.core.owl.Individual;

public class IndividualsTableModel extends AbstractTableModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6185237193249150989L;

	private List<Individual> individuals;
	
	public IndividualsTableModel(){
		this.individuals = new ArrayList<Individual>();
	}
	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return individuals.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return individuals.get(rowIndex);
	}
	
	public void addIndividuals(Set<Individual> individuals){
		this.individuals.clear();
		this.individuals.addAll(individuals);
	}
	
	public void removeIndividual(Individual ind){
		individuals.remove(ind);
		fireTableDataChanged();
	}
	
	public Individual getSelectedIndividual(int rowIndex){
		return individuals.get(rowIndex);
	}

}
