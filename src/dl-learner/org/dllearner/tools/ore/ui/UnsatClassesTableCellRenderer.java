package org.dllearner.tools.ore.ui;

import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREApplication;
import org.semanticweb.owl.model.OWLClass;

public class UnsatClassesTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6705062445027715783L;
	
	private final Icon icon = new ImageIcon(OREApplication.class.getResource("information.png"));
	
	private ExplanationManager manager;
	private Set<OWLClass> rootClasses;
	
	public UnsatClassesTableCellRenderer(ExplanationManager man){
		this.manager = man;
		rootClasses = manager.getRootUnsatisfiableClasses();
	}
	
	@Override
	protected void setValue(Object value) {
		if(value instanceof OWLClass){
			if(rootClasses.contains((OWLClass)value)){
//				setText(value.toString() );
				setIcon(icon);
//				setHorizontalTextPosition(LEADING);
			}
			else {
//				setText(value.toString());
				setIcon(null);
			}
		} else {
			super.setValue(value);
		}
	}
	

}
