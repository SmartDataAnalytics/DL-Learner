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
	String mode;
	boolean correct = false;
	
	public DescriptionPanel(ORE ore, Individual ind, ActionListener aL, String mode){
		
		super();
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		this.ore = ore;
		this.ind = ind;
		this.aL = aL;
		this.mode = mode;
		if(mode.equals("neg")){
			for(JLabel jL : ore.DescriptionToJLabelNeg(ind, ore.conceptToAdd)){
				add(jL);
				if(jL instanceof DescriptionLabel){
					
					((DescriptionLabel)jL).setIndOre(ore, ind);
					((DescriptionLabel)jL).init();
					((DescriptionLabel)jL).addActionListeners(aL);
					
				}
				
			}
		}
		else if(mode.equals("pos")){
			for(JLabel jL : ore.DescriptionToJLabelPos(ind, ore.conceptToAdd)){
				add(jL);
				if(jL instanceof DescriptionLabel){
					
					((DescriptionLabel)jL).setIndOre(ore, ind);
					((DescriptionLabel)jL).init();
					((DescriptionLabel)jL).addActionListeners(aL);
					
				}
				
			}
		}
	}
	
	public void updatePanel(){
		for(Component c : getComponents())
			if(c instanceof JLabel)
				remove(c);
		
		ore.updateReasoner();
		correct = true;
		if (mode.equals("neg")) {
			for (JLabel jL : ore.DescriptionToJLabelNeg(ind, ore.conceptToAdd)) {
				add(jL);
				if (jL instanceof DescriptionLabel) {
					((DescriptionLabel) jL).setIndOre(ore, ind);
					((DescriptionLabel) jL).init();
					((DescriptionLabel) jL).addActionListeners(aL);
					correct = false;

				}
			}
		}
		else if(mode.equals("pos")){
			for (JLabel jL : ore.DescriptionToJLabelPos(ind, ore.conceptToAdd)) {
				add(jL);
				if (jL instanceof DescriptionLabel) {
					((DescriptionLabel) jL).setIndOre(ore, ind);
					((DescriptionLabel) jL).init();
					((DescriptionLabel) jL).addActionListeners(aL);
					correct = false;

				}
			}
		}
		SwingUtilities.updateComponentTreeUI(this);
		

	}
	
	public boolean isCorrect(){
		return correct;
	}
}