package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.dllearner.core.owl.Individual;

public class DescriptionPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3684937339236885595L;
	ORE ore;
	Individual ind;
	ActionListener aL;
	
	public DescriptionPanel(ORE ore, Individual ind, ActionListener aL){
		
		super();
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		this.ore = ore;
		this.ind = ind;
		this.aL = aL;
		for(JLabel jL : ore.DescriptionToJLabel(ind, ore.conceptToAdd)){
			add(jL);
			if(jL instanceof DescriptionLabel){
				
				((DescriptionLabel)jL).setIndOre(ore, ind);
				((DescriptionLabel)jL).init();
				((DescriptionLabel)jL).addActionListeners(aL);
			}
		}
	}
	
	public void updatePanel(){//DescriptionButton descBut, Description desc){
		for(Component c : getComponents())
			if(c instanceof JLabel)
				remove(c);
		
		ore.updateReasoner();
		for(JLabel jL : ore.DescriptionToJLabel(ind, ore.conceptToAdd)){
			add(jL);
			if(jL instanceof DescriptionLabel){
				((DescriptionLabel)jL).setIndOre(ore, ind);
				((DescriptionLabel)jL).init();
				((DescriptionLabel)jL).addActionListeners(aL);
				
				
			}
		}
		SwingUtilities.updateComponentTreeUI(this);
		

	}
}