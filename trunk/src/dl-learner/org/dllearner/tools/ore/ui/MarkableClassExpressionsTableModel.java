package org.dllearner.tools.ore.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.LearningManagerListener;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxRenderer;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

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
			OWLDataFactory factory = OREManager.getInstance().getReasoner().getOWLOntologyManager().getOWLDataFactory();
			OWLClass cl = factory.getOWLClass(IRI.create(
					LearningManager.getInstance().getCurrentClass2Describe().getURI()));
			if(LearningManager.getInstance().isEquivalentDescription(descriptions.get(rowIndex))){
				return ManchesterSyntaxRenderer.render(factory.getOWLEquivalentClassesAxiom(cl, 
						OWLAPIDescriptionConvertVisitor.getOWLClassExpression(descriptions.get(rowIndex).getDescription())), false, 0);
			} else {
				return ManchesterSyntaxRenderer.render(factory.getOWLSubClassOfAxiom(cl, 
						OWLAPIDescriptionConvertVisitor.getOWLClassExpression(descriptions.get(rowIndex).getDescription())), false, 0);
			}
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
		fireTableDataChanged();
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
