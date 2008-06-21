package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectSomeRestriction;

public class DescriptionLabel extends JLabel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Description desc;
	private JPopupMenu menu;
	
	public DescriptionLabel(Description d){
		super(d.toString());
		this.desc = d;
		setForeground(Color.red);
		addMouseListener(this);
		menu = new JPopupMenu();
		if(!(desc instanceof Negation)){
			if(desc instanceof NamedClass){
				menu.add(new DescriptionMenuItem("remove class assertion " + desc.toString(), desc) );
				menu.add(new DescriptionMenuItem("move class assertion " + desc.toString() + " to ...", desc));
			}
			else if(desc instanceof ObjectSomeRestriction)
				menu.add(new DescriptionMenuItem("remove property assertion " + desc.toString(), desc));
		}
		else if(desc instanceof Negation){
			if(desc.getChild(0) instanceof NamedClass)
				menu.add(new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0)));
			else if(desc.getChild(0) instanceof ObjectSomeRestriction)
				menu.add(new DescriptionMenuItem("add property " + d.toString(), desc.getChild(0)));
		}
		
		
		
	}
	
	public Description getDescription(){
		return desc;
	}
	
	public void addActionListeners(ActionListener aL){
		for(Component c : menu.getComponents())
			((DescriptionMenuItem)c).addActionListener(aL);
	
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		menu.show(this.getParent(),getLocation().x ,getLocation().y+50);
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setText("<html><u>" + desc.toString() + "</u></html>");
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setText(desc.toString());
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
