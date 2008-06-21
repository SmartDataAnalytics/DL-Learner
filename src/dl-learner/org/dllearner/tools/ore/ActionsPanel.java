package org.dllearner.tools.ore;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.reasoning.OWLAPIReasoner;

public class ActionsPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3761077097231343915L;
	ORE ore;
	Individual ind;
	OWLAPIReasoner reasoner;
	ActionListener al;
	
	
	public ActionsPanel(ORE ore, Individual ind, ActionListener al){
		super(new GridLayout(0, 1));
		this.ore = ore;
		this.ind = ind;
		this.reasoner = ore.modi.reasoner;
		this.al = al;
	}
	
	public void init(){
		setBorder(new TitledBorder("actions"));
		JButton delete = new JButton("delete instance");
		add(delete);
		updatePanel();
		
//		for(Description d :ore.getCriticalDescriptions(ind, ore.getConceptToAdd())){
//			
//			if(!(d instanceof Negation)){
//				if(d instanceof NamedClass){
//					add(new DescriptionButton("remove class assertion to " + d.toString(), d ));
//					add(new DescriptionButton("move class assertion " + d.toString() + " to ...", d));
//				}
//				else if(d instanceof ObjectSomeRestriction)
//					add(new DescriptionButton("remove property assertion " + d.toString(), d));
//			}
//			else if(d instanceof Negation){
//				if(d.getChild(0) instanceof NamedClass)
//					add(new DescriptionButton("add class assertion to " + d.getChild(0).toString(), d));
//				else if(d.getChild(0) instanceof ObjectSomeRestriction)
//					add(new DescriptionButton("add property " + d.toString(), d));
//			}
//		}
	}
	
	
	public void addActionListeners(ActionListener al){
		for(Component c : getComponents())
			if(c instanceof JButton)
				((JButton)c).addActionListener(al);
	}
	
	public void updatePanel(){//DescriptionButton descBut, Description desc){
		for(Component c : getComponents())
			if(c instanceof DescriptionButton)
				remove(c);
		
		ore.modi.refreshReasoner();
		for(Description d :ore.getCriticalDescriptions(ind, ore.getConceptToAdd())){
			System.out.println(d + " " + ore.modi.reasoner.instanceCheck(d, ind));
			if(ore.modi.reasoner.instanceCheck(d, ind)){
				if(!(d instanceof Negation)){
					if(d instanceof NamedClass){
						add(new DescriptionButton("remove class assertion to " + d.toString(), d ));
						add(new DescriptionButton("move class assertion " + d.toString() + " to ...", d));
					}
					else if(d instanceof ObjectSomeRestriction)
						add(new DescriptionButton("remove property assertion " + d.toString(), d));
				}
				else if(d instanceof Negation){
					if(d.getChild(0) instanceof NamedClass)
						add(new DescriptionButton("add class assertion to " + d.getChild(0).toString(), d.getChild(0)));
					else if(d.getChild(0) instanceof ObjectSomeRestriction)
						add(new DescriptionButton("add property " + d.toString(), d));
				}
			}
		}
		addActionListeners(al);
		
		
		
		
		
//		String command = descBut.getActionCommand();
//		if(command.startsWith("remove class")){
//			for(Component c : getComponents())
//				if(c instanceof DescriptionButton && ((DescriptionButton)c).getDescription().equals(desc) && ((DescriptionButton)c).getActionCommand().startsWith("move class"))
//					remove(c);
//		}
//		else if(command.startsWith("move class")){
//			for(Component c : getComponents())
//				if(c instanceof DescriptionButton && ((DescriptionButton)c).getDescription().equals(desc) && ((DescriptionButton)c).getActionCommand().startsWith("remove class"))
//					remove(c);
//		}
//		
//		remove(descBut);		
//		
		SwingUtilities.updateComponentTreeUI(this);
		
	}

	
}
