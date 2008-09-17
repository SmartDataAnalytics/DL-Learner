/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

public class DescriptionPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3684937339236885595L;
	
	private ORE ore;
	private Individual ind;
	private ActionListener aL;
	private String mode;
	boolean correct = false;
	private Description newClassDescription;
	
	public DescriptionPanel(ORE ore, Individual ind, ActionListener aL, String mode){
		super();
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		this.ore = ore;
		this.newClassDescription = ore.getNewClassDescription();
		this.ind = ind;
		this.aL = aL;
		this.mode = mode;
		if(mode.equals("neg")){
			for(JLabel jL : ore.descriptionToJLabelNeg(ind, newClassDescription)){
				add(jL);
				if(jL instanceof DescriptionLabel){
					
					((DescriptionLabel)jL).setIndOre(ore, ind);
					((DescriptionLabel)jL).init();
					((DescriptionLabel)jL).addActionListeners(aL);
					
				}
				
			}
		}
		else if(mode.equals("pos")){
			for(JLabel jL : ore.DescriptionToJLabelPos(ind, newClassDescription)){
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
			for (JLabel jL : ore.descriptionToJLabelNeg(ind, newClassDescription)) {
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
			for (JLabel jL : ore.DescriptionToJLabelPos(ind, newClassDescription)) {
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