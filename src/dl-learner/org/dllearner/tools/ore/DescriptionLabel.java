package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectSomeRestriction;

public class DescriptionLabel extends JLabel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Description desc;
	private Individual ind;
	private ORE ore;
	private JPopupMenu menu;
	
	public DescriptionLabel(Description d){
		super(d.toString());
		this.desc = d;
		setForeground(Color.red);
		addMouseListener(this);
//		menu = new JPopupMenu();
//		if(!(desc instanceof Negation)){
//			if(desc instanceof NamedClass){
//				menu.add(new DescriptionMenuItem("remove class assertion " + desc.toString(), desc) );
//				DescriptionMenuItem dme = new DescriptionMenuItem("move class assertion " + desc.toString() + " to ...", desc);
//				for(NamedClass nc : ore.getpossibleMoveClasses(ind))
//					dme.add(new JMenuItem(nc.getName()));
//				menu.add(dme);
////				menu.add(new DescriptionMenuItem("move class assertion " + desc.toString() + " to ...", desc));
//			}
//			else if(desc instanceof ObjectSomeRestriction)
//				menu.add(new DescriptionMenuItem("remove property assertion " + desc.toString(), desc));
//		}
//		else if(desc instanceof Negation){
//			if(desc.getChild(0) instanceof NamedClass)
//				menu.add(new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0)));
//			else if(desc.getChild(0) instanceof ObjectSomeRestriction)
//				menu.add(new DescriptionMenuItem("add property " + d.toString(), desc.getChild(0)));
//		}
		
		
		
	}
	
	public void init(){
		menu = new JPopupMenu();
		if(!(desc instanceof Negation)){
			if(desc instanceof NamedClass){
				menu.add(new DescriptionMenuItem("remove class assertion " + desc.toString(), desc) );
				JMenu dme = new JMenu("move class assertion " + desc.toString() + " to ...");
				for(NamedClass nc : ore.getpossibleMoveClasses(ind))
					dme.add(new DescriptionMenuItem(nc.getName(), desc));
				menu.add(dme);
//				menu.add(new DescriptionMenuItem("move class assertion " + desc.toString() + " to ...", desc));
			}
			else if(desc instanceof ObjectSomeRestriction)
				menu.add(new DescriptionMenuItem("remove property assertion " + desc.toString(), desc));
		}
		else if(desc instanceof Negation){
			if(desc.getChild(0) instanceof NamedClass){
				DescriptionMenuItem item = new DescriptionMenuItem("add class assertion to " + desc.getChild(0).toString(), desc.getChild(0));
				menu.add(item);
				if(ore.hasComplement(desc, ind)){
					item.setEnabled(false);
					item.setToolTipText("class assertion not possible because individual is still asserted to its complement");
				}
			}
			else if(desc.getChild(0) instanceof ObjectSomeRestriction)
				menu.add(new DescriptionMenuItem("add property " + desc.toString(), desc.getChild(0)));
		}
	}
	
	public Description getDescription(){
		return desc;
	}
	
	public void addActionListeners(ActionListener aL){
		for(Component c : menu.getComponents()){
			if(c instanceof DescriptionMenuItem)
				((DescriptionMenuItem)c).addActionListener(aL);
			else if(c instanceof JMenu)
				for( int i = 0; i < ((JMenu)c).getItemCount(); i++)
					((JMenu)c).getItem(i).addActionListener(aL);
				
			
		}
	
		
	}
	
	public void setIndOre(ORE ore, Individual ind){
		this.ore = ore;
		this.ind = ind;
	}

	
	public void mouseClicked(MouseEvent e) {
		menu.show(this.getParent(),getLocation().x ,getLocation().y+50);
		
	}

	
	public void mouseEntered(MouseEvent e) {
		setText("<html><u>" + desc.toString() + "</u></html>");
		
	}

	
	public void mouseExited(MouseEvent e) {
		setText(desc.toString());
		
	}

	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
