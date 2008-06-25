package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;

public class StatsPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8418286820511803278L;
	ORE ore;
	Individual ind;
	
	public StatsPanel(ORE ore, Individual ind){
		super();
		this.ore = ore;
		this.ind = ind;
	}
	
	public void init(){
		
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));		
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.rowWeights = new double[] {0.0, 0.1, 0.1};
		gbl.rowHeights = new int[] {34, 7, 7};
		gbl.columnWeights = new double[] {0.1};
		gbl.columnWidths = new int[] {7};
		setLayout(gbl);
		
		
		
	
		        
       
        
        JPanel indPanel = new JPanel();
        indPanel.setBackground(new Color(224, 223, 227));
        indPanel.setLayout(new GridLayout(0, 1));
        JLabel indLb = new JLabel("Individual:");
        indLb.setFont(indLb.getFont().deriveFont(Font.BOLD));
        JLabel indLb1 = new JLabel(ind.getName());
        indPanel.add(indLb);
        indPanel.add(indLb1);
        
        JPanel classesPanel = new JPanel();
        classesPanel.setBackground(new Color(224, 223, 227));
        classesPanel.setLayout(new GridLayout(0, 1));
        JLabel classLb = new JLabel("Classes:");
        classLb.setFont(indLb.getFont().deriveFont(Font.BOLD));
        classesPanel.add(classLb);
        
        Set<NamedClass> t = null;
					
		t = ore.reasoner2.getConcepts(ind);
		
		for(NamedClass nc : t)
			classesPanel.add(new JLabel(nc.getName()));
        
		

		add(indPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       
        add(classesPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));    
	}
}
