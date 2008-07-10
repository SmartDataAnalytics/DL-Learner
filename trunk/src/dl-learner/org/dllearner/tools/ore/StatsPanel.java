package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;

public class StatsPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8418286820511803278L;
	ORE ore;
	Individual ind;
	Set<NamedClass> classes;
	Set<String> newClasses;
	JPanel classesPanel;
	
	
	public StatsPanel(ORE ore, Individual ind){
		super();
		this.ore = ore;
		this.ind = ind;
	}
	
	public void init(){
		
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));		
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.rowWeights = new double[] {0.0, 0.0, 0.1};
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
        
        classesPanel = new JPanel();
        classesPanel.setBackground(new Color(224, 223, 227));
        classesPanel.setLayout(new GridLayout(0, 1));
        JLabel classLb = new JLabel("Classes:");
        classLb.setFont(indLb.getFont().deriveFont(Font.BOLD));
        classesPanel.add(classLb);
        
        classes = ore.reasoner2.getConcepts(ind);
        newClasses = new HashSet<String>();
		
		for(NamedClass nc : classes)
			classesPanel.add(new JLabel(nc.getName()));
        
		
		add(indPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(classesPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));    
	}
	
	public void updatePanel(String action, Description d){

		
		
		if(action.equals("undo")){
//			Set<String> afterUpdate = new HashSet<String>();
//			for(NamedClass nc : ore.reasoner2.getConcepts(ind))
//				afterUpdate.add(nc.toString());
//			Set<String> oldClasses = new HashSet<String>();
//			for(NamedClass nc : newClasses)
//				oldClasses.add(nc.toString());
		}else{
			Set<String> afterUpdate = new HashSet<String>();
			for(NamedClass nc : ore.reasoner2.getConcepts(ind))
				afterUpdate.add(nc.toString());
			Set<String> oldClasses = new HashSet<String>();
			for(NamedClass nc : classes)
				oldClasses.add(nc.toString());
			for(String nc : afterUpdate)
				if(!oldClasses.contains(nc)){
							
					ImageIcon icon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/new.gif");
					JLabel lab = new JLabel(nc);
					lab.setIcon(icon);
					lab.setHorizontalTextPosition(JLabel.LEFT);
					classesPanel.add(lab);
				}
				
			
			for(NamedClass nc : classes)
				
				if(!afterUpdate.contains(nc.toString()))
					for(Component co: classesPanel.getComponents())
						if(co instanceof JLabel)
							if(((JLabel)co).getText().equals(nc.toString()))
								((JLabel)co).setText("<html><strike>" + nc + "</strike></html>");
			newClasses.addAll(afterUpdate);
		}
		
		SwingUtilities.updateComponentTreeUI(this);
					
	}
}
