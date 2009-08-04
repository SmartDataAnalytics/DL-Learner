package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.dllearner.tools.ore.ExplanationManager;
import org.semanticweb.owl.model.OWLClass;

public class UnsatClassesListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6705062445027715783L;
	
	private ExplanationManager manager;
	private Set<OWLClass> rootClasses;
	
	public UnsatClassesListCellRenderer(ExplanationManager man){
		this.manager = man;
		rootClasses = manager.getRootUnsatisfiableClasses();
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		
		if(rootClasses.contains((OWLClass)value)){
			setText(value.toString() );
			setIcon(new ImageIcon("src/dl-learner/org/dllearner/tools/ore/information.png"));
			setHorizontalTextPosition(LEADING);
		}
		else {
			setText(value.toString());
			setIcon(null);
		}
		if(isSelected){
			setBackground(new Color(242, 242, 242));
		} else {
			setBackground(Color.WHITE);
		}
		return this;
	}

}
